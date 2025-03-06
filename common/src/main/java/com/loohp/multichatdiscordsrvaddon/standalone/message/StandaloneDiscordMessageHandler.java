package com.loohp.multichatdiscordsrvaddon.standalone.message;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.DiscordImageEvent;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordDisplayData;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordMessageContent;
import com.loohp.multichatdiscordsrvaddon.standalone.event.StandaloneInteractionEvents;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneInteractionHandler;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneDiscordContentUtils;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import static com.loohp.multichatdiscordsrvaddon.utils.ComponentProcessingUtils.*;

public class StandaloneDiscordMessageHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            Debug.debug("Triggered onMessageReceived");
            if (!event.getChannelType().equals(ChannelType.TEXT)) return;
            if (!event.isWebhookMessage() && !event.getAuthor().equals(event.getJDA().getSelfUser())) return;

            long messageID = event.getMessageIdLong();
            Message message = event.getMessage();
            TextChannel textChannel = event.getChannel().asTextChannel();
            String textOriginal = message.getContentRaw();
            boolean isWebhookMessage = event.isWebhookMessage();

            if (!MultiChatDiscordSrvAddon.plugin.isEnabled()) return;

            Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                if (isWebhookMessage) {
                    handleWebhook(messageID, message, textOriginal, textChannel);
                } else {
                    handleSelfBotMessage(message, textOriginal, textChannel);
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

    private void handleWebhook(long messageId, Message message, String textOriginal, TextChannel channel) {
        String text = textOriginal;
        if (!text.contains("<ICD=")) return;

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
            Debug.debug("on(Standalone)MessageReceived keys empty");
            return;
        }

        StandaloneWebhookManager.editMessage(channel, String.valueOf(messageId), text + " ...", null, null, null);

        OfflinePlayer player = DATA.get(matches.iterator().nextInt()).getPlayer();

        List<DiscordDisplayData> dataList = new ArrayList<>();
        for (IntIterator iterator = matches.iterator(); iterator.hasNext();) {
            int key = iterator.nextInt();
            DiscordDisplayData data = DATA.remove(key);
            if (data != null) {
                dataList.add(data);
            }
        }

        dataList.sort(DISPLAY_DATA_COMPARATOR);

        Debug.debug("on(Standalone)MessageReceived creating contents");
        ValuePairs<List<DiscordMessageContent>, StandaloneInteractionHandler> pair = StandaloneDiscordContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        StandaloneInteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel.getId(), textOriginal, text, contents, false, true);
        Bukkit.getPluginManager().callEvent(discordImageEvent);

        Debug.debug("on(Standalone)MessageReceived sending to Discord, Cancelled = " + discordImageEvent.isCancelled());
        if (discordImageEvent.isCancelled()) {
            StandaloneWebhookManager.editMessage(channel, String.valueOf(messageId), discordImageEvent.getOriginalMessage(), null, null, null);
        } else {
            text = discordImageEvent.getNewMessage();
            List<MessageEmbed> embeds = new ArrayList<>();
            Map<String, InputStream> attachments = new LinkedHashMap<>();
            int i = 0;
            for (DiscordMessageContent content : contents) {
                i =+ content.getAttachments().size();
                if (i <= 10) {
                    ValuePairs<List<MessageEmbed>, Set<String>> valuePair = StandaloneDiscordMessageContentUtils.toJDAMessageEmbeds(content);
                    embeds.addAll(valuePair.getFirst());

                    for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                        if (valuePair.getSecond().contains(attachment.getKey())) {
                            attachments.put(attachment.getKey(), new ByteArrayInputStream(attachment.getValue()));
                        }
                    }
                }
            }

            StandaloneWebhookManager.editMessage(channel, String.valueOf(messageId), text, embeds, attachments, interactionHandler.getInteractionToRegister());
            if (!interactionHandler.getInteractions().isEmpty()) {
                StandaloneInteractionEvents.register(message, interactionHandler, contents);
            }

            if (Config.i().getSettings().embedDeleteAfter() > 0) {
                String finalText = text;
                Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                    StandaloneWebhookManager.editMessage(channel, String.valueOf(messageId), finalText, Collections.emptyList(), Collections.emptyMap(), null);
                }, Config.i().getSettings().embedDeleteAfter() * 20L);
            }
        }
    }

    private void handleSelfBotMessage(Message message, String textOriginal, TextChannel channel) {
        String text = textOriginal;
        if (!text.contains("<ICD=")) return;

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
            Debug.debug("(Standalone)discordMessagesSent keys empty");
            return;
        }

        message.editMessage(text + " ...").queue();
        OfflinePlayer player = DATA.get(matches.iterator().nextInt()).getPlayer();

        List<DiscordDisplayData> dataList = new ArrayList<>();
        for (IntIterator iterator = matches.iterator(); iterator.hasNext();) {
            int key = iterator.nextInt();
            DiscordDisplayData data = DATA.remove(key);
            if (data != null) {
                dataList.add(data);
            }
        }

        dataList.sort(DISPLAY_DATA_COMPARATOR);

        Debug.debug("discord(Standalone)MessageSent creating contents");
        ValuePairs<List<DiscordMessageContent>, StandaloneInteractionHandler> pair = StandaloneDiscordContentUtils.createContents(dataList, player);
        List<DiscordMessageContent> contents = pair.getFirst();
        StandaloneInteractionHandler interactionHandler = pair.getSecond();

        DiscordImageEvent discordImageEvent = new DiscordImageEvent(channel.getId(), textOriginal, text, contents, false, true);
        Bukkit.getPluginManager().callEvent(discordImageEvent);

        Debug.debug("discord(Standalone)MessageSent sending to Discord, Cancelled = " + discordImageEvent.isCancelled());
        if (discordImageEvent.isCancelled()) {
            message.editMessage(discordImageEvent.getOriginalMessage()).queue();
        } else {
            text = discordImageEvent.getNewMessage();
            MessageEditAction action = message.editMessage(text);
            List<MessageEmbed> embeds = new ArrayList<>();

            int i = 0;

            List<FileUpload> fileUploads = new ArrayList<>();
            for (DiscordMessageContent content : contents) {
                i += content.getAttachments().size();
                if (i <= 10) {
                    ValuePairs<List<MessageEmbed>, Set<String>> valuePair = StandaloneDiscordMessageContentUtils.toJDAMessageEmbeds(content);
                    embeds.addAll(valuePair.getFirst());

                    for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                        if (valuePair.getSecond().contains(attachment.getKey())) {
                            fileUploads.add(FileUpload.fromData(attachment.getValue(), attachment.getKey()));
                        }
                    }
                }
            }
            action.setFiles(fileUploads);

            List<ItemComponent> itemComponents = new ArrayList<>();
            for (ActionRow row : interactionHandler.getInteractionToRegister()) {
                itemComponents.addAll(row.getComponents());
            }

            action.setEmbeds(embeds).setActionRow(itemComponents).queue(m -> {
                if (Config.i().getSettings().embedDeleteAfter() > 0) {
                    message.editMessageEmbeds().setActionRow().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                }
            });

            if (!interactionHandler.getInteractions().isEmpty()) {
                StandaloneInteractionEvents.register(message, interactionHandler, contents);
            }
        }
    }
}
