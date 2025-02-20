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

package com.loohp.multichatdiscordsrvaddon.hooks;

import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Bukkit;

public class ItemsAdderHook {

    public static String getResourcePackURL() {
        Debug.debug("Fetching ItemsAdder resourcepack URL...");
        if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) throw new IllegalStateException("Attempted to fetch ItemsAdder resource pack URL when ItemsAdder is not enabled on the server!");

        String url = ItemsAdder.getPackUrl(false);
        return url == null || url.trim().isEmpty() ? null : url;
    }

}
