/*
 * This file is part of InteractiveChatDiscordSrvAddon.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.multichatdiscordsrvaddon.listeners;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.multichatdiscordsrvaddon.api.InteractiveChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.modules.InventoryDisplay;
import com.loohp.multichatdiscordsrvaddon.modules.ItemDisplay;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.InteractiveChatDiscordSRVConfigReloadEvent;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackInfo;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.SubcommandData;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DiscordCommands implements Listener, SlashCommandProvider {

    public static final String CUSTOM_CHANNEL = "icdsrva:discord_commands";
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

    private static void layout0(OfflinePlayer player, String sha1, String title) throws Exception {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);

        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 54, title);
        int f1 = 0;
        int f2 = 0;
        int u = 45;
        for (int j = 0; j < Math.min(offlinePlayerData.getInventory().getSize(), 45); j++) {
            ItemStack item = offlinePlayerData.getInventory().getItem(j);
            if (item != null && !item.getType().equals(Material.AIR)) {
                if ((j >= 9 && j < 18) || j >= 36) {
                    if (item.getType().equals(MultiChatDiscordSrvAddon.plugin.invFrame1.getType())) {
                        f1++;
                    } else if (item.getType().equals(MultiChatDiscordSrvAddon.plugin.invFrame2.getType())) {
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
        ItemStack frame = f1 > f2 ? MultiChatDiscordSrvAddon.plugin.invFrame2.clone() : MultiChatDiscordSrvAddon.plugin.invFrame1.clone();
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
            String name = ChatColorUtils.translateAlternateColorCodes('&', MultiChatDiscordSrvAddon.plugin.shareInvCommandSkullName.replace("{Player}", player.getName()));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            inv.setItem(0, skull);
        });

        if (MultiChatDiscordSrvAddon.plugin.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        InteractiveChatDiscordSrvAddonAPI.addInventoryToItemShareList(InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY, sha1, inv);

        if (MultiChatDiscordSrvAddon.plugin.useBungeecord) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY, sha1, title, inv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void layout1(OfflinePlayer player, String sha1, String title) throws Exception {
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
                    if (item.getType().equals(MultiChatDiscordSrvAddon.plugin.invFrame1.getType())) {
                        f1++;
                    } else if (item.getType().equals(MultiChatDiscordSrvAddon.plugin.invFrame2.getType())) {
                        f2++;
                    }
                }
            }
        }
        ItemStack frame = f1 > f2 ? MultiChatDiscordSrvAddon.plugin.invFrame2.clone() : MultiChatDiscordSrvAddon.plugin.invFrame1.clone();
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
            String name = ChatColorUtils.translateAlternateColorCodes('&', MultiChatDiscordSrvAddon.plugin.shareInvCommandSkullName.replace("{Player}", player.getName()));
            meta.setDisplayName(name);
            skull.setItemMeta(meta);
            inv.setItem(10, skull);
        });

        if (MultiChatDiscordSrvAddon.plugin.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
            CompassUtils.hideLodestoneCompassesPosition(inv2);
        }

        InteractiveChatDiscordSrvAddonAPI.addInventoryToItemShareList(InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY1_UPPER, sha1, inv);
        InteractiveChatDiscordSrvAddonAPI.addInventoryToItemShareList(InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY1_LOWER, sha1, inv2);

        if (MultiChatDiscordSrvAddon.plugin.useBungeecord) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY1_UPPER, sha1, title, inv);
                BungeeMessageSender.addInventory(time, InteractiveChatDiscordSrvAddonAPI.SharedType.INVENTORY1_LOWER, sha1, title, inv2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void ender(OfflinePlayer player, String sha1, String title) throws Exception {
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

        if (MultiChatDiscordSrvAddon.plugin.hideLodestoneCompassPos) {
            CompassUtils.hideLodestoneCompassesPosition(inv);
        }

        InteractiveChatDiscordSrvAddonAPI.addInventoryToItemShareList(InteractiveChatDiscordSrvAddonAPI.SharedType.ENDERCHEST, sha1, inv);

        if (MultiChatDiscordSrvAddon.plugin.useBungeecord) {
            try {
                long time = System.currentTimeMillis();
                BungeeMessageSender.addInventory(time, InteractiveChatDiscordSrvAddonAPI.SharedType.ENDERCHEST, sha1, title, inv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ItemStack resolveItemStack(SlashCommandEvent event, OfflinePlayer player) {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);
        String subCommand = event.getSubcommandName();
        switch (subCommand) {
            case "mainhand":
                return offlinePlayerData.getInventory().getItem(offlinePlayerData.getSelectedSlot());
            case "offhand":
                return offlinePlayerData.getInventory().getSize() > 40 ? offlinePlayerData.getInventory().getItem(40) : null;
            case "hotbar":
            case "inventory":
                return offlinePlayerData.getInventory().getItem((int) event.getOptions().get(0).getAsLong() - 1);
            case "armor":
                return offlinePlayerData.getEquipment().getItem(EquipmentSlot.valueOf(event.getOptions().get(0).getAsString().toUpperCase()));
            case "ender":
                return offlinePlayerData.getEnderChest().getItem((int) event.getOptions().get(0).getAsLong() - 1);
        }
        return null;
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

    private DiscordSRV discordsrv;
    private Map<String, Component> components;

    public DiscordCommands(DiscordSRV discordsrv) {
        this.discordsrv = discordsrv;
        this.components = new ConcurrentHashMap<>();
    }

    public void init() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            if (MultiChatDiscordSrvAddon.plugin.useBungeecord) {
                if (MultiChatDiscordSrvAddon.plugin.playerlistCommandEnabled && MultiChatDiscordSrvAddon.plugin.playerlistCommandIsMainServer) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!PlayerUtils.isLocal(player)) {
                            StringBuilder text = new StringBuilder(MultiChatDiscordSrvAddon.plugin.playerlistCommandPlayerFormat +
                                    " " + MultiChatDiscordSrvAddon.plugin.playerlistCommandHeader +
                                    " " + MultiChatDiscordSrvAddon.plugin.playerlistCommandFooter +
                                    " " + MultiChatDiscordSrvAddon.plugin.playerinfoCommandFormatOnline);
                            for (String type : MultiChatDiscordSrvAddon.plugin.playerlistOrderingTypes) {
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

    @EventHandler
    public void onConfigReload(InteractiveChatDiscordSRVConfigReloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> reload());
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        Guild guild = discordsrv.getMainGuild();

        String memberLabel = MultiChatDiscordSrvAddon.plugin.discordMemberLabel;
        String memberDescription = MultiChatDiscordSrvAddon.plugin.discordMemberDescription;
        String slotLabel = MultiChatDiscordSrvAddon.plugin.discordSlotLabel;
        String slotDescription = MultiChatDiscordSrvAddon.plugin.discordSlotDescription;

        List<CommandData> commandDataList = new ArrayList<>();

        if (MultiChatDiscordSrvAddon.plugin.resourcepackCommandIsMainServer) {
            if (MultiChatDiscordSrvAddon.plugin.resourcepackCommandEnabled) {
                commandDataList.add(new CommandData(RESOURCEPACK_LABEL, ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.resourcepackCommandDescription)));
            }
        }
        if (MultiChatDiscordSrvAddon.plugin.playerinfoCommandIsMainServer) {
            if (MultiChatDiscordSrvAddon.plugin.playerinfoCommandEnabled) {
                commandDataList.add(new CommandData(PLAYERINFO_LABEL, ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.playerinfoCommandDescription)).addOptions(new OptionData(OptionType.USER, memberLabel, memberDescription, false)));
            }
        }
        if (MultiChatDiscordSrvAddon.plugin.playerlistCommandIsMainServer) {
            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandEnabled) {
                commandDataList.add(new CommandData(PLAYERLIST_LABEL, ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandDescription)));
            }
        }
        if (MultiChatDiscordSrvAddon.plugin.shareItemCommandIsMainServer) {
            Optional<ICPlaceholder> optItemPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.itemPlaceholder)).findFirst();
            if (MultiChatDiscordSrvAddon.plugin.shareItemCommandEnabled && optItemPlaceholder.isPresent()) {
                String itemDescription = ChatColorUtils.stripColor(optItemPlaceholder.get().getDescription());

                SubcommandData mainhandSubcommand = new SubcommandData("mainhand", itemDescription);
                SubcommandData offhandSubcommand = new SubcommandData("offhand", itemDescription);
                SubcommandData hotbarSubcommand = new SubcommandData("hotbar", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 9));
                SubcommandData inventorySubcommand = new SubcommandData("inventory", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 41));
                SubcommandData armorSubcommand = new SubcommandData("armor", itemDescription).addOptions(new OptionData(OptionType.STRING, slotLabel, slotDescription, true).addChoice("head", "head").addChoice("chest", "chest").addChoice("legs", "legs").addChoice("feet", "feet"));
                SubcommandData enderSubcommand = new SubcommandData("ender", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 27));

                commandDataList.add(new CommandData(ITEM_LABEL, ChatColorUtils.stripColor(optItemPlaceholder.get().getDescription())).addSubcommands(mainhandSubcommand).addSubcommands(offhandSubcommand).addSubcommands(hotbarSubcommand).addSubcommands(inventorySubcommand).addSubcommands(armorSubcommand).addSubcommands(enderSubcommand));

                if (MultiChatDiscordSrvAddon.plugin.shareItemCommandAsOthers) {
                    SubcommandData mainhandOtherSubcommand = new SubcommandData("mainhand", itemDescription).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData offhandOtherSubcommand = new SubcommandData("offhand", itemDescription).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData hotbarOtherSubcommand = new SubcommandData("hotbar", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 9)).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData inventoryOtherSubcommand = new SubcommandData("inventory", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 41)).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData armorOtherSubcommand = new SubcommandData("armor", itemDescription).addOptions(new OptionData(OptionType.STRING, slotLabel, slotDescription, true).addChoice("head", "head").addChoice("chest", "chest").addChoice("legs", "legs").addChoice("feet", "feet")).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData enderOtherSubcommand = new SubcommandData("ender", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 27)).addOption(OptionType.USER, memberLabel, memberDescription, true);

                    commandDataList.add(new CommandData(ITEM_OTHER_LABEL, ChatColorUtils.stripColor(optItemPlaceholder.get().getDescription())).addSubcommands(mainhandOtherSubcommand).addSubcommands(offhandOtherSubcommand).addSubcommands(hotbarOtherSubcommand).addSubcommands(inventoryOtherSubcommand).addSubcommands(armorOtherSubcommand).addSubcommands(enderOtherSubcommand));
                }
            }
        }
        if (MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
            Optional<ICPlaceholder> optInvPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.inventoryPlaceholder)).findFirst();
            if (MultiChatDiscordSrvAddon.plugin.shareInvCommandEnabled && optInvPlaceholder.isPresent()) {
                commandDataList.add(new CommandData(INVENTORY_LABEL, ChatColorUtils.stripColor(optInvPlaceholder.get().getDescription())));

                if (MultiChatDiscordSrvAddon.plugin.shareInvCommandAsOthers) {
                    commandDataList.add(new CommandData(INVENTORY_OTHER_LABEL, ChatColorUtils.stripColor(optInvPlaceholder.get().getDescription())).addOption(OptionType.USER, memberLabel, memberDescription, true));
                }
            }
        }
        if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
            Optional<ICPlaceholder> optEnderPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.enderChestPlaceholder)).findFirst();
            if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandEnabled && optEnderPlaceholder.isPresent()) {
                commandDataList.add(new CommandData(ENDERCHEST_LABEL, ChatColorUtils.stripColor(optEnderPlaceholder.get().getDescription())));

                if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandAsOthers) {
                    commandDataList.add(new CommandData(ENDERCHEST_OTHER_LABEL, ChatColorUtils.stripColor(optEnderPlaceholder.get().getDescription())).addOption(OptionType.USER, memberLabel, memberDescription, true));
                }
            }
        }

        return commandDataList.stream().map(each -> new PluginSlashCommand(MultiChatDiscordSrvAddon.plugin, each, guild.getId())).collect(Collectors.toSet());
    }

    public void reload() {
        DiscordSRV.api.updateSlashCommands();
    }

    @SlashCommand(path = "*")
    public void onSlashCommand(SlashCommandEvent event) {
        Guild guild = discordsrv.getMainGuild();
        if (event.getGuild().getIdLong() != guild.getIdLong()) {
            return;
        }
        if (!(event.getChannel() instanceof TextChannel)) {
            return;
        }
        TextChannel channel = (TextChannel) event.getChannel();
        String label = event.getName();
        if (MultiChatDiscordSrvAddon.plugin.resourcepackCommandEnabled && label.equalsIgnoreCase(RESOURCEPACK_LABEL)) {
            if (MultiChatDiscordSrvAddon.plugin.resourcepackCommandIsMainServer) {
                event.deferReply().setEphemeral(true).queue();
                List<MessageEmbed> messageEmbeds = new ArrayList<>();
                Map<String, byte[]> attachments = new HashMap<>();
                String footer = "InteractiveChatDiscordSRVAddon v" + MultiChatDiscordSrvAddon.plugin.getDescription().getVersion();
                int i = 0;
                List<ResourcePackInfo> packs = MultiChatDiscordSrvAddon.plugin.getResourceManager().getResourcePackInfo();
                for (ResourcePackInfo packInfo : packs) {
                    i++;
                    Component packName = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveName(packInfo)), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(MultiChatDiscordSrvAddon.plugin.language));
                    Component description = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveDescription(packInfo)), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(MultiChatDiscordSrvAddon.plugin.language));
                    EmbedBuilder builder = new EmbedBuilder().setAuthor(PlainTextComponentSerializer.plainText().serialize(packName)).setThumbnail("attachment://" + i + ".png");
                    if (packInfo.getStatus()) {
                        builder.setDescription(PlainTextComponentSerializer.plainText().serialize(description));
                        ChatColor firstColor = ChatColorUtils.getColor(LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().character(ChatColorUtils.COLOR_CHAR).build().serialize(description));
                        if (firstColor == null) {
                            firstColor = ChatColor.WHITE;
                        }
                        Color color = ColorUtils.getColor(firstColor);
                        if (color == null) {
                            color = new Color(0xAAAAAA);
                        } else if (color.equals(Color.WHITE)) {
                            color = DiscordContentUtils.OFFSET_WHITE;
                        }
                        builder.setColor(color);
                        if (packInfo.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                            builder.setFooter(LanguageUtils.getTranslation(TranslationKeyUtils.getNewIncompatiblePack(), MultiChatDiscordSrvAddon.plugin.language).getResult());
                        } else if (packInfo.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                            builder.setFooter(LanguageUtils.getTranslation(TranslationKeyUtils.getOldIncompatiblePack(), MultiChatDiscordSrvAddon.plugin.language).getResult());
                        }
                    } else {
                        builder.setColor(0xFF0000).setDescription(packInfo.getRejectedReason());
                    }
                    if (i >= packs.size()) {
                        builder.setFooter(footer, "https://resources.loohpjames.com/images/InteractiveChat-DiscordSRV-Addon.png");
                    }
                    messageEmbeds.add(builder.build());
                    try {
                        attachments.put(i + ".png", ImageUtils.toArray(ImageUtils.resizeImageAbs(packInfo.getIcon(), 128, 128)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                WebhookMessageUpdateAction<Message> action = event.getHook().setEphemeral(true).editOriginal("**" + LanguageUtils.getTranslation(TranslationKeyUtils.getServerResourcePack(), MultiChatDiscordSrvAddon.plugin.language).getResult() + "**").setEmbeds(messageEmbeds);
                for (Entry<String, byte[]> entry : attachments.entrySet()) {
                    action = action.addFile(entry.getValue(), entry.getKey());
                }
                action.queue();
            }
        } else if (MultiChatDiscordSrvAddon.plugin.playerinfoCommandEnabled && label.equalsIgnoreCase(PLAYERINFO_LABEL)) {
            if (MultiChatDiscordSrvAddon.plugin.playerinfoCommandIsMainServer) {
                String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
                if (minecraftChannel == null) {
                    if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels) {
                        event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.invalidDiscordChannel)).setEphemeral(true).queue();
                    }
                    return;
                }

                String discordUserId = event.getUser().getId();
                List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
                if (options.size() > 0) {
                    discordUserId = options.get(0).getAsUser().getId();
                }
                UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
                if (uuid == null) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.accountNotLinked)).setEphemeral(true).queue();
                    return;
                }
                event.deferReply().queue();

                int errorCode = -1;
                try {
                    OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                    errorCode--;
                    List<ToolTipComponent<?>> playerInfoComponents;
                    if (offlineICPlayer.isOnline() && !PlayerUtils.isVanished(((Player) offlineICPlayer))) {
                        playerInfoComponents = MultiChatDiscordSrvAddon.plugin.playerinfoCommandFormatOnline.stream().map(each -> {
                            each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));
                            return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                        }).collect(Collectors.toList());
                    } else {
                        playerInfoComponents = MultiChatDiscordSrvAddon.plugin.playerinfoCommandFormatOffline.stream().map(each -> {
                            each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));
                            return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                        }).collect(Collectors.toList());
                    }
                    errorCode--;
                    String title = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, MultiChatDiscordSrvAddon.plugin.playerinfoCommandFormatTitle)));
                    String subtitle = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, MultiChatDiscordSrvAddon.plugin.playerinfoCommandFormatSubTitle)));
                    BufferedImage image = ImageGeneration.getToolTipImage(playerInfoComponents, null);
                    errorCode--;
                    byte[] data = ImageUtils.toArray(image);
                    errorCode--;
                    event.getHook().editOriginalEmbeds(new EmbedBuilder().setTitle(title).setDescription(subtitle).setThumbnail(DiscordSRV.getAvatarUrl(offlineICPlayer.getName(), offlineICPlayer.getUniqueId())).setImage("attachment://PlayerInfo.png").setColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandColor).build()).addFile(data, "PlayerInfo.png").queue();
                } catch (Throwable e) {
                    e.printStackTrace();
                    event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").queue();
                    return;
                }
            }
        } else if (MultiChatDiscordSrvAddon.plugin.playerlistCommandEnabled && label.equalsIgnoreCase(PLAYERLIST_LABEL)) {
            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandIsMainServer) {
                String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
                if (minecraftChannel == null) {
                    if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels) {
                        event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.invalidDiscordChannel)).setEphemeral(true).queue();
                    }
                    return;
                }
                AtomicBoolean deleted = new AtomicBoolean(false);
                event.deferReply().queue(hook -> {
                    if (MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter > 0) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                            if (!deleted.get()) {
                                hook.deleteOriginal().queue();
                            }
                        }, MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter * 20L);
                    }
                });
                Map<OfflinePlayer, Integer> players;
                if (MultiChatDiscordSrvAddon.plugin.useBungeecord && MultiChatDiscordSrvAddon.plugin.playerlistCommandBungeecord && !Bukkit.getOnlinePlayers().isEmpty()) {
                    try {
                        List<ValueTrios<UUID, String, Integer>> bungeePlayers = InteractiveChatDiscordSrvAddonAPI.getBungeecordPlayerList().get();
                        players = new LinkedHashMap<>(bungeePlayers.size());
                        for (ValueTrios<UUID, String, Integer> playerinfo : bungeePlayers) {
                            UUID uuid = playerinfo.getFirst();
                            Player icPlayer = Bukkit.getPlayer(uuid);
                            if (icPlayer == null || !PlayerUtils.isVanished(icPlayer)) {
                                players.put(Bukkit.getOfflinePlayer(uuid), playerinfo.getThird());
                            }
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (-1)").queue();
                        return;
                    }
                } else {
                    players = Bukkit.getOnlinePlayers().stream().filter(each -> {
                        return each == null || !PlayerUtils.isVanished(each);
                    }).collect(Collectors.toMap(each -> each, each -> each.getPing(), (a, b) -> a));
                }
                if (players.isEmpty()) {
                    event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandEmptyServer)).queue();
                } else {
                    int errorCode = -2;
                    try {
                        List<ValueTrios<OfflinePlayer, Component, Integer>> player = new ArrayList<>();
                        Map<UUID, ValuePairs<List<String>, String>> playerInfo = new HashMap<>();
                        for (Entry<OfflinePlayer, Integer> entry : players.entrySet()) {
                            OfflinePlayer bukkitOfflinePlayer = entry.getKey();
                            playerInfo.put(bukkitOfflinePlayer.getUniqueId(), new ValuePairs<>(getPlayerGroups(bukkitOfflinePlayer), bukkitOfflinePlayer.getName()));
                            String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(bukkitOfflinePlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandPlayerFormat));
                            Component nameComponent;
                            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandParsePlayerNamesWithMiniMessage) {
                                nameComponent = MiniMessage.miniMessage().deserialize(name);
                            } else {
                                nameComponent = InteractiveChatComponentSerializer.legacySection().deserialize(name);
                            }
                            player.add(new ValueTrios<>(bukkitOfflinePlayer, nameComponent, entry.getValue()));
                        }
                        errorCode--;
                        sortPlayers(MultiChatDiscordSrvAddon.plugin.playerlistOrderingTypes, player, playerInfo);
                        errorCode--;
                        OfflinePlayer firstPlayer = Bukkit.getOfflinePlayer(players.keySet().iterator().next().getUniqueId());
                        List<Component> header = new ArrayList<>();
                        if (!MultiChatDiscordSrvAddon.plugin.playerlistCommandHeader.isEmpty()) {
                            header = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandHeader.replace("{OnlinePlayers}", players.size() + "")))));
                        }
                        errorCode--;
                        List<Component> footer = new ArrayList<>();
                        if (!MultiChatDiscordSrvAddon.plugin.playerlistCommandFooter.isEmpty()) {
                            footer = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandFooter.replace("{OnlinePlayers}", players.size() + "")))));
                        }
                        errorCode--;
                        int playerListMaxPlayers = MultiChatDiscordSrvAddon.plugin.playerlistMaxPlayers;
                        if (playerListMaxPlayers < 1) {
                            playerListMaxPlayers = Integer.MAX_VALUE;
                        }
                        BufferedImage image = ImageGeneration.getTabListImage(header, footer, player, MultiChatDiscordSrvAddon.plugin.playerlistCommandAvatar, MultiChatDiscordSrvAddon.plugin.playerlistCommandPing, playerListMaxPlayers);
                        errorCode--;
                        byte[] data = ImageUtils.toArray(image);
                        errorCode--;
                        event.getHook().editOriginalEmbeds(new EmbedBuilder().setImage("attachment://Tablist.png").setColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandColor).build()).addFile(data, "Tablist.png").queue(message -> {
                            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter > 0) {
                                deleted.set(true);
                                message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter, TimeUnit.SECONDS);
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                        event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").queue(message -> {
                            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter > 0) {
                                deleted.set(true);
                                message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter, TimeUnit.SECONDS);
                            }
                        });
                        return;
                    }
                }
            }
        } else if (MultiChatDiscordSrvAddon.plugin.shareItemCommandEnabled && (label.equalsIgnoreCase(ITEM_LABEL) || label.equalsIgnoreCase(ITEM_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels && MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.invalidDiscordChannel)).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (options.size() > 0) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (MultiChatDiscordSrvAddon.plugin.shareItemCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.accountNotLinked)).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlineICPlayer == null) {
                    if (MultiChatDiscordSrvAddon.plugin.shareItemCommandIsMainServer) {
                        event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (MultiChatDiscordSrvAddon.plugin.shareItemCommandIsMainServer) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (MultiChatDiscordSrvAddon.plugin.useBungeecord && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        ItemStack[] equipment;
                        if (VersionManager.version.isOld()) {
                            //noinspection deprecation
                            equipment = new ItemStack[] {icplayer.getEquipment().getHelmet(), icplayer.getEquipment().getChestplate(), icplayer.getEquipment().getLeggings(), icplayer.getEquipment().getBoots(), icplayer.getEquipment().getItemInHand()};
                        } else {
                            equipment = new ItemStack[] {icplayer.getEquipment().getHelmet(), icplayer.getEquipment().getChestplate(), icplayer.getEquipment().getLeggings(), icplayer.getEquipment().getBoots(), icplayer.getEquipment().getItemInMainHand(), icplayer.getEquipment().getItemInOffHand()};
                        }
                        try {
                            OfflinePlayerData offlinePlayerData = PlayerUtils.getData(icplayer);
                            BungeeMessageSender.forwardEquipment(System.currentTimeMillis(), icplayer.getUniqueId(), PlayerUtils.isRightHanded(icplayer), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), equipment);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                ItemStack itemStack = resolveItemStack(event, offlineICPlayer);
                if (itemStack == null) {
                    itemStack = new ItemStack(Material.AIR);
                }
                errorCode--;
                String title = ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.shareItemCommandTitle.replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                Component itemTag = ItemDisplay.createItemDisplay(offlineICPlayer, itemStack, title, true, null, false);
                Component resolvedItemTag = ComponentStringUtils.resolve(ComponentModernizing.modernize(itemTag), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(MultiChatDiscordSrvAddon.plugin.language));
                Component component = LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareItemCommandInGameMessageText.replace("{Player}", offlineICPlayer.getName())).replaceText(TextReplacementConfig.builder().matchLiteral("{ItemTag}").replacement(itemTag).build());
                Component resolvedComponent = LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareItemCommandInGameMessageText.replace("{Player}", offlineICPlayer.getName())).replaceText(TextReplacementConfig.builder().matchLiteral("{ItemTag}").replacement(resolvedItemTag).build());
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (MultiChatDiscordSrvAddon.plugin.shareItemCommandIsMainServer) {
                    errorCode--;

                    Inventory inv = null;
                    if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                        BlockState bsm = ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
                        if (bsm instanceof InventoryHolder) {
                            Inventory container = ((InventoryHolder) bsm).getInventory();
                            if (!container.isEmpty()) {
                                inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(container.getSize()));
                                for (int j = 0; j < container.getSize(); j++) {
                                    if (container.getItem(j) != null) {
                                        if (!container.getItem(j).getType().equals(Material.AIR)) {
                                            inv.setItem(j, container.getItem(j).clone());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ImageDisplayData data;
                    if (inv != null) {
                        data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM_CONTAINER, itemStack.clone(), new TitledInventoryWrapper(ItemStackUtils.getDisplayName(itemStack, false), inv));
                    } else {
                        data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM, itemStack.clone());
                    }
                    ValuePairs<List<DiscordMessageContent>, InteractionHandler> pair = DiscordContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    InteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(resolvedComponent)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = content.toJDAMessageEmbeds();
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                            message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").queue(message -> {
                    if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                        message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                    }
                });
                return;
            }
        } else if (MultiChatDiscordSrvAddon.plugin.shareInvCommandEnabled && (label.equalsIgnoreCase(INVENTORY_LABEL) || label.equalsIgnoreCase(INVENTORY_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels && MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.invalidDiscordChannel)).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (options.size() > 0) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.accountNotLinked)).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                OfflinePlayerData offlinePlayerData = PlayerUtils.getData(offlineICPlayer);
                if (offlineICPlayer == null) {
                    if (MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                        event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (MultiChatDiscordSrvAddon.plugin.useBungeecord && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        BungeeMessageSender.forwardInventory(System.currentTimeMillis(), uuid, offlinePlayerData.isRightHanded(), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), null, offlinePlayerData.getInventory());
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                Component component = LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareInvCommandInGameMessageText.replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String title = ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.shareInvCommandTitle.replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String sha1 = HashUtils.createSha1(true, offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), title, offlinePlayerData.getInventory());
                errorCode--;
                layout0(offlineICPlayer, sha1, title);
                errorCode--;
                layout1(offlineICPlayer, sha1, title);
                errorCode--;
                component = component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareInvCommandInGameMessageHover)));
                component = component.clickEvent(ClickEvent.runCommand("/interactivechat viewinv " + sha1));
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (MultiChatDiscordSrvAddon.plugin.shareInvCommandIsMainServer) {
                    ImageDisplayData data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.INVENTORY, true, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getDefaultContainerTitle()), offlinePlayerData.getInventory()));
                    ValuePairs<List<DiscordMessageContent>, InteractionHandler> pair = DiscordContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    InteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(component)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = content.toJDAMessageEmbeds();
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                            message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").queue(message -> {
                    if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                        message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                    }
                });
                return;
            }
        } else if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandEnabled && (label.equals(ENDERCHEST_LABEL) || label.equals(ENDERCHEST_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels && MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.invalidDiscordChannel)).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (options.size() > 0) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
                    event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.accountNotLinked)).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                OfflinePlayerData offlinePlayerData = PlayerUtils.getData(offlineICPlayer);
                if (offlineICPlayer == null) {
                    if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
                        event.reply(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (MultiChatDiscordSrvAddon.plugin.useBungeecord && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        BungeeMessageSender.forwardEnderchest(System.currentTimeMillis(), uuid, offlinePlayerData.isRightHanded(), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), null, icplayer.getEnderChest());
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                Component component = LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareEnderCommandInGameMessageText.replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String title = ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.shareEnderCommandTitle.replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String sha1 = HashUtils.createSha1(true, offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), title, offlinePlayerData.getEnderChest());
                errorCode--;
                ender(offlineICPlayer, sha1, title);
                errorCode--;
                component = component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(MultiChatDiscordSrvAddon.plugin.shareEnderCommandInGameMessageHover)));
                component = component.clickEvent(ClickEvent.runCommand("/interactivechat viewender " + sha1));
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (MultiChatDiscordSrvAddon.plugin.shareEnderCommandIsMainServer) {
                    ImageDisplayData data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ENDERCHEST, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getEnderChestContainerTitle()), offlinePlayerData.getEnderChest()));
                    ValuePairs<List<DiscordMessageContent>, InteractionHandler> pair = DiscordContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    InteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(component)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = content.toJDAMessageEmbeds();
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                            message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")").queue(message -> {
                    if (MultiChatDiscordSrvAddon.plugin.embedDeleteAfter > 0) {
                        message.delete().queueAfter(MultiChatDiscordSrvAddon.plugin.embedDeleteAfter, TimeUnit.SECONDS);
                    }
                });
                return;
            }
        }
    }

    // todo migrate
    /*@EventHandler
    public void onProcessChat(PostPacketComponentProcessEvent event) {
        Component component = event.getComponent();
        for (Entry<String, Component> entry : components.entrySet()) {
            if (PlainTextComponentSerializer.plainText().serialize(component).contains(entry.getKey())) {
                event.setComponent(ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters(entry.getKey()), false, entry.getValue()));
                break;
            }
        }
    }*/

    public static class DiscordCommandRegistrationException extends RuntimeException {

        public DiscordCommandRegistrationException(String message) {
            super(message);
        }

        public DiscordCommandRegistrationException(Throwable cause) {
            super(cause);
        }

        public DiscordCommandRegistrationException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }

}
