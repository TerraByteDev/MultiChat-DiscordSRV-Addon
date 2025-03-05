package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.event.InternalServerChatEvent;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
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
        ChatUtils.sendMessage("<green>Registered Carbon Chat module!");
    }

    public void onCarbonChat(CarbonChatEvent event) {
        if (event.cancelled()) return;

        ChatChannel chatChannel = event.chatChannel();
        if (Config.i().getHook().ignoredChannels().contains(chatChannel.key().asString())) return;

        Player player = Bukkit.getPlayer(event.sender().uuid());
        if (player == null || !player.isOnline()) return;

        String plainMessage = MultiChatIntegration.formatForDiscord(PlainTextComponentSerializer.plainText().serialize(event.message()));
        ChatUtils.toAllow.put(plainMessage, plainMessage);

        Bukkit.getPluginManager().callEvent(new InternalServerChatEvent(plainMessage, plainMessage, player, true));
    }

    @Override
    public void disable(JavaPlugin plugin) {
    }

    @Override
    public String filter(MessageSender messageSender, String message) {
        return configManager.primaryConfig().applyChatFilters(message);
    }
}
