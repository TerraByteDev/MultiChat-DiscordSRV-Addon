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

package com.loohp.multichatdiscordsrvaddon.api;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.listeners.InboundToGameEvents;
import com.loohp.multichatdiscordsrvaddon.listeners.InboundToGameEvents.DiscordAttachmentData;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlaceholder;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import com.loohp.multichatdiscordsrvaddon.resources.ResourceManager;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MultiChatDiscordSrvAddonAPI {

    /**
     * Whether the plugin is ready
     *
     * @return true/false
     */
    public static boolean isReady() {
        return MultiChatDiscordSrvAddon.isReady;
    }

    /**
     * Get the current active resource manager<br>
     * A new instance is created whenever the plugin is reloaded<br>
     * Null will be returned if the plugin had yet to finish setting up, or when there is an error.
     *
     * @return the current resource manager or null
     */
    public static ResourceManager getCurrentResourceManager() {
        return MultiChatDiscordSrvAddon.plugin.isResourceManagerReady() ? MultiChatDiscordSrvAddon.plugin.getResourceManager() : null;
    }

    /**
     * Add a map to the shared map list
     *
     * @param hash key
     * @param item item to add to the map
     * @return The hashed key which can be used to retrieve the inventory
     */
    public static String addMapToMapSharedList(String hash, ItemStack item) {
        MultiChatDiscordSrvAddon.plugin.mapDisplay.put(hash, item);
        return hash;
    }

    /**
     * Get all active discord attachments
     *
     * @return A mapping of the assigned UUID to the discord attachments
     */
    public static Map<UUID, DiscordAttachmentData> getActiveDiscordAttachments() {
        return Collections.unmodifiableMap(InboundToGameEvents.DATA);
    }

    /**
     * Get all active image preview maps
     *
     * @return A mapping of currently viewing players to the image preview maps
     */
    public static Map<Player, GraphicsToPacketMapWrapper> getActivePlayerImageMapViews() {
        return Collections.unmodifiableMap(InboundToGameEvents.MAP_VIEWERS);
    }

    /**
     * Get the preview image map by the assigned uuid
     *
     * @param uuid the uuid of the image wrapper
     * @return The image preview map (Could be null)
     */
    public static GraphicsToPacketMapWrapper getDiscordImageWrapperByUUID(UUID uuid) {
        Optional<DiscordAttachmentData> opt = InboundToGameEvents.DATA.values().stream().filter(each -> each.getUniqueId().equals(uuid)).findFirst();
        DiscordAttachmentData data;
        if (opt.isPresent() && (data = opt.get()).isImage()) {
            return data.getImageMap();
        } else {
            return null;
        }
    }

    public static ItemStack transformItemStack(ItemStack itemStack, UUID uuid) {
        return MultiChatDiscordSrvAddon.itemStackTransformFunctions.values().stream()
                .sorted(Comparator.comparing(each -> each.getFirst()))
                .map(each -> each.getSecond())
                .reduce((a, b) -> (i, u) -> b.apply(a.apply(i, u), u))
                .map(function -> function.apply(itemStack, uuid))
                .orElse(itemStack);
    }

    @Getter
    public enum SharedType {

        ITEM(0),
        INVENTORY(1),
        INVENTORY1_UPPER(2),
        INVENTORY1_LOWER(3),
        ENDERCHEST(4);

        private static final Map<Integer, SharedType> MAPPINGS = new HashMap<>();

        static {
            for (SharedType type : values()) {
                MAPPINGS.put(type.getValue(), type);
            }
        }

        public static SharedType fromValue(int value) {
            return MAPPINGS.get(value);
        }

        private final int value;

        SharedType(int value) {
            this.value = value;
        }

    }

    /**
     * Add an inventory to the shared inventory list
     *
     * @param type {@link SharedType} type
     * @param hash key
     * @param inventory relevant inventory
     * @return The hashed key which can be used to retrieve the inventory
     */
    public static String addInventoryToItemShareList(SharedType type, String hash, Inventory inventory) {
        switch (type) {
            case ITEM:
                MultiChatDiscordSrvAddon.plugin.itemDisplay.put(hash, inventory);
                MultiChatDiscordSrvAddon.plugin.upperSharedInventory.add(inventory);
                break;
            case INVENTORY:
                MultiChatDiscordSrvAddon.plugin.inventoryDisplay.put(hash, inventory);
                MultiChatDiscordSrvAddon.plugin.upperSharedInventory.add(inventory);
                break;
            case INVENTORY1_UPPER:
                MultiChatDiscordSrvAddon.plugin.inventoryDisplay1Upper.put(hash, inventory);
                MultiChatDiscordSrvAddon.plugin.upperSharedInventory.add(inventory);
                break;
            case INVENTORY1_LOWER:
                MultiChatDiscordSrvAddon.plugin.inventoryDisplay1Lower.put(hash, inventory);
                MultiChatDiscordSrvAddon.plugin.lowerSharedInventory.add(inventory);
                break;
            case ENDERCHEST:
                MultiChatDiscordSrvAddon.plugin.enderDisplay.put(hash, inventory);
                MultiChatDiscordSrvAddon.plugin.upperSharedInventory.add(inventory);
                break;
        }
        return hash;
    }

    /**
     * Get the placeholder list.
     *
     * @return The placeholder list
     */
    public static Collection<List<ICPlaceholder>> getPlaceholderList() {
        return new ArrayList<>(MultiChatDiscordSrvAddon.placeholderList.values());
    }

    public static CompletableFuture<List<ValueTrios<UUID, String, Integer>>> getBungeecordPlayerList() {
        CompletableFuture<List<ValueTrios<UUID, String, Integer>>> future = new CompletableFuture<>();
        try {
            BungeeMessageSender.requestBungeePlayerlist(System.currentTimeMillis(), future);
        } catch (Exception error) {
            future.completeExceptionally(error);
        }

        return future;
    }

}
