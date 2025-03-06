package com.loohp.multichatdiscordsrvaddon.standalone.linking;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;

public class StandaloneLinkMessageHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.PRIVATE) return;

        Debug.debug("Triggered on(Direct)MessageReceived");

        String code = event.getMessage().getContentRaw();
        UUID codeOwner = MultiChatDiscordSrvAddon.plugin.standaloneManager.getLinkManager().getCodeOwner(code);
        if (codeOwner == null) {
            event.getMessage().reply(Config.i().getMessages().unknownCode()).queue();
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(codeOwner);
            if (!offlinePlayer.hasPlayedBefore()) {
                event.getMessage().reply("An unexpected error occurred. Please try again later.").queue();
                throw new IllegalStateException("Attempted to link member to UUID [" + codeOwner + "] but OfflinePlayer has not played before!");
            }

            User user = event.getAuthor();
            MultiChatDiscordSrvAddon.plugin.standaloneManager.getLinkManager().getDatabase().saveLinkedUser(new LinkedUser(
                    user.getId(),
                    codeOwner
            ));

            event.getMessage().reply(Config.i().getMessages().discordLinkSuccess().replace("%linked_player_name%", Objects.requireNonNull(offlinePlayer.getName()))).queue();
            if (offlinePlayer.isOnline()) {
                ChatUtils.sendMessage(
                        Config.i().getMessages().inGameLinkSuccess()
                                .replace("%linked_member_name%", user.getName()),
                        offlinePlayer.getPlayer()
                );
            }
        }
    }
}
