package com.loohp.multichatdiscordsrvaddon.provider;

import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;

import java.util.UUID;

public interface DiscordProvider {

    String getUserAsMention(UUID playerUUID);

    String getUsername(LinkedUser user);

    String getBotUsername();

    LinkedUser getLinkedUser(UUID uuid);

    LinkedUser getLinkedUser(String id);

    int getChannelCount();

    int getMemberCount();

    int getServerCount();

}
