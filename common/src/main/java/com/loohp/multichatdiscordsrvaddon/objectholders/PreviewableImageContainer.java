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

package com.loohp.multichatdiscordsrvaddon.objectholders;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageSticker;
import lombok.Getter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PreviewableImageContainer {

    @Getter
    private final String name;
    @Getter
    private final String url;
    @Getter
    private final List<String> altUrls;
    @Getter
    private final String contentType;
    private final Supplier<CompletableFuture<InputStream>> retrieveInputStream;

    public PreviewableImageContainer(String name, String url, List<String> altUrls, String contentType, Supplier<CompletableFuture<InputStream>> retrieveInputStream) {
        this.name = name;
        this.url = url;
        this.altUrls = altUrls;
        this.contentType = contentType;
        this.retrieveInputStream = retrieveInputStream;
    }

    public List<String> getAllUrls() {
        List<String> urls = new ArrayList<>(altUrls);
        urls.add(0, url);
        return urls;
    }

    public CompletableFuture<InputStream> retrieveInputStream() {
        return retrieveInputStream == null ? null : retrieveInputStream.get();
    }
}
