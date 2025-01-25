/*
 * This file is part of InteractiveChat.
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

import com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderParser {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");
    private static final Map<UUID, Map<String, String>> remotePlaceholders = new HashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractiveChatDiscordSrvAddon.plugin, () -> {
            if (InteractiveChatDiscordSrvAddon.plugin.useBungeecord) {
                if (InteractiveChatDiscordSrvAddon.plugin.useTooltipOnTab) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        parse(Bukkit.getOfflinePlayer(player.getUniqueId()), InteractiveChatDiscordSrvAddon.plugin.tabTooltip);
                    }
                }
            }
        }, 100, 100);
    }

    public static String parse(OfflinePlayer offlineICPlayer, String str) {
        if (InteractiveChatDiscordSrvAddon.plugin.parsePAPIOnMainThread && !Bukkit.isPrimaryThread()) {
            try {
                CompletableFuture<String> future = new CompletableFuture<>();
                Bukkit.getScheduler().runTask(InteractiveChatDiscordSrvAddon.plugin, () -> future.complete(parse0(offlineICPlayer, str)));
                return future.get(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return "";
            } catch (TimeoutException e) {
                Player player = offlineICPlayer.getPlayer();
                if (player == null) {
                    return PlaceholderAPI.setPlaceholders(offlineICPlayer, str);
                } else {
                    if (PlayerUtils.isLocal(player)) {
                        return PlaceholderAPI.setPlaceholders(player, str);
                    } else {
                        return "";
                    }
                }
            }
        } else {
            return parse0(offlineICPlayer, str);
        }
    }

    private static String parse0(OfflinePlayer offlineICPlayer, String str) {
        Player player = offlineICPlayer.getPlayer();
        if (player == null) {
            return PlaceholderAPI.setPlaceholders(offlineICPlayer.getPlayer(), str);
        } else {
            if (PlayerUtils.isLocal(player)) {
                if (InteractiveChatDiscordSrvAddon.plugin.useBungeecord) {
                    List<ValuePairs<String, String>> pairs = new ArrayList<>();
                    for (Entry<String, String> entry : getAllPlaceholdersContained(player, str).entrySet()) {
                        pairs.add(new ValuePairs<>(entry.getKey(), entry.getValue()));
                    }
                    try {
                        BungeeMessageSender.forwardPlaceholders(System.currentTimeMillis(), player.getUniqueId(), pairs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return PlaceholderAPI.setPlaceholders(player, str);
            } else {
                Map<String, String> remotePlaceholderMappings = remotePlaceholders.get(player.getUniqueId());
                if (remotePlaceholderMappings != null) {
                    for (Entry<String, String> entry : remotePlaceholderMappings.entrySet()) {
                        str = str.replace(entry.getKey(), entry.getValue());

                        // todo - https://github.com/LOOHP/InteractiveChat/blob/7b13007fa153094f1757606bc19486e1441bc00e/common/src/main/java/com/loohp/interactivechat/bungeemessaging/BungeeMessageListener.java#L244
                    }
                }
                return str;
            }
        }
    }

    public static Map<String, String> getAllPlaceholdersContained(Player player, String str) {
        Map<String, String> matchingPlaceholders = new HashMap<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(str);
        while (matcher.find()) {
            String matching = matcher.group();
            matchingPlaceholders.put(matching, PlaceholderAPI.setPlaceholders(player, matching));
        }
        return matchingPlaceholders;
    }

}