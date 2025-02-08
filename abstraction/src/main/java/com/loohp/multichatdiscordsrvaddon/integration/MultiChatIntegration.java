package com.loohp.multichatdiscordsrvaddon.integration;

import org.bukkit.plugin.java.JavaPlugin;

public interface MultiChatIntegration {

    String getPluginName();

    boolean shouldEnable();

    void enable(JavaPlugin plugin);

}
