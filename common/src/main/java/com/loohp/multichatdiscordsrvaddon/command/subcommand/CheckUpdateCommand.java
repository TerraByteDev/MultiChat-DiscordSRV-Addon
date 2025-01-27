package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.updater.Updater;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import static com.loohp.multichatdiscordsrvaddon.InteractiveChatDiscordSrvAddon.plugin;

@Command("multichat")
public class CheckUpdateCommand {

    @Command("checkupdate")
    @CommandDescription("Check for updates")
    @Permission(value = {"multichatdiscordsrv.checkupdate"})
    public void execute(
            CommandSender sender
    ) {
        plugin.sendMessage("<grey>Checking for updates, please wait...", sender);
        if (Updater.checkUpdate(sender)) {
            plugin.sendMessage("<green>You are running the latest version of MultiChat-DiscordSRV-Addon! <grey>[" + plugin.getDescription().getVersion() + "]", sender);
        }
    }

}
