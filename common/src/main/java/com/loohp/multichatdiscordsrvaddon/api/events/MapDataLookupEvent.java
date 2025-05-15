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

import com.loohp.multichatdiscordsrvaddon.utils.FoliaUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class MapDataLookupEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Player player;
    private final int mapId;
    private final MapView mapView;
    @Setter
    private byte[] colors;
    @Setter
    private List<MapCursor> mapCursors;

    public MapDataLookupEvent(Player player, int mapId, MapView mapView, byte[] colors, List<MapCursor> mapCursors) {
        super(!FoliaUtils.isMainThread());
        this.player = player;
        this.mapId = mapId;
        this.mapView = mapView;
        this.colors = colors;
        this.mapCursors = mapCursors;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public boolean hasMapView() {
        return mapView != null;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}
