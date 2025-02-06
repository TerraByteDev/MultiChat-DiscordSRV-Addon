package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.updater.Updater;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Command("multichat")
public class CheckUpdateCommand {

    @Command("checkupdate")
    @CommandDescription("Check for updates")
    @Permission(value = {"multichatdiscordsrv.checkupdate"})
    public void execute(
            CommandSender sender
    ) {
        ChatUtils.sendMessage("<grey>Checking for updates, please wait...", sender);
        if (Updater.checkUpdate(sender)) {
            ChatUtils.sendMessage("<green>You are running the latest version of MultiChat-DiscordSRV-Addon! <grey>[" + plugin.getDescription().getVersion() + "]", sender);
        }
    }

}
