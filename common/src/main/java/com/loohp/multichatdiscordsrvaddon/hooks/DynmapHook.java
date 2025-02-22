package com.loohp.multichatdiscordsrvaddon.hooks;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.MultiChatIntegration;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.utils.DSRVUtils;
import me.lucko.helper.Events;
import me.lucko.helper.event.filter.EventFilters;
import org.bukkit.event.EventPriority;
import org.dynmap.DynmapWebChatEvent;

public class DynmapHook {

    public void init() {
        EventPriority eventPriority = EventPriority.valueOf(Config.i().getHook().priority());

        MultiChatIntegration integration = MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration();
        if (integration != null) {
            Events.subscribe(DynmapWebChatEvent.class, eventPriority)
                    .filter(EventFilters.ignoreCancelled())
                    .handler(e -> {
                        MessageSender messageSender = new MessageSender(e.getName());
                        String filtered = integration.filter(messageSender, e.getMessage());

                        if (!filtered.isEmpty()) {
                            DSRVUtils.sendDynmapMessage(messageSender.getName(), filtered);
                        }
                    });
        }
    }

}
