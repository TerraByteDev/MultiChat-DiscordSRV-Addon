package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@Command("multichat|mc")
public class UnlinkCommand {

    @Command("unlink")
    @Command("Unlink your Minecraft account from your Discord account.")
    @Permission(value = {"multichatdiscordsrv.standalone.unlink"})
    public void execute(Player player) {
        LinkedUser linkedUser = DiscordProviderManager.get().getLinkedUser(player.getUniqueId());

        if (linkedUser == null) {
            ChatUtils.sendMessage(
                    Config.i().getMessages().accountNotLinked(),
                    player
            );
            return;
        }

        MultiChatDiscordSrvAddon.plugin.standaloneManager.getLinkManager().getDatabase().removeLinkedUser(linkedUser).whenComplete((ignored, ex) -> {
            if (ex != null) {
                ChatUtils.sendMessage(
                        "<red>Whoops! Something went wrong. Please try again later.",
                        player
                );
                ex.printStackTrace();
                return;
            }

            ChatUtils.sendMessage(
                    Config.i().getMessages().unlinkSuccess(),
                    player
            );
        });
    }
}
