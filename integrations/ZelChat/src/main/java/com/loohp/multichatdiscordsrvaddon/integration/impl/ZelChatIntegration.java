package com.loohp.multichatdiscordsrvaddon.integration.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import it.pino.zelchat.api.ZelChatAPI;
import it.pino.zelchat.api.message.ChatMessage;
import it.pino.zelchat.api.message.state.MessageState;
import it.pino.zelchat.api.module.ChatModule;
import it.pino.zelchat.api.module.annotation.ChatModuleSettings;
import it.pino.zelchat.api.module.priority.ModulePriority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Getter
@ChatModuleSettings(
        pluginOwner = "MultiChatDiscordSrvAddon",
        priority = ModulePriority.NORMAL
)
public class ZelChatIntegration implements MultiChatIntegration, ChatModule {

    private final String pluginName = "ZelChat";

    @Override
    public boolean shouldEnable() {
        return Bukkit.getPluginManager().isPluginEnabled(getPluginName());
    }

    @Override
    public void enable(JavaPlugin plugin) {
        ModulePriority modulePriority = ModulePriority.valueOf(Config.i().getHook().priority());
        if (modulePriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority() + ".\nNote: ZelChat does not support the MONITOR event priority.");

        ZelChatAPI.get().getModuleManager().register(plugin, this);
        this.load();

        ChatUtils.sendMessage("<green>Registered external ZelChat module!");
    }

    @Override
    public void disable(JavaPlugin plugin) {
        this.unload();
        ZelChatAPI.get().getModuleManager().unregister(plugin, this);
    }

    @Override
    public String filter(MessageSender messageSender, String message) {
        return message; // todo same as todo below
    }

    @Override
    public void handleChatMessage(@NotNull ChatMessage chatMessage) {
        if (Config.i().getDebug().printInfoToConsole()) Debug.debug("ZelChat handleChatMessage method triggered.\nChannel Type = " + chatMessage.getChannel().getType().name() + "\nState = " + chatMessage.getState());

        if (Config.i().getHook().ignoredChannels().contains(chatMessage.getChannel().getType().name())
            || (chatMessage.getState() == MessageState.CANCELLED || chatMessage.getState() == MessageState.FILTERED_CANCELLED)) return;

        // todo wait for pino to expose filtering thru api

        String formatted = formatForDiscord(chatMessage.getRawMessage());

        ChatUtils.toAllow.put(formatted, formatted);
        DiscordSRV.getPlugin().processChatMessage(
                chatMessage.getBukkitPlayer(),
                formatted,
                DiscordSRV.getPlugin().getOptionalChannel("global"),
                false
        );
    }
}
