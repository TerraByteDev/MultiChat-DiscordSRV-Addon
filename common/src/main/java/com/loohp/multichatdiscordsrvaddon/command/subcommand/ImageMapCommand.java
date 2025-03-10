package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.listeners.InboundToGameEvents;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

import java.util.UUID;

@Command("multichat|mcd")
public class ImageMapCommand {

    @Command("imagemap <uuid>")
    @CommandDescription("View discord images on a map")
    public void execute(
            CommandSender sender,
            @Argument(value = "uuid") String uuid
    ) {
        if (!(sender instanceof Player)) {
            ChatUtils.sendMessage("<red>This command can only be executed by a player!", sender);
            return;
        }

        try {
            InboundToGameEvents.DiscordAttachmentData data = InboundToGameEvents.DATA.get(UUID.fromString(uuid));
            if (data != null && (data.isImage() || data.isVideo())) {

                GraphicsToPacketMapWrapper imageMap = data.getImageMap();
                if (imageMap.futureCancelled()) {
                    ChatUtils.sendMessage(Config.i().getMessages().linkExpired(), sender);
                } else if (imageMap.futureCompleted()) {
                    if (imageMap.getColors() == null || imageMap.getColors().isEmpty()) {
                        ChatUtils.sendMessage(Config.i().getMessages().linkExpired(), sender);
                    } else {
                        imageMap.show((Player) sender);
                    }
                } else {
                    ChatUtils.sendMessage(Config.i().getMessages().previewLoading(), sender);
                }
            } else {
                ChatUtils.sendMessage(Config.i().getMessages().linkExpired(), sender);
            }
        } catch (Exception e) {
            ChatUtils.sendMessage(Config.i().getMessages().linkExpired(), sender);
        }
    }

}
