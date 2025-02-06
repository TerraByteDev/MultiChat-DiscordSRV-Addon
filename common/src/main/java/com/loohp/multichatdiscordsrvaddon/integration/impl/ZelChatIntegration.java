package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.listeners.OutboundToDiscordEvents;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import it.pino.zelchat.api.ZelChatAPI;
import it.pino.zelchat.api.formatter.module.external.ExternalModule;
import it.pino.zelchat.api.formatter.module.priority.ModulePriority;
import it.pino.zelchat.api.message.ChatMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Getter
public class ZelChatIntegration extends ExternalModule implements MultiChatIntegration {

    private final String pluginName = "ZelChat";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled("ZelChat");
    }

    @Override
    public void enable() {
        ZelChatAPI.get().getFormatterService().registerExternalModule(plugin, this);
        ChatUtils.sendMessage("<green>Registered external ZelChat module!");
    }

    @Override
    public ModulePriority getPriority() {
        return ModulePriority.HIGHEST;
    }

    @Override
    public ChatMessage handleChatMessage(@NotNull ChatMessage chatMessage) {
        OutboundToDiscordEvents.toAllow.add(chatMessage.getRawMessage());
        System.out.println("raw msg: " + chatMessage.getRawMessage());
        System.out.println("raw comp: " + chatMessage.getMessage());
        DiscordSRV.getPlugin().processChatMessage(chatMessage.getBukkitPlayer(), chatMessage.getRawMessage(), DiscordSRV.getPlugin().getOptionalChannel("global"), false);
        return chatMessage;
    }
}
