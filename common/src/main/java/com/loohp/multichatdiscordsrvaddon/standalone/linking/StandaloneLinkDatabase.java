package com.loohp.multichatdiscordsrvaddon.standalone.linking;

import com.google.common.reflect.TypeToken;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.standalone.StandaloneManager;
import github.scarsz.discordsrv.dependencies.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class StandaloneLinkDatabase {

    private static final Gson gson = new Gson();
    private final StandaloneManager standaloneManager;
    private final Path path = Paths.get(MultiChatDiscordSrvAddon.plugin.getDataFolder() + "linked.json");

    private final Map<UUID, String> cachedUsers = new ConcurrentHashMap<>();

    public StandaloneLinkDatabase(StandaloneManager manager) {
        this.standaloneManager = manager;
    }

    @Nullable
    public CompletableFuture<LinkedUser> getLinkedUserById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            if (cachedUsers.containsValue(id)) {
                for (Map.Entry<UUID, String> entry : cachedUsers.entrySet()) {
                    if (entry.getValue().equals(id)) {
                        return new LinkedUser(id, entry.getKey());
                    }
                }
            }

            try {
                String content = Files.readString(path);
                List<LinkedUser> linkedUsers = gson.fromJson(content, new TypeToken<List<LinkedUser>>() {}.getType());

                LinkedUser user = linkedUsers.stream().filter(usr -> usr.getDiscordID().equals(id)).findFirst().orElse(null);
                if (user != null) cachedUsers.put(user.getUuid(), user.getDiscordID());

                return user;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON file", e);
            }
        }, standaloneManager.getScheduler());
    }

    @Nullable
    public CompletableFuture<LinkedUser> getLinkedUserByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (cachedUsers.containsKey(uuid)) {
                return new LinkedUser(cachedUsers.get(uuid), uuid);
            }

            try {
                String content = Files.readString(path);
                List<LinkedUser> linkedUsers = gson.fromJson(content, new TypeToken<List<LinkedUser>>() {}.getType());

                LinkedUser user = linkedUsers.stream().filter(usr -> usr.getUuid().equals(uuid)).findFirst().orElse(null);
                if (user != null) cachedUsers.put(uuid, user.getDiscordID());

                return user;
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON file", e);
            }
        }, standaloneManager.getScheduler());
    }

    public CompletableFuture<Void> saveLinkedUser(LinkedUser user) {
        return CompletableFuture.runAsync(() -> {
            try {
                String content = Files.readString(path);
                List<LinkedUser> linkedUsers = gson.fromJson(content, new TypeToken<List<LinkedUser>>() {}.getType());
                linkedUsers.add(user);
                String json = gson.toJson(linkedUsers);
                Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write JSON file", e);
            }
        }, standaloneManager.getScheduler());
    }

    public CompletableFuture<Void> removeLinkedUser(LinkedUser user) {
        return CompletableFuture.runAsync(() -> {
            try {
                String content = Files.readString(path);
                List<LinkedUser> linkedUsers = gson.fromJson(content, new TypeToken<List<LinkedUser>>() {}.getType());
                linkedUsers.removeIf(existingUser -> existingUser.getUuid().equals(user.getUuid()));
                String json = gson.toJson(linkedUsers);
                Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                cachedUsers.remove(user.getUuid());
            } catch (IOException e) {
                throw new RuntimeException("Failed to write JSON file", e);
            }
        }, standaloneManager.getScheduler());
    }
}
