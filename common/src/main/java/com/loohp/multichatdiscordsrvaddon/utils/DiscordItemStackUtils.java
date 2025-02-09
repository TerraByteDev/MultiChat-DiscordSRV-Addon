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

import com.google.common.collect.Multimap;
import com.cryptomorin.xseries.XMaterial;
import com.loohp.multichatdiscordsrvaddon.api.MultiChatDiscordSrvAddonAPI;
import com.loohp.multichatdiscordsrvaddon.config.Config;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.querz.nbt.tag.CompoundTag;
import org.apache.commons.lang3.math.Fraction;
import com.loohp.multichatdiscordsrvaddon.objectholders.ICMaterial;
import com.loohp.multichatdiscordsrvaddon.MultiChatDiscordSrvAddon;
import com.loohp.multichatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.multichatdiscordsrvaddon.nms.NMS;
import com.loohp.multichatdiscordsrvaddon.objectholders.EquipmentSlotGroup;
import com.loohp.multichatdiscordsrvaddon.objectholders.PaintingVariant;
import com.loohp.multichatdiscordsrvaddon.objectholders.ToolTipComponent;
import com.loohp.multichatdiscordsrvaddon.objectholders.ToolTipComponent.ToolTipType;
import com.loohp.multichatdiscordsrvaddon.resources.languages.SpecificTranslateFunction;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.KeybindComponent;
import github.scarsz.discordsrv.dependencies.kyori.adventure.text.TranslatableComponent;
import github.scarsz.discordsrv.dependencies.mcdiscordreserializer.discord.DiscordSerializer;
import github.scarsz.discordsrv.dependencies.mcdiscordreserializer.discord.DiscordSerializerOptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DecoratedPot;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.checkerframework.org.apache.commons.text.WordUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.loohp.multichatdiscordsrvaddon.utils.ComponentStringUtils.join;
import static com.loohp.multichatdiscordsrvaddon.utils.LanguageUtils.getTranslation;
import static com.loohp.multichatdiscordsrvaddon.utils.LanguageUtils.getTranslationKey;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getAttributeModifierKey;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBannerPatternItemName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBannerPatternName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBookAuthor;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBookGeneration;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBundleEmptyDescription;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getBundleLegacyFullness;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getCanDestroy;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getCanPlace;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getCrossbowProjectile;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getDiscFragmentName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getDurability;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getDyeColor;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getEffect;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getEffectLevel;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getEnchantmentDescription;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getEnchantmentLevel;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getEntityTypeName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFilledMapId;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFilledMapLevel;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFilledMapScale;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFireworkColor;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFireworkFade;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFireworkFlicker;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFireworkTrail;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getFireworkType;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getItemComponents;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getItemNbtTag;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getJukeboxSongDescription;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getModifierSlotGroupKey;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getModifierSlotKey;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getNoEffect;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPaintingDimension;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPotionDurationInfinite;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPotionWhenDrunk;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPotionWithAmplifier;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPotionWithDuration;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getPotterySherdName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getRocketFlightDuration;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateAppliesTo;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateArmorTrimAppliesTo;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateArmorTrimIngredients;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateIngredients;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateNetheriteUpgradeAppliesTo;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateNetheriteUpgradeIngredients;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSmithingTemplateUpgrade;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSpawnerDescription1;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getSpawnerDescription2;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getTrimPatternName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getTropicalFishBucketName;
import static com.loohp.multichatdiscordsrvaddon.utils.TranslationKeyUtils.getUnbreakable;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

@SuppressWarnings("deprecation")
public class DiscordItemStackUtils {
    
    private static final DecimalFormat ATTRIBUTE_FORMAT = new DecimalFormat("#.##");
    private static final DecimalFormat DURATION_FORMAT = new DecimalFormat("##.##");
    private static final int[] POTTERY_SHERD_ORDER = new int[] {3, 1, 2, 0};

    private static final boolean chatColorHasGetColor = Arrays.stream(ChatColor.class.getMethods()).anyMatch(each -> each.getName().equalsIgnoreCase("getColor") && each.getReturnType().equals(Color.class));

    private static ToolTipComponent<Component> tooltipEmpty() {
        return ToolTipComponent.empty();
    }
    
    private static ToolTipComponent<Component> tooltipText(Component component) {
        return ToolTipComponent.text(component);
    }

    private static ToolTipComponent<BufferedImage> tooltipImage(BufferedImage image) {
        return ToolTipComponent.image(image);
    }

