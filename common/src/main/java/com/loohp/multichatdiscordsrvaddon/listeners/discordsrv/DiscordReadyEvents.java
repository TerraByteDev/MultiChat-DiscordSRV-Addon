/*
 * This file is part of InteractiveChatDiscordSrvAddon.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.multichatdiscordsrvaddon.listeners.discordsrv;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVManager;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.GuildChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import org.bukkit.Bukkit;

import java.util.Locale;

public class DiscordReadyEvents {

    private volatile boolean init;

    public DiscordReadyEvents() {
        init = false;
        if (DiscordSRV.isReady) {
            init = true;
            ready();
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onDiscordReady(DiscordReadyEvent event) {
        if (!init) {
            init = true;
            ready();
        }
    }

    public void ready() {
        Debug.debug("Triggering discord ready...");
        DiscordSRV discordsrv = DiscordSRVManager.discordsrv;
        JDA jda = discordsrv.getJda();
        jda.addEventListener(new OutboundToDiscordEvents.JDAEvents());
        jda.addEventListener(new DiscordInteractionEvents());

        DiscordCommands discordCommands = new DiscordCommands(discordsrv);
        discordCommands.init();
        Bukkit.getPluginManager().registerEvents(discordCommands, MultiChatDiscordSrvAddon.plugin);
        DiscordSRV.api.addSlashCommandProvider(discordCommands);
        discordCommands.reload();

        PacketListenerPriority priority = PacketListenerPriority.valueOf(Config.i().getDiscordAttachments().priority().toUpperCase(Locale.ROOT));
        PacketEvents.getAPI().getEventManager().registerListener(discordCommands, priority);
        PacketEvents.getAPI().getEventManager().registerListener(DiscordSRVManager.inboundToGameEvents, priority);

        for (String channelId : discordsrv.getChannels().values()) {
            if (channelId != null) {
                try {
                    GuildChannel channel = jda.getGuildChannelById(channelId);
                    if (channel != null) {
                        Guild guild = channel.getGuild();
                        Member self = guild.getMember(jda.getSelfUser());
                        for (Permission permission : DiscordSRVManager.requiredPermissions) {
                            if (!self.hasPermission(channel, permission)) {
                                ChatUtils.sendMessage("<red>The bot used for DiscordSRV is missing the <yellow>" + permission.getName() + "<red> permission in the channel <yellow>" + channel.getName() + "<red> (Id: <yellow>" + channel.getId() + "<red>)");
                            }
                        }
                    }
                } catch (Exception e) {
                    new RuntimeException("Error when getting guild from channelId (" + channelId + ")", e).printStackTrace();
                }
            }
        }
    }

}
