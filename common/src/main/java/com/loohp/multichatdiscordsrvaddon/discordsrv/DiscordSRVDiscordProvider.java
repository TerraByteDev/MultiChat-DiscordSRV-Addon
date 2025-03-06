package com.loohp.multichatdiscordsrvaddon.discordsrv;

import com.loohp.multichatdiscordsrvaddon.provider.DiscordProvider;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DiscordSRVDiscordProvider implements DiscordProvider {
    @Override
    public String getUserAsMention(UUID playerUUID) {
        User user = getUser(playerUUID);
        return user != null ? user.getAsMention() : "";
    }

    @Override
    public String getUsername(LinkedUser user) {
        User jdaUser = DiscordSRVManager.discordsrv.getJda().getUserById(user.getDiscordID());
        return jdaUser != null ? jdaUser.getName() : null;
    }

    @Override
    public String getBotUsername() {
        JDA jda = DiscordSRVManager.discordsrv.getJda();
        return jda != null ? jda.getSelfUser().getName() : "";
    }

    @Override
    public LinkedUser getLinkedUser(UUID uuid) {
        User user = getUser(uuid);
        return user != null ? new LinkedUser(user.getId(), uuid) : null;
    }

    @Override
    public LinkedUser getLinkedUser(String id) {
        UUID uuid = DiscordSRVManager.discordsrv.getAccountLinkManager().getUuid(id);
        return uuid != null ? new LinkedUser(id, uuid) : null;
    }

    @Override
    public int getChannelCount() {
        JDA jda = DiscordSRVManager.discordsrv.getJda();

        return jda != null ? jda.getGuilds().size() : 0;
    }

    @Override
    public int getMemberCount() {
        JDA jda = DiscordSRVManager.discordsrv.getJda();

        return jda != null ? jda.getUsers().size() : 0;
    }

    @Override
    public int getServerCount() {
        JDA jda = DiscordSRVManager.discordsrv.getJda();

        return jda != null ? jda.getGuilds().stream().mapToInt(each -> each.getChannels().size()).sum() : 0;
    }

    @Nullable
    private User getUser(UUID uuid) {
        String userID = DiscordSRVManager.discordsrv.getAccountLinkManager().getDiscordId(uuid);
        return userID != null ? DiscordSRVManager.discordsrv.getJda().getUserById(userID) : null;
    }
}
