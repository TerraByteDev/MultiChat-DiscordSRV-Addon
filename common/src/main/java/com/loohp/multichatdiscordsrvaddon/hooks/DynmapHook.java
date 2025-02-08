package com.loohp.multichatdiscordsrvaddon.hooks;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.IntegrationManager;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.integration.dynmap.DynmapSender;
import com.loohp.multichatdiscordsrvaddon.utils.DSRVUtils;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.dynmap.DynmapWebChatEvent;

public class DynmapHook {

    public void init() {
        EventPriority eventPriority = EventPriority.valueOf(Config.i().getHook().priority());
        if (eventPriority == null) throw new IllegalArgumentException("Unknown Hook event priority: " + Config.i().getHook().priority());

        MultiChatIntegration integration = MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration();
        if (integration != null) {
            Events.subscribe(DynmapWebChatEvent.class, eventPriority)
                    .filter(EventFilters.ignoreCancelled())
                    .handler(e -> {
                        DynmapSender dynmapSender = new DynmapSender(e.getName());
                        String filtered = integration.filter(dynmapSender, e.getMessage());

                        if (!filtered.isEmpty()) {
                            DSRVUtils.sendDynmapMessage(dynmapSender.getName(), filtered);
                        }
                    });
        }
    }

}
