// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lost;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.graph.Region;

import java.util.HashMap;

/**
 * Component to track a players progress during Lost
 */
public class ProgressTrackingComponent implements Component {
    // Biomes mapped to the corresponding level URIs
    HashMap<String, String> biomeToPrefab = new HashMap<String, String>();
    // To track whether the well has been discovered
    boolean foundWell;
    // Stores the hut position once it is spawned to prevent overlapping with levels
    Vector3i hutPosition;

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
}
