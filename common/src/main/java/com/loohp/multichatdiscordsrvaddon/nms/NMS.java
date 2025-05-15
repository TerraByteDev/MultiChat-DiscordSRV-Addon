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

package com.loohp.multichatdiscordsrvaddon.nms;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.utils.VersionManager;

import java.lang.reflect.InvocationTargetException;

public class NMS {

    private static NMSWrapper instance;

    @SuppressWarnings("deprecation")
    public synchronized static NMSWrapper getInstance() {
        if (instance != null) {
            return instance;
        }
        try {
            Class<NMSWrapper> nmsImplClass = (Class<NMSWrapper>) Class.forName("com.loohp.multichatdiscordsrvaddon.nms." + VersionManager.version.name());
            instance = nmsImplClass.getConstructor().newInstance();
            NMSWrapper.setup(instance, MultiChatDiscordSrvAddon.plugin);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            if (VersionManager.version.isSupported()) {
                throw new RuntimeException("Missing NMSWrapper implementation for version " + VersionManager.version.name(), e);
            } else {
                throw new RuntimeException("No NMSWrapper implementation for UNSUPPORTED version " + VersionManager.version.name(), e);
            }
        }
    }

}
