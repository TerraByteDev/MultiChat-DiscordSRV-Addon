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

package com.loohp.multichatdiscordsrvaddon.resources.mods.chime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.loohp.multichatdiscordsrvaddon.resources.models.BlockModel;
import com.loohp.multichatdiscordsrvaddon.resources.models.Coordinates3D;
import com.loohp.multichatdiscordsrvaddon.resources.models.IModelManager;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelAxis;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelDisplay;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelDisplay.ModelDisplayPosition;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelElement;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelElement.ModelElementRotation;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelFace;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelFace.ModelFaceSide;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelGUILight;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelOverride;
import com.loohp.multichatdiscordsrvaddon.resources.models.ModelOverride.ModelOverrideType;
import com.loohp.multichatdiscordsrvaddon.resources.models.TextureUV;
import com.loohp.multichatdiscordsrvaddon.resources.mods.chime.ChimeModelOverride.ChimeModelOverrideType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ChimeBlockModel extends BlockModel {

    public static ChimeBlockModel fromJson(IModelManager manager, String resourceLocation, JSONObject rootJson, boolean useLegacyOverrides) {
        String parent = (String) rootJson.getOrDefault("parent", null);
        boolean ambientocclusion = (boolean) rootJson.getOrDefault("ambientocclusion", true);
        ModelGUILight guiLight = rootJson.containsKey("gui_light") ? ModelGUILight.fromKey((String) rootJson.get("gui_light")) : null;
        Map<ModelDisplayPosition, ModelDisplay> display = new EnumMap<>(ModelDisplayPosition.class);
        JSONObject displayJson = (JSONObject) rootJson.get("display");
        if (displayJson != null) {
            for (Object obj : displayJson.keySet()) {
                String displayKey = obj.toString();
                JSONArray rotationArray = (JSONArray) ((JSONObject) displayJson.get(displayKey)).get("rotation");
                JSONArray translationArray = (JSONArray) ((JSONObject) displayJson.get(displayKey)).get("translation");
                JSONArray scaleArray = (JSONArray) ((JSONObject) displayJson.get(displayKey)).get("scale");
                Coordinates3D rotation;
                if (rotationArray == null) {
                    rotation = new Coordinates3D(0, 0, 0);
                } else {
                    rotation = new Coordinates3D(((Number) rotationArray.get(0)).doubleValue(), ((Number) rotationArray.get(1)).doubleValue(), ((Number) rotationArray.get(2)).doubleValue());
                }
                Coordinates3D translation;
                if (translationArray == null) {
                    translation = new Coordinates3D(0, 0, 0);
                } else {
                    translation = new Coordinates3D(((Number) translationArray.get(0)).doubleValue(), ((Number) translationArray.get(1)).doubleValue(), ((Number) translationArray.get(2)).doubleValue());
                }
                Coordinates3D scale;
                if (scaleArray == null) {
                    scale = new Coordinates3D(1, 1, 1);
                } else {
                    scale = new Coordinates3D(((Number) scaleArray.get(0)).doubleValue(), ((Number) scaleArray.get(1)).doubleValue(), ((Number) scaleArray.get(2)).doubleValue());
                }
                ModelDisplayPosition displayPos = ModelDisplayPosition.fromKey(displayKey);
                display.put(displayPos, new ModelDisplay(displayPos, rotation, translation, scale));
            }
        }
        Map<String, String> texture = new HashMap<>();
        JSONObject textureJson = (JSONObject) rootJson.get("textures");
        if (textureJson != null) {
            for (Object obj : textureJson.keySet()) {
                String textureKey = obj.toString();
                texture.put(textureKey, textureJson.get(textureKey).toString());
            }
        }
        List<ModelElement> elements = new ArrayList<>();
        JSONArray elementsArray = (JSONArray) rootJson.get("elements");
        if (elementsArray != null) {
            for (Object obj : elementsArray) {
                JSONObject elementJson = (JSONObject) obj;
                String name = (String) elementJson.get("name");
                JSONArray fromArray = (JSONArray) elementJson.get("from");
                JSONArray toArray = (JSONArray) elementJson.get("to");
                Coordinates3D from = new Coordinates3D(((Number) fromArray.get(0)).doubleValue(), ((Number) fromArray.get(1)).doubleValue(), ((Number) fromArray.get(2)).doubleValue());
                Coordinates3D to = new Coordinates3D(((Number) toArray.get(0)).doubleValue(), ((Number) toArray.get(1)).doubleValue(), ((Number) toArray.get(2)).doubleValue());
                ModelElementRotation rotation;
                JSONObject rotationJson = (JSONObject) elementJson.get("rotation");
                if (rotationJson == null) {
                    rotation = null;
                } else {
                    Coordinates3D origin;
                    JSONArray originArray = (JSONArray) rotationJson.get("origin");
                    if (originArray == null) {
                        origin = new Coordinates3D(0, 0, 0);
                    } else {
                        origin = new Coordinates3D(((Number) originArray.get(0)).doubleValue(), ((Number) originArray.get(1)).doubleValue(), ((Number) originArray.get(2)).doubleValue());
                    }
                    ModelAxis axis = ModelAxis.valueOf(rotationJson.get("axis").toString().toUpperCase());
                    double angle = ((Number) rotationJson.get("angle")).doubleValue();
                    boolean rescale = (boolean) rotationJson.getOrDefault("rescale", false);
                    rotation = new ModelElementRotation(origin, axis, angle, rescale);
                }
                boolean shade = (boolean) elementJson.getOrDefault("shade", true);
                Map<ModelFaceSide, ModelFace> face = new EnumMap<>(ModelFaceSide.class);
                JSONObject facesJson = (JSONObject) elementJson.get("faces");
                if (facesJson != null) {
                    for (Object obj1 : facesJson.keySet()) {
                        String faceKey = obj1.toString();
                        ModelFaceSide side = ModelFaceSide.fromKey(faceKey);
                        JSONObject faceJson = (JSONObject) facesJson.get(faceKey);
                        TextureUV uv;
                        JSONArray uvArray = (JSONArray) faceJson.get("uv");
                        if (uvArray == null) {
                            uv = null;
                        } else {
                            uv = new TextureUV(((Number) uvArray.get(0)).doubleValue(), ((Number) uvArray.get(1)).doubleValue(), ((Number) uvArray.get(2)).doubleValue(), ((Number) uvArray.get(3)).doubleValue());
                        }
                        String faceTexture = (String) faceJson.get("texture");
                        if (texture.containsKey(faceTexture)) {
                            faceTexture = "#" + faceTexture;
                        }
                        Object cullfaceObj = faceJson.get("cullface");
                        ModelFaceSide cullface;
                        if (cullfaceObj instanceof String) {
                            cullface = ModelFaceSide.fromKey((String) cullfaceObj);
                        } else {
                            cullface = side;
                        }
                        int faceRotation = ((Number) faceJson.getOrDefault("rotation", 0)).intValue();
                        int faceTintindex = ((Number) faceJson.getOrDefault("tintindex", -1)).intValue();
                        face.put(side, new ModelFace(side, uv, faceTexture, cullface, faceRotation, faceTintindex));
                    }
                }
                elements.add(new ModelElement(name, from, to, rotation, shade, face));
            }
        }
        List<ChimeModelOverride> overrides;
        if (useLegacyOverrides) {
            overrides = new ArrayList<>();
            JSONArray overridesArray = (JSONArray) rootJson.get("overrides");
            if (overridesArray != null) {
                ListIterator<Object> itr = overridesArray.listIterator(overridesArray.size());
                while (itr.hasPrevious()) {
                    JSONObject overrideJson = (JSONObject) itr.previous();
                    JSONObject predicateJson = (JSONObject) overrideJson.get("predicate");
                    Map<ModelOverrideType, Float> predicates = new EnumMap<>(ModelOverrideType.class);
                    for (Object obj1 : predicateJson.keySet()) {
                        String predicateTypeKey = obj1.toString();
                        ModelOverrideType type = ModelOverrideType.fromKey(predicateTypeKey);
                        if (type != null) {
                            Object value = predicateJson.get(predicateTypeKey);
                            predicates.put(type, ((Number) value).floatValue());
                        }
                    }
                    Map<ChimeModelOverrideType, Object> chimePredicates = ChimeUtils.getAllPredicates(predicateJson);
                    String model = (String) overrideJson.get("model");
                    if (overrideJson.containsKey("texture")) {
                        String armorTexture = (String) overrideJson.get("texture");
                        overrides.add(new ChimeModelOverride(predicates, chimePredicates, model, armorTexture));
                    } else {
                        overrides.add(new ChimeModelOverride(predicates, chimePredicates, model));
                    }
                }
            }
        } else {
            overrides = Collections.emptyList();
        }
        return new ChimeBlockModel(manager, resourceLocation, parent, ambientocclusion, guiLight, display, texture, elements, overrides);
    }

    public ChimeBlockModel(IModelManager manager, String resourceLocation, String parent, boolean ambientocclusion, ModelGUILight guiLight, Map<ModelDisplayPosition, ModelDisplay> display, Map<String, String> textures, List<ModelElement> elements, List<ChimeModelOverride> overrides) {
        super(manager, resourceLocation, parent, ambientocclusion, guiLight, display, textures, elements, (List<ModelOverride>) (List<?>) overrides);
    }

    public List<ChimeModelOverride> getChimeOverrides() {
        return (List<ChimeModelOverride>) (List<?>) getOverrides();
    }

}
