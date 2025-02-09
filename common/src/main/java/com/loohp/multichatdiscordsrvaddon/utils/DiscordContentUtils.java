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

package com.loohp.multichatdiscordsrvaddon.utils;

import com.cryptomorin.xseries.XMaterial;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.debug.Debug;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.multichatdiscordsrvaddon.listeners.DiscordInteractionEvents;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.CustomItemTextureRegistry;
import com.loohp.multichatdiscordsrvaddon.resources.ModelRenderer;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelDisplay.ModelDisplayPosition;
import com.loohp.multichatdiscordsrvaddon.utils.DiscordItemStackUtils.DiscordToolTip;
import com.loohp.multichatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.ActionRow;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.selections.SelectOption;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.selections.SelectionMenu;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.selections.SelectionMenuInteraction;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.WebhookMessageUpdateAction;
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.interactions.ReplyAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public class DiscordContentUtils {

    public static final Color OFFSET_WHITE = new Color(0xFFFFFE);

    public static final String BOOK_EMOJI = "\uD83D\uDCD6";
    public static final String LEFT_EMOJI = "\u2B05\uFE0F";
    public static final String RIGHT_EMOJI = "\u27A1\uFE0F";

    public static ValuePairs<List<DiscordMessageContent>, InteractionHandler> createContents(List<DiscordDisplayData> dataList, OfflinePlayer player) {
        List<DiscordMessageContent> contents = new ArrayList<>();
        List<ActionRow> interactionsToRegister = new ArrayList<>();
        List<String> interactions = new ArrayList<>();
        BiConsumer<GenericComponentInteractionCreateEvent, List<DiscordMessageContent>> interactionConsumer = (event, discordMessageContents) -> {};
        int i = -1;
        for (DiscordDisplayData data : dataList) {
            i++;
            if (data instanceof ImageDisplayData) {
                ImageDisplayData iData = (ImageDisplayData) data;
                ImageDisplayType type = iData.getType();
                String title = iData.getTitle();
                if (iData.getItem() != null) {
                    Debug.debug("createContents creating item discord content");
                    ItemStack item = iData.getItem();
                    Color color = DiscordItemStackUtils.getDiscordColor(item);
                    if (color == null || color.equals(Color.WHITE)) {
                        color = OFFSET_WHITE;
                    }
                    try {
                        BufferedImage image = ImageGeneration.getItemStackImage(item, data.getPlayer(), Config.i().getInventoryImage().item().alternateAirTexture(), 48);
                        byte[] imageData = ImageUtils.toArray(image);

                        DiscordMessageContent content = new DiscordMessageContent(title, null, color);
                        content.setTitle(DiscordItemStackUtils.getItemNameForDiscord(item, player, Config.i().getResources().language()));
                        content.setThumbnail("attachment://Item_" + i + ".png");

                        content.addAttachment("Item_" + i + ".png", imageData);
                        contents.add(content);

                        DiscordToolTip discordToolTip = DiscordItemStackUtils.getToolTip(item, player, Config.i().getToolTipSettings().showAdvanceDetails());
                        List<ToolTipComponent<?>> toolTipComponents = discordToolTip.isHideTooltip() ? new ArrayList<>() : discordToolTip.getComponents();

                        boolean forceShow = false;
                        if (type.equals(ImageDisplayType.ITEM_CONTAINER) && Config.i().getDiscordItemDetailsAndInteractions().showContainers()) {
                            TitledInventoryWrapper inv = iData.getInventory();
                            BufferedImage container = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                            toolTipComponents.add(ToolTipComponent.image(container));
                            forceShow = true;

                            if (Config.i().getDiscordItemDetailsAndInteractions().allowInventorySelection()) {
                                UUID interactionUuid = UUID.randomUUID();
                                List<SelectOption> options = new ArrayList<>();
                                for (int u = 0; u < inv.getInventory().getSize(); u++) {
                                    ItemStack itemStack = inv.getInventory().getItem(u);
                                    if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                                        continue;
                                    }
                                    Component name = ItemStackUtils.getDisplayName(itemStack);
                                    String label = (u + 1) + " - " + PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(name, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())));
                                    if (label.length() > 100) {
                                        ItemStack stripNameItem = itemStack.clone();
                                        if (stripNameItem.getItemMeta() != null) {
                                            ItemMeta meta = stripNameItem.getItemMeta();
                                            meta.setDisplayName(null);
                                            stripNameItem.setItemMeta(meta);
                                        }
                                        name = ItemStackUtils.getDisplayName(stripNameItem);
                                        label = (u + 1) + " - " + PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(name, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())));
                                    }
                                    options.add(SelectOption.of(label, String.valueOf(u)));
                                }
                                int j = 0;
                                for (int u = 0; u < options.size(); u += 25) {
                                    String id = DiscordInteractionEvents.INTERACTION_ID_PREFIX + "inventory_item_" + interactionUuid + "_" + ++j;
                                    interactionsToRegister.add(ActionRow.of(SelectionMenu.create(id).addOptions(options.subList(u, Math.min(u + 25, options.size()))).build()));
                                    interactions.add(id);
                                }
                                interactionConsumer = interactionConsumer.andThen(getInventoryHandler(interactionUuid, inv.getInventory(), data.getPlayer()));
                            }
                        } else if (iData.isFilledMap() && Config.i().getDiscordItemDetailsAndInteractions().showMaps()) {
                            forceShow = true;
                        }

                        if ((forceShow || !discordToolTip.isHideTooltip()) && (forceShow || !discordToolTip.isBaseItem() || Config.i().getInventoryImage().item().useTooltipImageOnBaseItem())) {
                            BufferedImage tooltip = ImageGeneration.getToolTipImage(toolTipComponents, NMS.getInstance().getCustomTooltipResourceLocation(item));

                            if (iData.isFilledMap() && Config.i().getDiscordItemDetailsAndInteractions().showMaps()) {
                                MapView mapView = FilledMapUtils.getMapView(item);
                                boolean isContextual = mapView == null || FilledMapUtils.isContextual(mapView);
                                Player icPlayer = iData.getPlayer().getPlayer();
                                boolean isPlayerLocal = icPlayer != null && PlayerUtils.isLocal(icPlayer);
                                if (!isContextual || isPlayerLocal) {
                                    BufferedImage map = ImageGeneration.getMapImage(item, isPlayerLocal ? icPlayer : null).get();
                                    tooltip = ImageUtils.resizeImage(tooltip, 5);
                                    tooltip = ImageUtils.appendImageBottom(tooltip, map, 10, 0);
                                }
                            }

                            byte[] tooltipData = ImageUtils.toArray(tooltip);
                            content.addAttachment("ToolTip_" + i + ".png", tooltipData);
                            content.addImageUrl("attachment://ToolTip_" + i + ".png");
                        }

                        if (iData.isBook() && Config.i().getDiscordItemDetailsAndInteractions().showBooks()) {
                            List<Component> pages = BookUtils.getPages((BookMeta) item.getItemMeta());
                            if (pages.isEmpty()) {
                                pages = Collections.singletonList(Component.empty());
                            }
                            List<Supplier<BufferedImage>> pageImages = ImageGeneration.getBookInterfaceSuppliers(pages);
                            byte[][] cachedPageImages = new byte[pageImages.size()][];
                            cachedPageImages[0] = ImageUtils.toArray(pageImages.get(0).get());
                            if (!pageImages.isEmpty()) {
                                UUID interactionUuid = UUID.randomUUID();
                                interactionsToRegister.add(ActionRow.of(Button.secondary(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "open_book_" + interactionUuid, BOOK_EMOJI)));
                                interactions.add(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "open_book_" + interactionUuid);
                                interactions.add(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid);
                                interactions.add(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid);
                                interactions.add(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid);
                                interactionConsumer = interactionConsumer.andThen(getBookHandler(interactionUuid, color, pageImages, cachedPageImages));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (iData.getInventory() != null) {
                    Debug.debug("createContents creating inventory discord content");
                    TitledInventoryWrapper inv = iData.getInventory();
                    try {
                        BufferedImage image;
                        if (iData.isPlayerInventory()) {
                            if (Config.i().getInventoryImage().inventory().usePlayerInventoryView()) {
                                image = ImageGeneration.getPlayerInventoryImage(inv.getInventory(), iData.getPlayer());
                            } else {
                                image = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                            }
                        } else {
                            image = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                        }
                        Color color;
                        switch (type) {
                            case ENDERCHEST:
                                color = ColorUtils.hex2Rgb(Config.i().getInventoryImage().enderChest().embedColor());
                                break;
                            case INVENTORY:
                                color = ColorUtils.hex2Rgb(Config.i().getInventoryImage().inventory().embedColor());
                                break;
                            default:
                                color = Color.black;
                                break;
                        }
                        byte[] imageData = ImageUtils.toArray(image);
                        DiscordMessageContent content = new DiscordMessageContent(title, null, null, "attachment://Inventory_" + i + ".png", color);
                        content.addAttachment("Inventory_" + i + ".png", imageData);
                        if (type.equals(ImageDisplayType.INVENTORY) && Config.i().getInventoryImage().inventory().showExperienceLevel()) {
                            OfflinePlayerData offlinePlayerData = PlayerUtils.getData(iData.getPlayer());

                            int level = offlinePlayerData.getXpLevel();
                            byte[] bottleData = ImageUtils.toArray(MultiChatDiscordSrvAddon.plugin.modelRenderer.render(32, 32, ModelRenderer.SINGLE_RENDER, MultiChatDiscordSrvAddon.plugin.getResourceManager(), MultiChatDiscordSrvAddon.plugin.getResourceManager().getResourceRegistry(CustomItemTextureRegistry.IDENTIFIER, CustomItemTextureRegistry.class).getItemPostResolveFunction("minecraft:item/experience_bottle", null, XMaterial.EXPERIENCE_BOTTLE.parseItem(), VersionManager.version.isOld(), null, null, null, null, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())).orElse(null), VersionManager.version.isOld(), "minecraft:item/experience_bottle", ModelDisplayPosition.GUI, false, null, null).getImage(0));
                            content.addAttachment("Level_" + i + ".png", bottleData);
                            content.setFooter(ComponentStringUtils.convertFormattedString(LanguageUtils.getTranslation(TranslationKeyUtils.getLevelTranslation(level), Config.i().getResources().language()).getResult(), level));
                            content.setFooterImageUrl("attachment://Level_" + i + ".png");
                        }
                        contents.add(content);

                        if (Config.i().getDiscordItemDetailsAndInteractions().allowInventorySelection()) {
                            UUID interactionUuid = UUID.randomUUID();
                            List<SelectOption> options = new ArrayList<>();
                            for (int u = 0; u < inv.getInventory().getSize(); u++) {
                                ItemStack itemStack = inv.getInventory().getItem(u);
                                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                                    continue;
                                }
                                Component name = ItemStackUtils.getDisplayName(itemStack);
                                String label = (u + 1) + " - " + PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(name, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())));
                                if (label.length() > 100) {
                                    ItemStack stripNameItem = itemStack.clone();
                                    if (stripNameItem.getItemMeta() != null) {
                                        ItemMeta meta = stripNameItem.getItemMeta();
                                        meta.setDisplayName(null);
                                        stripNameItem.setItemMeta(meta);
                                    }
                                    name = ItemStackUtils.getDisplayName(stripNameItem);
                                    label = (u + 1) + " - " + PlainTextComponentSerializer.plainText().serialize(ComponentStringUtils.resolve(name, MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language())));
                                }
                                options.add(SelectOption.of(label, String.valueOf(u)));
                            }
                            int j = 0;
                            for (int u = 0; u < options.size(); u += 25) {
                                String id = DiscordInteractionEvents.INTERACTION_ID_PREFIX + "inventory_item_" + interactionUuid + "_" + ++j;
                                interactionsToRegister.add(ActionRow.of(SelectionMenu.create(id).addOptions(options.subList(u, Math.min(u + 25, options.size()))).build()));
                                interactions.add(id);
                            }
                            interactionConsumer = interactionConsumer.andThen(getInventoryHandler(interactionUuid, inv.getInventory(), data.getPlayer()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (data instanceof HoverClickDisplayData) {
                Debug.debug("createContents creating hover event discord content");
                try {
                    HoverClickDisplayData hData = (HoverClickDisplayData) data;
                    String title = hData.getDisplayText();
                    Color color = hData.getColor();
                    DiscordMessageContent content = new DiscordMessageContent(title, null, color);
                    String body = "";
                    String preview = null;
                    if (hData.hasHover()) {
                        if (Config.i().getHoverEventDisplay().useTooltipImage()) {
                            Component print = hData.getHoverText();
                            BufferedImage tooltip = ImageGeneration.getToolTipImage(print, true, null);
                            byte[] tooltipData = ImageUtils.toArray(tooltip);
                            content.addAttachment("ToolTip_" + i + ".png", tooltipData);
                            content.addImageUrl("attachment://ToolTip_" + i + ".png");
                            content.addDescription(null);
                        } else {
                            body += ComponentStringUtils.stripColorAndConvertMagic(MultiChatComponentSerializer.legacySection().serialize(ComponentStringUtils.resolve(hData.getHoverText(), MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(Config.i().getResources().language()))));
                        }
                    }
                    if (hData.hasClick()) {
                        switch (hData.getClickAction()) {
                            case COPY_TO_CLIPBOARD:
                                if (!body.isEmpty()) {
                                    body += "\n\n";
                                }
                                body += LanguageUtils.getTranslation(TranslationKeyUtils.getCopyToClipboard(), Config.i().getResources().language()).getResult() + ": __" + hData.getClickValue() + "__";
                                break;
                            case OPEN_URL:
                                if (!body.isEmpty()) {
                                    body += "\n\n";
                                }
                                String url = hData.getClickValue();
                                body += LanguageUtils.getTranslation(TranslationKeyUtils.getOpenUrl(), Config.i().getResources().language()).getResult() + ": __" + url + "__";
                                if (URLRequestUtils.IMAGE_URL_PATTERN.matcher(url).matches() && URLRequestUtils.isAllowed(url)) {
                                    preview = url;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (!body.isEmpty()) {
                        content.addDescription(body);
                    }
                    if (Config.i().getHoverEventDisplay().showCursorImage()) {
                        BufferedImage image = MultiChatDiscordSrvAddon.plugin.getResourceManager().getTextureManager().getTexture(ResourceRegistry.IC_MISC_TEXTURE_LOCATION + "hover_cursor").getTexture();
                        byte[] imageData = ImageUtils.toArray(image);
                        content.setAuthorIconUrl("attachment://Hover_" + i + ".png");
                        content.addAttachment("Hover_" + i + ".png", imageData);
                    }
                    if (preview != null) {
                        content.addImageUrl(preview);
                    }
                    contents.add(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return new ValuePairs<>(contents, new InteractionHandler(interactionsToRegister, interactions, ((long) Config.i().getSettings().timeout() * 60 * 1000), interactionConsumer));
    }

    private static BiConsumer<GenericComponentInteractionCreateEvent, List<DiscordMessageContent>> getInventoryHandler(UUID interactionUuid, Inventory inventory, OfflinePlayer player) {
        ItemStack[] items = IntStream.range(0, inventory.getSize()).mapToObj(i -> {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                return null;
            }
            return itemStack.clone();
        }).toArray(ItemStack[]::new);
        AtomicReference<OfflinePlayer> offlinePlayerAtomicReference = new AtomicReference<>(player);
        return (event, discordMessageContents) -> {
            User self = DiscordSRV.getPlugin().getJda().getSelfUser();
            User user = event.getUser();
            if (self.equals(user)) {
                return;
            }
            String id = event.getComponent().getId();
            if (id.startsWith(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "inventory_item_" + interactionUuid + "_") && event.getInteraction() instanceof SelectionMenuInteraction) {
                int slot = Integer.parseInt(((SelectionMenuInteraction) event.getInteraction()).getValues().get(0));
                if (slot >= 0 && slot < items.length) {
                    event.deferReply().setEphemeral(true).queue();
                    Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                        OfflinePlayer offlineICPlayer = offlinePlayerAtomicReference.updateAndGet(p -> p instanceof Player ? (p.isOnline() ? p : Bukkit.getOfflinePlayer(p.getUniqueId())) : p);
                        try {
                            ItemStack item = items[slot];
                            if (item == null) {
                                item = new ItemStack(Material.AIR);
                            }

                            String title = PlaceholderParser.parse(offlineICPlayer, ComponentStringUtils.stripColorAndConvertMagic(Config.i().getInventoryImage().item().itemTitle()));
                            ImageDisplayData data;
                            Inventory inv = getBlockInventory(item);
                            if (inv != null) {
                                data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM_CONTAINER, item.clone(), new TitledInventoryWrapper(ItemStackUtils.getDisplayName(item, false), inv));
                            } else {
                                data = new ImageDisplayData(offlineICPlayer, 0, title, ImageDisplayType.ITEM, item.clone());
                            }
                            ValuePairs<List<DiscordMessageContent>, InteractionHandler> result = createContents(Collections.singletonList(data), offlineICPlayer);
                            DiscordMessageContent content = result.getFirst().get(0);
                            InteractionHandler interactionHandler = result.getSecond();

                            WebhookMessageUpdateAction<Message> action = event.getHook().setEphemeral(true).editOriginalEmbeds(content.toJDAMessageEmbeds().getFirst());
                            for (Map.Entry<String, byte[]> entry : content.getAttachments().entrySet()) {
                                action.addFile(entry.getValue(), entry.getKey());
                            }
                            action.setActionRows(interactionHandler.getInteractionToRegister()).queue(message -> DiscordInteractionEvents.register(message, interactionHandler, Collections.singletonList(content)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        };
    }

    public static Inventory getBlockInventory(ItemStack item) {
        Inventory inv = null;
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
            if (bsm instanceof InventoryHolder) {
                Inventory container = ((InventoryHolder) bsm).getInventory();
                if (!isInventoryEmpty(container)) {
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
        return inv;
    }

    private static boolean isInventoryEmpty(Inventory inventory) {
        return inventory.isEmpty();
    }

    private static BiConsumer<GenericComponentInteractionCreateEvent, List<DiscordMessageContent>> getBookHandler(UUID interactionUuid, Color color, List<Supplier<BufferedImage>> imageSuppliers, byte[][] cachedImages) {
        Map<String, AtomicInteger> currentPages = new ConcurrentHashMap<>();
        List<SelectOption> selectOptions = IntStream.range(1, cachedImages.length + 1).mapToObj(i -> {
            String asText = String.valueOf(i);
            return SelectOption.of(ComponentStringUtils.convertFormattedString(LanguageUtils.getTranslation(TranslationKeyUtils.getBookPageIndicator(), Config.i().getResources().language()).getResult(), i, cachedImages.length), asText);
        }).collect(Collectors.toList());
        return (event, discordMessageContents) -> {
            User self = DiscordSRV.getPlugin().getJda().getSelfUser();
            User user = event.getUser();
            if (self.equals(user)) {
                return;
            }
            String id = event.getComponent().getId();
            Message message = event.getMessage();

            if (id.equals(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "open_book_" + interactionUuid)) {
                AtomicInteger currentPage = new AtomicInteger(0);
                currentPages.put(user.getId(), currentPage);
                DiscordMessageContent bookContent = new DiscordMessageContent(null, null, null, "attachment://Page.png", color);
                bookContent.addAttachment("Page.png", cachedImages[0]);
                ValuePairs<List<MessageEmbed>, Set<String>> pair = bookContent.toJDAMessageEmbeds();
                ReplyAction action = event.replyEmbeds(pair.getFirst()).setEphemeral(true);
                for (String name : pair.getSecond()) {
                    action = action.addFile(bookContent.getAttachments().get(name), name);
                }
                Button leftButton = Button.danger(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid, LEFT_EMOJI).asDisabled();
                Button rightButton = Button.success(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid, RIGHT_EMOJI);
                if (cachedImages.length == 1) {
                    rightButton = rightButton.asDisabled();
                }
                SelectionMenu selectionMenu = SelectionMenu.create(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid).setRequiredRange(1, 1).addOptions(selectOptions).setDefaultValues(List.of("1")).build();
                action.addActionRows(ActionRow.of(leftButton, rightButton), ActionRow.of(selectionMenu)).queue(h -> h.retrieveOriginal().queue(m -> DiscordInteractionEvents.getInteractionData(id).getMessageIds().add(m.getTextChannel().getId() + "/" + m.getId())));
                return;
            }
            event.deferEdit().queue();
            Bukkit.getScheduler().runTaskAsynchronously(MultiChatDiscordSrvAddon.plugin, () -> {
                AtomicInteger currentPage = currentPages.get(user.getId());
                if (currentPage == null) {
                    currentPages.put(user.getId(), currentPage = new AtomicInteger(0));
                }
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (currentPage) {
                    if (id.equals(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid) && event.getInteraction() instanceof SelectionMenuInteraction) {
                        int pageNumber = currentPage.updateAndGet(i -> Integer.parseInt(((SelectionMenuInteraction) event.getInteraction()).getValues().get(0)) - 1);
                        byte[] pageFile = cachedImages[pageNumber];
                        if (pageFile == null) {
                            try {
                                cachedImages[pageNumber] = pageFile = ImageUtils.toArray(imageSuppliers.get(pageNumber).get());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(message.getContentRaw()).retainFiles(Collections.emptyList()).addFile(pageFile, "Page.png");
                        Button leftButton = Button.danger(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid, LEFT_EMOJI);
                        if (currentPage.get() <= 0) {
                            leftButton = leftButton.asDisabled();
                        }
                        Button rightButton = Button.success(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid, RIGHT_EMOJI);
                        if (currentPage.get() >= cachedImages.length - 1) {
                            rightButton = rightButton.asDisabled();
                        }
                        SelectionMenu selectionMenu = SelectionMenu.create(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid).setRequiredRange(1, 1).addOptions(selectOptions).setDefaultValues(List.of(String.valueOf(currentPage.get() + 1))).build();
                        action.setActionRows(ActionRow.of(leftButton, rightButton), ActionRow.of(selectionMenu)).queue();
                    } else if (id.equals(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid)) {
                        if (currentPage.get() > 0) {
                            int pageNumber = currentPage.decrementAndGet();
                            byte[] pageFile = cachedImages[pageNumber];
                            if (pageFile == null) {
                                try {
                                    cachedImages[pageNumber] = pageFile = ImageUtils.toArray(imageSuppliers.get(pageNumber).get());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(message.getContentRaw()).retainFiles(Collections.emptyList()).addFile(pageFile, "Page.png");
                            Button leftButton = Button.danger(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid, LEFT_EMOJI);
                            if (currentPage.get() <= 0) {
                                leftButton = leftButton.asDisabled();
                            }
                            Button rightButton = Button.success(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid, RIGHT_EMOJI);
                            if (currentPage.get() >= cachedImages.length - 1) {
                                rightButton = rightButton.asDisabled();
                            }
                            SelectionMenu selectionMenu = SelectionMenu.create(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid).setRequiredRange(1, 1).addOptions(selectOptions).setDefaultValues(List.of(String.valueOf(currentPage.get() + 1))).build();
                            action.setActionRows(ActionRow.of(leftButton, rightButton), ActionRow.of(selectionMenu)).queue();
                        }
                    } else if (id.equals(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid)) {
                        if (currentPage.get() < cachedImages.length - 1) {
                            int pageNumber = currentPage.incrementAndGet();
                            byte[] pageFile = cachedImages[pageNumber];
                            if (pageFile == null) {
                                try {
                                    cachedImages[pageNumber] = pageFile = ImageUtils.toArray(imageSuppliers.get(pageNumber).get());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            WebhookMessageUpdateAction<Message> action = event.getHook().editOriginal(message.getContentRaw()).retainFiles(Collections.emptyList()).addFile(pageFile, "Page.png");
                            Button leftButton = Button.danger(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "left_book_" + interactionUuid, LEFT_EMOJI);
                            if (currentPage.get() <= 0) {
                                leftButton = leftButton.asDisabled();
                            }
                            Button rightButton = Button.success(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "right_book_" + interactionUuid, RIGHT_EMOJI);
                            if (currentPage.get() >= cachedImages.length - 1) {
                                rightButton = rightButton.asDisabled();
                            }
                            SelectionMenu selectionMenu = SelectionMenu.create(DiscordInteractionEvents.INTERACTION_ID_PREFIX + "selection_book_" + interactionUuid).setRequiredRange(1, 1).addOptions(selectOptions).setDefaultValues(List.of(String.valueOf(currentPage.get() + 1))).build();
                            action.setActionRows(ActionRow.of(leftButton, rightButton), ActionRow.of(selectionMenu)).queue();
                        }
                    }
                }
            });
        };
    }

    public static String join(List<String> strings, boolean translateCodes) {
        String joined = String.join("\n", strings);
        if (translateCodes) return ChatColorUtils.translateAlternateColorCodes('&', joined);
            else return joined;
    }

}
