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

package com.loohp.multichatdiscordsrvaddon.listeners;

import com.github.puregero.multilib.MultiLib;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
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
import lombok.Getter;
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

public class InboundToGameEvents implements Listener, PacketListener {

    public static final Pattern TENOR_HTML_PATTERN = Pattern.compile("<link class=\"dynamic\" rel=\"image_src\" href=\"https://media1\\.tenor\\.com/m/(.*?)/.*?\">");

    public static final Map<UUID, DiscordAttachmentData> DATA = new ConcurrentHashMap<>();
    public static final Map<Player, GraphicsToPacketMapWrapper> MAP_VIEWERS = new ConcurrentHashMap<>();

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onReceiveMessageFromDiscordPre(DiscordGuildMessagePreProcessEvent event) {
        Debug.debug("Triggering onReceiveMessageFromDiscordPre");
        DiscordSRV srv = MultiChatDiscordSrvAddon.discordsrv;
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
            for (List<ICPlaceholder> list : MultiChatDiscordSrvAddon.placeholderList.values()) {
                for (ICPlaceholder placeholder : list) {
                    component = component.replaceText(github.scarsz.discordsrv.dependencies.kyori.adventure.text.TextReplacementConfig.builder().match(placeholder.getKeyword()).replacement((result, builder) -> builder.content("\\" + result.group())).build());
                }
            }
            event.setMinecraftMessage(component);
        }
    }

    @Subscribe(priority = ListenerPriority.LOWEST)
    public void onReceiveMessageFromDiscordPostLowest(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.LOWEST)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onReceiveMessageFromDiscordPostLow(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.LOW)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.NORMAL)
    public void onReceiveMessageFromDiscordPostNormal(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.NORMAL)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGH)
    public void onReceiveMessageFromDiscordPostHigh(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.HIGH)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onReceiveMessageFromDiscordPostHighest(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.HIGHEST)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onReceiveMessageFromDiscordPostMonitor(DiscordGuildMessagePostProcessEvent event) {
        if (MultiChatDiscordSrvAddon.plugin.discordToGamePriority.equals(ListenerPriority.MONITOR)) {
            handleReceiveMessageFromDiscordPost(event);
        }
    }

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public void handleReceiveMessageFromDiscordPost(DiscordGuildMessagePostProcessEvent event) {
            try {
                CompletableFuture.runAsync(() -> {
                    Debug.debug("Triggering onReceiveMessageFromDiscordPost");
                    Message message = event.getMessage();

                    DiscordSRV srv = MultiChatDiscordSrvAddon.discordsrv;
                    User author = message.getAuthor();

                    TextChannel channel = event.getChannel();
                    Guild guild = channel.getGuild();
                    Member authorAsMember = guild.getMember(author);
                    String senderDiscordName = authorAsMember == null ? author.getName() : authorAsMember.getEffectiveName();
                    UUID senderUUID = srv.getAccountLinkManager().getUuid(author.getId());
                    OfflinePlayer offlinePlayer = null;

                    github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component component = event.getMinecraftMessage();

                    if (Config.i().getHook().shouldHook()) {
                        String serialized = MiniMessage.miniMessage().serialize(component);
                        assert MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration() != null;

                        if (senderUUID != null) offlinePlayer = Bukkit.getOfflinePlayer(senderUUID);

                        String filtered = MultiChatDiscordSrvAddon.plugin.integrationManager.getIntegration().filter(
                                new MessageSender(offlinePlayer != null ? offlinePlayer.getName() : ""),
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
                                    previewableImageContainers.add(PreviewableImageContainer.fromAttachment(attachment));
                                } else {
                                    DiscordAttachmentData data = new DiscordAttachmentData(attachment.getFileName(), url);
                                    DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                    Bukkit.getPluginManager().callEvent(dace);
                                    DATA.put(data.getUniqueId(), data);
                                    MultiLib.getGlobalRegionScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                                }
                            }
                        }
                        for (MessageSticker sticker : message.getStickers()) {
                            previewableImageContainers.add(PreviewableImageContainer.fromSticker(sticker));
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
                                MultiLib.getGlobalRegionScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                            } catch (Exception e) {
                                e.printStackTrace();
                                DiscordAttachmentData data = new DiscordAttachmentData(imageContainer.getName(), url);
                                DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                Bukkit.getPluginManager().callEvent(dace);
                                DATA.put(data.getUniqueId(), data);
                                MultiLib.getGlobalRegionScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
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
                                        MultiLib.getGlobalRegionScheduler().runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
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

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
            Debug.debug("Triggering onChatPacket");

            WrapperPlayServerSystemChatMessage messageWrapper = new WrapperPlayServerSystemChatMessage(event);
            if (Config.i().getDiscordAttachments().convert()) {
                Debug.debug("onChatPacket converting discord attachments");

                for (Entry<UUID, DiscordAttachmentData> entry : DATA.entrySet()) {
                    DiscordAttachmentData data = entry.getValue();
                    String url = data.getUrl();
                    net.kyori.adventure.text.Component component = messageWrapper.getMessage();

                    net.kyori.adventure.text.Component textComponent = ChatColorUtils.format(Config.i().getDiscordAttachments().formatting().text()
                            .replace("{FileName}", data.getFileName()));
                    if (Config.i().getDiscordAttachments().formatting().hover().enabled()) {
                        textComponent = textComponent.hoverEvent(HoverEvent.showText(ChatColorUtils.format(String.join("\n", Config.i().getDiscordAttachments().formatting().hover().hoverText())
                                .replace("{FileName}", data.getFileName()))));
                    }

                    if (Config.i().getDiscordAttachments().showImageUsingMaps() && data.isImage()) {
                        textComponent = textComponent.clickEvent(ClickEvent.runCommand("/mc imagemap " + data.getUniqueId().toString()));
                        net.kyori.adventure.text.Component imageAppend = ChatColorUtils.format(Config.i().getDiscordAttachments().formatting().imageOriginal()
                                .replace("{FileName}", data.getFileName()));

                        imageAppend.hoverEvent(HoverEvent.showText(ChatColorUtils.format(String.join("\n", Config.i().getDiscordAttachments().formatting().hover().imageOriginalHover())
                                .replace("{FileName}", data.getFileName()))));
                        imageAppend = imageAppend.clickEvent(ClickEvent.openUrl(url));
                        textComponent = textComponent.append(imageAppend);
                    } else {
                        textComponent = textComponent.clickEvent(ClickEvent.openUrl(url));
                    }

                    component = ComponentReplacing.replace(component, "\\\\?" + CustomStringUtils.escapeMetaCharacters(url), textComponent);

                    messageWrapper.setMessage(component);

                    event.setLastUsedWrapper(messageWrapper);
                    event.markForReEncode(true);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            MultiLib.getEntityScheduler(player).runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, null, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventory(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        int slot = event.getSlot();

        if (removed) {
            if (player.getInventory().equals(event.getClickedInventory()) && slot >= 9) {
                ItemStack item = player.getInventory().getItem(slot);
                MultiLib.getEntityScheduler(player).runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> player.getInventory().setItem(slot, item), null, 1);
            } else {
                event.setCursor(null);
            }
        }

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (event.getNewSlot() == event.getPreviousSlot()) {
            return;
        }

        Player player = event.getPlayer();
        boolean removed = MAP_VIEWERS.remove(player) != null;

        if (removed) {
            player.getInventory().setItemInHand(player.getInventory().getItemInHand());
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.PHYSICAL)) {
            return;
        }
        Player player = event.getPlayer();

        if (player.getGameMode().equals(GameMode.CREATIVE)) {
            MultiLib.getEntityScheduler(player).runDelayed(MultiChatDiscordSrvAddon.plugin, (task) -> {
                boolean removed = MAP_VIEWERS.remove(player) != null;

                if (removed) {
                    player.getInventory().setItemInHand(player.getInventory().getItemInHand());
                }
            }, null, 1);
        } else {
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            boolean removed = MAP_VIEWERS.remove(player) != null;

            if (removed) {
                player.getInventory().setItemInHand(player.getInventory().getItemInHand());
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        MAP_VIEWERS.remove(event.getPlayer());
    }

    public static class DiscordAttachmentData {

        @Getter
        private final String fileName;
        @Getter
        private final String url;
        @Getter
        private final GraphicsToPacketMapWrapper imageMap;
        private final UUID uuid;
        private final boolean isVideo;

        public DiscordAttachmentData(String fileName, String url, GraphicsToPacketMapWrapper imageMap, boolean isVideo) {
            this.fileName = fileName;
            this.url = url;
            this.imageMap = imageMap;
            this.uuid = UUID.randomUUID();
            this.isVideo = isVideo;
        }

        public DiscordAttachmentData(String fileName, String url) {
            this(fileName, url, null, false);
        }

        public boolean isImage() {
            return imageMap != null && !isVideo;
        }

        public boolean isVideo() {
            return imageMap != null && isVideo;
        }

        public UUID getUniqueId() {
            return uuid;
        }

        public int hashCode() {
            return 17 * uuid.hashCode();
        }

        public boolean equals(Object object) {
            if (object instanceof DiscordAttachmentData) {
                return ((DiscordAttachmentData) object).uuid.equals(this.uuid);
            }
            return false;
        }

    }

}
