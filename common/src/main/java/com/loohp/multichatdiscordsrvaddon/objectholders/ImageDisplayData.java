/*
 * This file is part of InteractiveChatDiscordSrvAddon2.
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

package com.loohp.multichatdiscordsrvaddon.objectholders;

import com.loohp.multichatdiscordsrvaddon.utils.BookUtils;
import com.loohp.multichatdiscordsrvaddon.utils.FilledMapUtils;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;


@Getter
public class ImageDisplayData extends DiscordDisplayData {

    private final String title;
    private final ImageDisplayType type;
    private final TitledInventoryWrapper inventory;
    private final boolean isPlayerInventory;
    private final ItemStack item;
    private final boolean isFilledMap;
    private final boolean isBook;

    private ImageDisplayData(OfflinePlayer player, int position, String title, ImageDisplayType type, TitledInventoryWrapper inventory, boolean isPlayerInventory, ItemStack item, boolean isFilledMap, boolean isBook) {
        super(player, position);
        this.type = type;
        this.title = title;
        this.inventory = inventory;
        this.isPlayerInventory = isPlayerInventory;
        this.item = item;
        this.isFilledMap = isFilledMap;
        this.isBook = isBook;
    }

    public ImageDisplayData(OfflinePlayer player, int position, String title, ImageDisplayType type, TitledInventoryWrapper inventory) {
        this(player, position, title, type, inventory, false, null, false, false);
    }

    public ImageDisplayData(OfflinePlayer player, int position, String title, ImageDisplayType type, boolean isPlayerInventory, TitledInventoryWrapper inventory) {
        this(player, position, title, type, inventory, isPlayerInventory, null, false, false);
    }

    public ImageDisplayData(OfflinePlayer player, int position, String title, ImageDisplayType type, ItemStack itemstack) {
        this(player, position, title, type, null, false, itemstack, FilledMapUtils.isFilledMap(itemstack), BookUtils.isTextBook(itemstack));
    }

    public ImageDisplayData(OfflinePlayer player, int position, String title, ImageDisplayType type, ItemStack itemstack, TitledInventoryWrapper inventory) {
        this(player, position, title, type, inventory, false, itemstack, FilledMapUtils.isFilledMap(itemstack), BookUtils.isTextBook(itemstack));
    }

    public boolean isPlayerInventory() {
        return isPlayerInventory;
    }

    public boolean isFilledMap() {
        return isFilledMap;
    }

    public boolean isBook() {
        return isBook;
    }

}