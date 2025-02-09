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

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed.Field;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.requests.RestAction;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.MessageAction;
import github.scarsz.discordsrv.objects.MessageFormat;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.util.*;
import java.util.Map.Entry;

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
    private final List<Field> fields;
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

    public DiscordMessageContent(String authorName, String authorIconUrl, int color) {
        this(authorName, authorIconUrl, new ArrayList<>(), new ArrayList<>(), color, new HashMap<>());
    }

    public DiscordMessageContent(Message message) {
        if (message.getEmbeds().isEmpty()) {
            throw new IllegalArgumentException("Not embeds found!");
        }
        MessageEmbed embed = message.getEmbeds().get(0);
        this.authorName = embed.getAuthor().getName();
        this.authorIconUrl = embed.getAuthor().getIconUrl();
        this.description = new ArrayList<>();
        this.fields = new ArrayList<>(embed.getFields());
        if (embed.getDescription() != null) {
            description.add(embed.getDescription());
        }
        this.imageUrl = new ArrayList<>();
        if (embed.getImage() != null) {
            imageUrl.add(embed.getImage().getUrl());
        }
        this.color = embed.getColorRaw();
        if (embed.getThumbnail() != null) {
            this.thumbnail = embed.getThumbnail().getUrl();
        }
        this.attachments = new HashMap<>();
    }

    public DiscordMessageContent(MessageFormat messageFormat) {
        this.authorName = messageFormat.getAuthorName();
        this.authorIconUrl = messageFormat.getAuthorImageUrl();
        this.description = new ArrayList<>();
        if (messageFormat.getDescription() != null) {
            description.add(messageFormat.getDescription());
        }
        if (messageFormat.getFields() == null) {
            this.fields = new ArrayList<>();
        } else {
            this.fields = new ArrayList<>(messageFormat.getFields());
        }
        this.imageUrl = new ArrayList<>();
        if (messageFormat.getImageUrl() != null) {
            imageUrl.add(messageFormat.getImageUrl());
        }
        this.color = messageFormat.getColorRaw();
        this.thumbnail = messageFormat.getThumbnailUrl();
        this.attachments = new HashMap<>();
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

    public void addFields(Field... field) {
        fields.addAll(Arrays.asList(field));
    }

    public void addAttachment(String name, byte[] data) {
        attachments.put(name, data);
    }

    public void clearAttachments() {
        attachments.clear();
    }

    @SuppressWarnings("deprecation")
    public RestAction<List<Message>> toJDAMessageRestAction(TextChannel channel) {
        Map<MessageAction, Set<String>> actions = new LinkedHashMap<>();
        Set<String> rootAttachments = new HashSet<>();
        rootAttachments.add(authorIconUrl);
        EmbedBuilder embed = new EmbedBuilder().setAuthor(authorName, null, authorIconUrl).setColor(color).setThumbnail(thumbnail).setTitle(title);
        for (Field field : fields) {
            embed.addField(field);
        }
        if (!description.isEmpty()) {
            embed.setDescription(description.get(0));
        }
        if (!imageUrl.isEmpty()) {
            String url = imageUrl.get(0);
            embed.setImage(url);
            rootAttachments.add(url);
        }
        if (imageUrl.size() == 1 || description.size() == 1) {
            if (footer != null) {
                if (footerImageUrl == null) {
                    embed.setFooter(footer);
                } else {
                    embed.setFooter(footer, footerImageUrl);
                    rootAttachments.add(footerImageUrl);
                }
            }
        }
        actions.put(channel.sendMessage(embed.build()), rootAttachments);
        for (int i = 1; i < imageUrl.size() || i < description.size(); i++) {
            Set<String> usedAttachments = new HashSet<>();
            EmbedBuilder otherEmbed = new EmbedBuilder().setColor(color);
            if (i < imageUrl.size()) {
                String url = imageUrl.get(i);
                otherEmbed.setImage(url);
                usedAttachments.add(url);
            }
            if (i < description.size()) {
                otherEmbed.setDescription(description.get(i));
            }
            if (!(i + 1 < imageUrl.size() || i + 1 < description.size())) {
                if (footer != null) {
                    if (footerImageUrl == null) {
                        otherEmbed.setFooter(footer);
                    } else {
                        otherEmbed.setFooter(footer, footerImageUrl);
                    }
                }
            }
            if (!otherEmbed.isEmpty()) {
                actions.put(channel.sendMessage(otherEmbed.build()), usedAttachments);
            }
        }
        Set<String> embeddedAttachments = new HashSet<>();
        for (Entry<MessageAction, Set<String>> entry : actions.entrySet()) {
            MessageAction action = entry.getKey();
            Set<String> neededUrls = entry.getValue();
            for (Entry<String, byte[]> attachment : attachments.entrySet()) {
                String attachmentName = attachment.getKey();
                if (neededUrls.contains("attachment://" + attachmentName)) {
                    action.addFile(attachment.getValue(), attachmentName);
                    embeddedAttachments.add(attachmentName);
                }
            }
        }
        MessageAction lastAction = actions.keySet().stream().skip(actions.size() - 1).findFirst().get();
        for (Entry<String, byte[]> attachment : attachments.entrySet()) {
            String attachmentName = attachment.getKey();
            if (!embeddedAttachments.contains(attachmentName)) {
                lastAction.addFile(attachment.getValue(), attachmentName);
            }
        }
        return RestAction.allOf(actions.keySet());
    }

    public ValuePairs<List<MessageEmbed>, Set<String>> toJDAMessageEmbeds() {
        List<Set<String>> actions = new ArrayList<>();
        List<MessageEmbed> list = new ArrayList<>();
        Set<String> embeddedAttachments = new HashSet<>();
        EmbedBuilder embed = new EmbedBuilder().setAuthor(authorName, null, authorIconUrl).setColor(color).setThumbnail(thumbnail).setTitle(title);
        if (thumbnail != null && thumbnail.startsWith("attachment://")) {
            embeddedAttachments.add(thumbnail.substring(13));
        }
        for (Field field : fields) {
            embed.addField(field);
        }
        if (!description.isEmpty()) {
            embed.setDescription(description.get(0));
        }
        if (!imageUrl.isEmpty()) {
            String url = imageUrl.get(0);
            embed.setImage(url);
            if (url.startsWith("attachment://")) {
                embeddedAttachments.add(url.substring(13));
            }
        }
        if (imageUrl.size() == 1 || description.size() == 1) {
            if (footer != null) {
                if (footerImageUrl == null) {
                    embed.setFooter(footer);
                } else {
                    embed.setFooter(footer, footerImageUrl);
                    if (footerImageUrl.startsWith("attachment://")) {
                        embeddedAttachments.add(footerImageUrl.substring(13));
                    }
                }
            }
        }
        list.add(embed.build());
        for (int i = 1; i < imageUrl.size() || i < description.size(); i++) {
            Set<String> usedAttachments = new HashSet<>();
            EmbedBuilder otherEmbed = new EmbedBuilder().setColor(color);
            if (i < imageUrl.size()) {
                String url = imageUrl.get(i);
                otherEmbed.setImage(url);
                usedAttachments.add(url);
                if (url.startsWith("attachment://")) {
                    embeddedAttachments.add(url.substring(13));
                }
            }
            if (i < description.size()) {
                otherEmbed.setDescription(description.get(i));
            }
            if (!(i + 1 < imageUrl.size() || i + 1 < description.size())) {
                if (footer != null) {
                    if (footerImageUrl == null) {
                        otherEmbed.setFooter(footer);
                    } else {
                        otherEmbed.setFooter(footer, footerImageUrl);
                        if (footerImageUrl.startsWith("attachment://")) {
                            embeddedAttachments.add(footerImageUrl.substring(13));
                        }
                    }
                }
            }
            if (!otherEmbed.isEmpty()) {
                list.add(otherEmbed.build());
                actions.add(usedAttachments);
            }
        }
        for (Set<String> neededUrls : actions) {
            for (Entry<String, byte[]> attachment : attachments.entrySet()) {
                String attachmentName = attachment.getKey();
                if (neededUrls.contains("attachment://" + attachmentName)) {
                    embeddedAttachments.add(attachmentName);
                }
            }
        }
        return new ValuePairs<>(list, embeddedAttachments);
    }

}
