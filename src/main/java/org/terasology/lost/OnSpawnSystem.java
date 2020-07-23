/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.lost;

import org.terasology.assets.management.AssetManager;
import org.terasology.biomesAPI.BiomeManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.Side;
import org.terasology.registry.In;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.spawner.FixedSpawner;
import org.terasology.math.Region3i;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphFacetProvider;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.generation.Region;
import org.terasology.lost.generator.*;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class OnSpawnSystem extends BaseComponentSystem {
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
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
        inventoryManager.giveItem(player, null, entityManager.create("Lost:antrumSabre"));
        ProgressTrackingComponent progressTrackingComponent = new ProgressTrackingComponent();
        progressTrackingComponent.addLevel("Grassland", "Lost:stonehengeWipedOutKey");
        progressTrackingComponent.addLevel("Tundra", "Lost:pyramidBladesOfTruth");
        progressTrackingComponent.addLevel("Bare", "Lost:pyramidBladesOfTruth");
        progressTrackingComponent.addLevel("Scorched", "Lost:pyramidBladesOfTruth");
        progressTrackingComponent.addLevel("Temperate desert", "Lost:pyramidBladesOfTruth");
        progressTrackingComponent.addLevel("Temperate rain forest", "Lost:templePlasmaOfFire");
        progressTrackingComponent.addLevel("Temperate deciduous forest", "Lost:templePlasmaOfFire");
        progressTrackingComponent.addLevel("Subtropical desert", "Lost:pyramidBladesOfTruth");
        progressTrackingComponent.addLevel("Shrubland", "Lost:stonehengeWipedOutKey");
        progressTrackingComponent.addLevel("Marsh", "Lost:stonehengeWipedOutKey");
        progressTrackingComponent.addLevel("Tropical rain forest", "Lost:templePlasmaOfFire");
        progressTrackingComponent.addLevel("Tropical seasonal forest", "Lost:templePlasmaOfFire");
        progressTrackingComponent.addLevel("Lakeshore", "Lost:well");
        progressTrackingComponent.addLevel("Coast", "Lost:well");
        progressTrackingComponent.addLevel("Beach", "Lost:well");
        player.addComponent(progressTrackingComponent);

        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f playerLocation = loc.getWorldPosition();
        Prefab prefab =
                assetManager.getAsset("Lost:hut", Prefab.class).orElse(null);
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        EntityRef item = entityBuilder.build();
        int x = (int) Math.round(playerLocation.getX()) + 5;
        int y = (int) Math.round(playerLocation.getZ()) + 5;
        Vector3i spawnPosition = new Vector3i(x, getGroundHeight(x, y), y);
        BlockRegionTransform b = BlockRegionTransform.createRotationThenMovement(Side.FRONT, Side.FRONT,
                spawnPosition);
        item.send(new SpawnStructureEvent(b));
    }

    @ReceiveEvent
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player) {
        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f playerLocation = loc.getWorldPosition();
        ProgressTrackingComponent progressTrackingComponent = player.getComponent(ProgressTrackingComponent.class);
        // fetch nearest biome center
        int searchRadius = 40;
        Vector3i extent = new Vector3i(searchRadius, 1, searchRadius);
        Vector3i desiredPos = new Vector3i(playerLocation.getX(), 1, playerLocation.getZ());
        Region3i spawnArea = Region3i.createFromCenterExtents(desiredPos, extent);
        Region worldRegion = LostWorldGenerator.world.getWorldData(spawnArea);
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
            Vector3i spawnPosition = new Vector3i(x, getGroundHeight(x, y), y);
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

    private int getGroundHeight(int x, int y) {
        // maximum possible height in polyworld is 52
        int height = 52;
        while (true) {
            String blockURI = worldProvider.getBlock(x, height, y).getURI().toString();
            if (!(blockURI.contains("air") || blockURI.contains("Leaf") || blockURI.contains("Trunk") || blockURI.contains("Cactus"))) {
                break;
            }
            height--;
        }
        return height;
    }
}
