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

import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.discordsrv.DiscordSRVMessageContentUtils;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVContentUtils;
import com.loohp.multichatdiscordsrvaddon.discordsrv.utils.DiscordSRVInteractionHandler;
import com.loohp.multichatdiscordsrvaddon.modules.ItemDisplay;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.api.events.MultiChatDiscordSRVConfigReloadEvent;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ResourcePackInfo;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionMapping;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.OptionType;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.CommandData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.OptionData;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.commands.build.SubcommandData;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.listeners.InboundEventListener.components;
import static com.loohp.multichatdiscordsrvaddon.utils.DiscordCommandUtils.*;
import static com.loohp.multichatdiscordsrvaddon.utils.DiscordInteractionUtils.OFFSET_WHITE;

public class DiscordCommands implements Listener, SlashCommandProvider {

    public static ItemStack resolveItemStack(SlashCommandEvent event, OfflinePlayer player) {
        OfflinePlayerData offlinePlayerData = PlayerUtils.getData(player);
        String subCommand = event.getSubcommandName();
        switch (subCommand) {
            case "mainhand":
                return offlinePlayerData.getInventory().getItem(offlinePlayerData.getSelectedSlot());
            case "offhand":
                return offlinePlayerData.getInventory().getSize() > 40 ? offlinePlayerData.getInventory().getItem(40) : null;
            case "hotbar":
            case "inventory":
                return offlinePlayerData.getInventory().getItem((int) event.getOptions().get(0).getAsLong() - 1);
            case "armor":
                return offlinePlayerData.getEquipment().getItem(EquipmentSlot.valueOf(event.getOptions().get(0).getAsString().toUpperCase()));
            case "ender":
                return offlinePlayerData.getEnderChest().getItem((int) event.getOptions().get(0).getAsLong() - 1);
        }
        return null;
    }

    private final DiscordSRV discordsrv;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")

    public DiscordCommands(DiscordSRV discordsrv) {
        this.discordsrv = discordsrv;
    }

    @EventHandler
    public void onConfigReload(MultiChatDiscordSRVConfigReloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> reload());
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        Guild guild = discordsrv.getMainGuild();

        String memberLabel = Config.i().getDiscordCommands().globalSettings().messages().member();
        String memberDescription = Config.i().getDiscordCommands().globalSettings().messages().memberDescription();
        String slotLabel = Config.i().getDiscordCommands().globalSettings().messages().slotLabel();
        String slotDescription = Config.i().getDiscordCommands().globalSettings().messages().slotDescription();

        List<CommandData> commandDataList = new ArrayList<>();

