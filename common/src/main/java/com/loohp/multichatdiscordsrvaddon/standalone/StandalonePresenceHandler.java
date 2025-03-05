package com.loohp.multichatdiscordsrvaddon.standalone;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StandalonePresenceHandler {

    private ScheduledExecutorService scheduler;

    public void start(JDA jda) {
        AtomicInteger currentPresence = new AtomicInteger();

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("MultiChatDiscordSrvAddon-Standalone-Presence-Thread-%d")
                .build();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory);
        this.scheduler.scheduleAtFixedRate(() -> {
            Config.PresenceObject object = Config.i().getStandalone().botPresence().presences().get(currentPresence.get());

            jda.getPresence().setPresence(
                    object.status(),
                    Activity.of(object.type(), object.description())
            );

            currentPresence.set((currentPresence.get() + 1) % Config.i().getStandalone().botPresence().presences().size());
        }, 0, Config.i().getStandalone().botPresence().presenceInterval(), TimeUnit.SECONDS);
    }

    public void dispose() {
        scheduler.shutdown();
    }
}
