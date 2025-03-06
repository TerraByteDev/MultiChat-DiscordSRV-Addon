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
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVInteractionHandler;
import com.loohp.multichatdiscordsrvaddon.utils.ChatColorUtils;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordMessageContent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SelectionMenuEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.loohp.multichatdiscordsrvaddon.utils.DiscordInteractionUtils.INTERACTION_ID_PREFIX;

public class DiscordInteractionEvents extends ListenerAdapter {

    public static final Map<String, InteractionData> REGISTER = new ConcurrentHashMap<>();

    public static void register(Message message, DiscordSRVInteractionHandler interactionHandler, List<DiscordMessageContent> discordMessageContent) {
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

    public static InteractionData getInteractionData(String interactionId) {
        return REGISTER.get(interactionId);
    }

    public static void unregisterAll() {
        REGISTER.clear();
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        handleInteraction(event);
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        handleInteraction(event);
    }

    private void handleInteraction(GenericComponentInteractionCreateEvent event) {
        String id = event.getComponent().getId();
        if (!id.startsWith(INTERACTION_ID_PREFIX)) {
            return;
        }
        InteractionData data = REGISTER.get(id);
        if (data != null) {
            data.getInteractionHandler().getReactionConsumer().accept(event, data.getContents());
            return;
        }
        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().interactionExpired())).setEphemeral(true).queue();
    }

    @Getter
    public static class InteractionData {

        private final DiscordSRVInteractionHandler interactionHandler;
        private final List<DiscordMessageContent> contents;
        private final List<String> interactionIds;
        private final List<String> messageIds;

        public InteractionData(DiscordSRVInteractionHandler interactionHandler, List<DiscordMessageContent> contents, List<String> interactionIds, List<String> messageIds) {
            this.interactionHandler = interactionHandler;
            this.contents = contents;
            this.interactionIds = interactionIds;
            this.messageIds = messageIds;
        }

        public InteractionData(DiscordSRVInteractionHandler interactionHandler, List<DiscordMessageContent> contents, List<String> interactionIds, String messageId) {
            this.interactionHandler = interactionHandler;
            this.contents = contents;
            this.interactionIds = interactionIds;
            List<String> messageIds = new ArrayList<>();
            messageIds.add(messageId);
            this.messageIds = messageIds;
        }

    }

}
