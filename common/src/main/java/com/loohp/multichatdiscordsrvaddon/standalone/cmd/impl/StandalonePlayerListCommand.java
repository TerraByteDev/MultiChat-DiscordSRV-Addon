package com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVContentUtils;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.discord.jda5.JDAInteraction;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.utils.DiscordCommandUtils.getPlayerGroups;
import static com.loohp.multichatdiscordsrvaddon.utils.DiscordCommandUtils.sortPlayers;

public class StandalonePlayerListCommand {

    public static void playerListCommand(
            JDAInteraction interaction
    ) {
        boolean isMCChannel = interaction.interactionEvent().getChannelId().equals(Config.i().getStandalone().channelId());
        if (!Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && !isMCChannel) {
            interaction.interactionEvent().reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        AtomicBoolean deleted = new AtomicBoolean(false);
        interaction.interactionEvent().deferReply().queue(hook -> {
            if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                    if (!deleted.get()) {
                        hook.deleteOriginal().queue();
                    }
                }, Config.i().getDiscordCommands().playerList().deleteAfter() * 20L);
            }
        });

        Map<OfflinePlayer, Integer> players;
        if (Config.i().getSettings().bungeecord() && Config.i().getDiscordCommands().playerList().listBungeecordPlayers() && !Bukkit.getOnlinePlayers().isEmpty()) {
            try {
                List<ValueTrios<UUID, String, Integer>> bungeePlayers = MultiChatDiscordSrvAddonAPI.getBungeecordPlayerList().get(); // todo
                players = new LinkedHashMap<>(bungeePlayers.size());
                for (ValueTrios<UUID, String, Integer> playerinfo : bungeePlayers) {
                    UUID uuid = playerinfo.getFirst();
                    Player icPlayer = Bukkit.getPlayer(uuid);
                    if (!PlayerUtils.isVanished(icPlayer)) {
                        players.put(Bukkit.getOfflinePlayer(uuid), playerinfo.getThird());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                interaction.interactionEvent().getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (-1)").queue();
                return;
            }
        } else {
            players = Bukkit.getOnlinePlayers().stream().filter(each -> {
                return !PlayerUtils.isVanished(each);
            }).collect(Collectors.toMap(each -> each, each -> each.getPing(), (a, b) -> a));
        }

        if (players.isEmpty()) {
            interaction.interactionEvent().getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerList().emptyServer())).queue();
        } else {
            int errorCode = -2;

            try {
                List<ValueTrios<OfflinePlayer, Component, Integer>> player = new ArrayList<>();
                Map<UUID, ValuePairs<List<String>, String>> playerInfo = new HashMap<>();
                for (Map.Entry<OfflinePlayer, Integer> entry : players.entrySet()) {
                    OfflinePlayer bukkitOfflinePlayer = entry.getKey();
                    playerInfo.put(bukkitOfflinePlayer.getUniqueId(), new ValuePairs<>(getPlayerGroups(bukkitOfflinePlayer), bukkitOfflinePlayer.getName()));
                    String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(bukkitOfflinePlayer, Config.i().getDiscordCommands().playerList().tablistOptions().playerFormat()));

                    if (Config.i().getDiscordCommands().playerList().tablistOptions().parsePlaceholdersTwice()) {
                        name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(bukkitOfflinePlayer, name));
                    }

                    Component nameComponent;
                    if (Config.i().getDiscordCommands().playerList().tablistOptions().parsePlayerNamesWithMiniMessage()) {
                        nameComponent = MiniMessage.miniMessage().deserialize(name);
                    } else {
                        nameComponent = MultiChatComponentSerializer.legacySection().deserialize(name);
                    }
                    player.add(new ValueTrios<>(bukkitOfflinePlayer, nameComponent, entry.getValue()));
                }
                errorCode--;
                sortPlayers(Config.i().getDiscordCommands().playerList().tablistOptions().playerOrder().orderBy(), player, playerInfo);
                errorCode--;
                OfflinePlayer firstPlayer = Bukkit.getOfflinePlayer(players.keySet().iterator().next().getUniqueId());
                List<Component> header = new ArrayList<>();
                if (!Config.i().getDiscordCommands().playerList().tablistOptions().headerText().isEmpty()) {
                    header = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, DiscordSRVContentUtils.join(Config.i().getDiscordCommands().playerList().tablistOptions().headerText(), true).replace("{OnlinePlayers}", players.size() + "")))));
                }
                errorCode--;
                List<Component> footer = new ArrayList<>();
                if (!Config.i().getDiscordCommands().playerList().tablistOptions().footerText().isEmpty()) {
                    footer = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, DiscordSRVContentUtils.join(Config.i().getDiscordCommands().playerList().tablistOptions().footerText(), true).replace("{OnlinePlayers}", players.size() + "")))));
                }
                errorCode--;
                int playerListMaxPlayers = Config.i().getDiscordCommands().playerList().tablistOptions().maxPlayersDisplayable();
                if (playerListMaxPlayers < 1) {
                    playerListMaxPlayers = Integer.MAX_VALUE;
                }
                BufferedImage image = ImageGeneration.getTabListImage(header, footer, player, Config.i().getDiscordCommands().playerList().tablistOptions().showPlayerAvatar(), Config.i().getDiscordCommands().playerList().tablistOptions().showPlayerPing(), playerListMaxPlayers);
                errorCode--;
                byte[] data = ImageUtils.toArray(image);
                errorCode--;
                interaction.interactionEvent().getHook().editOriginalEmbeds(new EmbedBuilder().setImage("attachment://Tablist.png").setColor(ColorUtils.hex2Rgb(Config.i().getDiscordCommands().playerList().tablistOptions().sidebarColor())).build()).setFiles(FileUpload.fromData(data, "Tablist.png")).queue(message -> {
                    if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                        deleted.set(true);
                        message.delete().queueAfter(Config.i().getDiscordCommands().playerList().deleteAfter(), TimeUnit.SECONDS);
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
                interaction.interactionEvent().getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                    if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                        deleted.set(true);
                        message.delete().queueAfter(Config.i().getDiscordCommands().playerList().deleteAfter(), TimeUnit.SECONDS);
                    }
                });
            }
        }
    }
}
