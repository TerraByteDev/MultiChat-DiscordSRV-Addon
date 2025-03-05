package com.loohp.multichatdiscordsrvaddon.integration;

import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import dev.vankka.mcdiscordreserializer.discord.DiscordSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

public interface MultiChatIntegration {

    String getPluginName();

    boolean shouldEnable();

    void enable(JavaPlugin plugin);

    void disable(JavaPlugin plugin);

    String filter(MessageSender messageSender, String message);

    static String formatForDiscord(String string) {
        return DiscordSerializer.INSTANCE.serialize(Component.text(string.replace("*", "\\*")));
    }

}
