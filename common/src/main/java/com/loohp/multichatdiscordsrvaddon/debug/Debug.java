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

package com.loohp.multichatdiscordsrvaddon.debug;

import com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.utils.ICLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Debug implements Listener {

    public static void debug(String info) {
        if (InteractiveChatDiscordSrvAddon.debug) {
            InteractiveChatDiscordSrvAddon.plugin.audience.sender(Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(ICLogger.DEBUG_PREFIX + " <gray>" + info));
        }
    }

    @EventHandler
    public void onJoinPluginActive(PlayerJoinEvent event) {
        if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEshakE")) {
            event.getPlayer().sendMessage(ChatColor.AQUA + "InteractiveChatDiscordSrvAddon " + InteractiveChatDiscordSrvAddon.plugin.getDescription().getVersion() + " is running!");
        }
    }

}