        if (Config.i().getDiscordCommands().resourcePack().isMainServer()) {
            if (Config.i().getDiscordCommands().resourcePack().enabled()) {
                commandDataList.add(new CommandData(RESOURCEPACK_LABEL, ChatColorUtils.stripColor(Config.i().getDiscordCommands().resourcePack().description())));
            }
        }
        if (Config.i().getDiscordCommands().playerInfo().isMainServer()) {
            if (Config.i().getDiscordCommands().playerInfo().enabled()) {
                commandDataList.add(new CommandData(PLAYERINFO_LABEL, ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerInfo().description())).addOptions(new OptionData(OptionType.USER, memberLabel, memberDescription, false)));
            }
        }
        if (Config.i().getDiscordCommands().playerList().isMainServer()) {
            if (Config.i().getDiscordCommands().playerList().enabled()) {
                commandDataList.add(new CommandData(PLAYERLIST_LABEL, ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerList().description())));
            }
        }
        if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
            Optional<List<ICPlaceholder>> optItemPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.itemPlaceholder)).findFirst();
            if (Config.i().getDiscordCommands().shareItem().enabled() && optItemPlaceholder.isPresent()) {
                String itemDescription = ChatColorUtils.stripColor(optItemPlaceholder.get().getFirst().getDescription());

                SubcommandData mainhandSubcommand = new SubcommandData("mainhand", itemDescription);
                SubcommandData offhandSubcommand = new SubcommandData("offhand", itemDescription);
                SubcommandData hotbarSubcommand = new SubcommandData("hotbar", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 9));
                SubcommandData inventorySubcommand = new SubcommandData("inventory", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 41));
                SubcommandData armorSubcommand = new SubcommandData("armor", itemDescription).addOptions(new OptionData(OptionType.STRING, slotLabel, slotDescription, true).addChoice("head", "head").addChoice("chest", "chest").addChoice("legs", "legs").addChoice("feet", "feet"));
                SubcommandData enderSubcommand = new SubcommandData("ender", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 27));

                commandDataList.add(new CommandData(ITEM_LABEL, ChatColorUtils.stripColor(optItemPlaceholder.get().getFirst().getDescription())).addSubcommands(mainhandSubcommand).addSubcommands(offhandSubcommand).addSubcommands(hotbarSubcommand).addSubcommands(inventorySubcommand).addSubcommands(armorSubcommand).addSubcommands(enderSubcommand));

                if (Config.i().getDiscordCommands().shareItem().allowAsOthers()) {
                    SubcommandData mainhandOtherSubcommand = new SubcommandData("mainhand", itemDescription).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData offhandOtherSubcommand = new SubcommandData("offhand", itemDescription).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData hotbarOtherSubcommand = new SubcommandData("hotbar", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 9)).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData inventoryOtherSubcommand = new SubcommandData("inventory", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 41)).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData armorOtherSubcommand = new SubcommandData("armor", itemDescription).addOptions(new OptionData(OptionType.STRING, slotLabel, slotDescription, true).addChoice("head", "head").addChoice("chest", "chest").addChoice("legs", "legs").addChoice("feet", "feet")).addOption(OptionType.USER, memberLabel, memberDescription, true);
                    SubcommandData enderOtherSubcommand = new SubcommandData("ender", itemDescription).addOptions(new OptionData(OptionType.INTEGER, slotLabel, slotDescription, true).setRequiredRange(1, 27)).addOption(OptionType.USER, memberLabel, memberDescription, true);

                    commandDataList.add(new CommandData(ITEM_OTHER_LABEL, ChatColorUtils.stripColor(optItemPlaceholder.get().getFirst().getDescription())).addSubcommands(mainhandOtherSubcommand).addSubcommands(offhandOtherSubcommand).addSubcommands(hotbarOtherSubcommand).addSubcommands(inventoryOtherSubcommand).addSubcommands(armorOtherSubcommand).addSubcommands(enderOtherSubcommand));
                }
            }
        }
        if (Config.i().getDiscordCommands().shareInventory().isMainServer()) {
            Optional<List<ICPlaceholder>> optInvPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.inventoryPlaceholder)).findFirst();
            if (Config.i().getDiscordCommands().shareInventory().enabled() && optInvPlaceholder.isPresent()) {
                commandDataList.add(new CommandData(INVENTORY_LABEL, ChatColorUtils.stripColor(optInvPlaceholder.get().getFirst().getDescription())));

                if (Config.i().getDiscordCommands().shareInventory().allowAsOthers()) {
                    commandDataList.add(new CommandData(INVENTORY_OTHER_LABEL, ChatColorUtils.stripColor(optInvPlaceholder.get().getFirst().getDescription())).addOption(OptionType.USER, memberLabel, memberDescription, true));
                }
            }
        }
        if (Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
            Optional<List<ICPlaceholder>> optEnderPlaceholder = MultiChatDiscordSrvAddon.placeholderList.values().stream().filter(each -> each.equals(MultiChatDiscordSrvAddon.enderChestPlaceholder)).findFirst();
            if (Config.i().getDiscordCommands().shareEnderChest().enabled() && optEnderPlaceholder.isPresent()) {
                commandDataList.add(new CommandData(ENDERCHEST_LABEL, ChatColorUtils.stripColor(optEnderPlaceholder.get().getFirst().getDescription())));

                if (Config.i().getDiscordCommands().shareEnderChest().allowAsOthers()) {
                    commandDataList.add(new CommandData(ENDERCHEST_OTHER_LABEL, ChatColorUtils.stripColor(optEnderPlaceholder.get().getFirst().getDescription())).addOption(OptionType.USER, memberLabel, memberDescription, true));
                }
            }
        }

        return commandDataList.stream().map(each -> new PluginSlashCommand(MultiChatDiscordSrvAddon.plugin, each, guild.getId())).collect(Collectors.toSet());
    }

    public void reload() {
        DiscordSRV.api.updateSlashCommands();
    }

    @SlashCommand(path = "*")
    public void onSlashCommand(SlashCommandEvent event) {
        Guild guild = discordsrv.getMainGuild();
        if (event.getGuild().getIdLong() != guild.getIdLong()) {
            return;
        }
        if (!(event.getChannel() instanceof TextChannel)) {
            return;
        }
        TextChannel channel = (TextChannel) event.getChannel();
        String label = event.getName();
        if (Config.i().getDiscordCommands().resourcePack().enabled() && label.equalsIgnoreCase(RESOURCEPACK_LABEL)) {
            if (Config.i().getDiscordCommands().resourcePack().isMainServer()) {
                event.deferReply().setEphemeral(true).queue();
                List<MessageEmbed> messageEmbeds = new ArrayList<>();
                Map<String, byte[]> attachments = new HashMap<>();
                String footer = "MultiChatDiscordSrvAddon v" + MultiChatDiscordSrvAddon.plugin.getDescription().getVersion();
                int i = 0;
                List<ResourcePackInfo> packs = MultiChatDiscordSrvAddon.plugin.getResourceManager().getResourcePackInfo();
                for (ResourcePackInfo packInfo : packs) {
                    i++;
                    Component packName = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveName(packInfo)), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()));
                    Component description = ComponentStringUtils.resolve(ComponentModernizing.modernize(ResourcePackInfoUtils.resolveDescription(packInfo)), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()));
                    EmbedBuilder builder = new EmbedBuilder().setAuthor(PlainTextComponentSerializer.plainText().serialize(packName)).setThumbnail("attachment://" + i + ".png");
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
                        builder.setColor(0xFF0000).setDescription(packInfo.getRejectedReason());
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
                WebhookMessageUpdateAction<Message> action = event.getHook().setEphemeral(true).editOriginal("**" + LanguageUtils.getTranslation(TranslationKeyUtils.getServerResourcePack(), Config.i().getResources().language()).getResult() + "**").setEmbeds(messageEmbeds);
                for (Entry<String, byte[]> entry : attachments.entrySet()) {
                    action = action.addFile(entry.getValue(), entry.getKey());
                }
                action.queue();
            }
        } else if (Config.i().getDiscordCommands().playerInfo().enabled() && label.equalsIgnoreCase(PLAYERINFO_LABEL)) {
            if (Config.i().getDiscordCommands().playerInfo().isMainServer()) {
                String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
                if (minecraftChannel == null) {
                    if (Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels()) {
                        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel())).setEphemeral(true).queue();
                    }
                    return;
                }

                String discordUserId = event.getUser().getId();
                List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
                if (!options.isEmpty()) {
                    discordUserId = options.get(0).getAsUser().getId();
                }
                UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
                if (uuid == null) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked())).setEphemeral(true).queue();
                    return;
                }
                event.deferReply().queue();

                int errorCode = -1;
                try {
                    OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                    errorCode--;
                    List<ToolTipComponent<?>> playerInfoComponents;
                    if (offlineICPlayer.isOnline() && !PlayerUtils.isVanished(((Player) offlineICPlayer))) {
                        playerInfoComponents = Config.i().getDiscordCommands().playerInfo().infoFormatting().whenOnline().stream().map(each -> {
                            each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));

                            if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                                each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));
                            }

                            return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                        }).collect(Collectors.toList());
                    } else {
                        playerInfoComponents = Config.i().getDiscordCommands().playerInfo().infoFormatting().whenOffline().stream().map(each -> {
                            each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));

                            if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                                each = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, each));
                            }

                            return ToolTipComponent.text(LegacyComponentSerializer.legacySection().deserialize(each));
                        }).collect(Collectors.toList());
                    }
                    errorCode--;
                    String title = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, Config.i().getDiscordCommands().playerInfo().infoFormatting().title())));
                    String subtitle = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, Config.i().getDiscordCommands().playerInfo().infoFormatting().subtitle())));

                    if (Config.i().getDiscordCommands().playerInfo().parsePlaceholdersTwice()) {
                        title = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, title)));
                        subtitle = ChatColorUtils.stripColor(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(offlineICPlayer, subtitle)));
                    }

                    BufferedImage image = ImageGeneration.getToolTipImage(playerInfoComponents, null);
                    errorCode--;
                    byte[] data = ImageUtils.toArray(image);
                    errorCode--;
                    event.getHook().editOriginalEmbeds(new EmbedBuilder().setTitle(title).setDescription(subtitle).setThumbnail(DiscordSRV.getAvatarUrl(offlineICPlayer.getName(), offlineICPlayer.getUniqueId())).setImage("attachment://PlayerInfo.png").setColor(ColorUtils.hex2Rgb(Config.i().getDiscordCommands().playerList().tablistOptions().sidebarColor())).build()).addFile(data, "PlayerInfo.png").queue();
                } catch (Throwable e) {
                    e.printStackTrace();
                    event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue();
                }
            }
        } else if (Config.i().getDiscordCommands().playerList().enabled() && label.equalsIgnoreCase(PLAYERLIST_LABEL)) {
            if (Config.i().getDiscordCommands().playerList().isMainServer()) {
                String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
                if (minecraftChannel == null) {
                    if (Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels()) {
                        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel())).setEphemeral(true).queue();
                    }
                    return;
                }
                AtomicBoolean deleted = new AtomicBoolean(false);
                event.deferReply().queue(hook -> {
                    if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                            if (!deleted.get()) {
                                hook.deleteOriginal().queue();
                            }
                        }, Config.i().getDiscordCommands().playerList().deleteAfter() * 20L);
                    }
                });
                Map<OfflinePlayer, Integer> players;
                if (Config.i().getSettings().bungeecord() && Config.i().getDiscordCommands().playerList().listBungeecordPlayers() && !Bukkit.getOnlinePlayers().isEmpty()) {
                    try {
                        List<ValueTrios<UUID, String, Integer>> bungeePlayers = MultiChatDiscordSrvAddonAPI.getBungeecordPlayerList().get();
                        players = new LinkedHashMap<>(bungeePlayers.size());
                        for (ValueTrios<UUID, String, Integer> playerinfo : bungeePlayers) {
                            UUID uuid = playerinfo.getFirst();
                            Player icPlayer = Bukkit.getPlayer(uuid);
                            if (!PlayerUtils.isVanished(icPlayer)) {
                                players.put(Bukkit.getOfflinePlayer(uuid), playerinfo.getThird());
                            }
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (-1)").queue();
                        return;
                    }
                } else {
                    players = Bukkit.getOnlinePlayers().stream().filter(each -> {
                        return !PlayerUtils.isVanished(each);
                    }).collect(Collectors.toMap(each -> each, each -> each.getPing(), (a, b) -> a));
                }
                if (players.isEmpty()) {
                    event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getDiscordCommands().playerList().emptyServer())).queue();
                } else {
                    int errorCode = -2;
                    try {
                        List<ValueTrios<OfflinePlayer, Component, Integer>> player = new ArrayList<>();
                        Map<UUID, ValuePairs<List<String>, String>> playerInfo = new HashMap<>();
                        for (Entry<OfflinePlayer, Integer> entry : players.entrySet()) {
                            OfflinePlayer bukkitOfflinePlayer = entry.getKey();
                            playerInfo.put(bukkitOfflinePlayer.getUniqueId(), new ValuePairs<>(getPlayerGroups(bukkitOfflinePlayer), bukkitOfflinePlayer.getName()));
                            String name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(bukkitOfflinePlayer, Config.i().getDiscordCommands().playerList().tablistOptions().playerFormat()));

                            if (Config.i().getDiscordCommands().playerList().tablistOptions().parsePlaceholdersTwice()) {
                                name = ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(bukkitOfflinePlayer, name));
                            }

                            Component nameComponent;
                            if (Config.i().getDiscordCommands().playerList().tablistOptions().parsePlayerNamesWithMiniMessage()) {
                                nameComponent = MiniMessage.miniMessage().deserialize(name);
                            } else {
                                nameComponent = MultiChatComponentSerializer.legacySection().deserialize(name);
                            }
                            player.add(new ValueTrios<>(bukkitOfflinePlayer, nameComponent, entry.getValue()));
                        }
                        errorCode--;
                        sortPlayers(Config.i().getDiscordCommands().playerList().tablistOptions().playerOrder().orderBy(), player, playerInfo);
                        errorCode--;
                        OfflinePlayer firstPlayer = Bukkit.getOfflinePlayer(players.keySet().iterator().next().getUniqueId());
                        List<Component> header = new ArrayList<>();
                        if (!Config.i().getDiscordCommands().playerList().tablistOptions().headerText().isEmpty()) {
                            header = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, DiscordSRVContentUtils.join(Config.i().getDiscordCommands().playerList().tablistOptions().headerText(), true).replace("{OnlinePlayers}", players.size() + "")))));
                        }
                        errorCode--;
                        List<Component> footer = new ArrayList<>();
                        if (!Config.i().getDiscordCommands().playerList().tablistOptions().footerText().isEmpty()) {
                            footer = ComponentStyling.splitAtLineBreaks(LegacyComponentSerializer.legacySection().deserialize(ChatColorUtils.translateAlternateColorCodes('&', PlaceholderParser.parse(firstPlayer, DiscordSRVContentUtils.join(Config.i().getDiscordCommands().playerList().tablistOptions().footerText(), true).replace("{OnlinePlayers}", players.size() + "")))));
                        }
                        errorCode--;
                        int playerListMaxPlayers = Config.i().getDiscordCommands().playerList().tablistOptions().maxPlayersDisplayable();
                        if (playerListMaxPlayers < 1) {
                            playerListMaxPlayers = Integer.MAX_VALUE;
                        }
                        BufferedImage image = ImageGeneration.getTabListImage(header, footer, player, Config.i().getDiscordCommands().playerList().tablistOptions().showPlayerAvatar(), Config.i().getDiscordCommands().playerList().tablistOptions().showPlayerPing(), playerListMaxPlayers);
                        errorCode--;
                        byte[] data = ImageUtils.toArray(image);
                        errorCode--;
                        event.getHook().editOriginalEmbeds(new EmbedBuilder().setImage("attachment://Tablist.png").setColor(ColorUtils.hex2Rgb(Config.i().getDiscordCommands().playerList().tablistOptions().sidebarColor())).build()).addFile(data, "Tablist.png").queue(message -> {
                            if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                                deleted.set(true);
                                message.delete().queueAfter(Config.i().getDiscordCommands().playerList().deleteAfter(), TimeUnit.SECONDS);
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                        event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                            if (Config.i().getDiscordCommands().playerList().deleteAfter() > 0) {
                                deleted.set(true);
                                message.delete().queueAfter(Config.i().getDiscordCommands().playerList().deleteAfter(), TimeUnit.SECONDS);
                            }
                        });
                    }
                }
            }
        } else if (Config.i().getDiscordCommands().shareItem().enabled() && (label.equalsIgnoreCase(ITEM_LABEL) || label.equalsIgnoreCase(ITEM_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel())).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (!options.isEmpty()) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked())).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlineICPlayer == null) {
                    if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (Config.i().getSettings().bungeecord() && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        ItemStack[] equipment;
                        if (VersionManager.version.isOld()) {
                            //noinspection deprecation
                            equipment = new ItemStack[] {icplayer.getEquipment().getHelmet(), icplayer.getEquipment().getChestplate(), icplayer.getEquipment().getLeggings(), icplayer.getEquipment().getBoots(), icplayer.getEquipment().getItemInHand()};
                        } else {
                            equipment = new ItemStack[] {icplayer.getEquipment().getHelmet(), icplayer.getEquipment().getChestplate(), icplayer.getEquipment().getLeggings(), icplayer.getEquipment().getBoots(), icplayer.getEquipment().getItemInMainHand(), icplayer.getEquipment().getItemInOffHand()};
                        }
                        try {
                            OfflinePlayerData offlinePlayerData = PlayerUtils.getData(icplayer);
                            BungeeMessageSender.forwardEquipment(System.currentTimeMillis(), icplayer.getUniqueId(), PlayerUtils.isRightHanded(icplayer), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), equipment);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                ItemStack itemStack = resolveItemStack(event, offlineICPlayer);
                if (itemStack == null) {
                    itemStack = new ItemStack(Material.AIR);
                }
                errorCode--;
                String title = ChatColorUtils.stripColor(Config.i().getDiscordCommands().shareItem().inventoryTitle().replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                Component itemTag = ItemDisplay.createItemDisplay(offlineICPlayer, itemStack, title, true, null, false);
                Component resolvedItemTag = ComponentStringUtils.resolve(ComponentModernizing.modernize(itemTag), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()));
                Component component = LegacyComponentSerializer.legacySection().deserialize(Config.i().getDiscordCommands().shareItem().inGameMessage().text().replace("{Player}", offlineICPlayer.getName())).replaceText(TextReplacementConfig.builder().matchLiteral("{ItemTag}").replacement(itemTag).build());
                Component resolvedComponent = LegacyComponentSerializer.legacySection().deserialize(Config.i().getDiscordCommands().shareItem().inGameMessage().text().replace("{Player}", offlineICPlayer.getName())).replaceText(TextReplacementConfig.builder().matchLiteral("{ItemTag}").replacement(resolvedItemTag).build());
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                    errorCode--;

                    Inventory inv = null;
                    if (itemStack.getItemMeta() instanceof BlockStateMeta) {
                        BlockState bsm = ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
                        if (bsm instanceof InventoryHolder) {
                            Inventory container = ((InventoryHolder) bsm).getInventory();
                            if (!container.isEmpty()) {
                                inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(container.getSize()));
                                for (int j = 0; j < container.getSize(); j++) {
                                    if (container.getItem(j) != null) {
                                        if (!container.getItem(j).getType().equals(Material.AIR)) {
                                            inv.setItem(j, container.getItem(j).clone());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ImageDisplayData data;
                    if (inv != null) {
                        data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM_CONTAINER, itemStack.clone(), new TitledInventoryWrapper(ItemStackUtils.getDisplayName(itemStack, false), inv));
                    } else {
                        data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM, itemStack.clone());
                    }
                    ValuePairs<List<DiscordMessageContent>, DiscordSRVInteractionHandler> pair = DiscordSRVContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    DiscordSRVInteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(resolvedComponent)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (Config.i().getSettings().embedDeleteAfter() > 0) {
                            message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                    if (Config.i().getSettings().embedDeleteAfter() > 0) {
                        message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                    }
                });
            }
        } else if (Config.i().getDiscordCommands().shareInventory().enabled() && (label.equalsIgnoreCase(INVENTORY_LABEL) || label.equalsIgnoreCase(INVENTORY_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel())).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (!options.isEmpty()) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked())).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                OfflinePlayerData offlinePlayerData = PlayerUtils.getData(offlineICPlayer);
                if (offlineICPlayer == null) {
                    if (Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (Config.i().getSettings().bungeecord() && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        BungeeMessageSender.forwardInventory(System.currentTimeMillis(), uuid, offlinePlayerData.isRightHanded(), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), null, offlinePlayerData.getInventory());
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                Component component = LegacyComponentSerializer.legacySection().deserialize(Config.i().getDiscordCommands().shareInventory().inGameMessage().text().replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String title = ChatColorUtils.stripColor(Config.i().getDiscordCommands().shareInventory().inventoryTitle().replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String sha1 = HashUtils.createSha1(true, offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), title, offlinePlayerData.getInventory());
                errorCode--;
                layout0(offlineICPlayer, sha1, title);
                errorCode--;
                layout1(offlineICPlayer, sha1, title);
                errorCode--;
                component = component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(DiscordSRVContentUtils.join(Config.i().getDiscordCommands().shareInventory().inGameMessage().hover(), true))));
                component = component.clickEvent(ClickEvent.runCommand("/multichat viewinv " + sha1));
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (Config.i().getDiscordCommands().shareInventory().isMainServer()) {
                    ImageDisplayData data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.INVENTORY, true, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getDefaultContainerTitle()), offlinePlayerData.getInventory()));
                    ValuePairs<List<DiscordMessageContent>, DiscordSRVInteractionHandler> pair = DiscordSRVContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    DiscordSRVInteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(component)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (Config.i().getSettings().embedDeleteAfter() > 0) {
                            message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                    if (Config.i().getSettings().embedDeleteAfter() > 0) {
                        message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                    }
                });
            }
        } else if (Config.i().getDiscordCommands().shareEnderChest().enabled() && (label.equals(ENDERCHEST_LABEL) || label.equals(ENDERCHEST_OTHER_LABEL))) {
            String minecraftChannel = discordsrv.getChannels().entrySet().stream().filter(entry -> channel.getId().equals(entry.getValue())).map(Map.Entry::getKey).findFirst().orElse(null);
            if (minecraftChannel == null) {
                if (Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel())).setEphemeral(true).queue();
                }
                return;
            }
            String discordUserId = event.getUser().getId();
            List<OptionMapping> options = event.getOptionsByType(OptionType.USER);
            if (!options.isEmpty()) {
                discordUserId = options.get(0).getAsUser().getId();
            }
            UUID uuid = discordsrv.getAccountLinkManager().getUuid(discordUserId);
            if (uuid == null) {
                if (Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
                    event.reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked())).setEphemeral(true).queue();
                }
                return;
            }
            int errorCode = -1;
            try {
                OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
                OfflinePlayerData offlinePlayerData = PlayerUtils.getData(offlineICPlayer);
                if (offlineICPlayer == null) {
                    if (Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
                        event.reply(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").setEphemeral(true).queue();
                    }
                    return;
                }
                errorCode--;
                if (Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
                    event.deferReply().queue();
                }
                errorCode--;
                Player icplayer = offlineICPlayer.getPlayer();
                if (Config.i().getSettings().bungeecord() && icplayer != null) {
                    if (PlayerUtils.isLocal(icplayer)) {
                        BungeeMessageSender.forwardEnderchest(System.currentTimeMillis(), uuid, offlinePlayerData.isRightHanded(), offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), null, icplayer.getEnderChest());
                    } else {
                        TimeUnit.MILLISECONDS.sleep(MultiChatDiscordSrvAddon.remoteDelay);
                    }
                }
                errorCode--;
                Component component = LegacyComponentSerializer.legacySection().deserialize(Config.i().getDiscordCommands().shareEnderChest().inGameMessage().text().replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String title = ChatColorUtils.stripColor(Config.i().getDiscordCommands().shareEnderChest().inventoryTitle().replace("{Player}", offlineICPlayer.getName()));
                errorCode--;
                String sha1 = HashUtils.createSha1(true, offlinePlayerData.getSelectedSlot(), offlinePlayerData.getXpLevel(), title, offlinePlayerData.getEnderChest());
                errorCode--;
                ender(offlineICPlayer, sha1, title);
                errorCode--;
                component = component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacySection().deserialize(DiscordSRVContentUtils.join(Config.i().getDiscordCommands().shareEnderChest().inGameMessage().hover(), true))));
                component = component.clickEvent(ClickEvent.runCommand("/multichat viewender " + sha1));
                errorCode--;
                String key = "<DiscordShare=" + UUID.randomUUID() + ">";
                components.put(key, component);
                Bukkit.getScheduler().runTaskLater(MultiChatDiscordSrvAddon.plugin, () -> components.remove(key), 100);
                errorCode--;
                if (DiscordSRV.config().getBoolean("DiscordChatChannelDiscordToMinecraft")) {
                    discordsrv.broadcastMessageToMinecraftServer(minecraftChannel, ComponentStringUtils.toDiscordSRVComponent(Component.text(key)), event.getUser());
                }
                if (Config.i().getDiscordCommands().shareEnderChest().isMainServer()) {
                    ImageDisplayData data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ENDERCHEST, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getEnderChestContainerTitle()), offlinePlayerData.getEnderChest()));
                    ValuePairs<List<DiscordMessageContent>, DiscordSRVInteractionHandler> pair = DiscordSRVContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                    List<DiscordMessageContent> contents = pair.getFirst();
                    DiscordSRVInteractionHandler interactionHandler = pair.getSecond();
                    errorCode--;

                    WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(component)));
                    List<MessageEmbed> embeds = new ArrayList<>();
                    int i = 0;
                    for (DiscordMessageContent content : contents) {
                        i += content.getAttachments().size();
                        if (i <= 10) {
                            ValuePairs<List<MessageEmbed>, Set<String>> valuePair = DiscordSRVMessageContentUtils.toJDAMessageEmbeds(content);
                            embeds.addAll(valuePair.getFirst());
                            for (Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                                if (valuePair.getSecond().contains(attachment.getKey())) {
                                    action = action.addFile(attachment.getValue(), attachment.getKey());
                                }
                            }
                        }
                    }
                    action.setEmbeds(embeds).setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> {
                        if (!interactionHandler.getInteractions().isEmpty()) {
                            DiscordInteractionEvents.register(message, interactionHandler, contents);
                        }
                        if (Config.i().getSettings().embedDeleteAfter() > 0) {
                            message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                        }
                    });
                }
            } catch (Throwable e) {
                e.printStackTrace();
                event.getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                    if (Config.i().getSettings().embedDeleteAfter() > 0) {
                        message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                    }
                });
            }
        }
    }

    public static class DiscordCommandRegistrationException extends RuntimeException {

        public DiscordCommandRegistrationException(String message) {
            super(message);
        }

        public DiscordCommandRegistrationException(Throwable cause) {
            super(cause);
        }

        public DiscordCommandRegistrationException(String message, Throwable throwable) {
            super(message, throwable);
        }

    }

}
