package com.loohp.multichatdiscordsrvaddon.utils;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.listeners.discordsrv.DiscordCommands;
import com.loohp.multichatdiscordsrvaddon.modules.InventoryDisplay;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICInventoryHolder;
import com.loohp.multichatdiscordsrvaddon.objectholders.OfflinePlayerData;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DiscordCommandUtils {

    public static final String RESOURCEPACK_LABEL = "resourcepack";
    public static final String PLAYERINFO_LABEL = "playerinfo";
    public static final String PLAYERLIST_LABEL = "playerlist";
    public static final String ITEM_LABEL = "item";
    public static final String ITEM_OTHER_LABEL = "itemasuser";
    public static final String INVENTORY_LABEL = "inv";
    public static final String INVENTORY_OTHER_LABEL = "invasuser";
    public static final String ENDERCHEST_LABEL = "ender";
    public static final String ENDERCHEST_OTHER_LABEL = "enderasuser";

    public static final Set<String> DISCORD_COMMANDS;

    static {
        Set<String> discordCommands = new HashSet<>();
        for (Field field : DiscordCommands.class.getFields()) {
            if (field.getType().equals(String.class) && field.getName().endsWith("_LABEL")) {
                try {
                    discordCommands.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        DISCORD_COMMANDS = Collections.unmodifiableSet(discordCommands);
    }

    public static void layout0(OfflinePlayer player, String sha1, String title) throws Exception {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);

        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54, title);
        int f1 = 0;
        int f2 = 0;
        int u = 45;
        for (int j = 0; j < Math.min(offlinePlayerData.getInventory().getSize(), 45); j++) {
            ItemStack item = offlinePlayerData.getInventory().getItem(j);
            if (item != null && !item.getType().equals(Material.AIR)) {
                if ((j >= 9 && j < 18) || j >= 36) {
                    if (item.getType().equals(Config.i().getInventoryImage().inventory().frame().primary().getType())) {
                        f1++;
                    } else if (item.getType().equals(Config.i().getInventoryImage().inventory().frame().secondary().getType())) {
                        f2++;
                    }
                }
                if (j < 36) {
                    inv.setItem(u, item.clone());
                }
            }
            if (u >= 53) {
                u = 18;
            } else {
                u++;
            }
        }
        ItemStack frame = f1 > f2 ? Config.i().getInventoryImage().inventory().frame().secondary().clone() : Config.i().getInventoryImage().inventory().frame().primary().clone();
        if (frame.getItemMeta() != null) {
            ItemMeta frameMeta = frame.getItemMeta();
            frameMeta.setDisplayName(ChatColor.YELLOW + "");
            frame.setItemMeta(frameMeta);
        }
        for (int j = 0; j < 18; j++) {
            inv.setItem(j, frame);
        }

        int level = offlinePlayerData.getXpLevel();
        ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
        TranslatableComponent expText = Component.translatable(InventoryDisplay.getLevelTranslation(level)).color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
        if (level != 1) {
            expText = expText.arguments(Component.text(level + ""));
        }
        NMS.getInstance().setItemStackDisplayName(exp, expText);
        inv.setItem(1, exp);

        inv.setItem(3, offlinePlayerData.getInventory().getItem(39));
        inv.setItem(4, offlinePlayerData.getInventory().getItem(38));
        inv.setItem(5, offlinePlayerData.getInventory().getItem(37));
        inv.setItem(6, offlinePlayerData.getInventory().getItem(36));

        ItemStack offhand = offlinePlayerData.getInventory().getSize() > 40 ? offlinePlayerData.getInventory().getItem(40) : null;
        if (!VersionManager.version.isOld() || (offhand != null && offhand.getType().equals(Material.AIR))) {
            inv.setItem(8, offhand);
        }

        Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
            ItemMeta meta = skull.getItemMeta();
            String name = ChatColorUtils.translateAlternateColorCodes('&', Config.i().getDiscordCommands().shareInventory().skullDisplayName().replace("{Player}", player.getName()));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            inv.setItem(0, skull);
        });

        if (Config.i().getSettings().hideLodestoneCompassPos()) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY, sha1, inv);

        if (Config.i().getSettings().bungeecord()) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY, sha1, title, inv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void layout1(OfflinePlayer player, String sha1, String title) throws Exception {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);
        int selectedSlot = offlinePlayerData.getSelectedSlot();
        int level = offlinePlayerData.getXpLevel();

        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54, title);
        int f1 = 0;
        int f2 = 0;
        for (int j = 0; j < Math.min(offlinePlayerData.getInventory().getSize(), 45); j++) {
            if (j == selectedSlot || j >= 36) {
                ItemStack item = offlinePlayerData.getInventory().getItem(j);
                if (item != null && !item.getType().equals(Material.AIR)) {
                    if (item.getType().equals(Config.i().getInventoryImage().inventory().frame().primary().getType())) {
                        f1++;
                    } else if (item.getType().equals(Config.i().getInventoryImage().inventory().frame().secondary().getType())) {
                        f2++;
                    }
                }
            }
        }
        ItemStack frame = f1 > f2 ? Config.i().getInventoryImage().inventory().frame().secondary().clone() : Config.i().getInventoryImage().inventory().frame().primary().clone();
        if (frame.getItemMeta() != null) {
            ItemMeta frameMeta = frame.getItemMeta();
            frameMeta.setDisplayName(ChatColor.YELLOW + "");
            frame.setItemMeta(frameMeta);
        }
        for (int j = 0; j < 54; j++) {
            inv.setItem(j, frame);
        }
        inv.setItem(12, offlinePlayerData.getInventory().getItem(39));
        inv.setItem(21, offlinePlayerData.getInventory().getItem(38));
        inv.setItem(30, offlinePlayerData.getInventory().getItem(37));
        inv.setItem(39, offlinePlayerData.getInventory().getItem(36));

        ItemStack offhand = offlinePlayerData.getInventory().getSize() > 40 ? offlinePlayerData.getInventory().getItem(40) : null;
        if (VersionManager.version.isOld() && (offhand == null || offhand.getType().equals(Material.AIR))) {
            inv.setItem(24, offlinePlayerData.getInventory().getItem(selectedSlot));
        } else {
            inv.setItem(23, offhand);
            inv.setItem(25, offlinePlayerData.getInventory().getItem(selectedSlot));
        }

        ItemStack exp = XMaterial.EXPERIENCE_BOTTLE.parseItem();
        TranslatableComponent expText = Component.translatable(InventoryDisplay.getLevelTranslation(level)).color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
        if (level != 1) {
            expText = expText.arguments(Component.text(level + ""));
        }
        NMS.getInstance().setItemStackDisplayName(exp, expText);
        inv.setItem(37, exp);

        Inventory inv2 = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45, title);
        for (int j = 0; j < Math.min(offlinePlayerData.getInventory().getSize(), 45); j++) {
            ItemStack item = offlinePlayerData.getInventory().getItem(j);
            if (item != null && !item.getType().equals(Material.AIR)) {
                inv2.setItem(j, item.clone());
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            ItemStack skull = SkinUtils.getSkull(player.getUniqueId());
            ItemMeta meta = skull.getItemMeta();
            String name = ChatColorUtils.translateAlternateColorCodes('&', Config.i().getDiscordCommands().shareInventory().skullDisplayName().replace("{Player}", player.getName()));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            inv.setItem(10, skull);
        });

        if (Config.i().getSettings().hideLodestoneCompassPos()) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
            CompassUtils.hideLodestoneCompassesPosition(inv2);
        }

        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY1_UPPER, sha1, inv);
        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY1_LOWER, sha1, inv2);

        if (Config.i().getSettings().bungeecord()) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY1_UPPER, sha1, title, inv);
                BungeeMessageSender.addInventory(time, MultiChatDiscordSrvAddonAPI.SharedType.INVENTORY1_LOWER, sha1, title, inv2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void ender(OfflinePlayer player, String sha1, String title) throws Exception {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);
        int size = offlinePlayerData.getEnderChest().getSize();
        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(size), title);
        for (int j = 0; j < size; j++) {
            if (offlinePlayerData.getEnderChest().getItem(j) != null) {
                if (!offlinePlayerData.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
                    inv.setItem(j, offlinePlayerData.getEnderChest().getItem(j).clone());
                }
            }
        }

        if (Config.i().getSettings().hideLodestoneCompassPos()) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.ENDERCHEST, sha1, inv);

        if (Config.i().getSettings().bungeecord()) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, MultiChatDiscordSrvAddonAPI.SharedType.ENDERCHEST, sha1, title, inv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getPlayerGroups(OfflinePlayer player) {
        try {
            RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
            if (!rsp.getProvider().hasGroupSupport()) {
                return Collections.emptyList();
            }
            return Arrays.asList(rsp.getProvider().getPlayerGroups(Bukkit.getWorlds().get(0).getName(), player));
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("deprecation")
    public static List<ValueTrios<OfflinePlayer, Component, Integer>> sortPlayers(List<String> orderTypes, List<ValueTrios<OfflinePlayer, Component, Integer>> players, Map<UUID, ValuePairs<List<String>, String>> playerInfo) {
        if (players.size() <= 1) {
            return players;
        }
        Comparator<ValueTrios<OfflinePlayer, Component, Integer>> comparator = Comparator.comparing(each -> 0);
        for (String str : orderTypes) {
            String[] sections = str.split(":", 2);
            switch (sections[0].toUpperCase()) {
                case "GROUP":
                    if (sections.length > 1) {
                        List<String> groupOrder = Arrays.stream(sections[1].split(",")).map(each -> each.trim()).collect(Collectors.toList());
                        comparator = comparator.thenComparing(each -> {
                            ValuePairs<List<String>, String> info = playerInfo.get(each.getFirst().getUniqueId());
                            if (info == null) {
                                return Integer.MAX_VALUE;
                            }
                            return info.getFirst().stream().mapToInt(e -> groupOrder.indexOf(e)).max().orElse(Integer.MAX_VALUE - 1);
                        });
                    }
                    break;
                case "PLAYERNAME":
                    comparator = comparator.thenComparing(each -> {
                        ValuePairs<List<String>, String> info = playerInfo.get(each.getFirst().getUniqueId());
                        if (info == null) {
                            return "";
                        }
                        return info.getSecond();
                    });
                    break;
                case "PLAYERNAME_REVERSE":
                    comparator = comparator.thenComparing(Comparator.comparing(each -> {
                        ValuePairs<List<String>, String> info = playerInfo.get(((ValueTrios<OfflinePlayer, Component, Integer>) each).getFirst().getUniqueId());
                        if (info == null) {
                            return "";
                        }
                        return info.getSecond();
                    }).reversed());
                    break;
                case "PLACEHOLDER":
                    if (sections.length > 1) {
                        String placeholder = sections[1];
                        comparator = comparator.thenComparing(Comparator.comparing(each -> {
                            String parsedString = PlaceholderParser.parse(Bukkit.getOfflinePlayer(((ValueTrios<OfflinePlayer, Component, Integer>) each).getFirst().getUniqueId()), placeholder);
                            double value = Double.MAX_VALUE;
                            try {
                                value = Double.parseDouble(parsedString);
                            } catch (NumberFormatException ignore) {
                            }
                            return value;
                        }).thenComparing(each -> PlaceholderParser.parse(Bukkit.getOfflinePlayer(((ValueTrios<OfflinePlayer, Component, Integer>) each).getFirst().getUniqueId()), placeholder)));
                    }
                    break;
                case "PLACEHOLDER_REVERSE":
                    if (sections.length > 1) {
                        String placeholder = sections[1];
                        comparator = comparator.thenComparing(Comparator.comparing(each -> {
                            String parsedString = PlaceholderParser.parse(Bukkit.getOfflinePlayer(((ValueTrios<OfflinePlayer, Component, Integer>) each).getFirst().getUniqueId()), placeholder);
                            double value = Double.MAX_VALUE;
                            try {
                                value = Double.parseDouble(parsedString);
                            } catch (NumberFormatException ignore) {
                            }
                            return value;
                        }).thenComparing(each -> {
                            return PlaceholderParser.parse(Bukkit.getOfflinePlayer(((ValueTrios<OfflinePlayer, Component, Integer>) each).getFirst().getUniqueId()), placeholder);
                        }).reversed());
                    }
                    break;
                default:
                    break;
            }
        }
        comparator = comparator.thenComparing(each -> PlainTextComponentSerializer.plainText().serialize(each.getSecond())).thenComparing(each -> each.getFirst().getUniqueId());
        players.sort(comparator);
        return players;
    }

    public static void init() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            if (Config.i().getSettings().bungeecord()) {
                if (Config.i().getDiscordCommands().playerList().enabled() && Config.i().getDiscordCommands().playerList().isMainServer()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!PlayerUtils.isLocal(player)) {
                            StringBuilder text = new StringBuilder(Config.i().getDiscordCommands().playerList().tablistOptions().playerFormat() +
                                    " " + Config.i().getDiscordCommands().playerList().tablistOptions().headerText() +
                                    " " + Config.i().getDiscordCommands().playerList().tablistOptions().footerText() +
                                    " " + Config.i().getDiscordCommands().playerInfo().infoFormatting().whenOnline());
                            for (String type : Config.i().getDiscordCommands().playerList().tablistOptions().playerOrder().orderBy()) {
                                if (type.startsWith("PLACEHOLDER") && type.contains(":")) {
                                    String placeholder = type.split(":")[1];
                                    text.append(" ").append(placeholder);
                                }
                            }
                            try {
                                BungeeMessageSender.requestParsedPlaceholders(System.currentTimeMillis(), player.getUniqueId(), text.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }, 100, 100);
    }
}
