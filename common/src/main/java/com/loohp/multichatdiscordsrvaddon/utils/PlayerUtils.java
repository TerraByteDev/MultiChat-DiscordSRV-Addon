package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.listeners.ICPlayerEvents;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PlayerUtils {

    public static boolean isRightHanded(OfflinePlayer player) {
        if (!player.isOnline()) return false;
        return player.getPlayer().getMainHand() == MainHand.RIGHT;
    }

    public static boolean isLeftHanded(OfflinePlayer player) {
        if (!player.isOnline()) return false;
        return player.getPlayer().getMainHand() == MainHand.LEFT;
    }

    public static boolean isLocal(OfflinePlayer player) {
        return player.isOnline() && Bukkit.getPlayer(player.getUniqueId()) != null;
    }

    public static int getSelectedItemSlot(OfflinePlayer player) {
        int selectedSlot = 0;
        if (player.isOnline()) {
            try {
                File dat = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/playerdata", player.getUniqueId().toString() + ".dat");
                if (dat.exists()) {
                    NamedTag nbtData = NBTUtil.read(dat);
                    CompoundTag rootTag = (CompoundTag) nbtData.getTag();
                    selectedSlot = rootTag.getInt("SelectedItemSlot");
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        return selectedSlot;
    }

    public static Object getProperty(OfflinePlayer player, String key) {
        Map<String, Object> properties = ICPlayerEvents.CACHED_PROPERTIES.get(player.getUniqueId());
        if (properties != null) return properties.get(key);
            else return null;
    }

    public static ItemStack getMainHandItem(OfflinePlayer player) {
        if (player.isOnline()) {
            return player.getPlayer().getInventory().getItemInMainHand();
        }
        return null;
    }

    public static boolean hasPermission(UUID uuid, String node) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.hasPermission(node);
    }
}
