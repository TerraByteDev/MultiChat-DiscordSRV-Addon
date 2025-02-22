package com.loohp.multichatdiscordsrvaddon.integration;

import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import org.bukkit.plugin.java.JavaPlugin;

public interface MultiChatIntegration {

    String getPluginName();

    boolean shouldEnable();

    void enable(JavaPlugin plugin);

    void disable(JavaPlugin plugin);

    String filter(MessageSender messageSender, String message);

    default String formatForDiscord(String string) {
        return string.replace("*", "\\*");
    }

}
