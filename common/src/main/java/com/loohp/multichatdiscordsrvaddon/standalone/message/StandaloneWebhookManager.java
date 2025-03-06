package com.loohp.multichatdiscordsrvaddon.standalone.message;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.standalone.StandaloneManager;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.utils.requestbody.BufferedRequestBody;
import okhttp3.*;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class StandaloneWebhookManager {
    @Nullable
    public static Webhook webhook;

    public static void fetchWebhook(StandaloneManager manager) {
        List<Webhook> webhooks;

        try {
            webhooks = manager.getTextChannel().retrieveWebhooks().complete();

            for (Webhook w : webhooks) {
                if (Config.i().getStandalone().formatting().webhookName().equalsIgnoreCase(w.getName()) && w.getOwnerAsUser() == manager.getJda().getSelfUser()) {
                   webhook = w;
                }
            }

            if (webhook == null) webhook = manager.getTextChannel().createWebhook(Config.i().getStandalone().formatting().webhookName()).complete();
        } catch (InsufficientPermissionException exception) {
            ChatUtils.sendMessage("<red>The bot does not have the sufficient permissions to access Webhooks!");
            exception.printStackTrace();
        }
    }

    public static void editMessage(@NotNull TextChannel channel, @NotNull String messageID, @NotNull String newMessage, Collection<? extends MessageEmbed> embeds, Map<String, InputStream> attachments, Collection<? extends ActionRow> interactions) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            if (webhook == null) throw new IllegalStateException("Attmpeted to edit webhook message when webhook has not been fetched!");

            String webhookURL = webhook.getUrl() + "/messages/" + messageID;
            try {
                JSONObject jsonObject = new JSONObject();
                if (StringUtils.isNotBlank(newMessage)) jsonObject.put("content", newMessage);

                if (embeds != null) {
                    JSONArray jsonArray = new JSONArray();
                    for (MessageEmbed embed : embeds) {
                        if (embed != null) jsonArray.add(embed.toData().toMap());
                    }
                    jsonObject.put("embeds", jsonArray);
                }

                if (interactions != null) {
                    JSONArray jsonArray = new JSONArray();
                    for (ActionRow actionRow : interactions) {
                        jsonArray.add(actionRow.toData().toMap());
                    }
                    jsonObject.put("components", jsonArray);
                }

                List<String> attachmentIndex = null;
                if (attachments != null) {
                    attachmentIndex = new ArrayList<>(attachments.size());
                    JSONArray jsonArray = new JSONArray();
                    int i = 0;

                    for (String name : attachments.keySet()) {
                        attachmentIndex.add(name);
                        JSONObject attachmentObject = new JSONObject();
                        attachmentObject.put("id", i);
                        attachmentObject.put("filename", name);

                        jsonArray.add(attachmentIndex);
                        i++;
                    }

                    jsonObject.put("attachments", jsonArray);
                }

                JSONObject allowedMentions = new JSONObject();
                Set<String> parsed = MessageRequest.getDefaultMentions().stream()
                        .filter(Objects::nonNull)
                        .map(Message.MentionType::getParseKey)
                        .collect(Collectors.toSet());
                allowedMentions.put("parse", parsed);
                jsonObject.put("allowed_mentions", allowedMentions);

                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                multipartBuilder.addFormDataPart("payload_json", null, RequestBody.create(MediaType.get("application/json"), jsonObject.toString()));

                if (attachmentIndex != null) {
                    for (int i = 0; i < attachmentIndex.size(); i++) {
                        String name = attachmentIndex.get(i);
                        InputStream data = attachments.get(name);
                        if (data != null) {
                            multipartBuilder.addFormDataPart("files[" + i + "]", name, new BufferedRequestBody(Okio.source(data), null));
                            data.close();
                        }
                    }
                }

                Request.Builder requestBuilder = new Request.Builder()
                        .url(webhookURL)
                        .header("User-Agent", "MultiChatDiscordSRVAddon@" + MultiChatDiscordSrvAddon.plugin.getDescription().getVersion());
                requestBuilder.patch(multipartBuilder.build());

                OkHttpClient httpClient = MultiChatDiscordSrvAddon.plugin.standaloneManager.getJda().getHttpClient();
                try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
                    int res = response.code();
                    if (res == 404) {
                        ChatUtils.sendMessage("<red>Webhook edit attempt returned 404.");
                        return;
                    }
                }
            } catch (Exception error) {
                throw new RuntimeException("Failed to edit webhook message.", error);
            }
        });
    }


}
