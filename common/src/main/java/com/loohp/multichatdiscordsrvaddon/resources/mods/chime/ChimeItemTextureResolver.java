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

package com.loohp.multichatdiscordsrvaddon.resources.mods.chime;

import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.resources.CustomItemTextureRegistry.CustomItemTextureResolver;
import com.loohp.multichatdiscordsrvaddon.resources.languages.SpecificTranslateFunction;
import com.loohp.multichatdiscordsrvaddon.resources.models.BlockModel;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelOverride.ModelOverrideType;
import com.loohp.multichatdiscordsrvaddon.resources.mods.optifine.cit.EnchantmentProperties.OpenGLBlending;
import com.loohp.multichatdiscordsrvaddon.resources.textures.TextureResource;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class ChimeItemTextureResolver implements CustomItemTextureResolver {

    private final ChimeManager chimeManager;

    public ChimeItemTextureResolver(ChimeManager chimeManager) {
        this.chimeManager = chimeManager;
    }


    @Override
    public ValuePairs<BlockModel, Map<String, TextureResource>> getItemPostResolveFunction(ValuePairs<BlockModel, Map<String, TextureResource>> previousResult, String modelKey, EquipmentSlot heldSlot, ItemStack itemStack, boolean is1_8, Map<ModelOverrideType, Float> predicates, OfflinePlayer player, World world, LivingEntity entity, SpecificTranslateFunction translateFunction) {
        return new ValuePairs<>(chimeManager.resolveBlockModel(modelKey, is1_8, predicates, player, world, entity, itemStack, translateFunction), previousResult.getSecond());
    }

    @Override
    public Optional<TextureResource> getElytraOverrideTextures(EquipmentSlot heldSlot, ItemStack itemStack, SpecificTranslateFunction translateFunction) {
        return Optional.empty();
    }

    @Override
    public List<ValuePairs<TextureResource, OpenGLBlending>> getEnchantmentGlintOverrideTextures(EquipmentSlot heldSlot, ItemStack itemStack, SpecificTranslateFunction translateFunction) {
        return Collections.emptyList();
    }

    @Override
    public Optional<TextureResource> getArmorOverrideTextures(String layer, EquipmentSlot heldSlot, ItemStack itemStack, OfflinePlayer player, World world, LivingEntity entity, SpecificTranslateFunction translateFunction) {
        return Optional.ofNullable(chimeManager.getArmorOverrideTextures(layer, itemStack, player, world, entity, translateFunction));
    }

}
