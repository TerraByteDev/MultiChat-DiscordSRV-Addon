package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.event.InternalServerChatEvent;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import it.pino.zelchat.api.ZelChatAPI;
import it.pino.zelchat.api.formatter.module.external.ExternalModule;
import it.pino.zelchat.api.formatter.module.priority.ModulePriority;
import it.pino.zelchat.api.message.ChatMessage;
import it.pino.zelchat.api.message.MessageState;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
public class ZelChatIntegration extends ExternalModule implements MultiChatIntegration {

    private final String pluginName = "ZelChat";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    @Override
    public void enable(JavaPlugin plugin) {
        ModulePriority modulePriority = ModulePriority.valueOf(Config.i().getHook().priority());
        if (modulePriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority() + ".\nNote: ZelChat does not support the MONITOR event priority.");

        ZelChatAPI.get().getFormatterService().registerExternalModule(plugin, this);
        this.load();

        ChatUtils.sendMessage("<green>Registered external ZelChat module!");
    }

    @Override
    public void disable(JavaPlugin plugin) {
        this.unload();
        ZelChatAPI.get().getFormatterService().unregisterExternalModule(plugin, this);
    }

    @Override
    public String filter(MessageSender messageSender, String message) {
        return message; // todo same as todo below
    }

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.valueOf(Config.i().getHook().priority());
    }

    @Override
    public ChatMessage handleChatMessage(@NotNull ChatMessage chatMessage) {
        if (Config.i().getHook().ignoredChannels().contains(chatMessage.getChannel().getType().name())
            || (chatMessage.getState() == MessageState.CANCELLED || chatMessage.getState() == MessageState.FILTERED_CANCELLED)) return chatMessage;

        // todo wait for pino to expose filtering thru api

        String formatted = MultiChatIntegration.formatForDiscord(chatMessage.getRawMessage());

        ChatUtils.toAllow.put(formatted, formatted);
        Bukkit.getPluginManager().callEvent(new InternalServerChatEvent(formatted, formatted, chatMessage.getBukkitPlayer(), true));

        return chatMessage;
    }
}
