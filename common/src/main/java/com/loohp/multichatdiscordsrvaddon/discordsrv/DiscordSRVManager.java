package com.loohp.multichatdiscordsrvaddon.discordsrv;

import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.listeners.discordsrv.*;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordSRVManager {

    public static final List<Permission> requiredPermissions = Collections.unmodifiableList(Arrays.asList(
            Permission.MESSAGE_READ,
            Permission.MESSAGE_WRITE,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MANAGE_WEBHOOKS
    ));
    public static DiscordSRV discordsrv;
    public static ListenerPriority gameToDiscordPriority = ListenerPriority.HIGHEST;
    public static ListenerPriority ventureChatToDiscordPriority = ListenerPriority.HIGHEST;
    public static ListenerPriority discordToGamePriority = ListenerPriority.HIGH;
    public static InboundToGameEvents inboundToGameEvents;

    public static void onLoad() {
        DiscordSRV.api.requireIntent(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        DiscordSRV.api.subscribe(new DiscordCommandEvents());
    }

    public static void enable() {
        discordsrv = DiscordSRV.getPlugin();

        inboundToGameEvents = new InboundToGameEvents();
        DiscordSRV.api.subscribe(inboundToGameEvents);
        DiscordSRV.api.subscribe(new DiscordReadyEvents());
        DiscordSRV.api.subscribe(new LegacyDiscordCommandEvents());
        DiscordSRV.api.subscribe(new OutboundToDiscordEvents());

        Bukkit.getServer().getPluginManager().registerEvents(new InboundToGameEvents(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new OutboundToDiscordEvents(), this);

        discordsrv.reloadRegexes();
        DiscordProviderManager.setInstance(new DiscordSRVDiscordProvider());
    }

}
