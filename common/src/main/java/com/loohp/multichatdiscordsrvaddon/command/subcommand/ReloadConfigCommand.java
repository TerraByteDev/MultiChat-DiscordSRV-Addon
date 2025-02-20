package com.loohp.multichatdiscordsrvaddon.command.subcommand;

import com.loohp.multichatdiscordsrvaddon.api.events.MultiChatDiscordSRVConfigReloadEvent;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;
import org.incendo.cloud.annotations.Permission;

import java.util.concurrent.TimeUnit;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Command("multichat|mc")
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
                    Config.i().reload();
                    ImageGeneration.onConfigReload();
                    Bukkit.getPluginManager().callEvent(new MultiChatDiscordSRVConfigReloadEvent());
                    ChatUtils.sendMessage(Config.i().getMessages().reloadConfig(), sender);
                } catch (Throwable e) {
                    e.printStackTrace();
                    ChatUtils.sendMessage("<red>Whoops! Looks like something went wrong: <grey>" + e.getMessage(), sender);
                } finally {
                    plugin.resourceReloadLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            ChatUtils.sendMessage("<red>Whoops! Looks like something went wrong: <grey>" + e.getMessage(), sender);
        }
    }
}
