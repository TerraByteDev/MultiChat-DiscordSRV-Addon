package com.loohp.multichatdiscordsrvaddon.objectholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.Inventory;

@AllArgsConstructor
@Getter
@Setter
public class OfflinePlayerData {

    private boolean rightHanded;
    private int xpLevel;
    private int selectedSlot;
    private Inventory inventory;
    private Inventory enderChest;
    private ICPlayerEquipment equipment;

}
