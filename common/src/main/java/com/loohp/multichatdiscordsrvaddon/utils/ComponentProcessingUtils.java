package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.api.events.*;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.registry.DiscordDataRegistry;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentProcessingUtils {

    public static final Int2ObjectMap<DiscordDisplayData> DATA = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
    public static final IntFunction<Pattern> DATA_PATTERN = i -> Pattern.compile("<ICD=" + i + "\\\\?>");
    public static final IDProvider DATA_ID_PROVIDER = new IDProvider();
    public static final Comparator<DiscordDisplayData> DISPLAY_DATA_COMPARATOR = Comparator.comparing(DiscordDisplayData::getPosition);

    public static Component processGameMessage(OfflinePlayer icSender, Component component, Component originalPlain) {
        boolean reserializer = Config.i().getSettings().useDiscordFormattingSerializer();
        PlaceholderCooldownManager cooldownManager = MultiChatDiscordSrvAddon.placeholderCooldownManager;
        long now = cooldownManager.checkMessage(icSender.getUniqueId(), PlainTextComponentSerializer.plainText().serialize(component)).getTimeNow();

        GameMessagePreProcessEvent gameMessagePreProcessEvent = new GameMessagePreProcessEvent(icSender, component, false);
        Bukkit.getPluginManager().callEvent(gameMessagePreProcessEvent);
        if (gameMessagePreProcessEvent.isCancelled()) {
            return null;
        }
        component = ComponentFlattening.flatten(gameMessagePreProcessEvent.getComponent());

        String plain = MultiChatComponentSerializer.plainText().serialize(originalPlain);

        for (Pattern pattern : MultiChatDiscordSrvAddon.plugin.toBlockPatterns) {
            Matcher matcher = pattern.matcher(plain);
            if (matcher.matches()) {
                return null;
            }
        }

        Debug.debug("onGameToDiscord processing custom placeholders");
        for (ICPlaceholder placeholder : MultiChatDiscordSrvAddonAPI.getPlaceholderList()) {
            if (!placeholder.isBuildIn()) {
                CustomPlaceholder customP = (CustomPlaceholder) placeholder;
                Matcher matcher = customP.getKeyword().matcher(plain);
                if (matcher.find()) {
                    if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), customP, now)) {
                        String replaceText;
                        if (customP.getReplace().isEnabled()) {
                            replaceText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icSender, customP.getReplace().getReplaceText()));
                        } else {
                            replaceText = null;
                        }
                        List<Component> toAppend = new LinkedList<>();
                        Set<String> shown = new HashSet<>();
                        component = ComponentReplacing.replace(component, customP.getKeyword().pattern(), true, (result, matchedComponents) -> {
                            String replaceString = replaceText == null ? result.group() : CustomStringUtils.applyReplacementRegex(replaceText, result, 1);
                            if (!shown.contains(replaceString)) {
                                shown.add(replaceString);
                                int position = result.start();
                                if (Config.i().getHoverEventDisplay().enabled() && !Config.i().getHoverEventDisplay().ignoredPlaceholderKeys().contains(customP.getKey())) {
                                    HoverClickDisplayData.Builder hoverClick = new HoverClickDisplayData.Builder().player(icSender).postion(position).color(DiscordDataRegistry.DISCORD_HOVER_COLOR).displayText(ChatColorUtils.stripColor(replaceString));
                                    boolean usingHoverClick = false;

                                    if (customP.getHover().isEnabled()) {
                                        usingHoverClick = true;
                                        String hoverText = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icSender, CustomStringUtils.applyReplacementRegex(customP.getHover().getText(), result, 1)));
                                        Color color = ColorUtils.getFirstColor(hoverText);
                                        hoverClick.hoverText(LegacyComponentSerializer.legacySection().deserialize(hoverText));
                                        if (color != null) {
                                            hoverClick.color(color);
                                        }
                                    }

                                    if (customP.getClick().isEnabled()) {
                                        usingHoverClick = true;
                                        String clickValue = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(icSender, CustomStringUtils.applyReplacementRegex(customP.getClick().getValue(), result, 1)));
                                        hoverClick.clickAction(customP.getClick().getAction()).clickValue(CustomStringUtils.applyReplacementRegex(clickValue, result, 1));
                                    }

                                    if (usingHoverClick) {
                                        int hoverId = DATA_ID_PROVIDER.getNext();
                                        DATA.put(hoverId, hoverClick.build());
                                        toAppend.add(Component.text("<ICD=" + hoverId + ">"));
                                    }
                                }
                            }
                            return replaceText == null ? Component.empty().children(matchedComponents) : LegacyComponentSerializer.legacySection().deserialize(replaceString);
                        });
                        for (Component componentToAppend : toAppend) {
                            component = component.append(componentToAppend);
                        }
                    } else {
                        return null;
                    }
                }
            }
        }

        if (Config.i().getInventoryImage().item().enabled()) {
            Debug.debug("onGameToDiscord processing item display");
            Matcher matcher = MultiChatDiscordSrvAddon.itemPlaceholder.getKeyword().matcher(plain);
            if (matcher.find()) {
                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.itemPlaceholder)).findFirst().get(), now)) {
                    ItemStack item = PlayerUtils.getMainHandItem(icSender);
                    boolean isAir = item.getType().equals(Material.AIR);
                    String itemStr = PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(ComponentModernizing.modernize(ItemStackUtils.getDisplayName(item)), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())));
                    itemStr = ComponentStringUtils.stripColorAndConvertMagic(itemStr);

                    int amount = item.getAmount();
                    if (isAir) {
                        amount = 1;
                    }

                    String replaceText = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, (amount == 1 ? Config.i().getInventoryImage().item().embedDisplay().single() : Config.i().getInventoryImage().item().embedDisplay().multiple()).replace("{Amount}", String.valueOf(amount))).replace("{Item}", itemStr));
                    if (reserializer) {
                        replaceText = DiscordSerializer.INSTANCE.serialize(Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);

                    component = ComponentReplacing.replace(component, "\\[.*" + (icSender.isOnline() ? PlainTextComponentSerializer.plainText().serialize(NMS.getInstance().getItemStackDisplayName(icSender.getPlayer().getEquipment().getItemInMainHand())) : "item") + ".*\\]", true, Component.text("[item]"));

                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.itemPlaceholder.getKeyword().pattern(), true, (groups) -> {
                        replaced.set(true);
                        return replaceComponent;
                    });

                    if (replaced.get() && Config.i().getInventoryImage().item().enabled()) {
                        int inventoryId = DATA_ID_PROVIDER.getNext();
                        int position = matcher.start();

                        String title = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().item().itemTitle()));

                        Inventory inv = DiscordContentUtils.getBlockInventory(item);

                        GameMessageProcessItemEvent gameMessageProcessItemEvent = new GameMessageProcessItemEvent(icSender, title, component, false, inventoryId, item.clone(), inv);
                        Bukkit.getPluginManager().callEvent(gameMessageProcessItemEvent);
                        if (!gameMessageProcessItemEvent.isCancelled()) {
                            component = gameMessageProcessItemEvent.getComponent();
                            title = gameMessageProcessItemEvent.getTitle();
                            if (gameMessageProcessItemEvent.hasInventory()) {
                                DATA.put(inventoryId, new ImageDisplayData(icSender, position, title, ImageDisplayType.ITEM_CONTAINER, gameMessageProcessItemEvent.getItemStack().clone(), new TitledInventoryWrapper(ItemStackUtils.getDisplayName(item, false), gameMessageProcessItemEvent.getInventory())));
                            } else {
                                DATA.put(inventoryId, new ImageDisplayData(icSender, position, title, ImageDisplayType.ITEM, gameMessageProcessItemEvent.getItemStack().clone()));
                            }
                        }
                        component = component.append(Component.text("<ICD=" + inventoryId + ">"));
                    }
                } else {
                    return null;
                }
            }
        }

        if (Config.i().getInventoryImage().inventory().enabled()) {
            Debug.debug("onGameToDiscord processing inventory display");
            Matcher matcher = MultiChatDiscordSrvAddon.inventoryPlaceholder.getKeyword().matcher(plain);
            if (matcher.find()) {
                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.inventoryPlaceholder)).findFirst().get(), now)) {
                    String replaceText = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().inventory().inventoryTitle()));
                    if (reserializer) {
                        replaceText = DiscordSerializer.INSTANCE.serialize(Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);
                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.inventoryPlaceholder.getKeyword().pattern(), true, (groups) -> {
                        replaced.set(true);
                        return replaceComponent;
                    });

                    if (replaced.get() && Config.i().getInventoryImage().inventory().enabled()) {
                        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(icSender);
                        int inventoryId = DATA_ID_PROVIDER.getNext();
                        int position = matcher.start();

                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45);
                        for (int j = 0; j < offlinePlayerData.getInventory().getSize(); j++) {
                            if (offlinePlayerData.getInventory().getItem(j) != null) {
                                if (!offlinePlayerData.getInventory().getItem(j).getType().equals(Material.AIR)) {
                                    inv.setItem(j, offlinePlayerData.getInventory().getItem(j).clone());
                                }
                            }
                        }
                        String title = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().inventory().inventoryTitle()));

                        GameMessageProcessPlayerInventoryEvent gameMessageProcessPlayerInventoryEvent = new GameMessageProcessPlayerInventoryEvent(icSender, title, component, false, inventoryId, inv);
                        Bukkit.getPluginManager().callEvent(gameMessageProcessPlayerInventoryEvent);
                        if (!gameMessageProcessPlayerInventoryEvent.isCancelled()) {
                            component = gameMessageProcessPlayerInventoryEvent.getComponent();
                            title = gameMessageProcessPlayerInventoryEvent.getTitle();
                            DATA.put(inventoryId, new ImageDisplayData(icSender, position, title, ImageDisplayType.INVENTORY, true, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getDefaultContainerTitle()), gameMessageProcessPlayerInventoryEvent.getInventory())));
                        }

                        component = component.append(Component.text("<ICD=" + inventoryId + ">"));
                    }
                } else {
                    return null;
                }
            }
        }

        if (Config.i().getInventoryImage().enderChest().enabled()) {
            Debug.debug("onGameToDiscord processing enderchest display");
            Matcher matcher = MultiChatDiscordSrvAddon.enderChestPlaceholder.getKeyword().matcher(plain);
            if (matcher.find()) {
                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.enderChestPlaceholder)).findFirst().get(), now)) {
                    String replaceText = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().enderChest().inventoryTitle()));
                    if (reserializer) {
                        replaceText = DiscordSerializer.INSTANCE.serialize(Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);
                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.enderChestPlaceholder.getKeyword().pattern(), true, (groups) -> {
                        replaced.set(true);
                        return replaceComponent;
                    });

                    OfflinePlayerData offlinePlayerData = PlayerUtils.getData(icSender);

                    if (replaced.get() && Config.i().getInventoryImage().enderChest().enabled()) {
                        int inventoryId = DATA_ID_PROVIDER.getNext();
                        int position = matcher.start();

                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(offlinePlayerData.getEnderChest().getSize()));
                        for (int j = 0; j < offlinePlayerData.getEnderChest().getSize(); j++) {
                            if (offlinePlayerData.getEnderChest().getItem(j) != null) {
                                if (!offlinePlayerData.getEnderChest().getItem(j).getType().equals(Material.AIR)) {
                                    inv.setItem(j, offlinePlayerData.getEnderChest().getItem(j).clone());
                                }
                            }
                        }
                        String title = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().enderChest().inventoryTitle()));

                        GameMessageProcessInventoryEvent gameMessageProcessInventoryEvent = new GameMessageProcessInventoryEvent(icSender, title, component, false, inventoryId, inv);
                        Bukkit.getPluginManager().callEvent(gameMessageProcessInventoryEvent);
                        if (!gameMessageProcessInventoryEvent.isCancelled()) {
                            component = gameMessageProcessInventoryEvent.getComponent();
                            title = gameMessageProcessInventoryEvent.getTitle();
                            DATA.put(inventoryId, new ImageDisplayData(icSender, position, title, ImageDisplayType.ENDERCHEST, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getEnderChestContainerTitle()), gameMessageProcessInventoryEvent.getInventory())));
                        }

                        component = component.append(Component.text("<ICD=" + inventoryId + ">"));
                    }
                } else {
                    return null;
                }
            }
        }

        if (Config.i().getDiscordMention().translateMentions() && !Config.i().getDiscordMention().suppressDiscordPings()) {
            Debug.debug("onGameToDiscord processing mentions");
            //boolean hasMentionPermission = PlayerUtils.hasPermission(icSender.getUniqueId(), "interactivechat.mention.player", true, 200); todo
            boolean hasMentionPermission = true;
            if (hasMentionPermission) {
                Map<String, UUID> names = new HashMap<>();
                for (Player icPlayer : Bukkit.getOnlinePlayers()) {
                    UUID uuid = icPlayer.getUniqueId();
                    names.put(ChatColorUtils.stripColor(icPlayer.getName()), uuid);
                    names.put(ChatColorUtils.stripColor(icPlayer.getDisplayName()), uuid);
                }
                Set<UUID> processedReceivers = new HashSet<>();
                for (Map.Entry<String, UUID> entry : names.entrySet()) {
                    String name = entry.getKey();
                    UUID uuid = entry.getValue();

                    String discordMention = DiscordProviderManager.get().getUserAsMention(uuid);
                    component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters('@' + name), true, PlainTextComponentSerializer.plainText().deserialize(discordMention));
                }
            }
            if (!hasMentionPermission /*|| !PlayerUtils.hasPermission(icSender.getUniqueId(), "interactivechat.mention.here", false, 200) todo */) {
                component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters('@' + "here"), false, Component.text("`" + '@' + "here`"));
            }
            if (! hasMentionPermission /*|| !PlayerUtils.hasPermission(icSender.getUniqueId(), "interactivechat.mention.everyone", false, 200) todo */) {
                component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters('@' + "everyone"), false, Component.text("`" + '@'+ "everyone`"));
            }
        }

        GameMessagePostProcessEvent gameMessagePostProcessEvent = new GameMessagePostProcessEvent(icSender, component, false);
        Bukkit.getPluginManager().callEvent(gameMessagePostProcessEvent);
        if (gameMessagePostProcessEvent.isCancelled()) {
            return null;
        }
        component = gameMessagePostProcessEvent.getComponent();
        return component;
    }

}
