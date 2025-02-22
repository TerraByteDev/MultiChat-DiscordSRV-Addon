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

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.RemoteMCPlayer;
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

    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            if (Config.i().getSettings().bungeecord()) {
                if (Config.i().getTabCompletion().playerNameTooltip().enabled()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        parse(Bukkit.getOfflinePlayer(player.getUniqueId()), Config.i().getTabCompletion().playerNameTooltip().toolTip());
                    }
                }
            }
        }, 100, 100);
    }

    public static String parse(OfflinePlayer offlineICPlayer, String str) {
        if (Config.i().getSettings().parsePlaceholdersOnMainThread() && !Bukkit.isPrimaryThread()) {
            try {
                CompletableFuture<String> future = new CompletableFuture<>();
                Bukkit.getScheduler().runTask(MultiChatDiscordSrvAddon.plugin, () -> future.complete(parse0(offlineICPlayer, str)));
                return future.get(1500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return "";
            } catch (TimeoutException e) {
                Player player = offlineICPlayer.getPlayer();

                if (!offlineICPlayer.isOnline()) return PlaceholderAPI.setPlaceholders(offlineICPlayer, str);
                    else if (PlayerUtils.isLocal(player)) return PlaceholderAPI.setPlaceholders(player, str);
                    else return "";
            }
        } else {
            return parse0(offlineICPlayer, str);
        }
    }

    private static String parse0(OfflinePlayer offlineICPlayer, String str) {
        Player player = offlineICPlayer.getPlayer();
        if (player == null) {
            return PlaceholderAPI.setPlaceholders(offlineICPlayer, str);
        } else {
            if (PlayerUtils.isLocal(player)) {
                if (Config.i().getSettings().bungeecord()) {
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
                RemoteMCPlayer mcPlayer = PlayerUtils.getMCPlayer(player.getUniqueId());

                Map<String, String> remotePlaceholderMappings = mcPlayer.getRemotePlaceholders();
                if (remotePlaceholderMappings != null) {
                    for (Entry<String, String> entry : remotePlaceholderMappings.entrySet()) {
                        str = str.replace(entry.getKey(), entry.getValue());
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