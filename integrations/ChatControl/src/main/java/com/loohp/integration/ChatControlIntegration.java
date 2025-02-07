package com.loohp.integration;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import com.loohp.multichatdiscordsrvaddon.utils.DSRVUtils;
import com.loohp.multichatdiscordsrvaddon.utils.ReflectionUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
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

    public void onChannelPreChatEvent(ChannelPreChatEvent event) {
        if (Config.i().getHook().ignoredChannels().contains(event.getChannel().getName())
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

    public void onPlayerMessage(PlayerMessageEvent event) {
        if (event.getPlayer() == null) return;

        ChatUtils.toAllow.add(event.getCheck().getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                event.getPlayer(),
                event.getCheck().getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }



    @Override
    public void enable() {
        EventPriority eventPriority = EventPriority.valueOf(Config.i().getHook().priority());
        if (eventPriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority());

        if (Config.i().getHook().useChannels()) Events.subscribe(ChannelPreChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onChannelPreChatEvent);
        else Events.subscribe(PlayerMessageEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onPlayerMessage);

        if (Config.i().getHook().dynmap().filter()) Events.subscribe(DynmapWebChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onDynmapWebChatEvent);

        Bukkit.getPluginManager().registerEvents(this, ReflectionUtils.getPlugin());
        ChatUtils.sendMessage("<green>Registered external ChatControl v11 module!");
    }

    public void onDynmapWebChatEvent(DynmapWebChatEvent event) {
        String dynmapUsername = event.getName();

        DynmapSender dynmapSender = null;
        if (!dynmapUsername.isEmpty()) {
            Player player = Bukkit.getPlayerExact(dynmapUsername);
            if (player != null) dynmapSender = new DynmapSender(dynmapUsername, player.getUniqueId(), player);
        } else {
            dynmapSender = new DynmapSender(Config.i().getHook().dynmap().fallbackName(), ChatUtils.ZERO_UUID, null);
        }

        Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromDynmap(dynmapSender), event.getMessage());
        if (checker.isCancelledSilently()) return;

        DSRVUtils.sendDynmapMessage(dynmapUsername, checker.getMessage());
    }
}
