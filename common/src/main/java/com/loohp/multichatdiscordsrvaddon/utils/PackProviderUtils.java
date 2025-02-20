package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.hooks.ItemsAdderHook;
import com.loohp.multichatdiscordsrvaddon.hooks.NexoHook;
import com.loohp.multichatdiscordsrvaddon.hooks.OraxenHook;

import java.util.Locale;

public class PackProviderUtils {

    public static String getResourcePackURL() {
        String selectedProviderType = Config.i().getResources().serverResourcePackProvider().toUpperCase(Locale.ROOT);
        Debug.debug("Selected provider type: " + selectedProviderType);

        ProviderType providerType = ProviderType.valueOf(selectedProviderType);
        Debug.debug("Parsed provider type: " + providerType);

        return switch (providerType) {
            case ORAXEN -> {
                Debug.debug("Oraxen provider type.");
                yield OraxenHook.getResourcePackURL();
            }
            case NEXO -> {
                Debug.debug("Nexo provider type");
                yield NexoHook.getResourcePackURL();
            }
            case ITEMSADDER -> {
                Debug.debug("ItemsAdder provider type.");
                yield ItemsAdderHook.getResourcePackURL();
            }
            case NONE -> null;
        };

    }

}