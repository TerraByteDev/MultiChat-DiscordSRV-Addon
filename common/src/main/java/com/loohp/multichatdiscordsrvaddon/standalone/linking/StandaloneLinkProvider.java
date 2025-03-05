package com.loohp.multichatdiscordsrvaddon.standalone.linking;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.link.LinkProvider;
import net.dv8tion.jda.api.entities.User;

import java.util.UUID;

public class StandaloneLinkProvider implements LinkProvider {



    @Override
    public String getUserAsMention(UUID playerUUID) {
        User user = MultiChatDiscordSrvAddon.plugin.standaloneManager.getJda().getUserById(getLinkedDiscordUUID(playerUUID));
        if (user != null) {
            return user.getAsMention();
        } else return "";
    }

    private static String getLinkedDiscordUUID(UUID uuid) {
        return null;
    }
}
