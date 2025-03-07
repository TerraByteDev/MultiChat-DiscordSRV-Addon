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

package com.loohp.multichatdiscordsrvaddon.listeners.discordsrv;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVImageUtils;
import com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVManager;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordAttachmentData;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlaceholder;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.DiscordAttachmentConversionEvent;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.GifReader;
import com.loohp.multichatdiscordsrvaddon.modules.DiscordToGameMention;
import com.loohp.multichatdiscordsrvaddon.objectholders.PreviewableImageContainer;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message.Attachment;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageSticker;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.minimessage.MiniMessage;
import github.scarsz.discordsrv.util.MessageUtil;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVManager.discordToGamePriority;
import static com.loohp.multichatdiscordsrvaddon.listeners.InboundEventListener.*;

public class InboundToGameEvents {

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onReceiveMessageFromDiscordPre(DiscordGuildMessagePreProcessEvent event) {
        Debug.debug("Triggering onReceiveMessageFromDiscordPre");
        DiscordSRV srv = DiscordSRVManager.discordsrv;
        Map<Pattern, String> discordRegexes = srv.getDiscordRegexes();
        if (discordRegexes != null) {
            discordRegexes.keySet().removeIf(pattern -> pattern.pattern().equals("@+(everyone|here)"));
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordToGame(DiscordGuildMessagePostProcessEvent event) {
        Debug.debug("Triggering onDiscordToGame");
        MultiChatDiscordSrvAddon.plugin.messagesCounter.incrementAndGet();
        github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component component = event.getMinecraftMessage();
        if (Config.i().getSettings().escapePlaceholdersSentFromDiscord()) {
            Debug.debug("onDiscordToGame escaping placeholders");
            for (ICPlaceholder placeholder : MultiChatDiscordSrvAddon.placeholderList.values()) {
                component = component.replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().match(placeholder.getKeyword()).replacement((result, builder) -> builder.content("\\" + result.group())).build());
            }
            event.setMinecraftMessage(component);
        }
    }

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onReceiveMessageFromDiscordPostLowest(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.LOWEST)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onReceiveMessageFromDiscordPostLow(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.LOW)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onReceiveMessageFromDiscordPostNormal(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.NORMAL)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onReceiveMessageFromDiscordPostHigh(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.HIGH)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onReceiveMessageFromDiscordPostHighest(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.HIGHEST)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onReceiveMessageFromDiscordPostMonitor(DiscordGuildMessagePostProcessEvent event) {
        if (discordToGamePriority.equals(ListenerPriority.MONITOR)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public void handleReceiveMessageFromDiscordPost(DiscordGuildMessagePostProcessEvent event) {
            try {
                CompletableFuture.runAsync(() -> {
                    Debug.debug("Triggering onReceiveMessageFromDiscordPost");
                    Message message = event.getMessage();

                    DiscordSRV srv = DiscordSRVManager.discordsrv;
                    User author = message.getAuthor();

                    TextChannel channel = event.getChannel();
                    Guild guild = channel.getGuild();
                    Member authorAsMember = guild.getMember(author);
                    String senderDiscordName = authorAsMember == null ? author.getName() : authorAsMember.getEffectiveName();
                    UUID senderUUID = srv.getAccountLinkManager().getUuid(author.getId());
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(senderUUID);

                    github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component component = event.getMinecraftMessage();

                    if (Config.i().getHook().shouldHook()) {
                        String serialized = MiniMessage.miniMessage().serialize(component);
                        assert MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration() != null;

                        String filtered = MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration().filter(
                                new MessageSender(Objects.requireNonNull(offlinePlayer.getName())),
                                serialized
                        );

                        event.setMinecraftMessage(MiniMessage.miniMessage().deserialize(filtered));
                    }

                    if (Config.i().getDiscordMention().translateMentions()) {
                        Debug.debug("onReceiveMessageFromDiscordPost translating mentions");

                        Set<UUID> mentionTitleSent = new HashSet<>();
                        Map<Member, UUID> channelMembers = new HashMap<>();

                        for (Entry<UUID, String> entry : srv.getAccountLinkManager().getManyDiscordIds(Bukkit.getOnlinePlayers().stream().map(each -> each.getUniqueId()).collect(Collectors.toSet())).entrySet()) {
                            Member member = guild.getMemberById(entry.getValue());
                            if (member != null && member.hasAccess(channel)) {
                                channelMembers.put(member, entry.getKey());
                            }
                        }

                        if (message.mentionsEveryone()) {
                            //github.scarsz.discordsrv.dependencies.kyori.adventure.text.event.HoverEvent<Component> hover = Component.text(MultiChatDiscordSrvAddon.plugin.mentionHover.replace("{DiscordUser}", senderDiscordName).replace("{TextChannel}", "#" + channel.getName()).replace("{Guild}", guild.getName())).asHoverEvent();
                            component = component.replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("@here").replacement(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@here"))).build()).replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("@everyone").replacement(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@everyone"))).build());
                            for (UUID uuid : channelMembers.values()) {
                                mentionTitleSent.add(uuid);
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    DiscordToGameMention.playTitleScreen(senderDiscordName, channel.getName(), guild.getName(), player);
                                }
                            }
                        }

                        List<Role> mentionedRoles = message.getMentionedRoles();
                        for (Role role : mentionedRoles) {
                            //github.scarsz.discordsrv.dependencies.kyori.adventure.text.event.HoverEvent<Component> hover = Component.text(InteractiveChatDiscordSrvAddon.plugin.mentionHover.replace("{DiscordUser}", senderDiscordName).replace("{TextChannel}", "#" + channel.getName()).replace("{Guild}", guild.getName())).asHoverEvent();
                            component = component.replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("@" + role.getName()).replacement(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@" + role.getName()))).build());
                            for (Entry<Member, UUID> entry : channelMembers.entrySet()) {
                                UUID uuid = entry.getValue();
                                if (!mentionTitleSent.contains(uuid) && entry.getKey().getRoles().contains(role)) {
                                    mentionTitleSent.add(uuid);
                                    Player player = Bukkit.getPlayer(uuid);
                                    if (player != null) {
                                        DiscordToGameMention.playTitleScreen(senderDiscordName, channel.getName(), guild.getName(), player);
                                    }
                                }
                            }
                        }

                        List<User> mentionedUsers = message.getMentionedUsers();
                        if (!mentionedUsers.isEmpty()) {
                            for (User user : mentionedUsers) {
                                //github.scarsz.discordsrv.dependencies.kyori.adventure.text.event.HoverEvent<Component> hover = Component.text(InteractiveChatDiscordSrvAddon.plugin.mentionHover.replace("{DiscordUser}", senderDiscordName).replace("{TextChannel}", "#" + channel.getName()).replace("{Guild}", guild.getName())).asHoverEvent();
                                component = component.replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().matchLiteral("@" + user.getName()).replacement(github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@" + user.getName()))).build());
                                Member member = guild.getMember(user);
                                if (member != null) {
                                    UUID uuid = channelMembers.get(member);
                                    if (uuid != null && !mentionTitleSent.contains(uuid) && (senderUUID == null || !senderUUID.equals(uuid))) {
                                        mentionTitleSent.add(uuid);
                                        Player player = Bukkit.getPlayer(uuid);
                                        if (player != null) {
                                            DiscordToGameMention.playTitleScreen(senderDiscordName, channel.getName(), guild.getName(), player);
                                        }
                                    }
                                }
                            }
                        }

                        event.setMinecraftMessage(component);
                    }

                    String processedMessage = MessageUtil.toLegacy(component);

                    if (Config.i().getDiscordAttachments().convert()) {
                        Debug.debug("onReceiveMessageFromDiscordPost converting discord attachments");
                        Set<String> processedUrl = new HashSet<>();
                        List<PreviewableImageContainer> previewableImageContainers = new ArrayList<>(message.getAttachments().size() + message.getStickers().size());
                        for (Attachment attachment : message.getAttachments()) {
                            MultiChatDiscordSrvAddon.plugin.attachmentCounter.incrementAndGet();
                            String url = attachment.getUrl();
                            if (processedMessage.contains(url)) {
                                processedUrl.add(url);
                                if ((attachment.isImage() || attachment.isVideo()) && attachment.getSize() <= Config.i().getDiscordAttachments().fileSizeLimit()) {
                                    previewableImageContainers.add(DiscordSRVImageUtils.fromAttachment(attachment));
                                } else {
                                    DiscordAttachmentData data = new DiscordAttachmentData(attachment.getFileName(), url);
                                    DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                    Bukkit.getPluginManager().callEvent(dace);
                                    DATA.put(data.getUniqueId(), data);
                                    Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                                }
                            }
                        }
                        for (MessageSticker sticker : message.getStickers()) {
                            previewableImageContainers.add(DiscordSRVImageUtils.fromSticker(sticker));
                        }
                        for (PreviewableImageContainer imageContainer : previewableImageContainers) {
                            MultiChatDiscordSrvAddon.plugin.attachmentImageCounter.incrementAndGet();
                            String url = imageContainer.getUrl();
                            List<ThrowingSupplier<InputStream>> methods = new ArrayList<>();
                            for (String url0 : imageContainer.getAllUrls()) {
                                if (URLRequestUtils.isAllowed(url0)) {
                                    methods.add(() -> URLRequestUtils.getInputStream0(url0));
                                }
                            }

                            try (InputStream stream = URLRequestUtils.retrieveUntilSuccessful(methods)) {
                                String type = imageContainer.getContentType();
                                GraphicsToPacketMapWrapper map;
                                boolean isVideo = false;
                                if (type.endsWith("gif.png") || type.endsWith("apng")) {
                                    throw new UnsupportedOperationException("Animated PNG not yet supported, this error can be ignored");
                                } else if (type.endsWith("gif")) {
                                    map = new GraphicsToPacketMapWrapper(Config.i().getDiscordAttachments().playbackBar().enabled(), !Config.i().getDiscordAttachments().imageMapBackground().transparent() ? ColorUtils.hex2Rgb(Config.i().getDiscordAttachments().imageMapBackground().color()) : null);
                                    GifReader.readGif(stream, MultiChatDiscordSrvAddon.plugin.mediaReadingService, (frames, e) -> {
                                        if (e != null) {
                                            e.printStackTrace();
                                            map.completeFuture(null);
                                        } else {
                                            map.completeFuture(frames);
                                        }
                                    });
                                } else {
                                    BufferedImage image = ImageIO.read(stream);
                                    map = new GraphicsToPacketMapWrapper(image, !Config.i().getDiscordAttachments().imageMapBackground().transparent() ? ColorUtils.hex2Rgb(Config.i().getDiscordAttachments().imageMapBackground().color()) : null);
                                }
                                DiscordAttachmentData data = new DiscordAttachmentData(imageContainer.getName(), url, map, isVideo);
                                DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                Bukkit.getPluginManager().callEvent(dace);
                                DATA.put(data.getUniqueId(), data);
                                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                            } catch (Exception e) {
                                e.printStackTrace();
                                DiscordAttachmentData data = new DiscordAttachmentData(imageContainer.getName(), url);
                                DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                Bukkit.getPluginManager().callEvent(dace);
                                DATA.put(data.getUniqueId(), data);
                                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                            }
                        }

                        Matcher matcher = URLRequestUtils.URL_PATTERN.matcher(message.getContentRaw());
                        while (matcher.find()) {
                            String url = matcher.group();
                            String imageUrl = url;
                            if (!processedUrl.contains(url) && URLRequestUtils.isAllowed(url)) {
                                if (url.startsWith("https://tenor.com/")) {
                                    try {
                                        String html = HTTPRequestUtils.getTextResponse(url);
                                        Matcher matcher2 = TENOR_HTML_PATTERN.matcher(html);
                                        if (matcher2.find()) {
                                            imageUrl = "https://c.tenor.com/" + matcher2.group(1) + "/tenor.gif";
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                long size = HTTPRequestUtils.getContentSize(imageUrl);
                                if (size >= 0 && size <= Config.i().getDiscordAttachments().fileSizeLimit()) {
                                    MultiChatDiscordSrvAddon.plugin.attachmentImageCounter.incrementAndGet();
                                    try (InputStream stream = URLRequestUtils.getInputStream(imageUrl)) {
                                        String type = HTTPRequestUtils.getContentType(imageUrl);

                                        if (type == null || !type.startsWith("image/")) {
                                            continue;
                                        }
                                        GraphicsToPacketMapWrapper map;
                                        boolean isVideo = false;
                                        if (type.endsWith("gif.png") || type.endsWith("apng")) {
                                            throw new UnsupportedOperationException("Animated PNG not yet supported, this error can be ignored");
                                        } else if (type.endsWith("gif")) {
                                            map = new GraphicsToPacketMapWrapper(Config.i().getDiscordAttachments().playbackBar().enabled(), !Config.i().getDiscordAttachments().imageMapBackground().transparent() ? ColorUtils.hex2Rgb(Config.i().getDiscordAttachments().imageMapBackground().color()) : null);
                                            GifReader.readGif(stream, MultiChatDiscordSrvAddon.plugin.mediaReadingService, (frames, e) -> {
                                                if (e != null) {
                                                    e.printStackTrace();
                                                    map.completeFuture(null);
                                                } else {
                                                    map.completeFuture(frames);
                                                }
                                            });
                                        } else {
                                            BufferedImage image = ImageIO.read(stream);
                                            map = new GraphicsToPacketMapWrapper(image, !Config.i().getDiscordAttachments().imageMapBackground().transparent() ? ColorUtils.hex2Rgb(Config.i().getDiscordAttachments().imageMapBackground().color()) : null);
                                        }
                                        String name = matcher.group(1);
                                        DiscordAttachmentData data = new DiscordAttachmentData(name, url, map, isVideo);
                                        DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                        Bukkit.getPluginManager().callEvent(dace);
                                        DATA.put(data.getUniqueId(), data);
                                        Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                                    } catch (FileNotFoundException ignore) {
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }, service).exceptionally((ex) -> {
                    throw new RuntimeException(ex);
                }).get(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
    }

}
