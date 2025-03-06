package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

@Command("multichat|mc")
public class LinkCommand {

    @Command("link")
    @CommandDescription("Link your discord account to your Minecraft account!")
    @Permission(value = {"multichatdiscordsrv.standalone.link"})
    public void execute(Player sender) {
        LinkedUser linkedUser = DiscordProviderManager.get().getLinkedUser(sender.getUniqueId());
        if (linkedUser != null) {
            String username = DiscordProviderManager.get().getUsername(linkedUser);
            if (username == null) {
                ChatUtils.sendMessage("<red>Woah! You've caught an ultra rare error! Report this to the devs ;)");
                return;
            }

            ChatUtils.sendMessage(
                    Config.i().getMessages().alreadyLinked().replace("%linked_member_name%", username),
                    sender
            );
            return;
        }

        MultiChatDiscordSrvAddon.plugin.standaloneManager.getLinkManager().createCode(sender.getUniqueId()).whenComplete((code, ex) -> {
            if (ex != null) {
                ChatUtils.sendMessage("<red>An unexpected error occurred.", sender);
                ex.printStackTrace();
                return;
            }

            ChatUtils.sendMessage(
                    Config.i().getMessages().linkPrompt()
                            .replace("%code%", code)
                            .replace("%bot_name%", DiscordProviderManager.get().getBotUsername()),
                    sender
            );
        });
    }
}
