package com.loohp.multichatdiscordsrvaddon.hooks;

import io.th0rgal.oraxen.OraxenPlugin;
import org.bukkit.Bukkit;

public class OraxenHook {

    public static String getResourcePackURL() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Nexo")) throw new IllegalStateException("Attempted to fetch Nexo resource pack URL when Nexo is not enabled on the server!");
        return OraxenPlugin.get().getUploadManager().getHostingProvider().getPackURL();
    }
}
