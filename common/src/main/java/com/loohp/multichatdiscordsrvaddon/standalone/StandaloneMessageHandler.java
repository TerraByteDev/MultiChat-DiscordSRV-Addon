package com.loohp.multichatdiscordsrvaddon.standalone;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.event.InternalServerChatEvent;
import com.loohp.multichatdiscordsrvaddon.listeners.discordsrv.OutboundToDiscordEvents;
import lombok.Setter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;

@Setter
public class StandaloneMessageHandler {

    private static StandaloneManager standaloneManager;

    static {
        standaloneManager = MultiChatDiscordSrvAddon.plugin.standaloneManager;
    }

    public static void handleChat(InternalServerChatEvent event) {
        Debug.debug("Triggering Standalone handleChat");

        if (Config.i().getStandalone().formatting().useWebhooks()) {
            handleWebhook(event);
        } else {
            handlePlainText(event);
        }
    }

    private static void handlePlainText(InternalServerChatEvent event) {
        String formattedUsername = standaloneManager.getFormattedUsername(event.getEmitter());

        Component processedComponent = OutboundToDiscordEvents.processGameMessage(
                event.getEmitter(),
                Component.text(event.getFormatted()),
                Component.text(event.getPlainText())
        );

        MessageCreateData.fromContent(Config.i().getStandalone().formatting().plainTextFormat()
                .replace("%username%", formattedUsername)
                .replace("%message%", event.getFormatted()));
    }

    private static void handleWebhook(InternalServerChatEvent event) {

    }
}
