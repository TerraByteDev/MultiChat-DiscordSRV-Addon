/*
 * This file is part of InteractiveChatDiscordSrvAddon2.
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

package com.loohp.multichatdiscordsrvaddon.api.events;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;

/**
 * This is the base class of all GameMessageProcessEvents
 *
 * @author LOOHP
 */
@Getter
public class GameMessageProcessEvent extends GameMessageEvent {

    private final int processId;
    @Setter
    private String title;

    public GameMessageProcessEvent(OfflinePlayer sender, String title, Component component, boolean cancel, int processId) {
        super(sender, component, cancel);
        this.processId = processId;
        this.title = title;
    }

}
