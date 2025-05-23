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

package com.loohp.multichatdiscordsrvaddon.updater;

import com.github.puregero.multilib.MultiLib;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import com.loohp.multichatdiscordsrvaddon.utils.GithubBuildInfo;
import com.loohp.multichatdiscordsrvaddon.utils.GithubUtils;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.Locale;

public class Updater implements Listener {

    public static UpdateStatus checkUpdate(CommandSender... senders) {
        GithubBuildInfo currentBuild = GithubBuildInfo.CURRENT;
        GithubBuildInfo latestBuild;
        GithubUtils.GitHubStatusLookup lookupStatus;

        UpdateStatus updateStatus = new UpdateStatus(false, false);

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
            updateStatus.setFailed(true);
            return updateStatus;
        }

        if (lookupStatus.isBehind()) {
            if (currentBuild.isStable()) {
                ChatUtils.sendMessage("<green>A new version of MultiChat-DiscordSRV-Addon is available: " + latestBuild.getId() + "!", senders);
                ChatUtils.sendMessage("<grey>Download at: <click:open_url:'https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/releases/tag'>https://github.com/TerraByteDev/MultiChat-DiscordSRV-Addon/releases/tag</click>", senders);
            } else {
                ChatUtils.sendMessage("<yellow>You are running a development build of MultiChat-DiscordSRV-Addon!\nThe latest available development build is " + String.format(Locale.ROOT, "%,d", lookupStatus.getDistance()) + " commits ahead.", senders);
            }
        }

        updateStatus.setUpToDate(lookupStatus.isBehind());
        return updateStatus;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static final class UpdateStatus {
        private boolean isUpToDate;
        private boolean failed;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        MultiLib.getAsyncScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> {
            if (Config.i().getSettings().updater()) {
                Player player = event.getPlayer();
                if (player.hasPermission("multichatdiscordsrv.checkupdate")) {
                    Updater.checkUpdate(player);
                }
            }
        }, 100);
    }

}
