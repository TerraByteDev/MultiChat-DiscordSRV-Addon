package com.loohp.multichatdiscordsrvaddon.provider;

public class DiscordProviderManager {
    private static DiscordProvider discordProvider;

    public static DiscordProvider get() {
        if (discordProvider == null) throw new IllegalStateException("Attempted to fetch DiscordProvider before it was registered!");

        return discordProvider;
    }

    public static void setInstance(DiscordProvider instance) {
        if (discordProvider != null) throw new IllegalStateException("Attempted to set DiscordProvider instance when one is already set!");

        discordProvider = instance;
    }
}
