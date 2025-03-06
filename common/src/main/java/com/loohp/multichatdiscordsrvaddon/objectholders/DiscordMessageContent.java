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
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.*;

public class DiscordMessageContent {

    @Getter
    @Setter
    private String authorName;
    @Getter
    @Setter
    private String authorIconUrl;
    @Setter
    @Getter
    private String title;
    private List<String> description;
    private List<String> imageUrl;
    @Getter
    @Setter
    private String thumbnail;
    @Getter
    private final List<MCField> fields;
    @Setter
    @Getter
    private int color;
    @Setter
    @Getter
    private String footer;
    @Setter
    @Getter
    private String footerImageUrl;
    @Setter
    @Getter
    private Map<String, byte[]> attachments;

    public DiscordMessageContent(String authorName, String authorIconUrl, List<String> description, List<String> imageUrl, int color, Map<String, byte[]> attachments) {
        this.authorName = authorName;
        this.authorIconUrl = authorIconUrl;
        this.description = description;
        this.imageUrl = imageUrl;
        this.fields = new ArrayList<>();
        this.color = color;
        this.attachments = attachments;
        this.footer = null;
        this.footerImageUrl = null;
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, String description, String imageUrl, Color color) {
        this(authorName, authorIconUrl, new ArrayList<>(Collections.singletonList(description)), new ArrayList<>(Collections.singletonList(imageUrl)), color.getRGB(), new HashMap<>());
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, String description, String imageUrl, int color) {
        this(authorName, authorIconUrl, new ArrayList<>(Collections.singletonList(description)), new ArrayList<>(Collections.singletonList(imageUrl)), color, new HashMap<>());
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, Color color) {
        this(authorName, authorIconUrl, new ArrayList<>(), new ArrayList<>(), color.getRGB(), new HashMap<>());
    }

    public void addFields(MCField... field) {
        fields.addAll(Arrays.asList(field));
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, int color) {
        this(authorName, authorIconUrl, new ArrayList<>(), new ArrayList<>(), color, new HashMap<>());
    }

    public List<String> getDescriptions() {
        return description;
    }

    public void setDescriptions(List<String> description) {
        this.description = description;
    }

    public void addDescription(String description) {
        this.description.add(description);
    }

    public void setDescription(int index, String description) {
        this.description.set(index, description);
    }

    public void clearDescriptions() {
        description.clear();
    }

    public List<String> getImageUrls() {
        return imageUrl;
    }

    public void setImageUrls(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrl.add(imageUrl);
    }

    public void setImageUrl(int index, String imageUrl) {
        this.imageUrl.set(index, imageUrl);
    }

    public void clearImageUrls() {
        imageUrl.clear();
    }

    public void addAttachment(String name, byte[] data) {
        attachments.put(name, data);
    }

    public void clearAttachments() {
        attachments.clear();
    }

}
