/*
 * This file is part of InteractiveChatDiscordSrvAddon.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.multichatdiscordsrvaddon.api.events;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;

/**
 * This event is called after the plugin translate a normal inventory
 * placeholder. (Currently only used by the enderchest) You can change the
 * contents of the inventory/item in this event.
 * <p>
 * For player inventories, please use GameMessageProcessPlayerInventoryEvent
 *
 * @author LOOHP
 */
public class GameMessageProcessInventoryEvent extends GameMessageProcessEvent {

    private Inventory inventory;

    public GameMessageProcessInventoryEvent(OfflinePlayer sender, String title, Component component, boolean cancel, int processId,
                                            Inventory inventory) {
        super(sender, title, component, cancel, processId);
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

}
