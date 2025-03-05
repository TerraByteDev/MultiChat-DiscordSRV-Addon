package com.loohp.multichatdiscordsrvaddon.standalone;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
}
