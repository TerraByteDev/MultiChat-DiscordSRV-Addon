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

package com.loohp.multichatdiscordsrvaddon.listeners.discordsrv;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVMessageContentUtils;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVContentUtils;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVInteractionHandler;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.DiscordImageEvent;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.resources.languages.SpecificTranslateFunction;
import com.loohp.multichatdiscordsrvaddon.utils.DiscordItemStackUtils.DiscordToolTip;
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
import github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import github.scarsz.discordsrv.objects.MessageFormat;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static com.loohp.multichatdiscordsrvaddon.utils.ChatUtils.toAllow;
import static com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVManager.ventureChatToDiscordPriority;
import static com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVManager.gameToDiscordPriority;
import static com.loohp.multichatdiscordsrvaddon.utils.ComponentProcessingUtils.*;

@SuppressWarnings("deprecation")
public class OutboundToDiscordEvents implements Listener {

    public static final Int2ObjectMap<AttachmentData> RESEND_WITH_ATTACHMENT = Int2ObjectMaps.synchronize(new Int2ObjectLinkedOpenHashMap<>());
    private static final Map<UUID, Component> DEATH_MESSAGE = new ConcurrentHashMap<>();

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onGameToDiscordLowest(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.LOWEST)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onGameToDiscordLow(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.LOW)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onGameToDiscordNormal(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.NORMAL)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onGameToDiscordHigh(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.HIGH)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onGameToDiscordHighest(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.HIGHEST)) {
            handleGameToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onGameToDiscordMonitor(GameChatMessagePreProcessEvent event) {
        if (gameToDiscordPriority.equals(ListenerPriority.MONITOR)) {
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
        if (ventureChatToDiscordPriority.equals(ListenerPriority.LOWEST)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onVentureChatHookToDiscordLow(VentureChatMessagePreProcessEvent event) {
        if (ventureChatToDiscordPriority.equals(ListenerPriority.LOW)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onVentureChatHookToDiscordNormal(VentureChatMessagePreProcessEvent event) {
        if (ventureChatToDiscordPriority.equals(ListenerPriority.NORMAL)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onVentureChatHookToDiscordHigh(VentureChatMessagePreProcessEvent event) {
        if (ventureChatToDiscordPriority.equals(ListenerPriority.HIGH)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onVentureChatHookToDiscordHighest(VentureChatMessagePreProcessEvent event) {
        if (ventureChatToDiscordPriority.equals(ListenerPriority.HIGHEST)) {
            handleVentureChatHookToDiscord(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onVentureChatHookToDiscordMonitor(VentureChatMessagePreProcessEvent event) {
        if (ventureChatToDiscordPriority.equals(ListenerPriority.MONITOR)) {
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

        Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            Debug.debug("onDeathMessageSend sending item to discord");
            TextChannel destinationChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(event.getChannel());
            if (event.isUsingWebhooks()) {
                ValuePairs<List<MessageEmbed>, Set<String>> pair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
                Map<String, InputStream> attachments = new LinkedHashMap<>();
                for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                    if (pair.getSecond().contains(attachment.getKey())) {
                        attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                    }
                }

                WebhookUtil.deliverMessage(destinationChannel, event.getWebhookName(), event.getWebhookAvatarUrl(), null, pair.getFirst(), attachments, null);
            } else {
                DiscordSRVMessageContentUtils.toJDAMessageRestAction(content, destinationChannel).queue();
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
                color = DiscordInteractionUtils.OFFSET_WHITE;
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
        DiscordMessageContent content = DiscordSRVMessageContentUtils.create(message);

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
            ValuePairs<List<MessageEmbed>, Set<String>> pair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
            Map<String, InputStream> attachments = new LinkedHashMap<>();
            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                if (pair.getSecond().contains(attachment.getKey())) {
                    attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                }
            }

            WebhookUtil.deliverMessage(destinationChannel, event.getWebhookName(), event.getWebhookAvatarUrl(), null, pair.getFirst(), attachments, null);
        } else {
            DiscordSRVMessageContentUtils.toJDAMessageRestAction(content, destinationChannel).queue();
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
                Matcher matcher = DATA_PATTERN.apply(key).matcher(text);
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
        ValuePairs<List<DiscordMessageContent>, DiscordSRVInteractionHandler> pair = DiscordSRVContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        DiscordSRVInteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel.getId(), textOriginal, text, contents, false, true);
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
                    ValuePairs<List<MessageEmbed>, Set<String>> valuePair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
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
                Matcher matcher = DATA_PATTERN.apply(key).matcher(text);
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
        ValuePairs<List<DiscordMessageContent>, DiscordSRVInteractionHandler> pair = DiscordSRVContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        DiscordSRVInteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel.getId(), textOriginal, text, contents, false, true);
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
                    ValuePairs<List<MessageEmbed>, Set<String>> valuePair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
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
                Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
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
                Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
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
