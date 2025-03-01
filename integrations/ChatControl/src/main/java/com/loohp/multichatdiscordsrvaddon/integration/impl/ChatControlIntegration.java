package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mineacademy.chatcontrol.api.ChannelPreChatEvent;
import org.mineacademy.chatcontrol.api.ChatControlAPI;
import org.mineacademy.chatcontrol.lib.model.DynmapSender;
import org.mineacademy.chatcontrol.model.Checker;
import org.mineacademy.chatcontrol.model.WrappedSender;

@SuppressWarnings("deprecation")
@Getter
public class ChatControlIntegration implements MultiChatIntegration {

    private final String pluginName = "ChatControl";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }


    @SuppressWarnings("ConstantValue")
    @Override
    public void enable(JavaPlugin plugin) {
        EventPriority eventPriority = EventPriority.valueOf(Config.i().getHook().priority());
        if (eventPriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority());

        Events.subscribe(ChannelPreChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> Config.i().getHook().useChannels())
                .filter(e -> !Config.i().getHook().ignoredChannels().contains(e.getChannel().getName()))
                .filter(e -> e.getSender() instanceof Player)
                .handler(this::onChannelPreChatEvent);

        Events.subscribe(AsyncPlayerChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !Config.i().getHook().useChannels())
                .handler(this::onPlayerMessage);

        ChatUtils.sendMessage("<green>Registered external ChatControl v11 module!");
    }

    @Override
    public void disable(JavaPlugin plugin) {}

    @SuppressWarnings("DataFlowIssue")
    @Override
    public String filter(MessageSender messageSender, String message) {
        try {
            String dynmapUsername = messageSender.getName();

            DynmapSender chatControlDynmapSender = null;
            if (!dynmapUsername.isEmpty()) {
                Player player = Bukkit.getPlayerExact(dynmapUsername);
                if (player != null) chatControlDynmapSender = new DynmapSender(dynmapUsername, player.getUniqueId(), player);
            } else {
                chatControlDynmapSender = new DynmapSender(Config.i().getHook().dynmap().fallbackName(), ChatUtils.ZERO_UUID, null);
            }

            Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromDynmap(chatControlDynmapSender), message);
            if (checker.isCancelledSilently()) return "";

            return formatForDiscord(checker.getMessage());
        } catch (Exception ignored) {}

        return "";
    }

    public void onChannelPreChatEvent(ChannelPreChatEvent event) {
        try {
            Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromSender(event.getSender()), event.getMessage());
            if (checker.isCancelledSilently()) return;

            String formatted = formatForDiscord(checker.getMessage());

            ChatUtils.toAllow.add(formatted);
            DiscordSRV.getPlugin().processChatMessage(
                    (Player) event.getSender(),
                    formatted,
                    DiscordSRV.getPlugin().getOptionalChannel("global"),
                    false
            );
        } catch (Exception ignored) {}
    }

    public void onPlayerMessage(AsyncPlayerChatEvent event) {
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
