package com.loohp.multichatdiscordsrvaddon.standalone.message;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.standalone.StandaloneManager;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    public static void editMessage(@NotNull TextChannel channel, @NotNull String messageID, @NotNull String newMessage, Collection<? extends MessageEmbed> embeds) {
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

                JSONObject allowedMentions = new JSONObject();
                Set<String> parsed = MessageRequest.getDefaultMentions().stream()
                        .filter(Objects::nonNull)
                        .map(Message.MentionType::getParseKey)
                        .collect(Collectors.toSet());
                allowedMentions.put("parse", parsed);
                jsonObject.put("allowed_mentions", allowedMentions);

                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                multipartBuilder.addFormDataPart("payload_json", null, RequestBody.create(MediaType.get("application/json"), jsonObject.toString()));

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
