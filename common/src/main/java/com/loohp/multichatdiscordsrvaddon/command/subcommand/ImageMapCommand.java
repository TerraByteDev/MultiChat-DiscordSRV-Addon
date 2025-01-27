package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.listeners.InboundToGameEvents;
import com.loohp.multichatdiscordsrvaddon.updater.Updater;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

import java.util.UUID;

import static com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon.plugin;

@Command("multichat")
public class ImageMapCommand {

    @Command("imagemap <uuid>")
    @CommandDescription("View discord images on a map")
    public void execute(
            CommandSender sender,
            @Argument(value = "uuid") String uuid
    ) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage("<red>This command can only be executed by a player!", sender);
        }

        try {
            InboundToGameEvents.DiscordAttachmentData data = InboundToGameEvents.DATA.get(UUID.fromString(uuid));
            if (data != null && (data.isImage() || data.isVideo())) {

                GraphicsToPacketMapWrapper imageMap = data.getImageMap();
                if (imageMap.futureCancelled()) {
                    plugin.sendMessage(plugin.linkExpired, sender);
                } else if (imageMap.futureCompleted()) {
                    if (imageMap.getColors() == null || imageMap.getColors().isEmpty()) {
                        plugin.sendMessage(plugin.linkExpired, sender);
                    } else {
                        imageMap.show((Player) sender);
                    }
                } else {
                    plugin.sendMessage(plugin.previewLoading, sender);
                }
            } else {
                plugin.sendMessage(plugin.linkExpired, sender);
            }
        } catch (Exception e) {
            plugin.sendMessage(plugin.linkExpired, sender);
        }
    }

}
