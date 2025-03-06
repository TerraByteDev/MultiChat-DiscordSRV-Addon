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

package com.loohp.multichatdiscordsrvaddon.standalone.utils;

import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordMessageContent;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

@Getter
public class StandaloneInteractionHandler {

    private final Collection<? extends ActionRow> interactionToRegister;
    private final List<String> interactions;
    private final long expire;
    private final BiConsumer<GenericComponentInteractionCreateEvent, List<DiscordMessageContent>> reactionConsumer;

    public StandaloneInteractionHandler(Collection<? extends ActionRow> interactionToRegister, List<String> interactions, long expire, BiConsumer<GenericComponentInteractionCreateEvent, List<DiscordMessageContent>> reactionConsumer) {
        this.interactionToRegister = interactionToRegister;
        this.interactions = interactions;
        this.expire = expire;
        this.reactionConsumer = reactionConsumer;
    }

}
