package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.listeners.OutboundToDiscordEvents;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.mineacademy.chatcontrol.api.ChatChannelEvent;
import org.mineacademy.chatcontrol.api.PlayerMessageEvent;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Getter
public class ChatControlRedIntegration implements MultiChatIntegration, Listener {

    private final String pluginName = "ChatControlRed";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled("ChatControlRed");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChannelPostChatEvent(ChatChannelEvent event) {
        if (!Config.i().getHook().useChannels()
                || Config.i().getHook().ignoredChannels().contains(event.getChannel().getName())
                || event.isCancelled()
                || !(event.getSender() instanceof Player)) return;

        OutboundToDiscordEvents.toAllow.add(event.getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                (Player) event.getSender(),
                event.getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }

    @EventHandler
    public void onPlayerMessage(PlayerMessageEvent event) {
        if (event.getPlayer() == null || event.isCancelled() || Config.i().getHook().useChannels()) return;

        OutboundToDiscordEvents.toAllow.add(event.getCheck().getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                event.getPlayer(),
                event.getCheck().getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }

    @Override
    public void enable() {
        ChatUtils.sendMessage("<yellow>Tip: Consider upgrading to ChatControl v11 (now open source!) for better compatibility with MultiChatDiscordSrvAddon, and more features!");

        Bukkit.getPluginManager().registerEvents(this, MultiChatDiscordSrvAddon.plugin);
        ChatUtils.sendMessage("<green>Registered ChatControlRed listener!");
    }





}
