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

package com.loohp.multichatdiscordsrvaddon.listeners;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.ConcurrentCacheHashMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import com.loohp.multichatdiscordsrvaddon.utils.HTTPRequestUtils;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ICPlayerEvents implements Listener {

    public static final ConcurrentCacheHashMap<UUID, Map<String, Object>> CACHED_PROPERTIES = new ConcurrentCacheHashMap<>(300000);

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MultiChatDiscordSrvAddon.plugin, CACHED_PROPERTIES::cleanUp, 12000, 12000);
    }

    public static final String PROFILE_URL = "https://api.loohpjames.com/spigot/plugins/interactivechatdiscordsrvaddon/profile/%s";

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        populate(event.getPlayer(), true);
    }

    private void populate(Player player, boolean scheduleAsync) {
        if (scheduleAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> populate(player, false));
            return;
        }

        try {
            if (Config.i().getStandalone().enabled()) MultiChatDiscordSrvAddon.plugin.standaloneManager.getDatabase().getLinkedUserByUuid(player.getUniqueId()).get(10, TimeUnit.SECONDS);

            Map<String, Object> cacheProperties = CACHED_PROPERTIES.get(player.getUniqueId());
            if (cacheProperties == null) {
                cacheProperties = new HashMap<>();
                JSONObject json = HTTPRequestUtils.getJSONResponse(PROFILE_URL.replace("%s", player.getName()));
                if (json != null && json.containsKey("properties")) {
                    JSONObject properties = (JSONObject) json.get("properties");
                    for (Object obj : properties.keySet()) {
                        try {
                            String key = (String) obj;
                            String value = (String) properties.get(key);
                            if (value.endsWith(".png")) {
                                BufferedImage image = ImageUtils.downloadImage(value);
                                cacheProperties.put(key, image);
                            } else if (value.endsWith(".bin")) {
                                byte[] data = HTTPRequestUtils.download(value);
                                cacheProperties.put(key, data);
                            } else {
                                cacheProperties.put(key, value);
                            }
                        } catch (Exception ignore) {
                        }
                    }
                }
            }

            CACHED_PROPERTIES.put(player.getUniqueId(), cacheProperties);
        } catch (Exception ignored) {}
    }

}
