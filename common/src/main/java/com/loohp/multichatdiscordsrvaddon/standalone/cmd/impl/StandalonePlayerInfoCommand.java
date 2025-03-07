package com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.objectholders.ToolTipComponent;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.key.CloudKey;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class StandalonePlayerInfoCommand {

    public static final CloudKey<User> USER = CloudKey.of(Config.i().getDiscordCommands().globalSettings().messages().member(), User.class);

    public static void playerInfoCommand(
            JDAInteraction interaction,
            Optional<User> optionalUser
    ) {
        boolean isMCChannel = interaction.interactionEvent().getChannelId().equals(Config.i().getStandalone().channelId());
        if (!Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && !isMCChannel) {
            interaction.interactionEvent().reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String userID = interaction.user().getId();
        if (optionalUser.isPresent()) {
            userID = optionalUser.get().getId();
        }

        LinkedUser linkedUser = DiscordProviderManager.get().getLinkedUser(userID);
        UUID uuid = linkedUser.getUuid();
        if (uuid == null) {
            interaction.interactionEvent()
                    .reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int errorCode = -1;
        try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            errorCode--;

            List<ToolTipComponent<?>> playerInfoComponents;
            if (offlinePlayer.isOnline() && !PlayerUtils.isVanished((Player) offlinePlayer)) {
                playerInfoComponents = Config.i().getDiscordCommands().playerInfo().infoFormatting().whenOnline().stream().map(each -> {
                    each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, each));

                    if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                        each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, each));
                    }

                    return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                }).collect(Collectors.toList());
            } else {
                playerInfoComponents = Config.i().getDiscordCommands().playerInfo().infoFormatting().whenOffline().stream().map(each -> {
                    each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, each));

                    if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                        each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, each));
                    }

                    return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                }).collect(Collectors.toList());
            }

            errorCode--;
            String title = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, Config.i().getDiscordCommands().playerInfo().infoFormatting().title())));
            String subtitle = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, Config.i().getDiscordCommands().playerInfo().infoFormatting().subtitle())));

            if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                title = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, title)));
                subtitle = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlinePlayer, subtitle)));
            }

            BufferedImage image = ImageGeneration.getToolTipImage(playerInfoComponents, null);
            errorCode--;
            byte[] data = ImageUtils.toArray(image);
            errorCode--;

            interaction.interactionEvent().getHook().editOriginalEmbeds(new EmbedBuilder()
                            .setTitle(title)
                            .setDescription(subtitle)
                            .setThumbnail(SkinUtils.getFormattedSkinURL(offlinePlayer))
                            .setImage("attachment://PlayerInfo.png")
                            .setColor(ColorUtils.hex2Rgb(Config.i().getDiscordCommands().playerList().tablistOptions().sidebarColor())).build())
                            .setFiles(FileUpload.fromData(data, "PlayerInfo.png"))
                            .queue();
        } catch (Throwable e) {
            e.printStackTrace();
            interaction.interactionEvent().getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue();
        }
    }
}
