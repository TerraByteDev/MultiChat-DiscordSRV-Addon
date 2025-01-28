package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.api.events.InteractiveChatDiscordSRVConfigReloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.concurrent.TimeUnit;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Command("multichat")
public class ReloadConfigCommand {

    @Command("reloadconfig")
    @CommandDescription("Reload the plugin's configs")
    @Permission(value = {"multichatdiscordsrv.reloadconfig"})
    public void execute(
            CommandSender sender
    ) {
        try {
            if (plugin.resourceReloadLock.tryLock(0, TimeUnit.MILLISECONDS)) {
                try {
                    plugin.reloadConfig();
                    Bukkit.getPluginManager().callEvent(new InteractiveChatDiscordSRVConfigReloadEvent());
                    plugin.sendMessage(plugin.reloadConfigMessage, sender);
                } catch (Throwable e) {
                    e.printStackTrace();
                    plugin.sendMessage("<red>Whoops! Looks like something went wrong: <grey>" + e.getMessage(), sender);
                } finally {
                    plugin.resourceReloadLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            plugin.sendMessage("<red>Whoops! Looks like something went wrong: <grey>" + e.getMessage(), sender);
        }
    }
}
