package com.loohp.multichatdiscordsrvaddon.objectholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class RemoteMCPlayer {

    private UUID uuid;
    private String server;
    private String name;
    private OfflinePlayerData offlinePlayerData;
    private boolean vanished;
    private Map<String, String> remotePlaceholders;

}
