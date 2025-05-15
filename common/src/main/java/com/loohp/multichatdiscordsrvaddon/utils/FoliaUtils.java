package com.loohp.multichatdiscordsrvaddon.utils;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class FoliaUtils {

    public static boolean folia = false;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

            folia = true;
        } catch (ClassNotFoundException ignored) {}
    }

    public static <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        MultiLib.getGlobalRegionScheduler().run(plugin, rt -> runnable.run());
        return future;
    }

    public static boolean isMainThread() {
        return !folia && Bukkit.isPrimaryThread();
    }
}
