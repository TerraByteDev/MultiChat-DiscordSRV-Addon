package com.loohp.multichatdiscordsrvaddon.integration;

import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

@Getter
public class IntegrationManager {

    @Nullable
    private MultiChatIntegration integration;

    public void load(String selected) {
        ChatUtils.sendMessage("<grey>Attempting to locate integration of name " + selected + "...");

        try {
            Class<?> clazz = Class.forName("com.loohp.multichatdiscordsrvaddon.integration.impl." + selected + "Integration");

            MultiChatIntegration integration = (MultiChatIntegration) clazz.getDeclaredConstructor().newInstance();
            if (integration.shouldEnable()) {
                ChatUtils.sendMessage("<grey>Detected " + integration.getPluginName() + ", enabling integration...");
                integration.enable(plugin);

                this.integration = integration;
            } else {
                ChatUtils.sendMessage("<red>Integration of name " + selected + " could not be enabled! Do you have the corresponding plugin installed?");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Integration of name " + selected + " not found or not enabled!", e);
        }
    }
}
