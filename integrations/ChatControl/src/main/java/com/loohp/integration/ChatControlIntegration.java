package com.loohp.integration;

import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.dynmap.DynmapWebChatEvent;
import org.mineacademy.chatcontrol.api.ChannelPreChatEvent;
import org.mineacademy.chatcontrol.api.ChatControlAPI;
import org.mineacademy.chatcontrol.api.PlayerMessageEvent;
import org.mineacademy.chatcontrol.lib.model.DynmapSender;
import org.mineacademy.chatcontrol.model.Checker;
import org.mineacademy.chatcontrol.model.WrappedSender;

@Getter
public class ChatControlIntegration implements MultiChatIntegration, Listener {

    private final String pluginName = "ChatControl";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled("ChatControl");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChannelPreChatEvent(ChannelPreChatEvent event) {
        if ((MultiChatDiscordSrvAddon.plugin.useChannels && MultiChatDiscordSrvAddon.plugin.ignoredChannels.contains(event.getChannel().getName()))
                || !(event.getSender() instanceof Player)
                || event.isCancelled()) return;

        Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromSender(event.getSender()), event.getMessage());
        if (checker.isCancelledSilently()) return;

        ChatUtils.toAllow.add(checker.getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                (Player) event.getSender(),
                checker.getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMessage(PlayerMessageEvent event) {
        if (event.getPlayer() == null || event.isCancelled() || MultiChatDiscordSrvAddon.plugin.useChannels) return;

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
        Bukkit.getPluginManager().registerEvents(this, MultiChatDiscordSrvAddon.plugin);
        MultiChatDiscordSrvAddon.plugin.sendMessage("<green>Registered external ChatControl v11 module!");
    }

    @EventHandler
    public void onDynmapWebChatEvent(DynmapWebChatEvent event) {
        if (!MultiChatDiscordSrvAddon.plugin.filterDynmap) return;

        String dynmapUsername = event.getName();

        DynmapSender dynmapSender = null;
        if (!dynmapUsername.isEmpty()) {
            Player player = Bukkit.getPlayerExact(dynmapUsername);
            if (player != null) dynmapSender = new DynmapSender(dynmapUsername, player.getUniqueId(), player);
        } else {
            dynmapSender = new DynmapSender(MultiChatDiscordSrvAddon.plugin.fallbackDynmapName, MultiChatDiscordSrvAddon.ZERO_UUID, null);
        }

        Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromDynmap(dynmapSender), event.getMessage());
        if (checker.isCancelledSilently()) return;

        OutboundToDiscordEvents.toAllow.add(checker.getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                dynmapSender.getOnlinePlayer(),
                checker.getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }
}
