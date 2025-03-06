package com.loohp.multichatdiscordsrvaddon.objectholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class LinkedUser {
    private String discordID;
    private UUID uuid;
}
