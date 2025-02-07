package com.loohp.multichatdiscordsrvaddon.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.WebhookUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;

public class DSRVUtils {

    public static void sendDynmapMessage(String playerName, String filteredMessage) {
        TextChannel textChannel = DiscordSRV.getPlugin().getMainTextChannel();

        if (textChannel != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            String avatarURL = DiscordSRV.getAvatarUrl(playerName, offlinePlayer.getUniqueId());

            WebhookUtil.deliverMessage(textChannel, playerName, avatarURL, filteredMessage, (Collection<? extends MessageEmbed>) null);
        } else {
            ChatUtils.sendMessage("<yellow>WARNING: Failed to find DisoordSRV Main text channel!");
        }
    }
}
