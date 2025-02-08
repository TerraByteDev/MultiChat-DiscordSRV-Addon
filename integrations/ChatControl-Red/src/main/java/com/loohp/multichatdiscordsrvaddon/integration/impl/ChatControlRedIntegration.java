package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.DynmapSender;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import com.loohp.multichatdiscordsrvaddon.utils.DSRVUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapWebChatEvent;
import org.mineacademy.chatcontrol.api.ChatChannelEvent;
import org.mineacademy.chatcontrol.api.SimpleChatEvent;
import org.mineacademy.chatcontrol.model.Checker;

@Getter
public class ChatControlRedIntegration implements MultiChatIntegration {

    private final String pluginName = "ChatControlRed";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    @Override
    public void enable(JavaPlugin plugin) {
        EventPriority eventPriority = EventPriority.valueOf(Config.i().getHook().priority());
        if (eventPriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority());

        if (Config.i().getHook().useChannels()) Events.subscribe(ChatChannelEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !Config.i().getHook().ignoredChannels().contains(e.getChannel().getName()))
                .filter(e -> e.getSender() instanceof Player)
                .handler(this::onChannelChatEvent);
        else Events.subscribe(SimpleChatEvent.class)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onPlayerMessage);

        if (Config.i().getHook().dynmap().filter()) Events.subscribe(DynmapWebChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onDynmapWebChatEvent);

        ChatUtils.sendMessage("<green>Registered external ChatControl Legacy (<v10) module!");
    }

    public void onChannelChatEvent(ChatChannelEvent event) {
        ChatUtils.toAllow.add(event.getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                (Player) event.getSender(),
                event.getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }

    public void onPlayerMessage(SimpleChatEvent event) {
        System.out.println(event.getMessage());;
    }

    public void onDynmapWebChatEvent(DynmapWebChatEvent event) {
        String dynmapUsername = event.getName();
        DynmapSender dynmapSender = new DynmapSender(dynmapUsername);

        Checker checker = Checker.filterChannel(dynmapSender, event.getMessage(), null);
        if (checker.isCancelledSilently()) return;

        DSRVUtils.sendDynmapMessage(dynmapUsername, checker.getMessage());
    }
}
