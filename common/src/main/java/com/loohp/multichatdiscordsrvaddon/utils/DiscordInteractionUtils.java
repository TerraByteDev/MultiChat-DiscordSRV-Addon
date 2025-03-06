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

package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.metrics.Metrics;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DiscordInteractionUtils {

    public static final String INTERACTION_ID_PREFIX;
    public static final Color OFFSET_WHITE = new Color(0xFFFFFE);

    static {
        try {
            String uuid = Metrics.getServerUUID();
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            INTERACTION_ID_PREFIX = "ICD_" + HashUtils.createSha1String(new ByteArrayInputStream(uuid.getBytes(StandardCharsets.UTF_8))) + "_";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