    public static Color getDiscordColor(ItemStack item) {
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && !meta.getDisplayName().isEmpty()) {
                String colorStr = ChatColorUtils.getFirstColors(meta.getDisplayName());
                if (colorStr.length() > 1) {
                    ChatColor chatColor = ColorUtils.toChatColor(colorStr);
                    if (chatColor != null && ChatColorUtils.isColor(chatColor)) {
                        return chatColorHasGetColor ? chatColor.getColor() : ColorUtils.getColor(chatColor);
                    }
                }
            }
        }
        return chatColorHasGetColor ? RarityUtils.getRarityColor(item).getColor() : ColorUtils.getColor(RarityUtils.getRarityColor(item));
    }

    public static String getItemNameForDiscord(ItemStack item, OfflinePlayer player, String language) {
        SpecificTranslateFunction translationFunction = MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(language);

        Player bukkitPlayer = player == null || player.getPlayer() == null || !PlayerUtils.isLocal(player) ? null : player.getPlayer();
        if (bukkitPlayer == null && !Bukkit.getOnlinePlayers().isEmpty()) {
            bukkitPlayer = Bukkit.getOnlinePlayers().iterator().next();
        }
        item = MultiChatDiscordSrvAddonAPI.transformItemStack(item, bukkitPlayer == null ? null : bukkitPlayer.getUniqueId());

        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        ICMaterial icMaterial = ICMaterial.from(item);
        String name = MultiChatComponentSerializer.legacySection().serialize(ComponentStringUtils.resolve(ItemStackUtils.getDisplayName(item), translationFunction));
        if (item.getAmount() == 1 || item == null || item.getType().equals(Material.AIR)) {
            name = Config.i().getInventoryImage().item().embedDisplay().single().replace("{Item}", ComponentStringUtils.stripColorAndConvertMagic(name)).replace("{Amount}", String.valueOf(item.getAmount()));
        } else {
            name = Config.i().getInventoryImage().item().embedDisplay().multiple().replace("{Item}", ComponentStringUtils.stripColorAndConvertMagic(name)).replace("{Amount}", String.valueOf(item.getAmount()));
        }

        return name;
    }

    @SuppressWarnings({"UnstableApiUsage", "PatternValidation"})
    public static DiscordToolTip getToolTip(ItemStack item, OfflinePlayer player, boolean showAdvanceDetails) throws Exception {
        String language = Config.i().getResources().language();
        SpecificTranslateFunction translationFunction = MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(language);

        Player bukkitPlayer = player == null || player.getPlayer() == null || !PlayerUtils.isLocal(player) ? null : player.getPlayer();
        if (bukkitPlayer == null && !Bukkit.getOnlinePlayers().isEmpty()) {
            bukkitPlayer = Bukkit.getOnlinePlayers().iterator().next();
        }
        item = MultiChatDiscordSrvAddonAPI.transformItemStack(item, bukkitPlayer == null ? null : bukkitPlayer.getUniqueId());
        World world = bukkitPlayer == null ? null : bukkitPlayer.getWorld();

        List<ToolTipComponent<?>> prints = new ArrayList<>();
        boolean hasCustomName = false;

        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        ICMaterial icMaterial = ICMaterial.from(item);

        Component itemDisplayNameComponent = ItemStackUtils.getDisplayName(item);
        prints.add(tooltipText(itemDisplayNameComponent));

        boolean hasMeta = item.getItemMeta() != null;
        boolean hideAdditionalFlags = hasMeta && item.getItemMeta().hasItemFlag(NMS.getInstance().getHideAdditionalItemFlag()) && (item.getItemMeta() instanceof PotionMeta || VersionManager.version.isNewerOrEqualTo(MCVersion.V1_20_6));

        if (hasMeta && item.getItemMeta().getDisplayName() != null) {
            hasCustomName = true;
        }

        if (icMaterial.isMaterial(XMaterial.DECORATED_POT) && hasMeta && item.getItemMeta() instanceof BlockStateMeta && !hideAdditionalFlags) {
            BlockState state = ((BlockStateMeta) item.getItemMeta()).getBlockState();
            if (state instanceof DecoratedPot) {
                DecoratedPot pot = (DecoratedPot) state;
                List<Material> materials = pot.getShards();
                if (!materials.stream().allMatch(e -> e.equals(Material.BRICK))) {
                    prints.add(tooltipEmpty());
                    for (int i : POTTERY_SHERD_ORDER) {
                        if (i < materials.size()) {
                            Key material = Key.key(materials.get(i).getKey().toString());
                            prints.add(tooltipText(translatable(getPotterySherdName(material)).color(GRAY)));
                        }
                    }
                }
            }
        }

        if (VersionManager.version.isNewerOrEqualTo(MCVersion.V1_20) && hasMeta && item.getItemMeta() instanceof ArmorMeta) {
            ArmorMeta armorMeta = (ArmorMeta) item.getItemMeta();
            ArmorTrim armorTrim = armorMeta.getTrim();
            if (armorTrim != null && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ARMOR_TRIM)) {
                TrimPattern trimPattern = armorTrim.getPattern();
                TrimMaterial trimMaterial = armorTrim.getMaterial();
                Component trimPatternDescription = NMS.getInstance().getTrimPatternDescription(trimPattern, trimMaterial);
                Component trimMaterialDescription = NMS.getInstance().getTrimMaterialDescription(trimMaterial);
                prints.add(tooltipText(translatable(getSmithingTemplateUpgrade()).color(GRAY)));
                prints.add(tooltipText(text(" ").append(trimPatternDescription)));
                prints.add(tooltipText(text(" ").append(trimMaterialDescription)));
            }
        }

        if (icMaterial.isOneOf(Collections.singletonList("CONTAINS:smithing_template")) && !hideAdditionalFlags) {
            Key key = Key.key(item.getType().getKey().toString());
            prints.add(tooltipText(translatable(getTrimPatternName(key)).color(GRAY)));
            prints.add(tooltipEmpty());
            prints.add(tooltipText(translatable(getSmithingTemplateAppliesTo()).color(GRAY)));
            boolean isNetheriteUpgrade = icMaterial.isMaterial(XMaterial.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
            if (isNetheriteUpgrade) {
                prints.add(tooltipText(text(" ").append(translatable(getSmithingTemplateNetheriteUpgradeAppliesTo()).color(BLUE))));
            } else {
                prints.add(tooltipText(text(" ").append(translatable(getSmithingTemplateArmorTrimAppliesTo()).color(BLUE))));
            }
            prints.add(tooltipText(translatable(getSmithingTemplateIngredients()).color(GRAY)));
            if (isNetheriteUpgrade) {
                prints.add(tooltipText(text(" ").append(translatable(getSmithingTemplateNetheriteUpgradeIngredients()).color(BLUE))));
            } else {
                prints.add(tooltipText(text(" ").append(translatable(getSmithingTemplateArmorTrimIngredients()).color(BLUE))));
            }
        }

        if (icMaterial.isMaterial(XMaterial.PAINTING) && !hideAdditionalFlags) {
            PaintingVariant paintingVariant = NMS.getInstance().getPaintingVariant(item);
            if (paintingVariant != null) {
                prints.add(tooltipText(paintingVariant.getTitle()));
                prints.add(tooltipText(paintingVariant.getAuthor()));
                prints.add(tooltipText(translatable(getPaintingDimension()).arguments(text(paintingVariant.getBlockWidth()), text(paintingVariant.getBlockHeight())).color(WHITE)));
                prints.add(tooltipImage(ImageGeneration.getPaintingImage(paintingVariant)));
            }
        }

        if (icMaterial.isMaterial(XMaterial.SPAWNER) && hasMeta && item.getItemMeta() instanceof BlockStateMeta && !hideAdditionalFlags) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            EntityType entityType = null;
            if (meta.hasBlockState()) {
                BlockState blockState = meta.getBlockState();
                if (blockState instanceof CreatureSpawner) {
                    CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState();
                    entityType = spawner.getSpawnedType();
                }
            }
            if (entityType == null) {
                prints.add(tooltipEmpty());
                prints.add(tooltipText(translatable(getSpawnerDescription1()).color(GRAY)));
                prints.add(tooltipText(text(" ").append(translatable(getSpawnerDescription2()).color(BLUE))));
            } else {
                prints.add(tooltipText(translatable(getEntityTypeName(entityType)).color(GRAY)));
            }
        }

        if (icMaterial.isMaterial(XMaterial.GOAT_HORN) && !hideAdditionalFlags) {
            Component description = NMS.getInstance().getInstrumentDescription(item);
            if (description != null) {
                prints.add(tooltipText(description.color(GRAY)));
            }
        }

        if (icMaterial.isOneOf(Collections.singletonList("CONTAINS:bundle")) && hasMeta && item.getItemMeta() instanceof BundleMeta && !hideAdditionalFlags) {
            List<ItemStack> items = ((BundleMeta) item.getItemMeta()).getItems();
            BufferedImage contentsImage = ImageGeneration.getBundleContainerInterface(player, items);
            Fraction weight = BundleUtils.getWeight(items);
            if (VersionManager.version.isNewerThan(MCVersion.V1_21_1)) {
                if (weight.compareTo(Fraction.ZERO) <= 0) {
                    prints.add(tooltipText(translatable(getBundleEmptyDescription()).color(GRAY)));
                }
                prints.add(tooltipImage(contentsImage));
            } else {
                prints.add(tooltipImage(contentsImage));
                int fullness = weight.getNumerator() * 64 / weight.getDenominator();
                prints.add(tooltipText(translatable(getBundleLegacyFullness()).arguments(text(fullness), text(64)).color(GRAY)));
            }
        }

        if (icMaterial.isMaterial(XMaterial.WRITTEN_BOOK) && hasMeta && item.getItemMeta() instanceof BookMeta && !hideAdditionalFlags) {
            BookMeta meta = (BookMeta) item.getItemMeta();
            String author = meta.getAuthor();
            if (author != null) {
                prints.add(tooltipText(translatable(getBookAuthor()).arguments(text(author)).color(GRAY)));
            }
            Generation generation = meta.getGeneration();
            if (generation == null) {
                generation = Generation.ORIGINAL;
            }
            prints.add(tooltipText(translatable(getBookGeneration(generation)).color(GRAY)));
        }

        if (icMaterial.isMaterial(XMaterial.SHIELD) && (!hideAdditionalFlags)) {
            if (NMS.getInstance().hasBlockEntityTag(item)) {
                List<Pattern> patterns = Collections.emptyList();
                if (!(item.getItemMeta() instanceof BannerMeta)) {
                    if (item.getItemMeta() instanceof BlockStateMeta) {
                        BlockStateMeta bmeta = (BlockStateMeta) item.getItemMeta();
                        if (bmeta.hasBlockState()) {
                            Banner bannerBlockMeta = (Banner) bmeta.getBlockState();
                            patterns = bannerBlockMeta.getPatterns();
                        }
                    }
                } else {
                    BannerMeta meta = (BannerMeta) item.getItemMeta();
                    patterns = meta.getPatterns();
                }

                int count = 0;
                for (Pattern pattern : patterns) {
                    if (++count > 6) {
                        break;
                    }
                    PatternType type = pattern.getPattern();
                    prints.add(tooltipText(translatable(getBannerPatternName(type, pattern.getColor())).color(GRAY)));
                }
            }
        }

        if (icMaterial.isOneOf(Collections.singletonList("CONTAINS:banner")) && (!hideAdditionalFlags)) {
            List<Pattern> patterns = Collections.emptyList();
            if (!(item.getItemMeta() instanceof BannerMeta)) {
                if (item.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta bmeta = (BlockStateMeta) item.getItemMeta();
                    Banner bannerBlockMeta = (Banner) bmeta.getBlockState();
                    patterns = bannerBlockMeta.getPatterns();
                }
            } else {
                BannerMeta meta = (BannerMeta) item.getItemMeta();
                patterns = meta.getPatterns();
            }

            int count = 0;
            for (Pattern pattern : patterns) {
                if (++count > 6) {
                    break;
                }
                PatternType type = pattern.getPattern();
                prints.add(tooltipText(translatable(getBannerPatternName(type, pattern.getColor())).color(GRAY)));
            }
        }

        if (icMaterial.isMaterial(XMaterial.TROPICAL_FISH_BUCKET) && !hideAdditionalFlags) {
            List<String> translations = getTropicalFishBucketName(item);
            if (!translations.isEmpty()) {
                prints.add(tooltipText(translatable(translations.get(0)).color(GRAY).decorate(TextDecoration.ITALIC)));
                if (translations.size() > 1) {
                    prints.add(tooltipText(join(empty().color(GRAY).decorate(TextDecoration.ITALIC), text(", "), translations.stream().skip(1).map(each -> translatable(each)))));
                }
            }
        }

        if (NMS.getInstance().isJukeboxPlayable(item) && NMS.getInstance().shouldSongShowInToolTip(item)) {
            prints.add(tooltipText(getJukeboxSongDescription(item).color(GRAY)));
        }

        if (icMaterial.isOneOf(Collections.singletonList("CONTAINS:disc_fragment")) && !hideAdditionalFlags) {
            prints.add(tooltipText(translatable(getDiscFragmentName(item)).color(GRAY)));
        }

        if (icMaterial.isOneOf(Collections.singletonList("CONTAINS:banner_pattern")) && !hideAdditionalFlags) {
            prints.add(tooltipText(translatable(getBannerPatternItemName(icMaterial)).color(GRAY)));
        }

        if (icMaterial.isMaterial(XMaterial.FIREWORK_ROCKET) && !hideAdditionalFlags) {
            OptionalInt flight = NMS.getInstance().getFireworkFlightDuration(item);
            if (flight.isPresent()) {
                prints.add(tooltipText(translatable(getRocketFlightDuration()).append(text(" " + flight.getAsInt())).color(GRAY)));
            }
            if (hasMeta && item.getItemMeta() instanceof FireworkMeta) {
                FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                for (FireworkEffect fireworkEffect : fireworkMeta.getEffects()) {
                    prints.add(tooltipText(translatable(getFireworkType(fireworkEffect.getType())).color(GRAY)));
                    if (!fireworkEffect.getColors().isEmpty()) {
                        prints.add(tooltipText(join(text("  ").color(GRAY), text(", "), fireworkEffect.getColors().stream().map(each -> translatable(getFireworkColor(each))))));
                    }
                    if (!fireworkEffect.getFadeColors().isEmpty()) {
                        prints.add(tooltipText(join(text("  ").append(translatable(getFireworkFade())).append(text(" ")).color(GRAY), text(", "), fireworkEffect.getFadeColors().stream().map(each -> translatable(getFireworkColor(each))))));
                    }
                    if (fireworkEffect.hasTrail()) {
                        prints.add(tooltipText(text("  ").append(translatable(getFireworkTrail())).color(GRAY)));
                    }
                    if (fireworkEffect.hasFlicker()) {
                        prints.add(tooltipText(text("  ").append(translatable(getFireworkFlicker())).color(GRAY)));
                    }
                }
            }
        }

        if (icMaterial.isMaterial(XMaterial.FIREWORK_STAR) && hasMeta && item.getItemMeta() instanceof FireworkEffectMeta && !hideAdditionalFlags) {
            FireworkEffectMeta fireworkEffectMeta = (FireworkEffectMeta) item.getItemMeta();
            FireworkEffect fireworkEffect = fireworkEffectMeta.getEffect();
            prints.add(tooltipText(translatable(getFireworkType(fireworkEffect.getType())).color(GRAY)));
            if (!fireworkEffect.getColors().isEmpty()) {
                prints.add(tooltipText(join(text("  ").color(GRAY), text(", "), fireworkEffect.getColors().stream().map(each -> translatable(getFireworkColor(each))))));
            }
            if (!fireworkEffect.getFadeColors().isEmpty()) {
                prints.add(tooltipText(join(text("  ").append(translatable(getFireworkFade())).append(text(" ")).color(GRAY), text(", "), fireworkEffect.getFadeColors().stream().map(each -> translatable(getFireworkColor(each))))));
            }
            if (fireworkEffect.hasTrail()) {
                prints.add(tooltipText(text("  ").append(translatable(getFireworkTrail())).color(GRAY)));
            }
            if (fireworkEffect.hasFlicker()) {
                prints.add(tooltipText(text("  ").append(translatable(getFireworkFlicker())).color(GRAY)));
            }
        }

        if (icMaterial.isMaterial(XMaterial.CROSSBOW) && !hideAdditionalFlags) {
            CrossbowMeta meta = (CrossbowMeta) item.getItemMeta();
            List<ItemStack> charged = meta.getChargedProjectiles();
            if (charged != null && !charged.isEmpty()) {
                ItemStack charge = charged.get(0);
                List<ToolTipComponent<?>> chargedItemInfo = getToolTip(charge, player, false).getComponents();
                Component chargeItemName = chargedItemInfo.get(0).getToolTipComponent(ToolTipType.TEXT);
                prints.add(tooltipText(translatable(getCrossbowProjectile()).color(WHITE).append(text(" [").color(WHITE)).append(chargeItemName).append(text("]").color(WHITE))));
                if (Config.i().getToolTipSettings().showFireworkRocketDetailsInCrossbow() && ICMaterial.from(charge).isMaterial(XMaterial.FIREWORK_ROCKET)) {
                    chargedItemInfo.stream().skip(1).forEachOrdered(each -> {
                        if (each.getType().equals(ToolTipType.TEXT)) {
                            prints.add(tooltipText(text("  ").append(each.getToolTipComponent(ToolTipType.TEXT))));
                        } else {
                            prints.add(each);
                        }
                    });
                }
            }
        }

        if (Config.i().getToolTipSettings().showMapScale() && FilledMapUtils.isFilledMap(item) && !hideAdditionalFlags) {
            MapMeta map = (MapMeta) item.getItemMeta();
            MapView mapView = FilledMapUtils.getMapView(item);
            int id = FilledMapUtils.getMapId(item);
            int scale = mapView == null ? 0 : mapView.getScale().getValue();
            if (!VersionManager.version.isLegacy()) {
                prints.add(tooltipText(translatable(getFilledMapId()).arguments(text(id)).color(GRAY)));
            } else {
                prints.set(0, tooltipText(prints.get(0).getToolTipComponent(ToolTipType.TEXT).append(text(" (#" + id + ")").color(GRAY))));
            }
            prints.add(tooltipText(translatable(getFilledMapScale()).arguments(text((int) Math.pow(2, scale))).color(GRAY)));
            prints.add(tooltipText(translatable(getFilledMapLevel()).arguments(text(scale), text(4)).color(GRAY)));
        }

        if (!hideAdditionalFlags) {
            if (item.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                List<PotionEffect> effects = PotionUtils.getAllPotionEffects(item);

                if (effects.isEmpty()) {
                    prints.add(tooltipText(translatable(getNoEffect()).color(GRAY)));
                } else {
                    Map<String, AttributeModifier> attributes = new HashMap<>();
                    for (PotionEffect effect : effects) {
                        if (VersionManager.version.isLegacy()) {
                            String key = getEffect(effect.getType());
                            String translation = getTranslation(key, language).getResult();
                            String description = "";
                            if (key.equals(translation)) {
                                description += WordUtils.capitalize(effect.getType().getName().toLowerCase().replace("_", " "));
                            } else {
                                description += translation;
                            }
                            int amplifier = effect.getAmplifier();
                            String effectLevelTranslation = getTranslation(getEffectLevel(amplifier), language).getResult();
                            if (!effectLevelTranslation.isEmpty()) {
                                description += " " + effectLevelTranslation;
                            }
                            if (!effect.getType().isInstant()) {
                                if (icMaterial.isMaterial(XMaterial.LINGERING_POTION)) {
                                    description += " (" + TimeUtils.getReadableTimeBetween(0, effect.getDuration() / 4 * 50L, ":", ChronoUnit.MINUTES, ChronoUnit.SECONDS, true) + ")";
                                } else {
                                    description += " (" + TimeUtils.getReadableTimeBetween(0, effect.getDuration() * 50L, ":", ChronoUnit.MINUTES, ChronoUnit.SECONDS, true) + ")";
                                }
                            }
                            ChatColor color;
                            try {
                                color = PotionUtils.getPotionEffectChatColor(effect.getType());
                            } catch (Throwable e) {
                                color = ChatColor.BLUE;
                            }
                            prints.add(tooltipText(LegacyComponentSerializer.legacySection().deserialize(color + description)));
                        } else {
                            String key = getEffect(effect.getType());
                            String potionName;
                            if (key.equals(getTranslation(key, language).getResult())) {
                                potionName = WordUtils.capitalize(effect.getType().getName().toLowerCase().replace("_", " "));
                            } else {
                                potionName = key;
                            }
                            int amplifier = effect.getAmplifier();
                            int duration;
                            if (effect.getType().isInstant()) {
                                duration = 0;
                            } else {
                                duration = effect.getDuration() * 50;
                                if (icMaterial.isMaterial(XMaterial.LINGERING_POTION)) {
                                    duration /= 4;
                                }
                            }
                            TextColor color;
                            try {
                                color = ColorUtils.toTextColor(PotionUtils.getPotionEffectChatColor(effect.getType()));
                            } catch (Throwable e) {
                                color = BLUE;
                            }
                            Component component = translatable(potionName);
                            if (amplifier > 0) {
                                component = translatable(getPotionWithAmplifier()).arguments(component, translatable(getEffectLevel(amplifier)));
                            }
                            if (duration > 20) {
                                Component time;
                                if (effect.getDuration() == -1) {
                                    time = translatable(getPotionDurationInfinite());
                                } else {
                                    time = text(TimeUtils.getReadableTimeBetween(0, duration, ":", ChronoUnit.MINUTES, ChronoUnit.SECONDS, true));
                                }
                                component = translatable(getPotionWithDuration()).arguments(component, time);
                            }
                            prints.add(tooltipText(component.color(color)));
                        }
                        attributes.putAll(PotionUtils.getPotionAttributes(effect));
                    }
                    if (!attributes.isEmpty()) {
                        prints.add(tooltipEmpty());
                        prints.add(tooltipText(translatable(getPotionWhenDrunk()).color(DARK_PURPLE)));
                        for (Entry<String, AttributeModifier> entry : attributes.entrySet()) {
                            String attributeName = entry.getKey();
                            AttributeModifier attributeModifier = entry.getValue();
                            double amount = attributeModifier.getAmount();
                            int operation = attributeModifier.getOperation().ordinal();
                            if (!(operation != 1 && operation != 2)) {
                                amount *= 100;
                            }
                            prints.add(tooltipText(translatable(getAttributeModifierKey(false, amount, operation)).arguments(text(DURATION_FORMAT.format(Math.abs(amount))), translatable(attributeName)).color(amount < 0 ? RED : BLUE)));
                        }
                    }
                }
            }
        }

        if (!hasMeta || (hasMeta && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))) {
            Map<Enchantment, Integer> enchantments;
            if (hasMeta && item.getItemMeta() instanceof EnchantmentStorageMeta) {
                enchantments = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
            } else {
                enchantments = item.getEnchantments();
            }
            for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                Component description = getEnchantmentDescription(enchantment);
                String enchantmentName;
                if (description instanceof net.kyori.adventure.text.TranslatableComponent) {
                    String key = ((net.kyori.adventure.text.TranslatableComponent) description).key();
                    if (key.equals(getTranslation(key, language).getResult())) {
                        continue;
                    }
                }
                if (enchantment.getMaxLevel() == 1 && level == 1) {
                    prints.add(tooltipText(description.color(GRAY)));
                } else {
                    prints.add(tooltipText(description.append(text(" ")).append(translatable(getEnchantmentLevel(level))).color(GRAY)));
                }
            }
        }

        if (Config.i().getToolTipSettings().showArmorColor() && hasMeta && item.getItemMeta() instanceof LeatherArmorMeta && item.getItemMeta().getItemFlags().stream().noneMatch(each -> each.name().equals("HIDE_DYE"))) {
            OptionalInt colorInt = NMS.getInstance().getLeatherArmorColor(item);
            if (colorInt.isPresent()) {
                Color color = new Color(colorInt.getAsInt());
                String hex = ColorUtils.rgb2Hex(color).toUpperCase();
                prints.add(tooltipText(translatable(getDyeColor()).arguments(text(hex)).color(GRAY)));
            }
        }

        if (hasMeta) {
            List<Component> loreLines = ItemStackUtils.getLore(item);
            if (loreLines != null) {
                for (Component lore : loreLines) {
                    Component component = lore.applyFallbackStyle(Style.style(DARK_PURPLE, TextDecoration.ITALIC));
                    prints.add(tooltipText(component));
                }
            }
        }

        if (hasMeta && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)) {
            if (VersionManager.version.isNewerOrEqualTo(MCVersion.V1_21)) {
                Map<EquipmentSlotGroup, List<ToolTipComponent<?>>> tooltips = new EnumMap<>(EquipmentSlotGroup.class);
                Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> attributeList = AttributeModifiersUtils.getAttributeModifiers(item);
                for (Entry<EquipmentSlotGroup, Multimap<String, AttributeModifier>> entry : attributeList.entrySet()) {
                    for (Entry<String, AttributeModifier> subEntry : entry.getValue().entries()) {
                        AttributeModifier attributemodifier = subEntry.getValue();
                        String attributeName = subEntry.getKey();
                        double amount = attributemodifier.getAmount();
                        AttributeModifier.Operation operation = attributemodifier.getOperation();

                        boolean flag = false;

                        if (bukkitPlayer != null) {
                            Key attributeModifierKey = NMS.getInstance().getAttributeModifierKey(attributemodifier);
                            if (attributeModifierKey.equals(AttributeModifiersUtils.BASE_ATTACK_DAMAGE_MODIFIER_ID)) {
                                amount += bukkitPlayer.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
                                flag = true;
                            } else if (attributeModifierKey.equals(AttributeModifiersUtils.BASE_ATTACK_SPEED_MODIFIER_ID)) {
                                amount += bukkitPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
                                flag = true;
                            }
                        }
                        if (!attributemodifier.getOperation().equals(AttributeModifier.Operation.ADD_SCALAR) && !attributemodifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_SCALAR_1)) {
                            if (attributeName.equals(AttributeModifiersUtils.GENERIC_KNOCKBACK_RESISTANCE)) {
                                amount = amount * 10.0D;
                            }
                        } else {
                            amount = amount * 100.0D;
                        }

                        if (flag || amount != 0) {
                            TextColor color = flag ? DARK_GREEN : (amount < 0 ? RED : BLUE);
                            Component component = translatable(getAttributeModifierKey(flag, amount, operation.ordinal())).arguments(text(ATTRIBUTE_FORMAT.format(Math.abs(amount))), translatable(attributeName)).color(color);
                            if (flag) {
                                component = text(" ").append(component);
                            }
                            ToolTipComponent<?> attributeComponent = tooltipText(component);
                            tooltips.computeIfAbsent(entry.getKey(), k -> new LinkedList<>()).add(attributeComponent);
                        }
                    }
                }
                for (EquipmentSlotGroup equipmentSlotGroup : EquipmentSlotGroup.values()) {
                    List<ToolTipComponent<?>> lines = tooltips.get(equipmentSlotGroup);
                    if (lines != null) {
                        String modifierSlotKey = getModifierSlotGroupKey(equipmentSlotGroup);
                        prints.add(tooltipEmpty());
                        prints.add(tooltipText(translatable(modifierSlotKey).color(GRAY)));
                        prints.addAll(lines);
                    }
                }
            } else {
                Map<EquipmentSlot, List<ToolTipComponent<?>>> tooltips = new EnumMap<>(EquipmentSlot.class);
                Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> attributeList = AttributeModifiersUtils.getAttributeModifiers(item);
                for (Entry<EquipmentSlotGroup, Multimap<String, AttributeModifier>> entry : attributeList.entrySet()) {
                    for (Entry<String, AttributeModifier> subEntry : entry.getValue().entries()) {
                        AttributeModifier attributemodifier = subEntry.getValue();
                        String attributeName = subEntry.getKey();
                        double amount = attributemodifier.getAmount();
                        AttributeModifier.Operation operation = attributemodifier.getOperation();

                        boolean flag = false;

                        if (bukkitPlayer != null) {
                            if (attributemodifier.getUniqueId().equals(AttributeModifiersUtils.BASE_ATTACK_DAMAGE_UUID)) {
                                amount += bukkitPlayer.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
                                amount += NMS.getInstance().getEnchantmentDamageBonus(item, null);
                                flag = true;
                            } else if (attributemodifier.getUniqueId().equals(AttributeModifiersUtils.BASE_ATTACK_SPEED_UUID)) {
                                amount += bukkitPlayer.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
                                flag = true;
                            }
                        }

                        if (!attributemodifier.getOperation().equals(AttributeModifier.Operation.ADD_SCALAR) && !attributemodifier.getOperation().equals(AttributeModifier.Operation.MULTIPLY_SCALAR_1)) {
                            if (attributeName.equals(AttributeModifiersUtils.GENERIC_KNOCKBACK_RESISTANCE)) {
                                amount = amount * 10.0D;
                            }
                        } else {
                            amount = amount * 100.0D;
                        }

                        if (amount != 0) {
                            TextColor color = flag ? DARK_GREEN : (amount < 0 ? RED : BLUE);
                            Component component = translatable(getAttributeModifierKey(flag, amount, operation.ordinal())).arguments(text(ATTRIBUTE_FORMAT.format(Math.abs(amount))), translatable(attributeName)).color(color);
                            if (flag) {
                                component = text(" ").append(component);
                            }
                            ToolTipComponent<?> attributeComponent = tooltipText(component);
                            List<EquipmentSlot> slots = entry.getKey().getEquipmentSlots();
                            if (!slots.isEmpty()) {
                                tooltips.computeIfAbsent(slots.get(0), k -> new LinkedList<>()).add(attributeComponent);
                            }
                        }
                    }
                }
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    List<ToolTipComponent<?>> lines = tooltips.get(equipmentSlot);
                    if (lines != null) {
                        String modifierSlotKey = getModifierSlotKey(equipmentSlot);
                        prints.add(tooltipEmpty());
                        prints.add(tooltipText(translatable(modifierSlotKey).color(GRAY)));
                        prints.addAll(lines);
                    }
                }
            }
        }

        if (hasMeta && NMS.getInstance().isItemUnbreakable(item) && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)) {
            prints.add(tooltipText(translatable(getUnbreakable()).color(BLUE)));
        }

        if (hasMeta && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_DESTROYS)) {
            List<ICMaterial> materialList = NMS.getInstance().getItemCanDestroyList(item);
            if (!materialList.isEmpty()) {
                prints.add(tooltipEmpty());
                prints.add(tooltipText(translatable(getCanDestroy()).color(GRAY)));
                for (ICMaterial parsedICMaterial : materialList) {
                    prints.add(tooltipText(translatable(getTranslationKey(parsedICMaterial.parseItem())).color(DARK_GRAY)));
                }
            }
        }

        if (hasMeta && !item.getItemMeta().hasItemFlag(ItemFlag.HIDE_PLACED_ON)) {
            List<ICMaterial> materialList = NMS.getInstance().getItemCanPlaceOnList(item);
            if (!materialList.isEmpty()) {
                prints.add(tooltipEmpty());
                prints.add(tooltipText(translatable(getCanPlace()).color(GRAY)));
                for (ICMaterial parsedICMaterial : materialList) {
                    prints.add(tooltipText(translatable(getTranslationKey(parsedICMaterial.parseItem())).color(DARK_GRAY)));
                }
            }
        }

        if (Config.i().getToolTipSettings().showDurability() && item.getType().getMaxDurability() > 0) {
            int durability = item.getType().getMaxDurability() - (VersionManager.version.isLegacy() ? item.getDurability() : ((Damageable) item.getItemMeta()).getDamage());
            int maxDur = item.getType().getMaxDurability();
            if (durability < maxDur) {
                prints.add(tooltipText(translatable(getDurability()).arguments(text(durability), text(maxDur)).color(WHITE)));
            }
        }
        if (showAdvanceDetails) {
            if (VersionManager.version.isNewerOrEqualTo(MCVersion.V1_20_6)) {
                prints.add(tooltipText(text(item.getType().getKey().toString()).color(DARK_GRAY)));
                int size = NMS.getInstance().getItemComponentsSize(item);
                if (size > 0) {
                    prints.add(tooltipText(translatable(getItemComponents()).arguments(text(size)).color(DARK_GRAY)));
                }
            } else {
                CompoundTag nbt = (CompoundTag) NBTParsingUtils.fromSNBT(ItemNBTUtils.getNMSItemStackJson(item));
                prints.add(tooltipText(text(nbt.getString("id")).color(DARK_GRAY)));
                CompoundTag tag = nbt.getCompoundTag("tag");
                if (tag != null) {
                    prints.add(tooltipText(translatable(getItemNbtTag()).arguments(text(tag.size())).color(DARK_GRAY)));
                }
            }
        }

        return new DiscordToolTip(prints, !hasCustomName && prints.size() <= 1, NMS.getInstance().shouldHideTooltip(item));
    }

    public static String toDiscordText(List<ToolTipComponent<?>> toolTipComponents, Function<ToolTipComponent<BufferedImage>, Component> imageToolTipHandler, String language, boolean embedLinks) {
        SpecificTranslateFunction translationFunction = MultiChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(language);
        DiscordSerializer serializerSpecial = new DiscordSerializer(DiscordSerializerOptions.defaults().withEmbedLinks(embedLinks));
        Function<?, String> resolver = component -> serializerSpecial.serialize(ComponentStringUtils.toDiscordSRVComponent(ComponentStringUtils.resolve((Component) component, translationFunction)));
        DiscordSerializer serializerRegular = new DiscordSerializer(new DiscordSerializerOptions(embedLinks, true, (Function<KeybindComponent, String>) resolver, (Function<TranslatableComponent, String>) resolver));
        return toolTipComponents.stream().map(toolTipComponent -> {
            if (toolTipComponent.getType().equals(ToolTipType.TEXT)) {
                Component component = toolTipComponent.getToolTipComponent(ToolTipType.TEXT);
                if (component != null) {
                    return serializerRegular.serialize(ComponentStringUtils.toDiscordSRVComponent(component));
                }
            } else if (toolTipComponent.getType().equals(ToolTipType.IMAGE)) {
                return serializerRegular.serialize(ComponentStringUtils.toDiscordSRVComponent(imageToolTipHandler.apply((ToolTipComponent<BufferedImage>) toolTipComponent)));
            }
            return "";
        }).collect(Collectors.joining("\n"));
    }

    public static class DiscordToolTip {

        @Getter
        private final List<ToolTipComponent<?>> components;
        private final boolean isBaseItem;
        private final boolean isHideTooltip;

        public DiscordToolTip(List<ToolTipComponent<?>> components, boolean isBaseItem, boolean isHideTooltip) {
            this.components = components;
            this.isBaseItem = isBaseItem;
            this.isHideTooltip = isHideTooltip;
        }

        public boolean isBaseItem() {
            return isBaseItem;
        }

        public boolean isHideTooltip() {
            return isHideTooltip;
        }
    }

}
