package com.loohp.multichatdiscordsrvaddon;

import com.loohp.multichatdiscordsrvaddon.utils.MCVersion;
import org.bukkit.Bukkit;

public class VersionManager {

    public static MCVersion version;
    public static String exactMinecraftVersion;

    public static void init() {
        exactMinecraftVersion = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(") + 5, Bukkit.getVersion().indexOf(")"));
        version = MCVersion.resolve();
    }
}
