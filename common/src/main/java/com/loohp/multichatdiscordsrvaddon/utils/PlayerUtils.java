package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.listeners.ICPlayerEvents;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICInventoryHolder;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlayerEquipment;
import com.loohp.multichatdiscordsrvaddon.objectholders.OfflinePlayerData;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.io.SNBTUtil;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

    public static OfflinePlayerData getData(OfflinePlayer player) {
        int selectedSlot = player.isOnline() ? player.getPlayer().getInventory().getHeldItemSlot() : 0;
        Inventory playerInventory = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45);
        Inventory enderChest = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.getDefaultEnderChestSize());
        int xp = player.isOnline() ? player.getPlayer().getExpToLevel() : 0;
        boolean isRightHanded = isRightHanded(player);
        if (!player.isOnline()) {
            try {
                File dat = new File(Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath() + "/playerdata", player.getUniqueId().toString() + ".dat");
                if (dat.exists()) {
                    NamedTag nbtData = NBTUtil.read(dat);
                    CompoundTag rootTag = (CompoundTag) nbtData.getTag();
                    selectedSlot = rootTag.getInt("SelectedItemSlot");
                    isRightHanded = !rootTag.containsKey("LeftHanded") || !rootTag.getBoolean("LeftHanded");
                    xp = rootTag.getInt("XpLevel");

                    for (CompoundTag entry : rootTag.getListTag("Inventory").asTypedList(CompoundTag.class)) {
                        int slot = entry.getByte("Slot");
                        entry.remove("Slot");
                        ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
                        if (slot == 100) {
                            slot = 36;
                        } else if (slot == 101) {
                            slot = 37;
                        } else if (slot == 102) {
                            slot = 38;
                        } else if (slot == 103) {
                            slot = 39;
                        } else if (slot == -106) {
                            slot = 40;
                        }
                        if (slot >= 0 && slot < playerInventory.getSize()) {
                            playerInventory.setItem(slot, item);
                        }
                    }
                    for (CompoundTag entry : rootTag.getListTag("EnderItems").asTypedList(CompoundTag.class)) {
                        int slot = entry.getByte("Slot");
                        entry.remove("Slot");
                        ItemStack item = ItemNBTUtils.getItemFromNBTJson(SNBTUtil.toSNBT(entry));
                        if (slot >= 0 && slot < enderChest.getSize()) {
                            enderChest.setItem(slot, item);
                        }
                    }
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        OfflinePlayerData data = new OfflinePlayerData(isRightHanded, xp, selectedSlot, playerInventory, enderChest, null);
        data.setEquipment(new ICPlayerEquipment(player, data));

        return data;
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

    public static boolean isVanished(Player player) {
        return player != null && player.hasMetadata("vanished") && player.getMetadata("vanished").getFirst().asBoolean();
    }
}
