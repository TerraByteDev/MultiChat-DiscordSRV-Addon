package com.loohp.multichatdiscordsrvaddon.command;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessingContext;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.services.type.ConsumerService;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    public static final CooldownManager INSTANCE = new CooldownManager();

    private final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>();
    private final long cooldownMillis = Config.i().getSettings().cooldown();

    /**
     * Checks if the player is on cooldown and handles it accordingly.
     *
     * @param player The player to check and manage cooldown for.
     * @return True if the player is on cooldown, false otherwise.
     */
    public boolean check(@NotNull Player player) {
        if (player.hasPermission("multichatdiscordsrv.cooldown.bypass")) return false;

        UUID playerUUID = player.getUniqueId();
        Instant now = Instant.now();
        Instant cooldownEnd = cooldowns.get(playerUUID);

        if (cooldownEnd != null && now.isBefore(cooldownEnd)) {
            long secondsLeft = Duration.between(now, cooldownEnd).getSeconds();
            ChatUtils.sendMessage(
                    Config.i().getMessages().onCooldown()
                            .replace("%cooldown%", String.valueOf(secondsLeft)),
                    player
            );
            return true;
        }

        cooldowns.put(playerUUID, now.plusMillis(cooldownMillis));
        return false;
    }

    /**
     * Removes the player's cooldown entry from the map.
     *
     * @param player The player whose cooldown is to be removed. Must not be null.
     */
    public void remove(@NotNull Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    public static final class CooldownPostProcessor<C> implements CommandPostprocessor<C> {

        @Override
        public void accept(@NonNull CommandPostprocessingContext<C> cCommandPostprocessingContext) {
            if (cCommandPostprocessingContext instanceof Player player) {
                if (INSTANCE.check(player)) {
                    ConsumerService.interrupt();
                } else INSTANCE.remove(player);
            }
        }

    }
}
