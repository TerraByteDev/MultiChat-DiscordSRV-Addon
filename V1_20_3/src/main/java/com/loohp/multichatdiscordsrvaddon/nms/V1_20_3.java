/*
 * This file is part of InteractiveChatDiscordSrvAddon-V1_20_3.
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

package com.loohp.multichatdiscordsrvaddon.nms;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import org.apache.commons.lang3.math.Fraction;
import com.loohp.multichatdiscordsrvaddon.utils.InteractiveChatComponentSerializer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.EnumChatFormat;
import net.minecraft.MinecraftVersion;
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.effect.AttributeModifierTemplate;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.EnumMonsterType;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.alchemy.PotionUtil;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.TileEntitySkull;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_20_R3.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.inventory.trim.CraftTrimMaterial;
import org.bukkit.craftbukkit.v1_20_R3.inventory.trim.CraftTrimPattern;
import org.bukkit.craftbukkit.v1_20_R3.map.CraftMapView;
import org.bukkit.craftbukkit.v1_20_R3.map.RenderData;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_20_3 extends NMSAddonWrapper {

    private final Method craftMapViewIsContextualMethod;
    private final Method bundleItemGetWeightMethod;
    private final Field craftSkullMetaProfileField;

    public V1_20_3() {
        try {
            craftMapViewIsContextualMethod = CraftMapView.class.getDeclaredMethod("isContextual");

            bundleItemGetWeightMethod = BundleItem.class.getDeclaredMethod("k", net.minecraft.world.item.ItemStack.class);
            craftSkullMetaProfileField = Class.forName("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftMetaSkull").getDeclaredField("profile");
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<ICMaterial, TintColorProvider.SpawnEggTintData> getSpawnEggColorMap() {
        Map<ICMaterial, TintColorProvider.SpawnEggTintData> mapping = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.h) {
            if (item instanceof ItemMonsterEgg) {
                ItemMonsterEgg egg = (ItemMonsterEgg) item;
                ICMaterial icMaterial = ICMaterial.of(CraftMagicNumbers.getMaterial(item));
                mapping.put(icMaterial, new TintColorProvider.SpawnEggTintData(egg.a(0), egg.a(1)));
            }
        }
        return mapping;
    }

    @Override
    public Key getMapCursorTypeKey(MapCursor mapCursor) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getPatternTypeKey(PatternType patternType) {
        NamespacedKey key = patternType.getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public DimensionManager getDimensionManager(World world) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        net.minecraft.world.level.dimension.DimensionManager manager = worldServer.E_();
        return new DimensionManager() {
            @Override
            public boolean hasFixedTime() {
                return manager.a();
            }
            @Override
            public OptionalLong getFixedTime() {
                return manager.f();
            }
            @Override
            public float timeOfDay(long i) {
                return manager.a(i);
            }
            @Override
            public boolean hasSkyLight() {
                return manager.g();
            }
            @Override
            public boolean hasCeiling() {
                return manager.h();
            }
            @Override
            public boolean ultraWarm() {
                return manager.i();
            }
            @Override
            public boolean natural() {
                return manager.j();
            }
            @Override
            public double coordinateScale() {
                return manager.k();
            }
            @Override
            public boolean createDragonFight() {
                return worldServer.ae() == net.minecraft.world.level.World.j && worldServer.ad().a(BuiltinDimensionTypes.c);
            }
            @Override
            public boolean piglinSafe() {
                return manager.b();
            }
            @Override
            public boolean bedWorks() {
                return manager.l();
            }
            @Override
            public boolean respawnAnchorWorks() {
                return manager.m();
            }
            @Override
            public boolean hasRaids() {
                return manager.c();
            }
            @Override
            public int minY() {
                return manager.n();
            }
            @Override
            public int height() {
                return manager.o();
            }
            @Override
            public int logicalHeight() {
                return manager.p();
            }
            @SuppressWarnings("PatternValidation")
            @Override
            public Key infiniburn() {
                MinecraftKey key = manager.q().b();
                return Key.key(key.b(), key.a());
            }
            @SuppressWarnings("PatternValidation")
            @Override
            public Key effectsLocation() {
                MinecraftKey key = manager.r();
                return Key.key(key.b(), key.a());
            }
            @Override
            public float ambientLight() {
                return manager.s();
            }
        };
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getNamespacedKey(World world) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        MinecraftKey key = worldServer.ae().a();
        return Key.key(key.b(), key.a());
    }

    @Override
    public BiomePrecipitation getPrecipitation(Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BiomeBase biomeBase = worldServer.a(location.getBlockX(), location.getBlockY(), location.getBlockZ()).a();
        BiomeBase.Precipitation precipitation = biomeBase.a(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return BiomePrecipitation.fromName(precipitation.name());
    }

    @Override
    public OptionalInt getTropicalFishBucketVariantTag(ItemStack bucket) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(bucket);
        NBTTagCompound nbt = nmsItemStack.v();
        if (nbt == null || !nbt.e("BucketVariantTag")) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(nbt.h("BucketVariantTag"));
    }

    @Override
    public PotionType getBasePotionType(ItemStack potion) {
        return ((PotionMeta) potion.getItemMeta()).getBasePotionType();
    }

    @Override
    public List<PotionEffect> getAllPotionEffects(ItemStack potion) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(potion);
        List<PotionEffect> effects = new ArrayList<>();
        for (MobEffect mobEffect : PotionUtil.a(nmsItemStack)) {
            effects.add(CraftPotionUtil.toBukkit(mobEffect));
        }
        return effects;
    }

    @Override
    public ChatColor getPotionEffectChatColor(PotionEffectType type) {
        MobEffectList mobEffectList = ((CraftPotionEffectType) type).getHandle();
        EnumChatFormat chatFormat = mobEffectList.f().a();
        return ChatColor.getByChar(chatFormat.toString().charAt(1));
    }

    @Override
    public Map<String, AttributeModifier> getPotionAttributeModifiers(PotionEffect effect) {
        Map<String, AttributeModifier> attributes = new HashMap<>();
        MobEffect mobEffect = CraftPotionUtil.fromBukkit(effect);
        MobEffectList mobEffectList = mobEffect.c();
        Map<AttributeBase, AttributeModifierTemplate> nmsMap = mobEffectList.h();
        for (Map.Entry<AttributeBase, AttributeModifierTemplate> entry : nmsMap.entrySet()) {
            String name = entry.getKey().c();
            AttributeModifierTemplate template = entry.getValue();
            AttributeModifier attributeModifier = CraftAttributeInstance.convert(template.a(effect.getAmplifier()));
            attributes.put(name, attributeModifier);
        }
        return attributes;
    }

    @Override
    public boolean isItemUnbreakable(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta != null && itemMeta.isUnbreakable();
    }

    @Override
    public List<ICMaterial> getItemCanPlaceOnList(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        List<ICMaterial> materials = new ArrayList<>();
        if (nmsItemStack.u() && nmsItemStack.v().b("CanPlaceOn", 9)) {
            NBTTagList nbtTagList = nmsItemStack.v().c("CanPlaceOn", 8);
            if (!nbtTagList.isEmpty()) {
                for (int i = 0; i < nbtTagList.size(); i++) {
                    try {
                        MinecraftKey key = MinecraftKey.a(nbtTagList.j(i));
                        Block block = BuiltInRegistries.e.a(key);
                        materials.add(ICMaterial.of(CraftMagicNumbers.getMaterial(block)));
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return materials;
    }

    @Override
    public List<ICMaterial> getItemCanDestroyList(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        List<ICMaterial> materials = new ArrayList<>();
        if (nmsItemStack.u() && nmsItemStack.v().b("CanDestroy", 9)) {
            NBTTagList nbtTagList = nmsItemStack.v().c("CanDestroy", 8);
            if (!nbtTagList.isEmpty()) {
                for (int i = 0; i < nbtTagList.size(); i++) {
                    try {
                        MinecraftKey key = MinecraftKey.a(nbtTagList.j(i));
                        Block block = BuiltInRegistries.e.a(key);
                        materials.add(ICMaterial.of(CraftMagicNumbers.getMaterial(block)));
                    } catch (Exception ignore) {
                    }
                }
            }
        }
        return materials;
    }

    @Override
    public OptionalInt getLeatherArmorColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack.u() && nmsItemStack.v().e("display")) {
            NBTTagCompound display = nmsItemStack.v().p("display");
            if (display.e("color")) {
                return OptionalInt.of(display.h("color"));
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean hasBlockEntityTag(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.b("BlockEntityTag") != null;
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Component getInstrumentDescription(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            if (nmsItemStack.u() && nmsItemStack.v().e("instrument")) {
                Key instrument = Key.key(nmsItemStack.v().l("instrument"));
                return Component.translatable("instrument." + instrument.namespace() + "." + instrument.value());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public PaintingVariant getPaintingVariant(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            if (nmsItemStack.u() && nmsItemStack.v().e("EntityTag")) {
                String variant = nmsItemStack.v().p("EntityTag").l("variant");
                MinecraftKey key = new MinecraftKey(variant);
                net.minecraft.world.entity.decoration.PaintingVariant paintingVariant = BuiltInRegistries.l.a(key);
                return new PaintingVariant(Key.key(key.b(), key.a()), paintingVariant.a(), paintingVariant.b());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getEntityNBT(Entity entity) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        nmsEntity.e(nbt);
        return nbt.toString();
    }

    @Override
    public float getLegacyTrimMaterialIndex(Object trimMaterial) {
        if (trimMaterial == null) {
            return 0.0F;
        }
        TrimMaterial nmsTrimMaterial = ((CraftTrimMaterial) trimMaterial).getHandle();
        return nmsTrimMaterial.c();
    }

    @Override
    public TextColor getTrimMaterialColor(Object trimMaterial) {
        if (trimMaterial == null) {
            return NamedTextColor.GRAY;
        }
        TrimMaterial nmsTrimMaterial = ((CraftTrimMaterial) trimMaterial).getHandle();
        TextColor textColor = InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsTrimMaterial.e())).color();
        return textColor == null ? NamedTextColor.GRAY : textColor;
    }

    @Override
    public AdvancementData getAdvancementDataFromBukkitAdvancement(Object bukkitAdvancement) {
        AdvancementHolder holder = ((CraftAdvancement) bukkitAdvancement).getHandle();
        Optional<AdvancementDisplay> optAdvancementDisplay = holder.b().c();
        if (!optAdvancementDisplay.isPresent()) {
            return null;
        }
        AdvancementDisplay display = optAdvancementDisplay.get();
        Component title = InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(display.a()));
        Component description = InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(display.b()));
        ItemStack item = CraftItemStack.asBukkitCopy(display.c());
        AdvancementType advancementType = AdvancementType.fromName(display.e().c());
        boolean isMinecraft = holder.a().b().equals(Key.MINECRAFT_NAMESPACE);
        return new AdvancementData(title, description, item, advancementType, isMinecraft);
    }

    @Override
    public Advancement getBukkitAdvancementFromEvent(Event event) {
        return ((PlayerAdvancementDoneEvent) event).getAdvancement();
    }

    @Override
    public boolean matchArmorSlot(ItemStack armorItem, EquipmentSlot slot) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(armorItem);
        Item item = nmsItemStack.d();
        if (!(item instanceof ItemArmor)) {
            return false;
        }
        return CraftEquipmentSlot.getSlot(((ItemArmor) item).g()).equals(slot);
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getArmorMaterialKey(ItemStack armorItem) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(armorItem);
        Item item = nmsItemStack.d();
        if (!(item instanceof ItemArmor)) {
            return null;
        }
        ArmorMaterial armorMaterial = ((ItemArmor) item).d();
        return Key.key(armorMaterial.e());
    }

    @Override
    public Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> getItemAttributeModifiers(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> result = new EnumMap<>(EquipmentSlotGroup.class);
        for (EnumItemSlot slot : EnumItemSlot.values()) {
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.forEquipmentSlot(CraftEquipmentSlot.getSlot(slot));
            Multimap<AttributeBase, net.minecraft.world.entity.ai.attributes.AttributeModifier> nmsMap = nmsItemStack.a(slot);
            for (Map.Entry<AttributeBase, net.minecraft.world.entity.ai.attributes.AttributeModifier> entry : nmsMap.entries()) {
                Multimap<String, AttributeModifier> attributes = result.computeIfAbsent(equipmentSlotGroup, k -> LinkedHashMultimap.create());
                String name = entry.getKey().c();
                AttributeModifier attributeModifier = CraftAttributeInstance.convert(entry.getValue());
                attributes.put(name, attributeModifier);
            }
        }
        return result;
    }

    @Override
    public Component getDeathMessage(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        CombatTracker combatTracker = entityPlayer.eK();
        return InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(combatTracker.a()));
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getDecoratedPotSherdPatternName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Item item = nmsItemStack.d();
        MinecraftKey key = DecoratedPotPatterns.a(item).a();
        return Key.key(key.b(), key.a());
    }

    @Override
    public boolean isJukeboxPlayable(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.d() instanceof ItemRecord;
    }

    @Override
    public boolean shouldSongShowInToolTip(ItemStack disc) {
        return true;
    }

    @Override
    public Component getJukeboxSongDescription(ItemStack disc) {
        NamespacedKey namespacedKey = disc.getType().getKey();
        return Component.translatable("item." + namespacedKey.getNamespace() + "." + namespacedKey.getKey() + ".desc");
    }

    @Override
    public Component getEnchantmentDescription(Enchantment enchantment) {
        NamespacedKey namespacedKey = enchantment.getKey();
        return Component.translatable("enchantment." + namespacedKey.getNamespace() + "." + namespacedKey.getKey());
    }

    @Override
    public String getEffectTranslationKey(PotionEffectType type) {
        NamespacedKey namespacedKey = type.getKey();
        return "effect." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }

    @Override
    public String getEntityTypeTranslationKey(EntityType type) {
        return CraftEntityType.bukkitToMinecraft(type).g();
    }

    @Override
    public FishHook getFishHook(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        EntityFishingHook entityFishingHook = entityPlayer.ck;
        return entityFishingHook == null ? null : (FishHook) entityFishingHook.getBukkitEntity();
    }

    @Override
    public String getServerResourcePack() {
        return Bukkit.getResourcePack();
    }

    @Override
    public String getServerResourcePackHash() {
        return Bukkit.getResourcePackHash();
    }

    @Override
    public int getServerResourcePackVersion() {
        return MinecraftVersion.a().a(EnumResourcePackType.a);
    }

    @Override
    public float getEnchantmentDamageBonus(ItemStack itemStack, LivingEntity livingEntity) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (livingEntity == null) {
            return EnchantmentManager.a(nmsItemStack, EnumMonsterType.a);
        }
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        return EnchantmentManager.a(nmsItemStack, entityLiving.eS());
    }

    @Override
    public int getItemComponentsSize(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameProfile getPlayerHeadProfile(ItemStack playerHead) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(playerHead);
        NBTTagCompound tag = nmsItemStack.v();
        if (tag == null) {
            return null;
        }
        return TileEntitySkull.d(tag);
    }

    @Override
    public ItemFlag getHideAdditionalItemFlag() {
        return ItemFlag.HIDE_POTION_EFFECTS;
    }

    @Override
    public boolean shouldHideTooltip(ItemStack itemStack) {
        return false;
    }

    @Override
    public Key getAttributeModifierKey(Object attributeModifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProfileProperty toProfileProperty(Property property) {
        return new ProfileProperty(property.name(), property.value(), property.signature());
    }

    @Override
    public Fraction getWeightForBundle(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            bundleItemGetWeightMethod.setAccessible(true);
            int weight = (int) bundleItemGetWeightMethod.invoke(null, nmsItemStack);
            return Fraction.getFraction(weight, 64);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomModelData getCustomModelData(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasCustomModelData()) {
            return null;
        }
        return new CustomModelData(itemMeta.getCustomModelData());
    }

    @Override
    public boolean hasDataComponent(ItemStack itemStack, String componentName, boolean ignoreDefault) {
        return false;
    }

    @Override
    public String getBlockStateProperty(ItemStack itemStack, String property) {
        return null;
    }

    @Override
    public ItemDamageInfo getItemDamageInfo(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable) {
            return new ItemDamageInfo(((Damageable) itemMeta).getDamage(), itemStack.getType().getMaxDurability());
        }
        return new ItemDamageInfo(0, itemStack.getType().getMaxDurability());
    }

    @Override
    public float getItemCooldownProgress(Player player, ItemStack itemStack) {
        return 0.0F;
    }

    @Override
    public float getSkyAngle(World world) {
        return 0F;
    }

    @Override
    public int getMoonPhase(World world) {
        return 0;
    }

    @Override
    public int getCrossbowPullTime(ItemStack itemStack, LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public int getItemUseTimeLeft(LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public int getTicksUsedSoFar(ItemStack itemStack, LivingEntity livingEntity) {
        return 0;
    }

    @Override
    public Key getItemModelResourceLocation(ItemStack itemStack) {
        return getNMSItemStackNamespacedKey(itemStack);
    }

    @Override
    public Boolean getEnchantmentGlintOverride(ItemStack itemStack) {
        return null;
    }

    @Override
    public Key getCustomTooltipResourceLocation(ItemStack itemStack) {
        return null;
    }

    @Override
    public String getBannerPatternTranslationKey(PatternType type, DyeColor color) {
        Key typeKey = getPatternTypeKey(type);
        return "block.minecraft.banner." + typeKey.value() + "." + color.name().toLowerCase();
    }

    @Override
    public Component getTrimMaterialDescription(Object trimMaterial) {
        TrimMaterial material = CraftTrimMaterial.bukkitToMinecraft((org.bukkit.inventory.meta.trim.TrimMaterial) trimMaterial);
        IChatBaseComponent description = material.e();
        return InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
    }

    @Override
    public Component getTrimPatternDescription(Object trimPattern, Object trimMaterial) {
        TrimPattern pattern = CraftTrimPattern.bukkitToMinecraft((org.bukkit.inventory.meta.trim.TrimPattern) trimPattern);
        IChatBaseComponent description;
        if (trimMaterial == null) {
            description = pattern.c();
        } else {
            TrimMaterial material = CraftTrimMaterial.bukkitToMinecraft((org.bukkit.inventory.meta.trim.TrimMaterial) trimMaterial);
            description = pattern.a(Holder.a(material));
        }
        return InteractiveChatComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
    }

    @Override
    public OptionalInt getFireworkFlightDuration(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItemStack.d() instanceof ItemFireworks) {
            NBTTagCompound nbt = nmsItemStack.b("Fireworks");
            if (nbt != null) {
                if (nbt.b("Flight", NBTBase.u)) {
                    return OptionalInt.of(nbt.f("Flight"));
                }
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean shouldShowOperatorBlockWarnings(ItemStack itemStack, Player player) {
        return false;
    }

    @Override
    public String getSkinValue(Player player) {
        Collection<Property> textures = ((CraftPlayer) player).getProfile().getProperties().get("textures");
        if (textures == null || textures.isEmpty()) {
            return null;
        }
        return textures.iterator().next().value();
    }

    @Override
    public String getSkinValue(ItemMeta skull) {
        try {
            if (skull instanceof SkullMeta && ((SkullMeta) skull).hasOwner()) {
                craftSkullMetaProfileField.setAccessible(true);
                GameProfile profile = (GameProfile) craftSkullMetaProfileField.get(skull);
                Collection<Property> textures = profile.getProperties().get("textures");
                if (textures == null || textures.isEmpty()) {
                    return null;
                }
                return textures.iterator().next().value();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public MapView getMapView(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof MapMeta) {
            return Bukkit.getMap(((MapMeta) meta).getMapId());
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getMapId(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof MapMeta) {
            return ((MapMeta) meta).getMapId();
        }
        return -1;
    }

    @Override
    public boolean isContextual(MapView mapView) {
        try {
            CraftMapView craftMapView = (CraftMapView) mapView;
            craftMapViewIsContextualMethod.setAccessible(true);
            return (boolean) craftMapViewIsContextualMethod.invoke(craftMapView);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getColors(MapView mapView, Player player) {
        CraftMapView craftMapView = (CraftMapView) mapView;
        RenderData renderData = craftMapView.render((CraftPlayer) player);
        return renderData.buffer;
    }

    @Override
    public List<MapCursor> getCursors(MapView mapView, Player player) {
        CraftMapView craftMapView = (CraftMapView) mapView;
        RenderData renderData = craftMapView.render((CraftPlayer) player);
        return renderData.cursors;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<MapIcon> toNMSMapIconList(List<MapCursor> mapCursors) {
        return mapCursors.stream().map(c -> {
            MapIcon.Type decorationTypeHolder = MapIcon.Type.a(c.getType().getValue());
            IChatBaseComponent iChat = CraftChatMessage.fromStringOrNull(c.getCaption());
            return new MapIcon(decorationTypeHolder, c.getX(), c.getY(), c.getDirection(), iChat);
        }).collect(Collectors.toList());
    }

    @Override
    public net.minecraft.world.item.ItemStack toNMSCopy(ItemStack itemstack) {
        return CraftItemStack.asNMSCopy(itemstack);
    }

    @Override
    public ItemStack toBukkitCopy(Object handle) {
        return CraftItemStack.asBukkitCopy((net.minecraft.world.item.ItemStack) handle);
    }

    @Override
    public ItemStack getItemFromNBTJson(String json) {
        try {
            NBTTagCompound nbtTagCompound = MojangsonParser.a(json);
            net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.a(nbtTagCompound);
            return toBukkitCopy(itemStack);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNMSItemStackJson(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTBase nbt = nmsItemStack.b(nbtTagCompound);
        return nbt.toString();
    }

    @Override
    public Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        NamespacedKey key = itemStack.getType().getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }

    @Override
    public String getNMSItemStackTag(ItemStack itemStack) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTTagCompound nbt = nmsItemStack.b(nbtTagCompound);
        NBTBase tag = nbt.p("tag");
        return tag == null ? null : tag.toString();
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;

        ClientboundClearTitlesPacket packet1 = new ClientboundClearTitlesPacket(true);
        connection.b(packet1);

        if (!PlainTextComponentSerializer.plainText().serialize(title).isEmpty()) {
            ClientboundSetTitleTextPacket packet2 = new ClientboundSetTitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(title)));
            connection.b(packet2);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            ClientboundSetSubtitleTextPacket packet3 = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(subtitle)));
            connection.b(packet3);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(actionbar).isEmpty()) {
            ClientboundSetActionBarTextPacket packet4 = new ClientboundSetActionBarTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(actionbar)));
            connection.b(packet4);
        }

        ClientboundSetTitlesAnimationPacket packet5 = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        connection.b(packet5);
    }

    @Override
    public NamedTag fromSNBT(String snbt) throws IOException {
        try {
            NBTTagCompound nbt = MojangsonParser.a(snbt);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NBTCompressedStreamTools.c(nbt, new DataOutputStream(out));
            return new NBTDeserializer(false).fromBytes(out.toByteArray());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Component getItemStackDisplayName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.y()));
    }

    @Override
    public void setItemStackDisplayName(ItemStack itemStack, Component component) {
        IChatBaseComponent nmsComponent = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component));
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        nmsItemStack.a(nmsComponent);
        ItemStack modifiedStack = toBukkitCopy(nmsItemStack);
        ItemMeta meta = modifiedStack.getItemMeta();
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemStackLore(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTTagCompound nbttagcompound = nmsItemStack.b("display");
        if (nbttagcompound.d("Lore") == 9) {
            List<Component> lore = new ArrayList<>();
            NBTTagList nbtLore = nbttagcompound.c("Lore", 8);
            for (int i = 0; i < nbtLore.size(); i++) {
                String json = nbtLore.j(i);
                lore.add(GsonComponentSerializer.gson().deserialize(json));
            }
            return lore;
        }
        return Collections.emptyList();
    }

    @Override
    public String getItemStackTranslationKey(ItemStack itemStack) {
        return itemStack.getTranslationKey();
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.C().e.toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public void sendFakePlayerInventory(Player player, Inventory inventory, boolean armor, boolean offhand) {
        ItemStack[] items = new ItemStack[46];
        Arrays.fill(items, ITEM_STACK_AIR);
        int u = 36;
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            items[u] = item == null ? ITEM_STACK_AIR : item.clone();
            u++;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            items[i] = item == null ? ITEM_STACK_AIR : item.clone();
        }
        if (armor) {
            u = 8;
            for (int i = 36; i < 40; i++) {
                ItemStack item = inventory.getItem(i);
                items[u] = item == null ? ITEM_STACK_AIR : item.clone();
                u--;
            }
        }
        if (offhand) {
            ItemStack item = inventory.getItem(40);
            items[45] = item == null ? ITEM_STACK_AIR : item.clone();
        }

        NonNullList<net.minecraft.world.item.ItemStack> itemList = NonNullList.a();
        for (ItemStack itemStack : items) {
            itemList.add(toNMSCopy(itemStack));
        }

        PacketPlayOutWindowItems packet1 = new PacketPlayOutWindowItems(0, 0, itemList, toNMSCopy(ITEM_STACK_AIR));
        PacketPlayOutSetSlot packet2 = new PacketPlayOutSetSlot(-1, -1, 0, toNMSCopy(ITEM_STACK_AIR));

        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;
        connection.b(packet1);
        connection.b(packet2);
    }

    @Override
    public void sendFakeMainHandSlot(Player player, ItemStack item) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = Collections.singletonList(new Pair<>(EnumItemSlot.a, toNMSCopy(item)));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), nmsEquipments);
        ((CraftPlayer) player).getHandle().c.b(packet);
    }

    @Override
    public void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors) {
        List<MapIcon> mapIcons = toNMSMapIconList(mapCursors);
        WorldMap.b b = new WorldMap.b(0, 0, 128, 128, colors);
        PacketPlayOutMap packet = new PacketPlayOutMap(mapId, (byte) 0, false, mapIcons, b);
        ((CraftPlayer) player).getHandle().c.b(packet);
    }

    @Override
    public Component getSkullOwner(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        ItemSkullPlayer skull = (ItemSkullPlayer) nmsItemStack.d();
        IChatBaseComponent owner = skull.m(nmsItemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(owner));
    }

}
