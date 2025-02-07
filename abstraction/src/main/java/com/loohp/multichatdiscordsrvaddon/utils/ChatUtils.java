package com.loohp.multichatdiscordsrvaddon.utils;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatUtils {

    public static BukkitAudiences audience;
    public static final String PREFIX = "<gray>[<reset><b><gradient:#0836FB:#00B6FF>MultiChatDiscordSRVAddon</gradient></b><gray>]<reset>";
    public static final String DEBUG_PREFIX = "<gray>[<reset><b><gradient:#0836FB:#00B6FF>MultiChatDiscordSRVAddon|DEBUG</gradient></b><gray>]<reset>";
    public static final List<String> toAllow = new ArrayList<>();

    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static void init(JavaPlugin plugin) {
        audience = BukkitAudiences.create(plugin);
    }

    public static void sendMessage(Object message, CommandSender... senders) {
        if (senders.length == 0) senders = new ConsoleCommandSender[]{Bukkit.getConsoleSender()};

        for (CommandSender sender : senders) {
            audience.sender(sender).sendMessage(message instanceof Component ? MiniMessage.miniMessage().deserialize(PREFIX).append(Component.text(" ")).append((Component) message) : MiniMessage.miniMessage().deserialize(PREFIX + " " + message));
        }
    }
}
