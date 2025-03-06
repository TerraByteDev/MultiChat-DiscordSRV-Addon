package com.loohp.multichatdiscordsrvaddon.listeners;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.event.InternalServerChatEvent;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.standalone.message.StandaloneGameMessageHandler;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class InternalEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInternalServerChat(InternalServerChatEvent event) {
        if (Config.i().getStandalone().enabled()) {
            StandaloneGameMessageHandler.handleChat(event);
        } else {
            DiscordSRV.getPlugin().processChatMessage(
                    event.getEmitter(),
                    event.getFormatted(),
                    DiscordSRV.getPlugin().getOptionalChannel("global"),
                     false
            );
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Config.i().getHook().shouldHook()) {
            String formatted = MultiChatIntegration.formatForDiscord(event.getMessage());

            Bukkit.getPluginManager().callEvent(new InternalServerChatEvent(
                    formatted, formatted, event.getPlayer(), true
            ));
        }
    }
}
