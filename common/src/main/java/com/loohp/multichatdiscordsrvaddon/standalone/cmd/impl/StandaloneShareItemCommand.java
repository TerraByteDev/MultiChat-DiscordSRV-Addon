package com.loohp.multichatdiscordsrvaddon.standalone.cmd.impl;

import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.bungee.BungeeMessageSender;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.listeners.discordsrv.DiscordInteractionEvents;
import com.loohp.multichatdiscordsrvaddon.modules.ItemDisplay;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.provider.DiscordProviderManager;
import com.loohp.multichatdiscordsrvaddon.standalone.cmd.StandaloneCommandUtils;
import com.loohp.multichatdiscordsrvaddon.standalone.event.StandaloneInteractionEvents;
import com.loohp.multichatdiscordsrvaddon.standalone.message.StandaloneDiscordMessageContentUtils;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneDiscordContentUtils;
import com.loohp.multichatdiscordsrvaddon.standalone.utils.StandaloneInteractionHandler;
import com.loohp.multichatdiscordsrvaddon.utils.*;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.incendo.cloud.discord.jda5.JDAInteraction;
import org.incendo.cloud.key.CloudKey;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.loohp.multichatdiscordsrvaddon.listeners.InboundEventListener.components;

public class StandaloneShareItemCommand {

    public static final CloudKey<Integer> slotKey = CloudKey.of(
            Config.i().getDiscordCommands().globalSettings().messages().slotLabel(),
            Integer.class
    );
    public static final CloudKey<String> armorKey = CloudKey.of(
            Config.i().getDiscordCommands().globalSettings().messages().slotLabel(),
            String.class
    );

    public static void shareItemCommand(
            JDAInteraction interaction
    ) {
        boolean isMCChannel = interaction.interactionEvent().getChannelId().equals(Config.i().getStandalone().channelId());
        if (!Config.i().getDiscordCommands().globalSettings().respondToCommandsInInvalidChannels() && !isMCChannel) {
            interaction.interactionEvent().reply(ChatColorUtils.stripColor(Config.i().getMessages().invalidDiscordChannel()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String discordUserID = interaction.user().getId();
        Optional<OptionMapping> options = interaction.getOptionMapping(StandalonePlayerInfoCommand.USER.name());
        if (!options.isEmpty()) {
            discordUserID = options.get().getAsUser().getId();
        }

        LinkedUser linkedUser = DiscordProviderManager.get().getLinkedUser(discordUserID);
        UUID uuid = linkedUser != null ? linkedUser.getUuid() : null;

        if (uuid == null) {
            if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                interaction.interactionEvent().reply(ChatColorUtils.stripColor(Config.i().getMessages().accountNotLinked())).setEphemeral(true).queue();
            }
            return;
        }

        int errorCode = -1;
        try {
            OfflinePlayer offlineICPlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlineICPlayer == null) {
                if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                    interaction.interactionEvent().reply(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").setEphemeral(true).queue();
                }
                return;
            }
            errorCode--;
            if (Config.i().getDiscordCommands().shareItem().isMainServer()) {
                interaction.interactionEvent().deferReply().queue();
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
            ItemStack itemStack = StandaloneCommandUtils.resolveItemStack(interaction.interactionEvent(), offlineICPlayer);
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

            for (Player player : Bukkit.getOnlinePlayers()) {
                ChatUtils.audience.player(player).sendMessage(Component.text(key));
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
                ValuePairs<List<DiscordMessageContent>, StandaloneInteractionHandler> pair = StandaloneDiscordContentUtils.createContents(Collections.singletonList(data), offlineICPlayer);
                List<DiscordMessageContent> contents = pair.getFirst();
                StandaloneInteractionHandler interactionHandler = pair.getSecond();
                errorCode--;

                WebhookMessageEditAction<Message> action = interaction.interactionEvent().getHook().editOriginal(ComponentStringUtils.stripColorAndConvertMagic(LegacyComponentSerializer.legacySection().serialize(resolvedComponent)));
                List<MessageEmbed> embeds = new ArrayList<>();
                int i = 0;
                for (DiscordMessageContent content : contents) {
                    i += content.getAttachments().size();
                    if (i <= 10) {
                        ValuePairs<List<MessageEmbed>, Set<String>> valuePair = StandaloneDiscordMessageContentUtils.toJDAMessageEmbeds(content);
                        embeds.addAll(valuePair.getFirst());

                        Collection<FileUpload> fileUploads = new HashSet<>();
                        for (Map.Entry<String, byte[]> attachment : content.getAttachments().entrySet()) {
                            if (valuePair.getSecond().contains(attachment.getKey())) {
                                fileUploads.add(FileUpload.fromData(attachment.getValue(), attachment.getKey()));
                            }
                        }
                        action = action.setFiles(fileUploads);
                    }
                }

                Collection<ItemComponent> componentCollection = new HashSet<>();
                for (ActionRow actionRow : interactionHandler.getInteractionToRegister()) {
                    componentCollection.addAll(actionRow.getComponents());
                }

                action.setEmbeds(embeds).setActionRow(componentCollection).queue(message -> {
                    if (!interactionHandler.getInteractions().isEmpty()) {
                        StandaloneInteractionEvents.register(message, interactionHandler, contents);
                    }
                    if (Config.i().getSettings().embedDeleteAfter() > 0) {
                        message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
            interaction.interactionEvent().getHook().editOriginal(ChatColorUtils.stripColor(Config.i().getMessages().unableToRetrieveData()) + " (" + errorCode + ")").queue(message -> {
                if (Config.i().getSettings().embedDeleteAfter() > 0) {
                    message.delete().queueAfter(Config.i().getSettings().embedDeleteAfter(), TimeUnit.SECONDS);
                }
            });
        }
    }

}
