package com.loohp.multichatdiscordsrvaddon.standalone.linking;

import com.loohp.multichatdiscordsrvaddon.standalone.StandaloneManager;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class StandaloneLinkManager {

    private final StandaloneLinkDatabase database;
    private final StandaloneManager manager;

    private final Map<UUID, String> codes = new HashMap<>();

    public StandaloneLinkManager(StandaloneManager manager) {
        this.manager = manager;
        this.database = new StandaloneLinkDatabase(manager);
        manager.getJda().addEventListener(new StandaloneLinkMessageHandler());
    }

    public CompletableFuture<String> createCode(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String code;
            do {
                code = String.format("%04d", (int) (Math.random() * 10000));
            } while (codes.containsValue(code));

            codes.put(uuid, code);

            return code;
        });
    }

    public UUID getCodeOwner(String code) {
        return codes.entrySet().stream()
                .filter(entry -> entry.getValue().equals(code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
