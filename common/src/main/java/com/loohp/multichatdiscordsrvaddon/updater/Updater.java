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

package com.loohp.multichatdiscordsrvaddon.updater;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import com.loohp.multichatdiscordsrvaddon.utils.GithubBuildInfo;
import com.loohp.multichatdiscordsrvaddon.utils.GithubUtils;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.Locale;

public class Updater implements Listener {

    public static boolean checkUpdate(CommandSender... senders) {
        GithubBuildInfo currentBuild = GithubBuildInfo.CURRENT;
        GithubBuildInfo latestBuild;
        GithubUtils.GitHubStatusLookup lookupStatus;

        try {
            if (currentBuild.isStable()) {
                latestBuild = GithubUtils.lookupLatestRelease();
                lookupStatus = GithubUtils.compare(latestBuild.getId(), currentBuild.getId());
            } else {
                latestBuild = null;
                lookupStatus = GithubUtils.compare(GithubUtils.MAIN_BRANCH, currentBuild.getId());
            }
        } catch (IOException error) {
            ChatUtils.sendMessage("<red>Failed to fetch latest version: " + error, senders);
            return true;
        }

        if (lookupStatus.isBehind()) {
            if (currentBuild.isStable()) {
                ChatUtils.sendMessage("<green>A new version of MultiChat-DiscordSRV-Addon is available: " + latestBuild.getId() + "!", senders);
                ChatUtils.sendMessage("<grey>Download at: <click:open_url:'https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/releases/tag'>https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/releases/tag</click>", senders);
            } else {
                ChatUtils.sendMessage("<yellow>You are running a development build of MultiChat-DiscordSRV-Addon!\nThe latest available development build is " + String.format(Locale.ROOT, "%,d", lookupStatus.getDistance()) + " commits ahead.", senders);
            }

            return false;
        }

        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
            if (Config.i().getSettings().updater()) {
                Player player = event.getPlayer();
                if (player.hasPermission("multichatdiscordsrv.update")) {
                    Updater.checkUpdate(player);
                }
            }
        }, 100);
    }

    public static class UpdaterResponse {

        private final String result;
        private final int spigotPluginId;
        private final boolean devBuildIsLatest;

        public UpdaterResponse(String result, int spigotPluginId, boolean devBuildIsLatest) {
            this.result = result;
            this.spigotPluginId = spigotPluginId;
            this.devBuildIsLatest = devBuildIsLatest;
        }

        public String getResult() {
            return result;
        }

        public int getSpigotPluginId() {
            return spigotPluginId;
        }

        public boolean isDevBuildLatest() {
            return devBuildIsLatest;
        }

    }

}
