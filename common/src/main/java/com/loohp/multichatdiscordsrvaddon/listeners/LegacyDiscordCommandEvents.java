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

import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import com.loohp.multichatdiscordsrvaddon.utils.ChatColorUtils;
import com.loohp.multichatdiscordsrvaddon.utils.ComponentStyling;
import com.loohp.multichatdiscordsrvaddon.utils.PlaceholderParser;
import com.loohp.multichatdiscordsrvaddon.utils.PlayerUtils;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordChatChannelListCommandMessageEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LegacyDiscordCommandEvents {

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onListPlayers(DiscordChatChannelListCommandMessageEvent event) {
        if (!MultiChatDiscordSrvAddon.plugin.playerlistCommandEnabled) {
            return;
        }
        DiscordSRV discordsrv = DiscordSRV.getPlugin();
        TextChannel channel = event.getChannel();
        if (MultiChatDiscordSrvAddon.plugin.playerlistCommandIsMainServer) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (MultiChatDiscordSrvAddon.plugin.respondToCommandsInInvalidChannels) {
                    event.setResult(DiscordChatChannelListCommandMessageEvent.Result.TREAT_AS_REGULAR_MESSAGE);
                }
                return;
            }
            if (MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter > 0) {
                event.setExpiration(MultiChatDiscordSrvAddon.plugin.playerlistCommandDeleteAfter * 1000);
            } else {
                event.setExpiration(0);
            }
            Map<OfflinePlayer, Integer> players;
            if (MultiChatDiscordSrvAddon.plugin.useBungeecord && MultiChatDiscordSrvAddon.plugin.playerlistCommandBungeecord && !Bukkit.getOnlinePlayers().isEmpty()) {
                try {
                    List<ValueTrios<UUID, String, Integer>> bungeePlayers = MultiChatDiscordSrvAddonAPI.getBungeecordPlayerList().get();
                    players = new LinkedHashMap<>(bungeePlayers.size());
                    for (ValueTrios<UUID, String, Integer> playerinfo : bungeePlayers) {
                        UUID uuid = playerinfo.getFirst();
                        Player icPlayer = Bukkit.getPlayer(uuid);
                        if (icPlayer == null || !PlayerUtils.isVanished(icPlayer)) {
                            players.put(Bukkit.getOfflinePlayer(uuid), playerinfo.getThird());
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    event.setPlayerListMessage(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (-1)");
                    return;
                }
            } else {
                players = Bukkit.getOnlinePlayers().stream().filter(each -> {
                    Player icPlayer = Bukkit.getPlayer(each.getUniqueId());
                    return icPlayer == null || !PlayerUtils.isVanished(icPlayer);
                }).collect(Collectors.toMap(each -> each, each -> each.getPing(), (a, b) -> a));
            }
            if (players.isEmpty()) {
                event.setPlayerListMessage(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandEmptyServer));
            } else {
                int errorCode = -2;
                try {
                    List<ValueTrios<OfflinePlayer, Component, Integer>> player = new ArrayList<>();
                    Map<UUID, ValuePairs<List<String>, String>> playerInfo = new HashMap<>();
                    for (Map.Entry<OfflinePlayer, Integer> entry : players.entrySet()) {
                        OfflinePlayer offlinePlayer = entry.getKey();
                        playerInfo.put(offlinePlayer.getUniqueId(), new ValuePairs<>(DiscordCommands.getPlayerGroups(offlinePlayer), offlinePlayer.getName()));
                        String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandPlayerFormat));
                        Component nameComponent;
                        if (MultiChatDiscordSrvAddon.plugin.playerlistCommandParsePlayerNamesWithMiniMessage) {
                            nameComponent = MiniMessage.miniMessage().deserialize(name);
                        } else {
                            nameComponent = LegacyComponentSerializer.legacySection().deserialize(name);
                        }
                        player.add(new ValueTrios<>(offlinePlayer, nameComponent, entry.getValue()));
                    }
                    errorCode--;
                    DiscordCommands.sortPlayers(MultiChatDiscordSrvAddon.plugin.playerlistOrderingTypes, player, playerInfo);
                    errorCode--;
                    OfflinePlayer firstPlayer = Bukkit.getOfflinePlayer(players.keySet().iterator().next().getUniqueId());
                    List<Component> header = new ArrayList<>();
                    if (!MultiChatDiscordSrvAddon.plugin.playerlistCommandHeader.isEmpty()) {
                        header = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandHeader.replace("{OnlinePlayers}", players.size() + "")))));
                    }
                    errorCode--;
                    List<Component> footer = new ArrayList<>();
                    if (!MultiChatDiscordSrvAddon.plugin.playerlistCommandFooter.isEmpty()) {
                        footer = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, MultiChatDiscordSrvAddon.plugin.playerlistCommandFooter.replace("{OnlinePlayers}", players.size() + "")))));
                    }
                    errorCode--;
                    int playerListMaxPlayers = MultiChatDiscordSrvAddon.plugin.playerlistMaxPlayers;
                    if (playerListMaxPlayers < 1) {
                        playerListMaxPlayers = Integer.MAX_VALUE;
                    }
                    BufferedImage image = ImageGeneration.getTabListImage(header, footer, player, MultiChatDiscordSrvAddon.plugin.playerlistCommandAvatar, MultiChatDiscordSrvAddon.plugin.playerlistCommandPing, playerListMaxPlayers);
                    errorCode--;
                    byte[] data = ImageUtils.toArray(image);
                    errorCode--;
                    channel.sendMessageEmbeds(new EmbedBuilder().setImage("attachment://Tablist.png").setColor(MultiChatDiscordSrvAddon.plugin.playerlistCommandColor).build()).addFile(data, "Tablist.png").queue(success -> {
                        if (event.getExpiration() > 0) {
                            success.delete().queueAfter(event.getExpiration(), TimeUnit.MILLISECONDS);
                        }
                    });
                    event.setResult(DiscordChatChannelListCommandMessageEvent.Result.NO_ACTION);
                } catch (Exception e) {
                    e.printStackTrace();
                    event.setPlayerListMessage(ChatColorUtils.stripColor(MultiChatDiscordSrvAddon.plugin.unableToRetrieveData) + " (" + errorCode + ")");
                    return;
                }
            }
            if (event.getExpiration() > 0 && DiscordSRV.config().getBoolean("DiscordChatChannelListCommandExpirationDeleteRequest")) {
                event.getTriggeringJDAEvent().getMessage().delete().queueAfter(event.getExpiration(), TimeUnit.MILLISECONDS);
            }
        }
    }

}
