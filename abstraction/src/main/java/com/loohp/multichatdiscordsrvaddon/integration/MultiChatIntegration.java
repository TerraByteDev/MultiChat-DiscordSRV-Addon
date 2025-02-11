package com.loohp.multichatdiscordsrvaddon.integration;

import com.loohp.multichatdiscordsrvaddon.integration.dynmap.DynmapSender;
import org.bukkit.plugin.java.JavaPlugin;

public interface MultiChatIntegration {

    String getPluginName();

    boolean shouldEnable();

    void enable(JavaPlugin plugin);

    void disable(JavaPlugin plugin);

    String filter(DynmapSender dynmapSender, String message);

}
