package com.loohp.multichatdiscordsrvaddon.standalone.event;

import com.loohp.multichatdiscordsrvaddon.api.events.DiscordAttachmentConversionEvent;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.GifReader;
import com.loohp.multichatdiscordsrvaddon.integration.sender.MessageSender;
import com.loohp.multichatdiscordsrvaddon.modules.DiscordToGameMention;
import com.loohp.multichatdiscordsrvaddon.objectholders.DiscordAttachmentData;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlaceholder;
import com.loohp.multichatdiscordsrvaddon.objectholders.LinkedUser;
import com.loohp.multichatdiscordsrvaddon.objectholders.PreviewableImageContainer;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.standalone.message.StandaloneWebhookManager;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneImageUtils;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import com.loohp.multichatdiscordsrvaddon.wrappers.GraphicsToPacketMapWrapper;
import com.vdurmont.emoji.EmojiParser;
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializer;
import dev.vankka.mcdiscordreserializer.minecraft.MinecraftSerializerOptions;
import dev.vankka.mcdiscordreserializer.rules.DiscordMarkdownRules;
import dev.vankka.simpleast.core.node.Node;
import dev.vankka.simpleast.core.parser.Rule;
import dev.vankka.simpleast.core.simple.SimpleMarkdownRules;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.placeholderList;
import static com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon.plugin;
import static com.loohp.multichatdiscordsrvaddon.listeners.InboundEventListener.DATA;
import static com.loohp.multichatdiscordsrvaddon.listeners.InboundEventListener.TENOR_HTML_PATTERN;

public class StandaloneInboundEvents extends ListenerAdapter {

    private final MinecraftSerializer serializer;
    private final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder().extractUrls().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    public StandaloneInboundEvents() {
        List<Rule<Object, Node<Object>, Object>> rules = new ArrayList<>();
        rules.add(SimpleMarkdownRules.createEscapeRule());
        rules.addAll(DiscordMarkdownRules.createMentionRules());
        rules.add(DiscordMarkdownRules.createSpecialTextRule());

        MinecraftSerializerOptions<Component> options = MinecraftSerializerOptions.defaults();
        MinecraftSerializerOptions<String> escapeOptions = MinecraftSerializerOptions.escapeDefaults();

        serializer = new MinecraftSerializer(options.withRules(rules), escapeOptions);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Config.i().getHook().shouldHook() && plugin.integrationManager.getIntegration() == null) return; // JDA is loaded before chat plugin may be ready, or integration manager is initialised.
        if (!event.getChannel().getId().equals(Config.i().getStandalone().channelId())) return;

