package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.integration.dynmap.DynmapSender;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import lombok.Getter;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.event.events.CarbonChatEvent;
import net.draycia.carbon.common.CarbonChatInternal;
import net.draycia.carbon.common.config.ConfigManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class CarbonChatIntegration implements MultiChatIntegration {
    private final String pluginName = "Carbon";

    private CarbonChat carbonChat;
    private CarbonChatInternal carbonChatInternal;
    private ConfigManager configManager;

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @Override
    public void enable(JavaPlugin plugin) {
        this.carbonChat = CarbonChatProvider.carbonChat();
        this.carbonChatInternal = (CarbonChatInternal) carbonChat;
        this.configManager = carbonChatInternal.injector().getInstance(ConfigManager.class);

        carbonChat.eventHandler().subscribe(CarbonChatEvent.class, this::onCarbonChat);
    }

    public void onCarbonChat(CarbonChatEvent event) {
        if (event.cancelled()) return;

        ChatChannel chatChannel = event.chatChannel();
        if (Config.i().getHook().ignoredChannels().contains(chatChannel.key().asString())) return;

        Player player = Bukkit.getPlayer(event.sender().uuid());
        if (player == null || !player.isOnline()) return;

        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        ChatUtils.toAllow.add(plainMessage);
        DiscordSRV.getPlugin().processChatMessage(
                player,
                plainMessage,
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );

        ChatUtils.sendMessage("<green>Registered Carbon Chat module!");
    }

    @Override
    public void disable(JavaPlugin plugin) {
    }

    @Override
    public String filter(DynmapSender dynmapSender, String message) {
        return configManager.primaryConfig().applyChatFilters(message);
    }
}
