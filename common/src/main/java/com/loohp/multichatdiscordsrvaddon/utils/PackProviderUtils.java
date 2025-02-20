package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.hooks.ItemsAdderHook;
import com.loohp.multichatdiscordsrvaddon.hooks.NexoHook;
import com.loohp.multichatdiscordsrvaddon.hooks.OraxenHook;

import java.util.Locale;

public class PackProviderUtils {

    public static String getResourcePackURL() {
        String selectedProviderType = Config.i().getResources().serverResourcePackProvider().toUpperCase(Locale.ROOT);

        ProviderType providerType = ProviderType.valueOf(selectedProviderType);
        if (providerType == null) throw new IllegalArgumentException("Unknown provider type: " + selectedProviderType + "! Please check your selected-resource-pack-provider config in the resources section.");

        return switch (providerType) {
            case ORAXEN -> OraxenHook.getResourcePackURL();
            case NEXO -> NexoHook.getResourcePackURL();
            case ITEMSADDER -> ItemsAdderHook.getResourcePackURL();
            default -> null;
        };
    }

}