package com.loohp.multichatdiscordsrvaddon.hooks;

import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.nexomc.nexo.NexoPlugin;
import org.bukkit.Bukkit;

public class NexoHook {

    public static String getResourcePackURL() {
        Debug.debug("Fetching Nexo resourcepack URL...");

        if (!Bukkit.getPluginManager().isPluginEnabled("oraxen")) throw new IllegalStateException("Attempted to fetch Oraxen resource pack URL when Oraxen is not enabled on the server!");
        return NexoPlugin.instance().packServer().packUrl();
    }

}
