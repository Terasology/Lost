// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lost;

import org.terasology.assets.management.AssetManager;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.lost.generator.LostWorldGenerator;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.registry.In;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.WorldProvider;
import org.terasology.world.generation.Region;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LevelSpawnSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private Console console;
    @In
    private AssetManager assetManager;
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player) {
        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f playerLocation = loc.getWorldPosition();
        ProgressTrackingComponent progressTrackingComponent = player.getComponent(ProgressTrackingComponent.class);
        // fetch nearest biome center
        int searchRadius = 40;
        Vector3i extent = new Vector3i(searchRadius, 1, searchRadius);
        Vector3i desiredPos = new Vector3i(playerLocation.getX(), 1, playerLocation.getZ());
        Region3i searchArea = Region3i.createFromCenterExtents(desiredPos, extent);
        Region worldRegion = LostWorldGenerator.world.getWorldData(searchArea);
        GraphFacet graphs = worldRegion.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet model = worldRegion.getFacet(WhittakerBiomeModelFacet.class);
        Vector2f playerPosition2d = new Vector2f(playerLocation.getX(), playerLocation.getZ());
        double nearestCenterDistance = 0;
        ImmutableVector2f center = null;
        String biomeName = null;
        for (Graph g : graphs.getAllGraphs()) {
            BiomeModel biomeModel = model.get(g);
            for (org.terasology.polyworld.graph.Region r : g.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(r);
                if (biome.getDisplayName().contains(event.getNewBiome().getDisplayName())) {
                    double temp = playerPosition2d.distanceSquared(r.getCenter());

                    if (nearestCenterDistance == 0) {
                        nearestCenterDistance = temp;
                        center = r.getCenter();
                        biomeName = biome.getDisplayName();
                    }

                    if (temp <= nearestCenterDistance) {
                        nearestCenterDistance = temp;
                        center = r.getCenter();
                        biomeName = biome.getDisplayName();
                    }
                }

            }
        }
        String levelURI = progressTrackingComponent.getLevelPrefab(biomeName);
        if ((!progressTrackingComponent.isWellFound()) && levelURI != null && levelURI.contains("well")) {
            progressTrackingComponent.foundWell = true;
        }

        if (progressTrackingComponent.isWellFound() && levelURI != null) {
            Prefab prefab =
                    assetManager.getAsset(levelURI, Prefab.class).orElse(null);
            EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
            EntityRef item = entityBuilder.build();
            // round center coordinates to Integers
            int x = (int) Math.ceil(center.getX());
            int y = (int) Math.ceil(center.getY());
            Vector3i spawnPosition = new Vector3i(x, getGroundHeight(x, y, Math.round(playerLocation.y),worldProvider), y);
            BlockRegionTransform b = BlockRegionTransform.createRotationThenMovement(Side.FRONT, Side.FRONT,
                    spawnPosition);
            item.send(new SpawnStructureEvent(b));
            Set<String> keySet = progressTrackingComponent.biomeToPrefab.keySet();
            for (String key : keySet) {
                if (progressTrackingComponent.getLevelPrefab(key) != null && progressTrackingComponent.getLevelPrefab(key).equalsIgnoreCase(levelURI)) {
                    progressTrackingComponent.biomeToPrefab.put(key, null);
                }
            }
        }
        player.saveComponent(progressTrackingComponent);
    }

    public static int getGroundHeight(int x, int y, int startHeight,WorldProvider worldProvider) {
        String startBlockURI = worldProvider.getBlock(x, startHeight, y).getURI().toString();
        if (startBlockURI.contains("air") || startBlockURI.contains("Leaf") || startBlockURI.contains("Trunk") || startBlockURI.contains("Cactus")) {
            int height = startHeight;
            while (true) {
                String blockURI = worldProvider.getBlock(x, height, y).getURI().toString();
                if (!(blockURI.contains("air") || blockURI.contains("Leaf") || blockURI.contains("Trunk") || blockURI.contains("Cactus"))) {
                    break;
                }
                height--;
            }
            return height;
        } else {
            int height = startHeight;
            while (true) {
                String blockURI = worldProvider.getBlock(x, height, y).getURI().toString();
                if (blockURI.contains("air") || blockURI.contains("Leaf") || blockURI.contains("Trunk") || blockURI.contains("Cactus")) {
                    break;
                }
                height++;
            }
            return height;
        }
    }
}
