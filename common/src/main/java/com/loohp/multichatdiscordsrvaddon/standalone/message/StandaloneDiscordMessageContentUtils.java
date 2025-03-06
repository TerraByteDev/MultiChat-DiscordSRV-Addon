package com.loohp.multichatdiscordsrvaddon.standalone.message;

import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordMessageContent;
import com.loohp.multichatdiscordsrvaddon.objectholders.MCField;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.*;

public class StandaloneDiscordMessageContentUtils {

    public static DiscordMessageContent create(Message message) {
        if (message.getEmbeds().isEmpty()) throw new IllegalArgumentException("No embeds found!");

        MessageEmbed embed = message.getEmbeds().get(0);

        List<String> desc = new ArrayList<>();
        if (embed.getDescription() != null) desc.add(embed.getDescription());

        List<String> imgURL = new ArrayList<>();
        if (embed.getImage() != null) imgURL.add(embed.getImage().getUrl());

        DiscordMessageContent content = new DiscordMessageContent(
                embed.getAuthor().getName(),
                embed.getAuthor().getIconUrl(),
                desc,
                imgURL,
                embed.getColorRaw(),
                new HashMap<>()
        );

        embed.getFields().forEach((field) -> content.addFields(new MCField(
                field.getName(),
                field.getValue(),
                field.isInline(),
                true
        )));
        content.setThumbnail(embed.getThumbnail().getUrl());

        return content;
    }

    public static RestAction<List<Message>> toJDAMessageRestAction(DiscordMessageContent content, TextChannel channel) {
        Map<MessageCreateAction, Set<String>> actions = new LinkedHashMap<>();
        Set<String> rootAttachments = new HashSet<>();
        rootAttachments.add(content.getAuthorIconUrl());

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(content.getAuthorName(), null, content.getAuthorIconUrl())
                .setColor(content.getColor())
                .setThumbnail(content.getThumbnail())
                .setTitle(content.getTitle());

        for (MCField field : content.getFields()) {
            embed.addField(new MessageEmbed.Field(
                    field.getName(),
                    field.getValue(),
                    field.isInline(),
                    true
            ));
        }

        if (!content.getDescriptions().isEmpty()) embed.setDescription(content.getDescriptions().get(0));

        if (!content.getImageUrls().isEmpty()) {
            String url = content.getImageUrls().get(0);
            embed.setImage(url);
            rootAttachments.add(url);
        }
        if (content.getImageUrls().size() == 1 || content.getDescriptions().size() == 1) {
            if (content.getFooter() != null) {
                if (content.getFooterImageUrl() == null) {
                    embed.setFooter(content.getFooter());
                } else {
                    embed.setFooter(content.getFooter(), content.getFooterImageUrl());
                    rootAttachments.add(content.getFooterImageUrl());
                }
            }
        }

        actions.put(channel.sendMessageEmbeds(embed.build()), rootAttachments);
        for (int i = 1; i < content.getImageUrls().size() || i < content.getDescriptions().size(); i++) {
            Set<String> usedAttachments = new HashSet<>();

            EmbedBuilder otherEmbed = new EmbedBuilder().setColor(content.getColor());
            if (i < content.getImageUrls().size()) {
                String url = content.getImageUrls().get(i);
                otherEmbed.setImage(url);
                usedAttachments.add(url);
            }
            if (i < content.getDescriptions().size()) {
                otherEmbed.setDescription(content.getDescriptions().get(i));
            }
            if (!(i + 1 < content.getImageUrls().size() || i + 1 < content.getDescriptions().size())) {
                if (content.getFooter() != null) {
                    if (content.getFooterImageUrl() == null) {
                        otherEmbed.setFooter(content.getFooter());
                    } else {
                        otherEmbed.setFooter(content.getFooter(), content.getFooterImageUrl());
                    }
                }
            }
            if (!otherEmbed.isEmpty()) {
                actions.put(channel.sendMessageEmbeds(otherEmbed.build()), usedAttachments);
            }
        }

        Set<String> embeddedAttachments = new HashSet<>();
        for (Map.Entry<MessageCreateAction, Set<String>> entry : actions.entrySet()) {
            MessageCreateAction action = entry.getKey();

            Set<String> necessaryURLs = entry.getValue();
            for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                String attachmentName = attachment.getKey();
                if (necessaryURLs.contains("attachment:// "+ attachmentName)) {
                    action.addFiles(FileUpload.fromData(attachment.getValue(), attachmentName));
                    embeddedAttachments.add(attachmentName);
                }
            }
        }

