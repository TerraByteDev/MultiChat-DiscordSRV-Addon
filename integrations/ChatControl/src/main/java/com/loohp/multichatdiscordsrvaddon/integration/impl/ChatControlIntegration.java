package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
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
import org.mineacademy.chatcontrol.api.ChannelPreChatEvent;
import org.mineacademy.chatcontrol.api.ChatControlAPI;
import org.mineacademy.chatcontrol.api.PlayerMessageEvent;
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

        if (Config.i().getHook().useChannels()) Events.subscribe(ChannelPreChatEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .filter(e -> !Config.i().getHook().ignoredChannels().contains(e.getChannel().getName()))
                .filter(e -> e.getSender() instanceof Player)
                .handler(this::onChannelPreChatEvent);
        else Events.subscribe(PlayerMessageEvent.class, eventPriority)
                .filter(EventFilters.ignoreCancelled())
                .handler(this::onPlayerMessage);

        ChatUtils.sendMessage("<green>Registered external ChatControl v11 module!");
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public String filter(com.loohp.multichatdiscordsrvaddon.integration.dynmap.DynmapSender dynmapSender, String message) {
        String dynmapUsername = dynmapSender.getName();

        DynmapSender chatControlDynmapSender = null;
        if (!dynmapUsername.isEmpty()) {
            Player player = Bukkit.getPlayerExact(dynmapUsername);
            if (player != null) chatControlDynmapSender = new DynmapSender(dynmapUsername, player.getUniqueId(), player);
        } else {
            chatControlDynmapSender = new DynmapSender(Config.i().getHook().dynmap().fallbackName(), ChatUtils.ZERO_UUID, null);
        }

        Checker checker = ChatControlAPI.checkMessage(WrappedSender.fromDynmap(chatControlDynmapSender), message);
        if (checker.isCancelledSilently()) return "";

        return checker.getMessage();
    }

    public void onChannelPreChatEvent(ChannelPreChatEvent event) {
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
        System.out.println(event.getType());
        if (event.getPlayer() == null) return;

        ChatUtils.toAllow.add(event.getCheck().getMessage());
        DiscordSRV.getPlugin().processChatMessage(
                event.getPlayer(),
                event.getCheck().getMessage(),
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }
}
