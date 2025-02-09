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

package com.loohp.multichatdiscordsrvaddon.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MCVersion {

    V1_21_4("1.21.4", "1_21_R3", 30),
    V1_21_3("1.21.3", "1_21_R2", 29),
    V1_21_2("1.21.2", "1_21_R2", 28),
    V1_21_1("1.21.1", "1_21_R1", 27),
    V1_21("1.21", "1_21_R1", 26),
    V1_20_6("1.20.6", "1_20_R4", 25),
    V1_20_3("1.20.3", "1_20_R3", 23),
    V1_20_2("1.20.2", "1_20_R2", 22),
    V1_20("1.20", "1_20_R1", 21),
    UNSUPPORTED("Unsupported", null, -1);

    public static final MCVersion MINIMUM_SUPPORTED_VERSION = V1_20;

    private static final MCVersion[] SUPPORTED_VALUES = Arrays.stream(values()).filter(v -> v.isSupported()).toArray(MCVersion[]::new);

    public static MCVersion resolve() {
        MCVersion version = fromVersion(Bukkit.getVersion());
        if (version.isSupported()) {
            return version;
        }
        return fromPackageName(Bukkit.getServer().getClass().getPackage().getName());
    }

    public static MCVersion fromVersion(String bukkitVersion) {
        Pattern versionPattern = Pattern.compile("(?i)\\(MC:? ([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)\\)");
        Matcher matcher = versionPattern.matcher(bukkitVersion);
        if (matcher.find()) {
            String minecraftVersion = matcher.group(1);
            for (MCVersion version : SUPPORTED_VALUES) {
                if (minecraftVersion.equals(version.getMinecraftVersion())) {
                    return version;
                }
            }
        }
        return UNSUPPORTED;
    }

    public static MCVersion fromPackageName(String packageName) {
        for (MCVersion version : SUPPORTED_VALUES) {
            if (packageName.contains(version.getPackageName())) {
                return version;
            }
        }
        return UNSUPPORTED;
    }

    public static MCVersion fromNumber(int number) {
        for (MCVersion version : SUPPORTED_VALUES) {
            if (version.shortNum == number) {
                return version;
            }
        }
        return UNSUPPORTED;
    }

    private final String name;
    @Getter
    private final String packageName;
    private final int shortNum;

    MCVersion(String name, String packageName, int shortNum) {
        this.name = name;
        this.packageName = packageName;
        this.shortNum = shortNum;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getMinecraftVersion() {
        return name;
    }

    public int getNumber() {
        return shortNum;
    }

    public int compareWith(MCVersion version) {
        return this.shortNum - version.shortNum;
    }

    public boolean isOlderThan(MCVersion version) {
        return compareWith(version) < 0;
    }

    public boolean isOlderOrEqualTo(MCVersion version) {
        return compareWith(version) <= 0;
    }

    public boolean isNewerThan(MCVersion version) {
        return compareWith(version) > 0;
    }

    public boolean isNewerOrEqualTo(MCVersion version) {
        return compareWith(version) >= 0;
    }

    public boolean isBetweenInclusively(MCVersion v1, MCVersion v2) {
        int difference = v1.compareWith(v2);
        if (difference == 0) {
            return this.equals(v1);
        } else if (difference < 0) {
            return this.isNewerOrEqualTo(v1) && this.isOlderOrEqualTo(v2);
        } else {
            return this.isNewerOrEqualTo(v2) && this.isOlderOrEqualTo(v1);
        }
    }

    public boolean isLegacy() {
        return false;
    }

    public boolean isOld() {
        return false;
    }

    public boolean isSupported() {
        return this.shortNum >= MINIMUM_SUPPORTED_VERSION.shortNum;
    }

    public boolean isLegacyRGB() {
        return false;
    }

}