        Debug.debug("Triggering on(Standalone)DiscordToGame");
        CompletableFuture.runAsync(() -> {
            try {
                if ((event.getMember() == null && !event.isWebhookMessage() || event.getAuthor().equals(plugin.standaloneManager.getJda().getSelfUser())))
                    return;

                if (event.isWebhookMessage()) {
                    if (StandaloneWebhookManager.webhook.getUrl().split("/")[6].equals(event.getAuthor().getId())) return;
                    if (Config.i().getStandalone().inboundSettings().blockWebhooks()) return;
                }

                String message = event.getMessage().getContentRaw();
                if (StringUtils.isBlank(message) && event.getMessage().getAttachments().isEmpty() && event.getMessage().getStickers().isEmpty()) return;
                message = message.replace("\u001B", ""); // replace escape

                if (Config.i().getStandalone().inboundSettings().blockBots() && event.getAuthor().isBot() && !event.getMessage().isWebhookMessage()) return;
                if (message.length() > Config.i().getStandalone().inboundSettings().truncationLength()) {
                    message = message.substring(0, Config.i().getStandalone().inboundSettings().truncationLength());
                }

                LinkedUser linkedUser = plugin.standaloneManager.getLinkManager().getDatabase().getLinkedUserById(event.getAuthor().getId()).get(5, TimeUnit.SECONDS);
                List<String> allowedColorRoles = Config.i().getStandalone().inboundSettings().colorCodeRoles();
                boolean strip = true;
                if (!event.isWebhookMessage()) {
                    for (Role role : event.getMember().getRoles()) {
                        if (allowedColorRoles.contains(role.getId())) {
                            strip = false;
                            break;
                        }
                    }
                }

                message = format(message, event, linkedUser);
                Component serialized = serializer.serialize(message);

                message = strip ? PlainTextComponentSerializer.plainText().serialize(serialized) : ComponentStringUtils.stripColorAndConvertMagic(ComponentStringUtils.stripColors(message));
                if (StringUtils.isBlank(message)) return;

                if (Config.i().getStandalone().inboundSettings().stripEmojis()) {
                    message = EmojiParser.removeAllEmojis(message);
                } else message = EmojiParser.parseToAliases(message);

                serialized = serializer.serialize(message);

                Debug.debug("on(Standalone)discordToGame escaping placeholders");
                for (List<ICPlaceholder> list : placeholderList.values()) {
                    for (ICPlaceholder placeholder : list) {
                        serialized = serialized.replaceText(TextReplacementConfig.builder()
                                .match(placeholder.getKeyword())
                                .replacement((result, builder) -> builder.content("\\" + result.group()))
                                .build()
                        );
                    }
                }

                if (Config.i().getDiscordMention().translateMentions()) {
                    Debug.debug("on(Standalone)messageReceived translating mentions");

                    Set<UUID> mentionTitleSent = new HashSet<>();
                    Map<Member, UUID> channelMembers = new HashMap<>();

                    for (Map.Entry<UUID, String> entry : DiscordProviderManager.get().getManyDiscordIds(Bukkit.getOnlinePlayers().stream().map(each -> each.getUniqueId()).collect(Collectors.toSet())).get(5, TimeUnit.SECONDS).entrySet()) {
                        Member member = event.getGuild().getMemberById(entry.getValue());
                        if (member != null && member.hasAccess(event.getGuildChannel())) {
                            channelMembers.put(member, entry.getKey());
                        }
                    }

                    if (event.getMessage().getMentions().mentionsEveryone()) {
                        serialized = serialized.replaceText(TextReplacementConfig.builder()
                                .matchLiteral("@here")
                                .replacement(Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@here")))
                                .build()
                        ).replaceText(TextReplacementConfig.builder()
                                .matchLiteral("@everyone")
                                .replacement(Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@everyone")))
                                .build()
                        );

                        for (UUID uuid : channelMembers.values()) {
                            mentionTitleSent.add(uuid);
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                DiscordToGameMention.playTitleScreen(event.getAuthor().getName(), event.getChannel().getName(), event.getGuild().getName(), player);
                            }
                        }
                    }

                    List<Role> mentionedRoles = event.getMessage().getMentions().getRoles();
                    for (Role role : mentionedRoles) {
                        serialized = serialized.replaceText(TextReplacementConfig.builder()
                                .matchLiteral("@" + role.getName())
                                .replacement(Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@" + role.getName())))
                                .build()
                        );

                        for (Map.Entry<Member, UUID> entry : channelMembers.entrySet()) {
                            UUID uuid = entry.getValue();
                            if (!mentionTitleSent.contains(uuid) && entry.getKey().getRoles().contains(role)) {
                                mentionTitleSent.add(uuid);
                                Player player = Bukkit.getPlayer(uuid);
                                if (player != null) {
                                    DiscordToGameMention.playTitleScreen(event.getAuthor().getName(), event.getChannel().getName(), event.getGuild().getName(), player);
                                }
                            }
                        }
                    }

                    List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
                    if (!mentionedUsers.isEmpty()) {
                        for (User user : mentionedUsers) {
                            serialized = serialized.replaceText(TextReplacementConfig.builder()
                                    .matchLiteral("@" + user.getName())
                                    .replacement(Component.text(Config.i().getDiscordMention().mentionHighlight().replace("{DiscordMention}", "@" + user.getName())))
                                    .build()
                            );

                            Member member = event.getGuild().getMember(user);
                            if (member != null) {
                                UUID uuid = channelMembers.get(member);
                                if (uuid != null && !mentionTitleSent.contains(uuid) && (linkedUser == null || !linkedUser.getUuid().equals(uuid))) {
                                    mentionTitleSent.add(uuid);
                                    Player player = Bukkit.getPlayer(uuid);
                                    if (player != null) {
                                        DiscordToGameMention.playTitleScreen(event.getAuthor().getName(), event.getChannel().getName(), event.getGuild().getName(), player);
                                    }
                                }
                            }
                        }
                    }

                    String processedMessage = LEGACY_SERIALIZER.serialize(serialized);
                    if (Config.i().getDiscordAttachments().convert()) {
                        Debug.debug("on(Standalone)MessageReceived converting discord attachments");
                        Set<String> processedUrl = new HashSet<>();
                        List<PreviewableImageContainer> previewableImageContainers = new ArrayList<>(event.getMessage().getAttachments().size() + event.getMessage().getStickers().size());

                        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                            plugin.attachmentCounter.incrementAndGet();
                            String url = attachment.getUrl();

                            if (processedMessage.contains(url)) {
                                processedUrl.add(url);
                                if ((attachment.isImage() || attachment.isVideo()) && attachment.getSize() <= Config.i().getDiscordAttachments().fileSizeLimit()) {
                                    previewableImageContainers.add(StandaloneImageUtils.fromAttachment(attachment));
                                } else {
                                    DiscordAttachmentData data = new DiscordAttachmentData(attachment.getFileName(), url);
                                    DiscordAttachmentConversionEvent dace = new DiscordAttachmentConversionEvent(url, data);
                                    Bukkit.getPluginManager().callEvent(dace);
                                    DATA.put(data.getUniqueId(), data);

                                    Bukkit.getScheduler().runTaskLater(plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L); // line 270
                                }
                            }

                            Matcher matcher = URLRequestUtils.URL_PATTERN.matcher(event.getMessage().getContentRaw());
                            while (matcher.find()) {
                                String matcherURL = matcher.group();
                                String imageURL = matcherURL;

                                if (!processedUrl.contains(matcherURL) && URLRequestUtils.isAllowed(matcherURL)) {
                                    if (matcherURL.startsWith("https://tenor.com/")) {
                                        try {
                                            String html = HTTPRequestUtils.getTextResponse(matcherURL);
                                            Matcher matcher1 = TENOR_HTML_PATTERN.matcher(html);
                                            if (matcher1.find()) {
                                                imageURL = "https://c.tenor.com/" + matcher1.group(1) + "/tenor.gif";
                                            }
                                        } catch (Exception e) {
                                            throw new RuntimeException("Failed to get text response for Tenor URL " + matcherURL, e);
                                        }
                                    }

                                    long size = HTTPRequestUtils.getContentSize(imageURL);
                                    if (size >= 0 && size <= Config.i().getDiscordAttachments().fileSizeLimit()) {
                                        plugin.attachmentImageCounter.incrementAndGet();

                                        try (InputStream stream = URLRequestUtils.getInputStream(imageURL)) {
                                            String type = HTTPRequestUtils.getContentType(imageURL);

                                            if (type == null || !type.startsWith("image/")) continue;

                                            GraphicsToPacketMapWrapper map;
                                            boolean isVideo = false;
                                            if (type.endsWith("gif.png") || type.endsWith("apng")) {
                                                throw new UnsupportedOperationException("Animated PNG not yet supported, this error can be ignored");
                                            } else if (type.endsWith("gif")) {
                                                map = new GraphicsToPacketMapWrapper(Config.i().getDiscordAttachments().playbackBar().enabled(), !Config.i().getDiscordAttachments().imageMapBackground().transparent() ? ColorUtils.hex2Rgb(Config.i().getDiscordAttachments().imageMapBackground().color()) : null);

                                                GifReader.readGif(stream, plugin.mediaReadingService, (frames, e) -> {
                                                    if (e != null) {
                                                        map.completeFuture(null);
                                                        e.printStackTrace();
                                                    } else map.completeFuture(frames);
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

                                            Bukkit.getScheduler().runTaskLater(plugin, () -> DATA.remove(data.getUniqueId()), Config.i().getDiscordAttachments().timeout() * 20L);
                                        } catch (FileNotFoundException ignored) {
                                        } catch (Exception error) {
                                            throw new RuntimeException(error);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    ChatUtils.audience.player(player).sendMessage(serialized);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to handle inbound message.", e);
            }
        }, plugin.standaloneManager.getScheduler()).exceptionally((ex) -> {
            ex.printStackTrace();
            return null;
        });
    }

    private String format(String msg, MessageReceivedEvent event, LinkedUser linkedUser) {
        OfflinePlayer offlinePlayer = linkedUser != null ? Bukkit.getOfflinePlayer(linkedUser.getUuid()) : null;

        String replaced = PlaceholderParser.parse(offlinePlayer, Config.i().getStandalone().inboundSettings().format())
                .replace("%member_role_color%", getRoleColor(event.getMember()))
                .replace("%member_role%", getHighestRolePrefix(event.getMember()))
                .replace("%member_name%", event.getAuthor().getName())
                .replace("%message%", msg);

        return Config.i().getHook().shouldHook() ? plugin.integrationManager.getIntegration().filter(new MessageSender(linkedUser != null ? offlinePlayer.getName() : ""), replaced) : replaced;
    }

    private String getRoleColor(Member member) {
        Color color = Color.white;
        if (member != null) {
            color = member.getColor();
            if (color == null) color = Color.white;
        }

        return "<#" + Integer.toHexString(color.getRGB()).substring(2) + ">";
    }

    private String getHighestRolePrefix(Member member) {
        if (member != null) {
            return member.getRoles().stream()
                    .findFirst()
                    .map(Role::getName)
                    .orElse("");
        }
        return "";
    }

}