        MessageCreateAction lastAction = actions.keySet().stream().skip(actions.size() - 1).findFirst().get();
        for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
            String attachmentName = attachment.getKey();
            if (!embeddedAttachments.contains(attachmentName)) {
                lastAction.addFiles(FileUpload.fromData(attachment.getValue(), attachmentName));
            }
        }

        return RestAction.allOf(actions.keySet());
    }

    public static ValuePairs<List<MessageEmbed>, Set<String>> toJDAMessageEmbeds(DiscordMessageContent content) {
        List<Set<String>> actions = new ArrayList<>();
        List<MessageEmbed> list = new ArrayList<>();
        Set<String> embeddedAttachments = new HashSet<>();

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(content.getAuthorName(), null, content.getAuthorIconUrl())
                .setColor(content.getColor())
                .setThumbnail(content.getThumbnail())
                .setTitle(content.getTitle());

        if (content.getThumbnail() != null && content.getThumbnail().startsWith("attachment://")) {
            embeddedAttachments.add(content.getThumbnail().substring(13));
        }

        for (MCField field : content.getFields()) {
            embed.addField(new MessageEmbed.Field(
                    field.getName(),
                    field.getValue(),
                    field.isInline(),
                    true
            ));
        }

        if (!content.getDescriptions().isEmpty()) {
            embed.setDescription(content.getDescriptions().get(0));
        }

        if (!content.getImageUrls().isEmpty()) {
            String url = content.getImageUrls().get(0);
            embed.setImage(url);
            if (url.startsWith("attachment://")) {
                embeddedAttachments.add(url.substring(13));
            }
        }

        if (content.getImageUrls().size() == 1 || content.getDescriptions().size() == 1) {
            if (content.getFooter() != null) {
                if (content.getFooterImageUrl() == null) {
                    embed.setFooter(content.getFooter());
                } else {
                    embed.setFooter(content.getFooter(), content.getFooterImageUrl());
                    if (content.getFooterImageUrl().startsWith("attachment://")) {
                        embeddedAttachments.add(content.getFooterImageUrl().substring(13));
                    }
                }
            }
        }

        list.add(embed.build());

        for (int i = 1; i < content.getImageUrls().size() || i < content.getDescriptions().size(); i++) {
            Set<String> usedAttachments = new HashSet<>();
            EmbedBuilder otherEmbed = new EmbedBuilder()
                    .setColor(content.getColor());

            if (i < content.getImageUrls().size()) {
                String url = content.getImageUrls().get(i);
                otherEmbed.setImage(url);
                usedAttachments.add(url);
                if (url.startsWith("attachment://")) {
                    embeddedAttachments.add(url.substring(13));
                }
            }

            if (i < content.getDescriptions().size()) {
                otherEmbed.setDescription(content.getDescriptions().get(i));
            }

            if (!(i + 1 < content.getImageUrls().size() || i + 1 < content.getDescriptions().size())) {
                if (content.getFooter() != null) {
                    if (content.getFooterImageUrl() == null) {
                        otherEmbed.setFooter(content.getFooter());
                    } else {
                        otherEmbed.setFooter(content.getFooter(), content.getFooterImageUrl());
                        if (content.getFooterImageUrl().startsWith("attachment://")) {
                            embeddedAttachments.add(content.getFooterImageUrl().substring(13));
                        }
                    }
                }
            }

            if (!otherEmbed.isEmpty()) {
                list.add(otherEmbed.build());
                actions.add(usedAttachments);
            }
        }

        for (Set<String> requiredURLs : actions) {
            for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                String attachmentName = attachment.getKey();
                if (requiredURLs.contains("attachment://" + attachmentName)) {
                    embeddedAttachments.add(attachmentName);
                }
            }
        }

        return new ValuePairs<>(list, embeddedAttachments);
    }
}
