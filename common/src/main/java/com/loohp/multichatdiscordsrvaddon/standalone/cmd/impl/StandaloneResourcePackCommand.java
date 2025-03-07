package com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl;

import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackInfo;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.incendo.cloud.discord.jda5.JDAInteraction;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;
import static com.loohp.multichatdiscordsrvaddon.utils.DiscordInteractionUtils.OFFSET_WHITE;

public class StandaloneResourcePackCommand {

    public static void resourcePackCommand(
            JDAInteraction interaction
    ) {
        if (!Config.i().getDiscordCommands().resourcePack().enabled() || !Config.i().getDiscordCommands().resourcePack().isMainServer()) return;

        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        Map<String, byte[]> attachments = new HashMap<>();
        String footer = "MultiChatDiscordSRVAddon v" + plugin.getDescription().getVersion();
        int i = 0;

        List<ResourcePackInfo> packs = plugin.getResourceManager().getResourcePackInfo();
        for (ResourcePackInfo packInfo : packs) {
            i++;
            Component packName = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveName(packInfo)), plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()));
            Component description = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveDescription(packInfo)), plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()));

            EmbedBuilder builder = new EmbedBuilder()
                    .setAuthor(PlainTextComponentSerializer.plainText().serialize(packName))
                    .setThumbnail("attachment://" + i + ".png");

            if (packInfo.getStatus()) {
                builder.setDescription(PlainTextComponentSerializer.plainText().serialize(description));

                ChatColor firstColor = ChatColorUtils.getColor(LegacyComponentSerializer.builder().useUnusualXRepeatedCharacterHexFormat().character(ChatColorUtils.COLOR_CHAR).build().serialize(description));
                if (firstColor == null) {
                    firstColor = ChatColor.WHITE;
                }
                Color color = ColorUtils.getColor(firstColor);
                if (color == null) {
                    color = new Color(0xAAAAAA);
                } else if (color.equals(Color.WHITE)) {
                    color = OFFSET_WHITE;
                }
                builder.setColor(color);
                if (packInfo.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) > 0) {
                    builder.setFooter(LanguageUtils.getTranslation(TranslationKeyUtils.getNewIncompatiblePack(), Config.i().getResources().language()).getResult());
                } else if (packInfo.compareServerPackFormat(ResourceRegistry.RESOURCE_PACK_VERSION) < 0) {
                    builder.setFooter(LanguageUtils.getTranslation(TranslationKeyUtils.getOldIncompatiblePack(), Config.i().getResources().language()).getResult());
                }
            } else {
                builder
                        .setColor(0xFF0000)
                        .setDescription(packInfo.getRejectedReason());
            }

            if (i >= packs.size()) {
                builder.setFooter(footer, "https://resources.loohpjames.com/images/InteractiveChat-DiscordSRV-Addon.png");
            }
            messageEmbeds.add(builder.build());

            try {
                attachments.put(i + ".png", ImageUtils.toArray(ImageUtils.resizeImageAbs(packInfo.getIcon(), 128, 128)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WebhookMessageEditAction<Message> action = interaction.interactionEvent().getHook().setEphemeral(true).editOriginal("**" + LanguageUtils.getTranslation(TranslationKeyUtils.getServerResourcePack(), Config.i().getResources().language()).getResult() + "**").setEmbeds(messageEmbeds);

        Collection<FileUpload> uploads = new HashSet<>();
        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            uploads.add(FileUpload.fromData(entry.getValue(), entry.getKey()));
        }
        action.setFiles(uploads);
        action.queue();
    }
}
