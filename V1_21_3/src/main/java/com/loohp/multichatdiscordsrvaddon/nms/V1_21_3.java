/*
 * This file is part of InteractiveChatDiscordSrvAddon-V1_21_3.
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
import com.google.gson.JsonElement;
import com.loohp.multichatdiscordsrvaddon.objectholders.*;
import com.loohp.multichatdiscordsrvaddon.objectholders.CustomModelData;
import com.loohp.multichatdiscordsrvaddon.utils.NativeJsonConverter;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapIcon;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.WorldMap;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import org.apache.commons.lang3.math.Fraction;
import com.loohp.multichatdiscordsrvaddon.utils.MultiChatGsonComponentSerializer;
import com.loohp.multichatdiscordsrvaddon.utils.ReflectionUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.EnumChatFormat;
import net.minecraft.MinecraftVersion;
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.CriterionConditionBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.packs.EnumResourcePackType;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.EntityTropicalFish;
import net.minecraft.world.entity.animal.EntityTropicalFish.d;
import net.minecraft.world.entity.projectile.EntityFishingHook;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentManager;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.PatternType;
import org.bukkit.craftbukkit.v1_21_R2.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.advancement.CraftAdvancement;
import org.bukkit.craftbukkit.v1_21_R2.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_21_R2.block.banner.CraftPatternType;
import org.bukkit.craftbukkit.v1_21_R2.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntityType;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R2.inventory.trim.CraftTrimMaterial;
import org.bukkit.craftbukkit.v1_21_R2.inventory.trim.CraftTrimPattern;
import org.bukkit.craftbukkit.v1_21_R2.map.CraftMapCursor;
import org.bukkit.craftbukkit.v1_21_R2.map.CraftMapView;
import org.bukkit.craftbukkit.v1_21_R2.map.RenderData;
import org.bukkit.craftbukkit.v1_21_R2.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_21_R2.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_21_R2.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_21_R2.util.CraftMagicNumbers;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_21_3 extends NMSWrapper {

    private final Method craftMapViewIsContextualMethod;
    private final Field adventureModePredicatePredicatesField;
    private final Method bundleContentsGetWeightMethod;
    private final Field craftSkullMetaProfileField;

    public V1_21_3() {
        try {
            craftMapViewIsContextualMethod = CraftMapView.class.getDeclaredMethod("isContextual");

            adventureModePredicatePredicatesField = ReflectionUtils.findDeclaredField(AdventureModePredicate.class, List.class, "predicates", "h");
            bundleContentsGetWeightMethod = ReflectionUtils.findDeclaredMethod(BundleContents.class, new Class[] {net.minecraft.world.item.ItemStack.class}, "getWeight", "b");
            craftSkullMetaProfileField = Class.forName("org.bukkit.craftbukkit.v1_21_R2.inventory.CraftMetaSkull").getDeclaredField("profile");
        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<ICMaterial, TintColorProvider.SpawnEggTintData> getSpawnEggColorMap() {
        Map<ICMaterial, TintColorProvider.SpawnEggTintData> mapping = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.g) {
            if (item instanceof ItemMonsterEgg) {
                ItemMonsterEgg egg = (ItemMonsterEgg) item;
                ICMaterial icMaterial = ICMaterial.of(CraftMagicNumbers.getMaterial(item));
                mapping.put(icMaterial, new TintColorProvider.SpawnEggTintData(egg.a(0), egg.a(1)));
            }
        }
        return mapping;
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getMapCursorTypeKey(MapCursor mapCursor) {
        NamespacedKey key = mapCursor.getType().getKey();
        return Key.key(key.getNamespace(), key.getKey());
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
        net.minecraft.world.level.dimension.DimensionManager manager = worldServer.G_();
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
                return worldServer.ah() == net.minecraft.world.level.World.j && worldServer.ag().a(BuiltinDimensionTypes.c);
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
        MinecraftKey key = worldServer.ah().a();
        return Key.key(key.b(), key.a());
    }

    @Override
    public BiomePrecipitation getPrecipitation(Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        BiomeBase biomeBase = worldServer.a(location.getBlockX(), location.getBlockY(), location.getBlockZ()).a();
        BiomeBase.Precipitation precipitation = biomeBase.a(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getBlockY());
        return BiomePrecipitation.fromName(precipitation.name());
    }

    @Override
    public OptionalInt getTropicalFishBucketVariantTag(ItemStack bucket) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(bucket);
        CustomData customData = nmsItemStack.a(DataComponents.X);
        if (customData.b()) {
            return OptionalInt.empty();
        }
        Optional<EntityTropicalFish.d> optional = customData.a(d.a.fieldOf("BucketVariantTag")).result();
        return optional.map(f -> OptionalInt.of(EntityTropicalFish.b.indexOf(f))).orElse(OptionalInt.empty());
    }

    @Override
    public PotionType getBasePotionType(ItemStack potion) {
        return ((PotionMeta) potion.getItemMeta()).getBasePotionType();
    }

    @Override
    public List<PotionEffect> getAllPotionEffects(ItemStack potion) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(potion);
        PotionContents potionContents = nmsItemStack.a(DataComponents.Q);
        List<PotionEffect> effects = new ArrayList<>();
        for (MobEffect mobEffect : potionContents.a()) {
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
        MobEffectList mobEffectList = mobEffect.c().a();
        mobEffectList.a(effect.getAmplifier(), (holder, nmsAttributeModifier) -> {
            String name = holder.a().c();
            AttributeModifier attributeModifier = CraftAttributeInstance.convert(nmsAttributeModifier);
            attributes.put(name, attributeModifier);
        });
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
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            AdventureModePredicate adventureModePredicate = nmsItemStack.a(DataComponents.m);
            if (adventureModePredicate == null) {
                return Collections.emptyList();
            }
            adventureModePredicatePredicatesField.setAccessible(true);
            List<CriterionConditionBlock> predicate = (List<CriterionConditionBlock>) adventureModePredicatePredicatesField.get(adventureModePredicate);
            List<ICMaterial> materials = new ArrayList<>();
            for (CriterionConditionBlock block : predicate) {
                Optional<HolderSet<Block>> optSet = block.b();
                if (optSet.isPresent()) {
                    for (Holder<Block> set : optSet.get()) {
                        materials.add(ICMaterial.of(CraftMagicNumbers.getMaterial(set.a())));
                    }
                }
            }
            return materials;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ICMaterial> getItemCanDestroyList(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            AdventureModePredicate adventureModePredicate = nmsItemStack.a(DataComponents.n);
            if (adventureModePredicate == null) {
                return Collections.emptyList();
            }
            adventureModePredicatePredicatesField.setAccessible(true);
            List<CriterionConditionBlock> predicate = (List<CriterionConditionBlock>) adventureModePredicatePredicatesField.get(adventureModePredicate);
            List<ICMaterial> materials = new ArrayList<>();
            for (CriterionConditionBlock block : predicate) {
                Optional<HolderSet<Block>> optSet = block.b();
                if (optSet.isPresent()) {
                    for (Holder<Block> set : optSet.get()) {
                        materials.add(ICMaterial.of(CraftMagicNumbers.getMaterial(set.a())));
                    }
                }
            }
            return materials;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OptionalInt getLeatherArmorColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        DyedItemColor dyedItemColor = nmsItemStack.a(DataComponents.J);
        return dyedItemColor == null ? OptionalInt.empty() : OptionalInt.of(dyedItemColor.a());
    }

    @Override
    public boolean hasBlockEntityTag(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.a(DataComponents.Y) != null;
    }

    @Override
    public Component getInstrumentDescription(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Holder<Instrument> holder = nmsItemStack.a(DataComponents.Z);
        if (holder == null) {
            return null;
        }
        IChatBaseComponent description = holder.a().d();
        return MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public PaintingVariant getPaintingVariant(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        CustomData customData = nmsItemStack.a(DataComponents.W);
        if (customData == null) {
            return null;
        }
        NBTTagCompound nbt = customData.d();
        if (nbt == null || !nbt.b("variant", 8)) {
            return null;
        }
        MinecraftKey key = MinecraftKey.a(nbt.l("variant"));
        IRegistryCustom customRegistry = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
        IRegistry<net.minecraft.world.entity.decoration.PaintingVariant> paintingRegistry = customRegistry.e(Registries.X);
        net.minecraft.world.entity.decoration.PaintingVariant paintingVariant = paintingRegistry.a(key);
        if (paintingVariant == null) {
            return null;
        }
        Optional<Component> title = paintingVariant.e().map(c -> MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(c)));
        Optional<Component> author = paintingVariant.f().map(c -> MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(c)));
        return new PaintingVariant(Key.key(key.b(), key.a()), paintingVariant.b(), paintingVariant.c(), title, author);
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
        TextColor textColor = MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsTrimMaterial.e())).color();
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
        Component title = MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(display.a()));
        Component description = MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(display.b()));
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
        Equippable equippable = nmsItemStack.a(DataComponents.D);
        if (equippable == null) {
            return false;
        }
        if (equippable.e().map(a -> a.a().anyMatch(s -> s.a().equals(EntityTypes.bS))).orElse(true)) {
            return CraftEquipmentSlot.getSlot(equippable.a()).equals(slot);
        }
        return false;
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getArmorMaterialKey(ItemStack armorItem) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(armorItem);
        Equippable equippable = nmsItemStack.a(DataComponents.D);
        if (equippable == null) {
            return null;
        }
        return equippable.c().map(key -> Key.key(key.b(), key.a())).orElse(null);
    }

    @Override
    public Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> getItemAttributeModifiers(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Map<EquipmentSlotGroup, Multimap<String, AttributeModifier>> result = new EnumMap<>(EquipmentSlotGroup.class);
        for (net.minecraft.world.entity.EquipmentSlotGroup slotGroup : net.minecraft.world.entity.EquipmentSlotGroup.values()) {
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.fromName(slotGroup.c());
            nmsItemStack.a(slotGroup, (holder, nmsAttributeModifier) -> {
                Multimap<String, AttributeModifier> attributes = result.computeIfAbsent(equipmentSlotGroup, k -> LinkedHashMultimap.create());
                String name = holder.a().c();
                AttributeModifier attributeModifier = CraftAttributeInstance.convert(nmsAttributeModifier);
                attributes.put(name, attributeModifier);
            });
        }
        return result;
    }

    @Override
    public Component getDeathMessage(Player player) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        CombatTracker combatTracker = entityPlayer.eQ();
        return MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(combatTracker.a()));
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getDecoratedPotSherdPatternName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Item item = nmsItemStack.h();
        MinecraftKey key = DecoratedPotPatterns.a(item).a();
        return Key.key(key.b(), key.a());
    }

    @Override
    public boolean isJukeboxPlayable(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        JukeboxPlayable jukeboxPlayable = nmsItemStack.a(DataComponents.ab);
        return jukeboxPlayable != null;
    }

    @Override
    public boolean shouldSongShowInToolTip(ItemStack disc) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(disc);
        JukeboxPlayable jukeboxPlayable = nmsItemStack.a(DataComponents.ab);
        if (jukeboxPlayable == null) {
            return false;
        }
        return jukeboxPlayable.b();
    }

    @Override
    public Component getJukeboxSongDescription(ItemStack disc) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(disc);
        JukeboxPlayable jukeboxPlayable = nmsItemStack.a(DataComponents.ab);
        if (jukeboxPlayable == null) {
            return null;
        }
        IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
        return jukeboxPlayable.a().a(registryAccess).map(h -> MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(h.a().c()))).orElse(null);
    }

    @Override
    public Component getEnchantmentDescription(Enchantment enchantment) {
        IChatBaseComponent description = CraftEnchantment.bukkitToMinecraft(enchantment).f();
        return MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
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
        EntityFishingHook entityFishingHook = entityPlayer.cv;
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
        EntityLiving entityLiving = ((CraftLivingEntity) livingEntity).getHandle();
        return EnchantmentManager.a(nmsItemStack, entityLiving);
    }

    @Override
    public int getItemComponentsSize(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.a().d();
    }

    @Override
    public GameProfile getPlayerHeadProfile(ItemStack playerHead) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(playerHead);
            ResolvableProfile resolvableProfile = nmsItemStack.a(DataComponents.ag);
            if (resolvableProfile == null) {
                return null;
            }
            return resolvableProfile.a().get().f();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    @Override
    public ItemFlag getHideAdditionalItemFlag() {
        return ItemFlag.HIDE_ADDITIONAL_TOOLTIP;
    }

    @Override
    public boolean shouldHideTooltip(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        return itemStack.getItemMeta().isHideTooltip();
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getAttributeModifierKey(Object attributeModifier) {
        NamespacedKey namespacedKey = ((AttributeModifier) attributeModifier).getKey();
        return Key.key(namespacedKey.getNamespace(), namespacedKey.getKey());
    }

    @Override
    public ProfileProperty toProfileProperty(Property property) {
        return new ProfileProperty(property.name(), property.value(), property.signature());
    }

    @Override
    public Fraction getWeightForBundle(ItemStack itemStack) {
        try {
            net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
            bundleContentsGetWeightMethod.setAccessible(true);
            org.apache.commons.lang3.math.Fraction weight = (org.apache.commons.lang3.math.Fraction) bundleContentsGetWeightMethod.invoke(null, nmsItemStack);
            return Fraction.getFraction(weight.getNumerator(), weight.getDenominator());
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

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getItemModelResourceLocation(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        MinecraftKey itemModel = nmsItemStack.a(DataComponents.i);
        if (itemModel == null) {
            return getNMSItemStackNamespacedKey(itemStack);
        }
        return Key.key(itemModel.b(), itemModel.a());
    }

    @Override
    public Boolean getEnchantmentGlintOverride(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasEnchantmentGlintOverride()) {
            return null;
        }
        return itemMeta.getEnchantmentGlintOverride();
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getCustomTooltipResourceLocation(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasTooltipStyle()) {
            return null;
        }
        NamespacedKey namespacedKey = itemMeta.getTooltipStyle();
        return Key.key(namespacedKey.getNamespace(), namespacedKey.getKey());
    }

    @Override
    public String getBannerPatternTranslationKey(PatternType type, DyeColor color) {
        String translationKey = CraftPatternType.bukkitToMinecraft(type).b();
        return translationKey + "." + color.name().toLowerCase();
    }

    @Override
    public Component getTrimMaterialDescription(Object trimMaterial) {
        TrimMaterial material = CraftTrimMaterial.bukkitToMinecraft((org.bukkit.inventory.meta.trim.TrimMaterial) trimMaterial);
        IChatBaseComponent description = material.e();
        return MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
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
        return MultiChatGsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(description));
    }

    @Override
    public OptionalInt getFireworkFlightDuration(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        Fireworks fireworks = nmsItemStack.a(DataComponents.af);
        if (fireworks == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(fireworks.a());
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

    @Override
    public List<MapIcon> toNMSMapIconList(List<MapCursor> mapCursors) {
        return mapCursors.stream().map(c -> {
            Holder<MapDecorationType> decorationTypeHolder = CraftMapCursor.CraftType.bukkitToMinecraftHolder(c.getType());
            IChatBaseComponent iChat = CraftChatMessage.fromStringOrNull(c.getCaption());
            return new MapIcon(decorationTypeHolder, c.getX(), c.getY(), c.getDirection(), Optional.ofNullable(iChat));
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
            IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
            NBTTagCompound nbtTagCompound = MojangsonParser.a(json);
            net.minecraft.world.item.ItemStack itemStack = net.minecraft.world.item.ItemStack.a(registryAccess, nbtTagCompound);
            return toBukkitCopy(itemStack);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNMSItemStackJson(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return "{id: \"minecraft:air\", count: 1}";
        }
        IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTBase nbt = nmsItemStack.b(registryAccess, nbtTagCompound);
        return nbt.toString();
    }

    @SuppressWarnings({"PatternValidation", "unchecked", "rawtypes"})
    @Override
    public Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return Collections.emptyMap();
        }
        IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        DataComponentPatch dataComponentPatch = nmsItemStack.e();
        Map<Key, DataComponentValue> convertedComponents = new HashMap<>();
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : dataComponentPatch.b()) {
            DataComponentType<?> type = entry.getKey();
            Optional<?> optValue = entry.getValue();
            MinecraftKey minecraftKey = BuiltInRegistries.ao.b(type);
            Key key = Key.key(minecraftKey.b(), minecraftKey.a());
            if (optValue.isPresent()) {
                Codec codec = type.b();
                if (codec != null) {
                    Object nativeJsonElement = codec.encodeStart(registryAccess.a(JsonOps.INSTANCE), optValue.get()).getOrThrow();
                    JsonElement jsonElement = NativeJsonConverter.fromNative(nativeJsonElement);
                    DataComponentValue value = GsonDataComponentValue.gsonDataComponentValue(jsonElement);
                    convertedComponents.put(key, value);
                }
            } else {
                convertedComponents.put(key, DataComponentValue.removed());
            }
        }
        return convertedComponents;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ItemStack getItemStackFromDataComponents(ItemStack itemStack, Map<Key, DataComponentValue> dataComponents) {
        if (dataComponents.isEmpty()) {
            return itemStack;
        }
        try {
            IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
            net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
            DataComponentPatch.a builder = DataComponentPatch.a();
            for (Map.Entry<Key, DataComponentValue> entry : dataComponents.entrySet()) {
                Key key = entry.getKey();
                DataComponentValue value = entry.getValue();
                MinecraftKey minecraftKey = MinecraftKey.a(key.namespace(), key.value());
                Optional<DataComponentType<?>> optType = BuiltInRegistries.ao.b(minecraftKey);
                if (optType.isPresent()) {
                    DataComponentType<?> type = optType.get();
                    if (value instanceof DataComponentValue.Removed) {
                        builder.a(type);
                    } else if (value instanceof GsonDataComponentValue) {
                        JsonElement jsonElement = ((GsonDataComponentValue) value).element();
                        Object nativeJsonElement = NativeJsonConverter.toNative(jsonElement);
                        Object result = type.b().decode(registryAccess.a((DynamicOps<Object>) (DynamicOps<?>) JsonOps.INSTANCE), nativeJsonElement).getOrThrow().getFirst();
                        builder.a((DataComponentType) type, result);
                    }
                }
            }
            nmsItemStack.a(builder.a());
            return toBukkitCopy(nmsItemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Key getNMSItemStackNamespacedKey(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return Key.key("minecraft", "air");
        }
        NamespacedKey key = itemStack.getType().getKey();
        return Key.key(key.getNamespace(), key.getKey());
    }

    @Override
    public String getNMSItemStackTag(ItemStack itemStack) {
        if (itemStack.getType().isAir()) {
            return null;
        }
        IRegistryCustom registryAccess = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().K_();
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        NBTBase nbt = nmsItemStack.b(registryAccess, nbtTagCompound);
        if (nbt instanceof NBTTagCompound) {
            NBTBase tag = ((NBTTagCompound) nbt).p("tag");
            return tag == null ? null : tag.toString();
        }
        return null;
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle, Component actionbar, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().f;

        ClientboundClearTitlesPacket packet1 = new ClientboundClearTitlesPacket(true);
        connection.sendPacket(packet1);

        if (!PlainTextComponentSerializer.plainText().serialize(title).isEmpty()) {
            ClientboundSetTitleTextPacket packet2 = new ClientboundSetTitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(title)));
            connection.sendPacket(packet2);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(subtitle).isEmpty()) {
            ClientboundSetSubtitleTextPacket packet3 = new ClientboundSetSubtitleTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(subtitle)));
            connection.sendPacket(packet3);
        }

        if (!PlainTextComponentSerializer.plainText().serialize(actionbar).isEmpty()) {
            ClientboundSetActionBarTextPacket packet4 = new ClientboundSetActionBarTextPacket(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(actionbar)));
            connection.sendPacket(packet4);
        }

        ClientboundSetTitlesAnimationPacket packet5 = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        connection.sendPacket(packet5);
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
        IChatBaseComponent ichatbasecomponent = nmsItemStack.y();
        ichatbasecomponent = ichatbasecomponent != null ? ichatbasecomponent : nmsItemStack.z();
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(ichatbasecomponent));
    }

    @Override
    public void setItemStackDisplayName(ItemStack itemStack, Component component) {
        IChatBaseComponent nmsComponent = CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(component));
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        DataComponentPatch dataComponentPatch = DataComponentPatch.a().a(DataComponents.g, nmsComponent).a();
        nmsItemStack.a(dataComponentPatch);
        ItemStack modifiedStack = toBukkitCopy(nmsItemStack);
        ItemMeta meta = modifiedStack.getItemMeta();
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
    }

    @Override
    public List<Component> getItemStackLore(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = toNMSCopy(itemStack);
        ItemLore lore = nmsItemStack.a(DataComponents.j);
        if (lore == null) {
            return Collections.emptyList();
        }
        return lore.b().stream().map(e -> GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(e))).collect(Collectors.toList());
    }

    @Override
    public String getItemStackTranslationKey(ItemStack itemStack) {
        return itemStack.getTranslationKey();
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.C().a().toString();
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

        PlayerConnection connection = ((CraftPlayer) player).getHandle().f;
        connection.sendPacket(packet1);
        connection.sendPacket(packet2);
    }

    @Override
    public void sendFakeMainHandSlot(Player player, ItemStack item) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = Collections.singletonList(new Pair<>(EnumItemSlot.a, toNMSCopy(item)));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), nmsEquipments);
        ((CraftPlayer) player).getHandle().f.sendPacket(packet);
    }

    @Override
    public void sendFakeMapUpdate(Player player, int mapId, List<MapCursor> mapCursors, byte[] colors) {
        List<MapIcon> mapIcons = toNMSMapIconList(mapCursors);
        WorldMap.c c = new WorldMap.c(0, 0, 128, 128, colors);
        PacketPlayOutMap packet = new PacketPlayOutMap(new MapId(mapId), (byte) 0, false, Optional.of(mapIcons), Optional.of(c));
        ((CraftPlayer) player).getHandle().f.sendPacket(packet);
    }

    @Override
    public Component getSkullOwner(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        ItemSkullPlayer skull = (ItemSkullPlayer) nmsItemStack.h();
        IChatBaseComponent owner = skull.a(nmsItemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(owner));
    }

}
