package com.loohp.multichatdiscordsrvaddon.bungee;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICInventoryHolder;
import com.loohp.multichatdiscordsrvaddon.objectholders.OfflinePlayerData;
import com.loohp.multichatdiscordsrvaddon.objectholders.RemoteMCPlayer;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValueTrios;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;

public class BungeeMessageListener implements PluginMessageListener {

    private final Map<Integer, byte[][]> incoming;
    private final Map<UUID, CompletableFuture<?>> toComplete = new ConcurrentHashMap<>();

    public BungeeMessageListener() {
        Cache<Integer, byte[][]> incomingCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
        incoming = incomingCache.asMap();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] bytes) {
        if (!channel.equalsIgnoreCase("interchat:main")) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            int packetNumber = input.readInt();
            int packetChunkIndex = input.readInt();
            int packetChunkSize = input.readInt();
            int packetId = input.readShort();
            byte[] data = new byte[bytes.length - 14];
            input.readFully(data);

            byte[][] chunks = incoming.remove(packetNumber);
            if (chunks == null) chunks = new byte[packetChunkSize][];

            if (chunks.length != packetChunkSize) {
                byte[][] adjusted = new byte[packetChunkSize][];
                System.arraycopy(chunks, 0, adjusted, 0, adjusted.length);
                chunks = adjusted;
            }

            if (packetChunkIndex >= 0 && packetChunkIndex < chunks.length) {
                chunks[packetChunkIndex] = data;
            }

            if (CustomArrayUtils.anyNull(chunks)) {
                incoming.put(packetNumber, chunks);
                return;
            }

            data = new byte[Arrays.stream(chunks).mapToInt(a -> a.length).sum()];
            for (int i = 0, pos = 0; i < chunks.length; i++) {
                byte[] chunk = chunks[i];
                System.arraycopy(chunk, 0, data, pos, chunk.length);
                pos += chunk.length;
            }

            if (Config.i().getDebug().pluginMessagePacketVerbose()) {
                ChatUtils.sendMessage("<grey>MC Message Inbound - <yellow>ID " + packetId + " <grey>via <yellow>" + player.getName());
            }

            try {
                ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
                switch (packetId) {
                    case 0x00:
                        int playerAmount = dataInput.readInt();
                        Set<UUID> localUUIDs = Bukkit.getOnlinePlayers().stream().map(each -> each.getUniqueId()).collect(Collectors.toSet());
                        Map<UUID, RemoteMCPlayer> current = PlayerUtils.getRemoteUsers();
                        Map<UUID, RemoteMCPlayer> newSet = new ConcurrentHashMap<>();

                        for (int i = 0; i < playerAmount; i++) {
                            String server = DataTypeIO.readString(dataInput, StandardCharsets.UTF_8);
                            UUID uuid = DataTypeIO.readUUID(dataInput);
                            String name = DataTypeIO.readString(dataInput, StandardCharsets.UTF_8);

                            RemoteMCPlayer remoteMCPlayer = PlayerUtils.getMCPlayer(uuid);
                            if (remoteMCPlayer != null) {
                                if (!remoteMCPlayer.getServer().equals(server)) {
                                    remoteMCPlayer.setServer(server);
                                }
                            }
                            if (!localUUIDs.contains(uuid) && !PlayerUtils.getRemoteUsers().containsKey(uuid)) {
                                remoteMCPlayer = PlayerUtils.createOrUpdateRemotePlayer(server, name, uuid, true, 0, 0, Bukkit.createInventory(ICInventoryHolder.INSTANCE, 45), Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.getDefaultEnderChestSize()), false);
                            }
                            newSet.put(uuid, remoteMCPlayer);
                        }

                        newSet.keySet().forEach(current::remove);
                        for (UUID uuid : current.keySet()) {
                            PlayerUtils.removeRemoteMCPlayer(uuid);
                        }
                        for (UUID uuid : localUUIDs) {
                            PlayerUtils.removeRemoteMCPlayer(uuid);
                        }

                        break;

                    case 0x01:
                        int delay = input.readInt();
                        short itemStackScheme = input.readShort();
                        short inventoryScheme = input.readShort();
                        MultiChatDiscordSrvAddon.remoteDelay = delay;
                        BungeeMessageSender.itemStackScheme = itemStackScheme;
                        BungeeMessageSender.inventoryScheme = inventoryScheme;

                    case 0x03:
                        UUID uuid = DataTypeIO.readUUID(input);
                        RemoteMCPlayer mcPlayer = PlayerUtils.getMCPlayer(uuid);
                        if (mcPlayer == null) break;
                        OfflinePlayerData offlinePlayerData = mcPlayer.getOfflinePlayerData();

                        boolean rightHanded = input.readBoolean();
                        offlinePlayerData.setRightHanded(rightHanded);
                        int selectedSlot = input.readByte();
                        offlinePlayerData.setSelectedSlot(selectedSlot);
                        int level = input.readInt();
                        offlinePlayerData.setXpLevel(level);

                        int size = input.readByte();
                        ItemStack[] equipment = new ItemStack[size];
                        for (int i = 0; i < equipment.length; i++) {
                            equipment[i] = DataTypeIO.readItemStack(input, StandardCharsets.UTF_8);
                        }
                        offlinePlayerData.getEquipment().setHelmet(equipment[0]);
                        offlinePlayerData.getEquipment().setChestplate(equipment[1]);
                        offlinePlayerData.getEquipment().setLeggings(equipment[2]);
                        offlinePlayerData.getEquipment().setBoots(equipment[3]);
                        if (VersionManager.version.isOld()) {
                            offlinePlayerData.getEquipment().setItemInHand(equipment[4]);
                        } else {
                            offlinePlayerData.getEquipment().setItemInMainHand(equipment[4]);
                            offlinePlayerData.getEquipment().setItemInOffHand(equipment[5]);
                        }

                        mcPlayer.setOfflinePlayerData(offlinePlayerData);
                        break;

                    case 0x04:
                        UUID uuid1 = DataTypeIO.readUUID(input);
                        RemoteMCPlayer mcPlayer1 = PlayerUtils.getMCPlayer(uuid1);
                        if (mcPlayer1 == null) break;

                        OfflinePlayerData offlinePlayerData1 = mcPlayer1.getOfflinePlayerData();
                        boolean rightHanded1 = input.readBoolean();
                        offlinePlayerData1.setRightHanded(rightHanded1);
                        int selectedSlot1 = input.readByte();
                        offlinePlayerData1.setSelectedSlot(selectedSlot1);
                        int level1 = input.readInt();
                        offlinePlayerData1.setXpLevel(level1);
                        int type = input.readByte();
                        if (type == 0) {
                            offlinePlayerData1.setInventory(DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null));
                        } else {
                            offlinePlayerData1.setEnderChest(DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null));
                        }
                        mcPlayer1.setOfflinePlayerData(offlinePlayerData1);

                        break;

                    case 0x05:
                        UUID uuid2 = DataTypeIO.readUUID(input);
                        RemoteMCPlayer mcPlayer2 = PlayerUtils.getMCPlayer(uuid2);
                        if (mcPlayer2 == null) break;

                        int size1 = input.readInt();
                        for (int i = 0; i < size1; i++) {
                            String placeholder = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            String text = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                            mcPlayer2.getRemotePlaceholders().put(placeholder, text);
                        }
                        break;

                    case 0x0E:
                        MultiChatDiscordSrvAddonAPI.SharedType sharedType = MultiChatDiscordSrvAddonAPI.SharedType.fromValue(input.readByte());
                        String sha1 = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        Inventory inventory = DataTypeIO.readInventory(input, StandardCharsets.UTF_8, null);
                        MultiChatDiscordSrvAddonAPI.addInventoryToItemShareList(sharedType, sha1, inventory);
                        break;

                    case 0x0F:
                        int requestType = input.readByte();
                        UUID playerUUID2 = DataTypeIO.readUUID(input);
                        Player player1 = Bukkit.getPlayer(playerUUID2);
                        if (player1 != null) {
                            RemoteMCPlayer mcPlayer3 = PlayerUtils.getMCPlayer(player1.getUniqueId());
                            OfflinePlayerData offlinePlayerData2 = mcPlayer3.getOfflinePlayerData();
                            switch (requestType) {
                                case 0:
                                    BungeeMessageSender.forwardInventory(System.currentTimeMillis(), player1.getUniqueId(), offlinePlayerData2.isRightHanded(), offlinePlayerData2.getSelectedSlot(), offlinePlayerData2.getXpLevel(), null, offlinePlayerData2.getInventory());
                                    break;

                                case 1:
                                    BungeeMessageSender.forwardInventory(System.currentTimeMillis(), player1.getUniqueId(), offlinePlayerData2.isRightHanded(), offlinePlayerData2.getSelectedSlot(), offlinePlayerData2.getXpLevel(), null, offlinePlayerData2.getEnderChest());
                                    break;

                            }
                        }
                        break;

                    case 0x10:
                        UUID requestUUID = DataTypeIO.readUUID(input);
                        int requestType2 = input.readByte();

                        switch (requestType2) {
                            case 0:
                                List<ValueTrios<UUID, String, Integer>> playerList = new ArrayList<>();
                                int playerListSize = input.readInt();
                                for (int i = 0; i < playerListSize; i++) {
                                    playerList.add(new ValueTrios<>(
                                            DataTypeIO.readUUID(input),
                                            DataTypeIO.readString(input, StandardCharsets.UTF_8),
                                            input.readInt()
                                    ));
                                }

                                CompletableFuture<List<ValueTrios<UUID, String, Integer>>> future = (CompletableFuture<List<ValueTrios<UUID, String, Integer>>>) toComplete.remove(requestUUID);
                                if (future != null) future.complete(playerList);

                                break;

                            default:
                                break;
                        }

                        break;

                    case 0x12:
                        UUID playerUUID4 = DataTypeIO.readUUID(input);
                        String placeholders = DataTypeIO.readString(input, StandardCharsets.UTF_8);
                        RemoteMCPlayer mcPlayer3 = PlayerUtils.getMCPlayer(playerUUID4);
                        if (mcPlayer3 != null && PlayerUtils.isLocal(mcPlayer3)) {
                            PlaceholderParser.parse(Bukkit.getOfflinePlayer(mcPlayer3.getUuid()), placeholders);
                        }

                        break;

                    case 0x14:
                        int size3 = input.readInt();
                        for (int i = 0; i < size3; i++) {
                            UUID playerUUID = DataTypeIO.readUUID(input);
                            boolean vanished = input.readBoolean();
                            RemoteMCPlayer mcPlayer4 = PlayerUtils.getMCPlayer(playerUUID);
                            if (mcPlayer4 != null) {
                                mcPlayer4.setVanished(vanished);
                            }
                        }
                }
            } catch (Exception error) {
                throw new RuntimeException("Failed to handle plugin message. Consider enabling the verbose plugin message debug option at the bottom of config.yml!", error);
            }
        });
    }

    public void addToComplete(UUID uuid, CompletableFuture<?> future) {
        toComplete.put(uuid, future);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            CompletableFuture<?> removedFuture = toComplete.remove(uuid);
            if (removedFuture != null && !removedFuture.isCompletedExceptionally() && !removedFuture.isCancelled()) {
                removedFuture.completeExceptionally(new TimeoutException("The proxy did not respond in time to the request."));
            }
        }, 400);
    }
}
