package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.dynmap.DynmapSender;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.chatcontrol.api.ChatChannelEvent;
import org.mineacademy.chatcontrol.api.SimpleChatEvent;
import org.mineacademy.chatcontrol.model.Checker;

@SuppressWarnings("deprecation")
@Getter
public class ChatControlRedIntegration implements MultiChatIntegration {

    private final String pluginName = "ChatControlRed";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    @SuppressWarnings("ConstantValue")
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

        ChatUtils.sendMessage("<green>Registered external ChatControl Legacy (<v10) module!");
    }

    @Override
    public void disable(JavaPlugin plugin) {}

    @Override
    public String filter(DynmapSender dynmapSender, String message) {

        Checker checker = Checker.filterChannel(dynmapSender, message, null);
        if (checker.isCancelledSilently()) return "";

        return formatForDiscord(checker.getMessage());
    }

    public void onChannelChatEvent(ChatChannelEvent event) {
        String formatted = formatForDiscord(event.getMessage());

        ChatUtils.toAllow.add(formatted);
        DiscordSRV.getPlugin().processChatMessage(
                (Player) event.getSender(),
                formatted,
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }

    public void onPlayerMessage(SimpleChatEvent event) {
        String formatted = formatForDiscord(event.getMessage());

        ChatUtils.toAllow.add(formatted);
        DiscordSRV.getPlugin().processChatMessage(
                event.getPlayer(),
                formatted,
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }
}
