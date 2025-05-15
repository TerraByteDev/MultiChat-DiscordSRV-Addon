/*
 * This file is part of InteractiveChatDiscordSrvAddon2.
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

import com.github.puregero.multilib.MultiLib;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.DiscordImageEvent;
import com.loohp.multichatdiscordsrvaddon.api.events.GameMessagePostProcessEvent;
import com.loohp.multichatdiscordsrvaddon.api.events.GameMessagePreProcessEvent;
import com.loohp.multichatdiscordsrvaddon.api.events.GameMessageProcessInventoryEvent;
import com.loohp.multichatdiscordsrvaddon.api.events.GameMessageProcessItemEvent;
import com.loohp.multichatdiscordsrvaddon.api.events.GameMessageProcessPlayerInventoryEvent;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.registry.DiscordDataRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.languages.SpecificTranslateFunction;
import com.loohp.multichatdiscordsrvaddon.utils.DiscordItemStackUtils.DiscordToolTip;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AchievementMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.AchievementMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.DeathMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DeathMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.api.events.VentureChatMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.ChannelType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import github.scarsz.discordsrv.objects.MessageFormat;
import github.scarsz.discordsrv.util.MessageUtil;
import github.scarsz.discordsrv.util.WebhookUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.utils.ChatUtils.toAllow;

@SuppressWarnings("deprecation")
public class OutboundToDiscordEvents implements Listener {

    public static final Comparator<DiscordDisplayData> DISPLAY_DATA_COMPARATOR = Comparator.comparing(DiscordDisplayData::getPosition);
    public static final Int2ObjectMap<DiscordDisplayData> DATA = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
    public static final IntFunction<Pattern> DATA_PATTERN = i -> Pattern.compile("<ICD=" + i + "\\\\?>");
    public static final Int2ObjectMap<AttachmentData> RESEND_WITH_ATTACHMENT = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
    private static final IDProvider DATA_ID_PROVIDER = new IDProvider();
    private static final Map<UUID, Component> DEATH_MESSAGE = new ConcurrentHashMap<>();

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onGameToDiscordLowest(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.LOWEST)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onGameToDiscordLow(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.LOW)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onGameToDiscordNormal(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.NORMAL)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onGameToDiscordHigh(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.HIGH)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onGameToDiscordHighest(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.HIGHEST)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onGameToDiscordMonitor(GameChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.gameToDiscordPriority.equals(ListenerPriority.MONITOR)) {
            handleGameToDiscord(event);
        }
    }

    public void handleGameToDiscord(GameChatMessagePreProcessEvent event) {
        Debug.debug("Triggering onGameToDiscord");

        boolean pluginHookEnabled = Config.i().getHook().shouldHook();

        String originalPlain;

        if (event.isCancelled()) {
            Debug.debug("onGameToDiscord already cancelled");
            return;
        } else if (pluginHookEnabled && !toAllow.containsKey(event.getMessage())) {
            event.setCancelled(true);
            return;
        } else originalPlain = toAllow.get(event.getMessage());

        if (pluginHookEnabled) toAllow.remove(event.getMessage());
        MultiChatDiscordSrvAddon.plugin.messagesCounter.incrementAndGet();

        Player sender = event.getPlayer();
        OfflinePlayer icSender = Bukkit.getOfflinePlayer(sender.getUniqueId());
        Component message = ComponentStringUtils.toRegularComponent(event.getMessageComponent());

        message = processGameMessage(icSender, message, Component.text(originalPlain));

        if (message == null) {
            event.setCancelled(true);
            return;
        }

        event.setMessageComponent(ComponentStringUtils.toDiscordSRVComponent(message));
    }

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onVentureChatHookToDiscordLowest(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.LOWEST)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onVentureChatHookToDiscordLow(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.LOW)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onVentureChatHookToDiscordNormal(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.NORMAL)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onVentureChatHookToDiscordHigh(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.HIGH)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onVentureChatHookToDiscordHighest(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.HIGHEST)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onVentureChatHookToDiscordMonitor(VentureChatMessagePreProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.ventureChatToDiscordPriority.equals(ListenerPriority.MONITOR)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    public void handleVentureChatHookToDiscord(VentureChatMessagePreProcessEvent event) {
        Debug.debug("Triggering onVentureChatHookToDiscord");
        if (event.isCancelled()) {
            Debug.debug("onVentureChatHookToDiscord already cancelled");
            return;
        }
        MultiChatDiscordSrvAddon.plugin.messagesCounter.incrementAndGet();

        Player icSender;
        MineverseChatPlayer mcPlayer = event.getVentureChatEvent().getMineverseChatPlayer();
        if (mcPlayer != null) {
            icSender = Bukkit.getPlayer(mcPlayer.getUUID());
        } else {
            icSender = Bukkit.getPlayer(event.getVentureChatEvent().getUsername());
        }
        if (icSender == null) {
            return;
        }
        Component message = ComponentStringUtils.toRegularComponent(event.getMessageComponent());

        message = processGameMessage(icSender, message, message);

        if (message == null) {
            event.setCancelled(true);
            return;
        }

        event.setMessageComponent(ComponentStringUtils.toDiscordSRVComponent(message));
    }

    public Component processGameMessage(OfflinePlayer icSender, Component component, Component originalPlain) {
        boolean reserializer = DiscordSRV.config().getBoolean("Experiment_MCDiscordReserializer_ToDiscord");
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
        for (List<ICPlaceholder> list : MultiChatDiscordSrvAddonAPI.getPlaceholderList()) {
            for (ICPlaceholder placeholder : list) {
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
        }

        if (Config.i().getInventoryImage().item().enabled()) {
            Debug.debug("onGameToDiscord processing item display");
            ValuePairs<Boolean, Matcher> matcherPairs = PatternUtils.matches(plain, MultiChatDiscordSrvAddon.itemPlaceholder);

            if (matcherPairs.getFirst()) {
                Matcher matcher = matcherPairs.getSecond();

                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.itemPlaceholder)).findFirst().get().getFirst(), now)) {
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
                        replaceText = MessageUtil.reserializeToDiscord(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);

                    component = ComponentReplacing.replace(component, "\\[.*" + (icSender.isOnline() ? PlainTextComponentSerializer.plainText().serialize(NMS.getInstance().getItemStackDisplayName(icSender.getPlayer().getEquipment().getItemInMainHand())) : "item") + ".*\\]", true, Component.text("[item]"));

                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.itemPlaceholder.stream().map(k -> k.getKeyword().pattern()).collect(Collectors.toList()), true, (groups) -> {
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

            ValuePairs<Boolean, Matcher> matcherPairs = PatternUtils.matches(plain, MultiChatDiscordSrvAddon.inventoryPlaceholder);
            if (matcherPairs.getFirst()) {
                Matcher matcher = matcherPairs.getSecond();

                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.inventoryPlaceholder)).findFirst().get().getFirst(), now)) {
                    String replaceText = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().inventory().inventoryTitle()));
                    if (reserializer) {
                        replaceText = MessageUtil.reserializeToDiscord(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);
                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.inventoryPlaceholder.stream().map(k -> k.getKeyword().pattern()).collect(Collectors.toList()), true, (groups) -> {
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

            ValuePairs<Boolean, Matcher> matcherPairs = PatternUtils.matches(plain, MultiChatDiscordSrvAddon.enderChestPlaceholder);

            if (matcherPairs.getFirst()) {
                Matcher matcher = matcherPairs.getSecond();

                if (!cooldownManager.isPlaceholderOnCooldownAt(icSender.getUniqueId(), MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.enderChestPlaceholder)).findFirst().get().getFirst(), now)) {
                    String replaceText = ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(icSender, Config.i().getInventoryImage().enderChest().inventoryTitle()));
                    if (reserializer) {
                        replaceText = MessageUtil.reserializeToDiscord(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(replaceText));
                    }

                    AtomicBoolean replaced = new AtomicBoolean(false);
                    Component replaceComponent = LegacyComponentSerializer.legacySection().deserialize(replaceText);
                    component = ComponentReplacing.replace(component, MultiChatDiscordSrvAddon.enderChestPlaceholder.stream().map(k -> k.getKeyword().pattern()).collect(Collectors.toList()), true, (groups) -> {
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

        DiscordSRV srv = MultiChatDiscordSrvAddon.discordsrv;
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
                for (Entry<String, UUID> entry : names.entrySet()) {
                    String name = entry.getKey();
                    UUID uuid = entry.getValue();
                    String userId = srv.getAccountLinkManager().getDiscordId(uuid);
                    if (userId != null) {
                        User user = srv.getJda().getUserById(userId);
                        if (user != null) {
                            String discordMention = user.getAsMention();
                            component = ComponentReplacing.replace(component, CustomStringUtils.escapeMetaCharacters('@' + name), true, PlainTextComponentSerializer.plainText().deserialize(discordMention));
                        }
                    }
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

    //=====Death Message

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!Config.i().getDeathMessage().showItems()) {
            return;
        }
        Debug.debug("Triggered onDeath");
        Player player = event.getEntity();
        Component deathMessage = NMS.getInstance().getDeathMessage(player);
        DEATH_MESSAGE.put(player.getUniqueId(), deathMessage);
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onDeathMessageSendPre(DeathMessagePreProcessEvent event) {
        Debug.debug("Triggered onDeathMessageSendPre");
        if (event.isCancelled()) {
            return;
        }
        if (!Config.i().getDeathMessage().translateDeathmessage()) {
            return;
        }
        Component deathMessage = DEATH_MESSAGE.get(event.getPlayer().getUniqueId());
        if (deathMessage == null) {
            return;
        }
        event.setDeathMessage(PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(deathMessage, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()))));
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onDeathMessageSendPost(DeathMessagePostProcessEvent event) {
        Debug.debug("Triggered onDeathMessageSendPost");
        Component deathMessage = DEATH_MESSAGE.remove(event.getPlayer().getUniqueId());
        if (deathMessage == null) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (!Config.i().getDeathMessage().showItems()) {
            return;
        }
        ItemStack item = ComponentStringUtils.extractItemStack(deathMessage);
        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || meta.getDisplayName().isEmpty()) {
            return;
        }
        Color color = null;
        if (!event.getDiscordMessage().getEmbeds().isEmpty()) {
            color = event.getDiscordMessage().getEmbeds().get(0).getColor();
        }
        if (color == null) {
            color = Color.black;
        }
        Player player = event.getPlayer();

        DiscordMessageContent content = new DiscordMessageContent(Config.i().getDeathMessage().title(), null, color);
        try {
            BufferedImage image = ImageGeneration.getItemStackImage(item, player, Config.i().getInventoryImage().item().alternateAirTexture());
            byte[] itemData = ImageUtils.toArray(image);
            content.setTitle(DiscordItemStackUtils.getItemNameForDiscord(item, null, Config.i().getResources().language()));
            content.setThumbnail("attachment://Item.png");
            content.addAttachment("Item.png", itemData);

            DiscordToolTip discordToolTip = DiscordItemStackUtils.getToolTip(item, player, Config.i().getToolTipSettings().showAdvanceDetails());
            if (!discordToolTip.isHideTooltip() &&(!discordToolTip.isBaseItem() || Config.i().getInventoryImage().item().useTooltipImageOnBaseItem())) {
                BufferedImage tooltip = ImageGeneration.getToolTipImage(discordToolTip.getComponents(), NMS.getInstance().getCustomTooltipResourceLocation(item));
                byte[] tooltipData = ImageUtils.toArray(tooltip);
                content.addAttachment("ToolTip.png", tooltipData);
                content.addImageUrl("attachment://ToolTip.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiLib.getAsyncScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> {
            Debug.debug("onDeathMessageSend sending item to discord");
            TextChannel destinationChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(event.getChannel());
            if (event.isUsingWebhooks()) {
                ValuePairs<List<MessageEmbed>, Set<String>> pair = content.toJDAMessageEmbeds();
                Map<String, InputStream> attachments = new LinkedHashMap<>();
                for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                    if (pair.getSecond().contains(attachment.getKey())) {
                        attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                    }
                }

                WebhookUtil.deliverMessage(destinationChannel, event.getWebhookName(), event.getWebhookAvatarUrl(), null, pair.getFirst(), attachments, null);
            } else {
                content.toJDAMessageRestAction(destinationChannel).queue();
            }
        }, 5);
    }

    //===== Advancement

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onAdvancement(AchievementMessagePreProcessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Debug.debug("Triggered onAdvancement");
        MessageFormat messageFormat = event.getMessageFormat();
        if (messageFormat == null) {
            return;
        }

        Debug.debug("onAdvancement getting achievement");
        Event bukkitEvent = event.getTriggeringBukkitEvent();
        Object advancement = NMS.getInstance().getBukkitAdvancementFromEvent(bukkitEvent);
        AdvancementData data = NMS.getInstance().getAdvancementDataFromBukkitAdvancement(advancement);

        SpecificTranslateFunction translateFunction = MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language());

        String title = MultiChatComponentSerializer.legacySection().serialize(ComponentStringUtils.resolve(data.getTitle(), translateFunction));
        String description = MultiChatComponentSerializer.legacySection().serialize(ComponentStringUtils.resolve(data.getDescription(), translateFunction));
        ItemStack item = data.getItem();
        AdvancementType advancementType = data.getAdvancementType();
        boolean isMinecraft = data.isMinecraft();

        Debug.debug("onAdvancement processing advancement");
        if (Config.i().getAdvancements().changeToItemIcon() && item != null && advancementType != null) {
            String content = messageFormat.getContent();
            if (content == null) {
                content = "";
            }
            try {
                int id = DATA_ID_PROVIDER.getNext();
                BufferedImage thumbnail = ImageGeneration.getAdvancementIcon(item, advancementType, true, event.getPlayer());
                byte[] thumbnailData = ImageUtils.toArray(thumbnail);
                content += "<ICA=" + id + ">";
                messageFormat.setContent(content);
                RESEND_WITH_ATTACHMENT.put(id, new AttachmentData("Thumbnail.png", thumbnailData));
                messageFormat.setThumbnailUrl("attachment://Thumbnail.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Config.i().getAdvancements().correctAdvancementName() && title != null) {
            event.setAchievementName(ChatColorUtils.stripColor(title));
            messageFormat.setAuthorName(ComponentStringUtils.convertFormattedString(LanguageUtils.getTranslation(advancementType.getTranslationKey(), Config.i().getResources().language()).getResult(), event.getPlayer().getName(), ChatColorUtils.stripColor(title)));
            Color color;
            if (isMinecraft) {
                color = ColorUtils.getColor(advancementType.getColor());
            } else {
                String colorStr = ChatColorUtils.getFirstColors(title);
                color = ColorUtils.getColor(colorStr == null || colorStr.isEmpty() ? advancementType.getColor() : ColorUtils.toChatColor(colorStr));
            }
            if (color.equals(Color.white)) {
                color = DiscordContentUtils.OFFSET_WHITE;
            }
            messageFormat.setColorRaw(color.getRGB());
        }
        if (Config.i().getAdvancements().showDescription() && description != null) {
            messageFormat.setDescription(ChatColorUtils.stripColor(description));
        }
        event.setMessageFormat(messageFormat);
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onAdvancementSend(AchievementMessagePostProcessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Debug.debug("Triggered onAdvancementSend");
        Message message = event.getDiscordMessage();
        if (!message.getContentRaw().contains("<ICA=")) {
            return;
        }
        String text = message.getContentRaw();

        IntSet matches = new IntLinkedOpenHashSet();
        synchronized (RESEND_WITH_ATTACHMENT) {
            for (int key : RESEND_WITH_ATTACHMENT.keySet()) {
                if (text.contains("<ICA=" + key + ">")) {
                    matches.add(key);
                }
            }
        }
        event.setCancelled(true);
        DiscordMessageContent content = new DiscordMessageContent(message);

        for (IntIterator itr = matches.iterator(); itr.hasNext();) {
            int key = itr.nextInt();
            AttachmentData data = RESEND_WITH_ATTACHMENT.remove(key);
            if (data != null) {
                content.addAttachment(data.getName(), data.getData());
            }
        }

        TextChannel destinationChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(event.getChannel());
        Debug.debug("onAdvancementSend sending message to discord");
        if (event.isUsingWebhooks()) {
            ValuePairs<List<MessageEmbed>, Set<String>> pair = content.toJDAMessageEmbeds();
            Map<String, InputStream> attachments = new LinkedHashMap<>();
            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                if (pair.getSecond().contains(attachment.getKey())) {
                    attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                }
            }

            WebhookUtil.deliverMessage(destinationChannel, event.getWebhookName(), event.getWebhookAvatarUrl(), null, pair.getFirst(), attachments, null);
        } else {
            content.toJDAMessageRestAction(destinationChannel).queue();
        }
    }

    //=====

    private static void handleSelfBotMessage(Message message, String textOriginal, TextChannel channel) {
        String text = textOriginal;

        if (!text.contains("<ICD=")) {
            return;
        }

        IntSet matches = new IntLinkedOpenHashSet();

        synchronized (DATA) {
            for (int key : DATA.keySet()) {
                Matcher matcher = OutboundToDiscordEvents.DATA_PATTERN.apply(key).matcher(text);
                if (matcher.find()) {
                    text = matcher.replaceAll("");
                    matches.add(key);
                }
            }
        }

        if (matches.isEmpty()) {
            Debug.debug("discordMessageSent keys empty");
            return;
        }

        message.editMessage(text + " ...").queue();
        OfflinePlayer player = DATA.get(matches.iterator().nextInt()).getPlayer();

        List<DiscordDisplayData> dataList = new ArrayList<>();

        for (IntIterator itr = matches.iterator(); itr.hasNext();) {
            int key = itr.nextInt();
            DiscordDisplayData data = DATA.remove(key);
            if (data != null) {
                dataList.add(data);
            }
        }

        dataList.sort(DISPLAY_DATA_COMPARATOR);

        Debug.debug("discordMessageSent creating contents");
        ValuePairs<List<DiscordMessageContent>, InteractionHandler> pair = DiscordContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        InteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel, textOriginal, text, contents, false, true);
        Bukkit.getPluginManager().callEvent(discordImageEvent);
        Debug.debug("discordMessageSent sending to discord, Cancelled: " + discordImageEvent.isCancelled());
        if (discordImageEvent.isCancelled()) {
            message.editMessage(discordImageEvent.getOriginalMessage()).queue();
        } else {
            text = discordImageEvent.getNewMessage();
            MessageAction action = message.editMessage(text);
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
            action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(m -> {
                if (Config.i().getSettings().embedDeleteAfter() > 0) {
                    m.editMessageEmbeds().setActionRows().retainFiles(Collections.emptyList()).queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                }
            });
            if (!interactionHandler.getInteractions().isEmpty()) {
                DiscordInteractionEvents.register(message, interactionHandler, contents);
            }
        }
    }

    private static void handleWebhook(long messageId, Message message, String textOriginal, TextChannel channel) {
        String text = textOriginal;
        if (!text.contains("<ICD=")) {
            return;
        }

        IntSet matches = new IntLinkedOpenHashSet();

        synchronized (DATA) {
            for (int key : DATA.keySet()) {
                Matcher matcher = OutboundToDiscordEvents.DATA_PATTERN.apply(key).matcher(text);
                if (matcher.find()) {
                    text = matcher.replaceAll("");
                    matches.add(key);
                }
            }
        }

        if (matches.isEmpty()) {
            Debug.debug("onMessageReceived keys empty");
            return;
        }

        String webHookUrl = WebhookUtil.getWebhookUrlToUseForChannel(channel);
        WebhookUtil.editMessage(channel, String.valueOf(messageId), text + " ...", (Collection<? extends MessageEmbed>) null);

        OfflinePlayer player = DATA.get(matches.iterator().nextInt()).getPlayer();

        List<DiscordDisplayData> dataList = new ArrayList<>();

        for (IntIterator itr = matches.iterator(); itr.hasNext();) {
            int key = itr.nextInt();
            DiscordDisplayData data = DATA.remove(key);
            if (data != null) {
                dataList.add(data);
            }
        }

        dataList.sort(DISPLAY_DATA_COMPARATOR);

        Debug.debug("onMessageReceived creating contents");
        ValuePairs<List<DiscordMessageContent>, InteractionHandler> pair = DiscordContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        InteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel, textOriginal, text, contents, false, true);
        Bukkit.getPluginManager().callEvent(discordImageEvent);

        Debug.debug("onMessageReceived sending to discord, Cancelled: " + discordImageEvent.isCancelled());
        if (discordImageEvent.isCancelled()) {
            WebhookUtil.editMessage(channel, String.valueOf(messageId), discordImageEvent.getOriginalMessage(), (Collection<? extends MessageEmbed>) null);
        } else {
            text = discordImageEvent.getNewMessage();
            List<MessageEmbed> embeds = new ArrayList<>();
            Map<String, InputStream> attachments = new LinkedHashMap<>();
            int i = 0;
            for (DiscordMessageContent content : contents) {
                i += content.getAttachments().size();
                if (i <= 10) {
                    ValuePairs<List<MessageEmbed>, Set<String>> valuePair = content.toJDAMessageEmbeds();
                    embeds.addAll(valuePair.getFirst());
                    for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                        if (valuePair.getSecond().contains(attachment.getKey())) {
                            attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                        }
                    }
                }
            }
            WebhookUtil.editMessage(channel, String.valueOf(messageId), text, embeds, attachments, interactionHandler.getInteractionToRegister());
            if (!interactionHandler.getInteractions().isEmpty()) {
                DiscordInteractionEvents.register(message, interactionHandler, contents);
            }
            if (Config.i().getSettings().embedDeleteAfter() > 0) {
                String finalText = text;
                MultiLib.getAsyncScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> {
                    WebhookUtil.editMessage(channel, String.valueOf(messageId), finalText, Collections.emptyList(), Collections.emptyMap(), Collections.emptyList());
                }, Config.i().getSettings().embedDeleteAfter() * 20L);
            }
        }
    }

    public static class JDAEvents extends ListenerAdapter {

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            try {
                Debug.debug("Triggered onMessageReceived");
                if (!event.getChannelType().equals(ChannelType.TEXT)) {
                    return;
                }
                if (!event.isWebhookMessage() && !event.getAuthor().equals(event.getJDA().getSelfUser())) {
                    return;
                }
                long messageId = event.getMessageIdLong();
                Message message = event.getMessage();
                TextChannel channel = event.getTextChannel();
                String textOriginal = message.getContentRaw();
                boolean isWebhookMessage = event.isWebhookMessage();

                if (!MultiChatDiscordSrvAddon.plugin.isEnabled()) {
                    return;
                }
                MultiLib.getAsyncScheduler().runNow(MultiChatDiscordSrvAddon.plugin, (task) -> {
                    if (isWebhookMessage) {
                        handleWebhook(messageId, message, textOriginal, channel);
                    } else {
                        handleSelfBotMessage(message, textOriginal, channel);
                    }
                });
            } catch (IllegalStateException e) {
                if (e.getMessage().trim().equalsIgnoreCase("zip file closed")) {
                    throw new RuntimeException("MultiChatDiscordSrvAddon didn't start properly due to an earlier error during startup, please look for that if you are asking for support. Remember to check the pinned messages first when you do so.", e);
                } else {
                    throw e;
                }
            }
        }

    }

}
