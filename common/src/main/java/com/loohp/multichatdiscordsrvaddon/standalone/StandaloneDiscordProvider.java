package com.loohp.multichatdiscordsrvaddon.standalone;

import com.loohp.multichatdiscordsrvaddon.provider.DiscordProvider;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.util.UUID;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

public class StandaloneDiscordProvider implements DiscordProvider {

    @Override
    public String getUserAsMention(UUID playerUUID) {
        User user = plugin.standaloneManager.getJda().getUserById(getLinkedUser(playerUUID).getDiscordID());
        if (user != null) {
            return user.getAsMention();
        } else return "";
    }

    @Override
    public String getUsername(LinkedUser user) {
        User jdaUser = plugin.standaloneManager.getJda().getUserById(user.getDiscordID());
        return jdaUser != null ? jdaUser.getName() : null;
    }

    @Override
    public String getBotUsername() {
        JDA jda = plugin.standaloneManager.getJda();
        return jda != null ? jda.getSelfUser().getName() : "";
    }

    @Override
    public LinkedUser getLinkedUser(UUID uuid) {
        return plugin.standaloneManager.getLinkManager().getDatabase().getLinkedUserByUuid(uuid).join();
    }

    @Override
    public LinkedUser getLinkedUser(String discordID) {
        return plugin.standaloneManager.getLinkManager().getDatabase().getLinkedUserById(discordID).join();
    }

    @Override
    public int getChannelCount() {
        JDA jda = plugin.standaloneManager.getJda();

        return jda != null ? jda.getGuilds().stream().mapToInt(each -> each.getChannels().size()).sum() : 0;
    }

    @Override
    public int getMemberCount() {
        JDA jda = plugin.standaloneManager.getJda();

        return jda != null ? jda.getUsers().size() : 0;
    }

    @Override
    public int getServerCount() {
        JDA jda = plugin.standaloneManager.getJda();

        return jda != null ? jda.getGuilds().size() : 0;
    }
}
