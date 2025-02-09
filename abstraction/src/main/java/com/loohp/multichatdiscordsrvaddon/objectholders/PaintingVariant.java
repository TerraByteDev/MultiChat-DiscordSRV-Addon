/*
 * This file is part of InteractiveChatDiscordSrvAddon-Abstraction.
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

package com.loohp.multichatdiscordsrvaddon.objectholders;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public class PaintingVariant {

    private static Component getLegacyPaintingTitle(Key key) {
        return Component.translatable("painting." + key.namespace() + "." + key.value() + ".title").color(NamedTextColor.YELLOW);
    }

    private static Component getLegacyPaintingAuthor(Key key) {
        return Component.translatable("painting." + key.namespace() + "." + key.value() + ".author").color(NamedTextColor.GRAY);
    }

    private final Key key;
    private final int offsetX;
    private final int offsetY;
    private final int blockWidth;
    private final int blockHeight;

    private final Component title;
    private final Component author;

    public PaintingVariant(Key key, int offsetX, int offsetY, int blockWidth, int blockHeight, Component title, Component author) {
        this.key = key;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.title = title;
        this.author = author;
    }

    public PaintingVariant(Key key, int blockWidth, int blockHeight, Component title, Component author) {
        this(key, 0, 0, blockWidth, blockHeight, title, author);
    }

    public PaintingVariant(Key key, int offsetX, int offsetY, int blockWidth, int blockHeight) {
        this(key, offsetX, offsetY, blockWidth, blockHeight, getLegacyPaintingTitle(key), getLegacyPaintingAuthor(key));
    }

    public PaintingVariant(Key key, int blockWidth, int blockHeight) {
        this(key, blockWidth, blockHeight, getLegacyPaintingTitle(key), getLegacyPaintingAuthor(key));
    }

    public int getPixelWidth() {
        return blockWidth * 16;
    }

    public int getPixelHeight() {
        return blockHeight * 16;
    }
}
