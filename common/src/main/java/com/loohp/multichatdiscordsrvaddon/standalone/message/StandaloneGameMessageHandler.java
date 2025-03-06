package com.loohp.multichatdiscordsrvaddon.standalone.message;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.event.InternalServerChatEvent;
import com.loohp.multichatdiscordsrvaddon.standalone.StandaloneManager;
import com.loohp.multichatdiscordsrvaddon.utils.ComponentProcessingUtils;
import com.loohp.multichatdiscordsrvaddon.utils.SkinUtils;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import lombok.Setter;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;

@Setter
public class StandaloneGameMessageHandler {

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
        Component processedComponent = ComponentProcessingUtils.processGameMessage(
                event.getEmitter(),
                Component.text(event.getFormatted()),
                Component.text(event.getPlainText())
        );

        if (processedComponent != null) return;
        MessageCreateData data = MessageCreateData.fromContent(Config.i().getStandalone().formatting().plainTextFormat()
                .replace("%username%", formattedUsername)
                .replace("%message%", DiscordSerializer.INSTANCE.serialize(processedComponent)));

        standaloneManager.getTextChannel().sendMessage(data).queue();
    }

    private static void handleWebhook(InternalServerChatEvent event) {
        String formattedUsername = standaloneManager.getFormattedUsername(event.getEmitter());
        Component processedComponent = ComponentProcessingUtils.processGameMessage(
                event.getEmitter(),
                Component.text(event.getFormatted()),
                Component.text(event.getPlainText())
        );

        StandaloneWebhookManager.webhook
                .sendMessage(DiscordSerializer.INSTANCE.serialize(processedComponent))
                .setAvatarUrl(SkinUtils.getFormattedSkinURL(event.getEmitter()))
                .setUsername(formattedUsername)
                .queue();
    }
}
