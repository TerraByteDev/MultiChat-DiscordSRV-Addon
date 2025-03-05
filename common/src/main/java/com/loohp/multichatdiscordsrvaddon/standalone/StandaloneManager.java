package com.loohp.multichatdiscordsrvaddon.standalone;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.time.Duration;

public class StandaloneManager {

    private JDA jda;
    @Getter
    private TextChannel textChannel;

    private StandalonePresenceHandler presenceHandler;

    public void initialise() {
        try {
            this.jda = JDABuilder.createDefault(Config.i().getStandalone().token())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_WEBHOOKS)
                    .build();

            this.jda.awaitReady();

            fetchGlobalChannel();

            if (Config.i().getStandalone().botPresence().enabled()) {
                this.presenceHandler = new StandalonePresenceHandler();
                this.presenceHandler.start(this.jda);
            }
        } catch (InterruptedException exception) {
            ChatUtils.sendMessage("Failed to initialise MultiChatDiscordSRVAddon Standalone implementation!");
            exception.printStackTrace();
        }
    }

    public void dispose() {
        if (presenceHandler != null) presenceHandler.dispose();

        try {
            this.jda.shutdown();
            if (!this.jda.awaitShutdown(Duration.ofSeconds(10))) {
                this.jda.shutdownNow();
            }
        } catch (InterruptedException exception) {
            ChatUtils.sendMessage("<red>Failed to shutdown Standalone bot!");
            exception.printStackTrace();
        }
    }

    private void fetchGlobalChannel() {
        String channelID = Config.i().getStandalone().channelID();

        this.textChannel = this.jda.getTextChannelById(channelID);
        if (textChannel == null) {
            throw new IllegalArgumentException("Invalid Discord chat channel ID: " + channelID);
        }
    }
}
