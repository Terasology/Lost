// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lost;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;

/**
 * Component to track a players progress during Lost
 */
public class ProgressTrackingComponent implements Component<ProgressTrackingComponent> {
    // Biomes mapped to the corresponding level URIs
    public HashMap<String, String> biomeToPrefab = new HashMap<String, String>();
    // To track whether the well has been discovered
    public boolean foundWell;
    // Stores the hut position once it is spawned to prevent overlapping with levels
    public Vector3i hutPosition = new Vector3i();

    public String getLevelPrefab(String biomeName) {
        return biomeToPrefab.get(biomeName);
    }

    public boolean isWellFound() {
        return foundWell;
    }

    public void addLevel(String prefabName, String... biomes) {
        for (String biome : biomes) {
            biomeToPrefab.put(biome, prefabName);
        }
    }

    @Override
    public void copyFrom(ProgressTrackingComponent other) {
        this.biomeToPrefab = Maps.newHashMap(other.biomeToPrefab);
        this.foundWell = other.foundWell;
        this.hutPosition.set(other.hutPosition);
    }
}
