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

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * This event is called after the plugin translate an item placeholder.<br>
 * hasInventory() is true if this item is a container (like a shulker box)<br>
 * getInventory() is null if hasInventory() is false<br>
 * setInventory() accepts null as value<br>
 * You can change the contents of the inventory/item in this event.
 *
 * @author LOOHP
 */
@Getter
@Setter
public class GameMessageProcessItemEvent extends GameMessageProcessEvent {

    private ItemStack itemstack;
    private Inventory inventory;

    public GameMessageProcessItemEvent(OfflinePlayer sender, String title, Component component, boolean cancel, int processId, ItemStack itemstack, Inventory inventory) {
        super(sender, title, component, cancel, processId);
        this.itemstack = itemstack;
        this.inventory = inventory;
    }

    public GameMessageProcessItemEvent(OfflinePlayer sender, String title, Component component, boolean cancel, int processId, ItemStack itemstack) {
        this(sender, title, component, cancel, processId, itemstack, null);
    }

    public ItemStack getItemStack() {
        return itemstack;
    }

    public void setItemStack(ItemStack itemstack) {
        this.itemstack = itemstack;
    }

    public boolean hasInventory() {
        return inventory != null;
    }

}
