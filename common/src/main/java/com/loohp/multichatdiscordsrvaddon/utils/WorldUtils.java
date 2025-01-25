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

package com.loohp.multichatdiscordsrvaddon.utils;

import net.kyori.adventure.key.Key;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.BiomePrecipitation;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldUtils {

    public static Key getNamespacedKey(World world) {
        return NMS.getInstance().getNamespacedKey(world);
    }

    public static boolean isNatural(World world) {
        return world.isNatural();
    }

    public static BiomePrecipitation getPrecipitation(Location location) {
        return NMS.getInstance().getPrecipitation(location);
    }

}

