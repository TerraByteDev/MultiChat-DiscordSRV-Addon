package com.loohp.multichatdiscordsrvaddon.standalone.event;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordMessageContent;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneInteractionHandler;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.loohp.multichatdiscordsrvaddon.utils.DiscordInteractionUtils.INTERACTION_ID_PREFIX;

public class StandaloneInteractionEvents {

    public static final Map<String, InteractionData> REGISTER = new ConcurrentHashMap<>();

    public static void register(Message message, StandaloneInteractionHandler interactionHandler, List<DiscordMessageContent> discordMessageContent) {
        String messageId = message.getChannel().getId() + "/" + message.getId();
        List<String> interactionIds = interactionHandler.getInteractions();
        InteractionData interactionData = new InteractionData(interactionHandler, discordMessageContent, interactionIds, messageId);
        for (String id : interactionIds) {
            if (!id.startsWith(INTERACTION_ID_PREFIX)) {
                throw new IllegalArgumentException("InteractionIds must start with the INTERACTION_ID_PREFIX, however \"" + id + "\" does not");
            }
            REGISTER.put(id, interactionData);
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            for (String id : interactionIds) {
                REGISTER.remove(id);
            }
        }, interactionHandler.getExpire() / 50);
    }

    @Getter
    public static class InteractionData {

        private final StandaloneInteractionHandler interactionHandler;
        private final List<DiscordMessageContent> contents;
        private final List<String> interactionIds;
        private final List<String> messageIds;

        public InteractionData(StandaloneInteractionHandler interactionHandler, List<DiscordMessageContent> contents, List<String> interactionIds, List<String> messageIds) {
            this.interactionHandler = interactionHandler;
            this.contents = contents;
            this.interactionIds = interactionIds;
            this.messageIds = messageIds;
        }

        public InteractionData(StandaloneInteractionHandler interactionHandler, List<DiscordMessageContent> contents, List<String> interactionIds, String messageId) {
            this.interactionHandler = interactionHandler;
            this.contents = contents;
            this.interactionIds = interactionIds;
            List<String> messageIds = new ArrayList<>();
            messageIds.add(messageId);
            this.messageIds = messageIds;
        }

    }
}
