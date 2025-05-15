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

package com.loohp.multichatdiscordsrvaddon.resources;

import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;
import com.loohp.multichatdiscordsrvaddon.objectholders.TintColorProvider;
import com.loohp.multichatdiscordsrvaddon.resources.models.BlockModel;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelOverride;
import com.loohp.multichatdiscordsrvaddon.resources.textures.TextureResource;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;

@Getter
public class ModelLayer {

    private final String modelKey;
    private final Map<ModelOverride.ModelOverrideType, Float> predicates;
    private final Map<String, TextureResource> providedTextures;
    private final TintColorProvider tintColorProvider;
    private final Function<BlockModel, ValuePairs<BlockModel, Map<String, TextureResource>>> postResolveFunction;

    public ModelLayer(String modelKey, Map<ModelOverride.ModelOverrideType, Float> predicates, Map<String, TextureResource> providedTextures, TintColorProvider tintColorProvider, Function<BlockModel, ValuePairs<BlockModel, Map<String, TextureResource>>> postResolveFunction) {
        this.modelKey = modelKey;
        this.predicates = predicates;
        this.providedTextures = providedTextures;
        this.tintColorProvider = tintColorProvider;
        this.postResolveFunction = postResolveFunction;
    }

}