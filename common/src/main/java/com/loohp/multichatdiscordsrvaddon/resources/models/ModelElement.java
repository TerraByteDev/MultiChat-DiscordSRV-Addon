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

package com.loohp.multichatdiscordsrvaddon.resources.models;

import com.loohp.multichatdiscordsrvaddon.resources.models.ModelFace.ModelFaceSide;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ModelElement {

    @Getter
    private final String name;
    @Getter
    private final Coordinates3D from;
    @Getter
    private final Coordinates3D to;
    @Getter
    private final ModelElementRotation rotation;
    @Getter
    private final boolean shade;
    private final Map<ModelFaceSide, ModelFace> face;

    public ModelElement(String name, Coordinates3D from, Coordinates3D to, ModelElementRotation rotation, boolean shade, Map<ModelFaceSide, ModelFace> face) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.rotation = rotation;
        this.shade = shade;
        this.face = Collections.unmodifiableMap(face);
    }

    public Map<ModelFaceSide, ModelFace> getFaces() {
        return face;
    }

    public ModelFace getFace(ModelFaceSide side) {
        return face.get(side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModelElement that = (ModelElement) o;
        return shade == that.shade && Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(rotation, that.rotation) && Objects.equals(face, that.face);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, rotation, shade, face);
    }

    @Getter
    public static class ModelElementRotation {

        private final Coordinates3D origin;
        private final ModelAxis axis;
        private final double angle;
        private final boolean rescale;

        public ModelElementRotation(Coordinates3D origin, ModelAxis axis, double angle, boolean rescale) {
            this.origin = origin;
            this.axis = axis;
            this.angle = angle;
            this.rescale = rescale;
        }

    }

}
