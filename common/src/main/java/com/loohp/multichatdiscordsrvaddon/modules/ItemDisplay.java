/*
 * This file is part of InteractiveChat.
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

package com.loohp.multichatdiscordsrvaddon.modules;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.api.events.ItemPlaceholderEvent;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICInventoryHolder;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ItemDisplay {

    public static boolean useInventoryView(ItemStack item) {
        try {
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
                if (bsm instanceof InventoryHolder) {
                    Inventory container = ((InventoryHolder) bsm).getInventory();
                    if ((container.getSize() % 9) != 0) {
                        return false;
                    }
                    for (int i = 0; i < container.getSize(); i++) {
                        ItemStack containerItem = container.getItem(i);
                        if (containerItem != null && !containerItem.getType().equals(Material.AIR)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static Component createItemDisplay(Player player, Player receiver, Component component, long timeSent, boolean showHover, Component alternativeHover, boolean preview) throws Exception {
        ItemStack item = player.getEquipment().getItemInMainHand();
        if (item == null) item = new ItemStack(Material.AIR);

        item = MultiChatDiscordSrvAddonAPI.transformItemStack(item, receiver.getUniqueId());

        ItemPlaceholderEvent event = new ItemPlaceholderEvent(player, receiver, component, timeSent, item);
        Bukkit.getPluginManager().callEvent(event);
        item = event.getItemStack();

        return createItemDisplay(player, item, Config.i().getInventoryImage().item().itemTitle(), showHover, alternativeHover, preview);
    }

    public static Component createItemDisplay(OfflinePlayer player, ItemStack item) throws Exception {
        return createItemDisplay(player, item, Config.i().getInventoryImage().item().itemTitle(), true, null, false);
    }

    public static Component createItemDisplay(OfflinePlayer player, ItemStack item, String rawTitle, boolean showHover, Component alternativeHover, boolean preview) throws Exception {
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        if (Config.i().getSettings().hideLodestoneCompassPos()) {
            item = CompassUtils.hideLodestoneCompassPosition(item);
        }

        boolean trimmed = false;
        boolean isAir = item.getType().equals(Material.AIR);
        int itemAmount = isAir && VersionManager.version.isNewerOrEqualTo(MCVersion.V1_20_6) ? 1 : item.getAmount();
        ItemMeta itemMeta = item.getItemMeta();

        ItemStack originalItem = item.clone();

        String itemJson = ItemNBTUtils.getNMSItemStackJson(item);
        ItemStack trimmedItem = null;
        if (Config.i().getSettings().sendOriginalMessageIfExceedLengthLimit() && itemJson.length() > Config.i().getSettings().itemTagMaxLength()) {
            trimmedItem = new ItemStack(item.getType());
            trimmedItem.addUnsafeEnchantments(item.getEnchantments());
            if (itemMeta != null && itemMeta.hasDisplayName()) {
                ItemStack nameItem = trimmedItem.clone();
                Component name = NMS.getInstance().getItemStackDisplayName(item);
                NMS.getInstance().setItemStackDisplayName(nameItem, name);
                String newjson = ItemNBTUtils.getNMSItemStackJson(nameItem);
                if (newjson.length() <= Config.i().getSettings().itemTagMaxLength()) {
                    trimmedItem = nameItem;
                }
            }
            if (item.getItemMeta() != null && item.getItemMeta().hasLore()) {
                ItemStack loreItem = trimmedItem.clone();
                ItemMeta meta = loreItem.getItemMeta();
                meta.setLore(item.getItemMeta().getLore());
                loreItem.setItemMeta(meta);
                String newjson = ItemNBTUtils.getNMSItemStackJson(loreItem);
                if (newjson.length() <= Config.i().getSettings().itemTagMaxLength()) {
                    trimmedItem = loreItem;
                }
            }
            trimmed = true;
        }

        String amountString = "";
        Component itemDisplayNameComponent = ItemStackUtils.getDisplayName(item);

        amountString = String.valueOf(itemAmount);
        Key key = ItemNBTUtils.getNMSItemStackNamespacedKey(item);
        ShowItem showItem;
        if (VersionManager.version.isNewerOrEqualTo(MCVersion.V1_20_6)) {
            if (item.getType().equals(Material.AIR)) {
                showHover = false;
            }
            Map<Key, DataComponentValue> dataComponents = ItemNBTUtils.getNMSItemStackDataComponents(trimmedItem == null ? item : trimmedItem);
            showItem = dataComponents.isEmpty() ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, dataComponents);
        } else {
            String tag = ItemNBTUtils.getNMSItemStackTag(trimmedItem == null ? item : trimmedItem);
            showItem = tag == null ? ShowItem.showItem(key, itemAmount) : ShowItem.showItem(key, itemAmount, BinaryTagHolder.binaryTagHolder(tag));
        }
        HoverEvent<ShowItem> hoverEvent = HoverEvent.showItem(showItem);
        String title = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, rawTitle));
        String sha1 = HashUtils.createSha1(title, item);

        String command = null;
        boolean isMapView = false;

        if (!preview) {
            if (Config.i().getInventoryImage().item().previewMaps() && FilledMapUtils.isFilledMap(item)) {
                isMapView = true;
                if (!MultiChatDiscordSrvAddon.plugin.mapDisplay.containsKey(sha1)) {
                    MultiChatDiscordSrvAddonAPI.addMapToMapSharedList(sha1, item);
                }
            } else if (!MultiChatDiscordSrvAddon.plugin.itemDisplay.containsKey(sha1)) {
                if (useInventoryView(item)) {
                    Inventory container = ((InventoryHolder) ((BlockStateMeta) item.getItemMeta()).getBlockState()).getInventory();
                    Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, container.getSize() + 9, title);
                    ItemStack empty = Config.i().getInventoryImage().item().frame().primary().clone();
                    if (item.getType().equals(Config.i().getInventoryImage().item().frame().primary().getType())) {
                        empty = Config.i().getInventoryImage().item().frame().secondary().clone();
                    }
                    if (empty.getItemMeta() != null) {
                        ItemMeta emptyMeta = empty.getItemMeta();
                        emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                        empty.setItemMeta(emptyMeta);
                    }
                    for (int j = 0; j < 9; j++) {
                        inv.setItem(j, empty);
                    }
                    inv.setItem(4, isAir ? null : originalItem);
                    for (int j = 0; j < container.getSize(); j++) {
                        ItemStack shulkerItem = container.getItem(j);
                        if (shulkerItem != null && !shulkerItem.getType().equals(Material.AIR)) {
                            inv.setItem(j + 9, shulkerItem == null ? null : shulkerItem.clone());
                        }
                    }
                    MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.ITEM, sha1, inv);
                } else {
                    if (VersionManager.version.isOld()) {
                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, 27, title);
                        ItemStack empty = Config.i().getInventoryImage().item().frame().primary().clone();
                        if (item.getType().equals(Config.i().getInventoryImage().item().frame().primary().getType())) {
                            empty = Config.i().getInventoryImage().item().frame().secondary().clone();
                        }
                        if (empty.getItemMeta() != null) {
                            ItemMeta emptyMeta = empty.getItemMeta();
                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                            empty.setItemMeta(emptyMeta);
                        }
                        for (int j = 0; j < inv.getSize(); j++) {
                            inv.setItem(j, empty);
                        }
                        inv.setItem(13, isAir ? null : originalItem);
                        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.ITEM, sha1, inv);
                    } else {
                        Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryType.DROPPER, title);
                        ItemStack empty = Config.i().getInventoryImage().item().frame().primary().clone();
                        if (item.getType().equals(Config.i().getInventoryImage().item().frame().primary().getType())) {
                            empty = Config.i().getInventoryImage().item().frame().secondary().clone();
                        }
                        if (empty.getItemMeta() != null) {
                            ItemMeta emptyMeta = empty.getItemMeta();
                            emptyMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
                            empty.setItemMeta(emptyMeta);
                        }
                        for (int j = 0; j < inv.getSize(); j++) {
                            inv.setItem(j, empty);
                        }
                        inv.setItem(4, isAir ? null : originalItem);
                        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(MultiChatDiscordSrvAddonAPI.SharedType.ITEM, sha1, inv);
                    }
                }
            }
            command = isMapView ? "/multichat viewmap " + sha1 : "/multichat viewitem " + sha1;
        }

        if (trimmed) {
            ChatUtils.sendMessage("<red>Trimmed an item's display meta data as it's NBT exceeds the maximum characters allowed in the chat [THIS IS NOT A BUG]");
        }

        Component itemDisplayComponent = LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(player, itemAmount == 1 ? Config.i().getInventoryImage().item().embedDisplay().single() : Config.i().getInventoryImage().item().embedDisplay().multiple().replace("{Amount}", amountString))));
        itemDisplayComponent = itemDisplayComponent.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(itemDisplayNameComponent).build());
        if (showHover) {
            itemDisplayComponent = itemDisplayComponent.hoverEvent(hoverEvent);
        } else if (alternativeHover != null) {
            itemDisplayComponent = itemDisplayComponent.hoverEvent(HoverEvent.showText(alternativeHover));
        }
        if (command != null && !isAir && (isMapView)) {
            itemDisplayComponent = itemDisplayComponent.clickEvent(ClickEvent.runCommand(command));
        }
        return ComponentCompacting.optimize(itemDisplayComponent);
    }

}