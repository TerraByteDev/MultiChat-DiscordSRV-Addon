package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ICLogger {

    public static final String PREFIX = "<gray>[<reset><b><gradient:#0836FB:#00B6FF>MultiChatDiscordSRVAddon</gradient></b><gray>]<reset>";
    public static final String DEBUG_PREFIX = "<gray>[<reset><b><gradient:#0836FB:#00B6FF>MultiChatDiscordSRVAddon|DEBUG</gradient></b><gray>]<reset>";

    public static void info(Object message, Object... args) {
        Component infoLog = MiniMessage.miniMessage().deserialize(PREFIX + " <#4294ed>" + format(message, args) + "<#4294ed><reset>");
        MultiChatDiscordSrvAddon.plugin.audience.console().sendMessage(infoLog);
    }

    public static void warn(Object message, Object... args) {
        Component warnLog = MiniMessage.miniMessage().deserialize(PREFIX + " <#f28f24>" + format(message, args) + "<#f28f24><reset>");
        MultiChatDiscordSrvAddon.plugin.audience.console().sendMessage(warnLog);
    }

    public static void fatal(Object message, Object... args) {
        Component fatalLog = MiniMessage.miniMessage().deserialize(PREFIX + " <#e73f38>" + format(message, args) + "<#e73f38><reset>");
        MultiChatDiscordSrvAddon.plugin.audience.console().sendMessage(fatalLog);
    }

    private static String format(Object message, Object... args) {
        if (message == null) {
            return null;
        }
        String formattedMessage = message.toString();
        for (Object arg : args) {
            formattedMessage = formattedMessage.replaceFirst("\\{\\}", arg.toString());
        }
        return ColorUtils.wipeColors(formattedMessage);
    }

}
