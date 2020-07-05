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
        progressTrackingComponent.addLevel("Grassland", "Lost:well");
        progressTrackingComponent.addLevel("Tundra", "Lost:well");
        progressTrackingComponent.addLevel("Bare", "Lost:well");
        progressTrackingComponent.addLevel("Scorched", "Lost:well");
        progressTrackingComponent.addLevel("Taiga", "Lost:well");
        progressTrackingComponent.addLevel("Temperate desert", "Lost:well");
        progressTrackingComponent.addLevel("Temperate rain forest", "Lost:well");
        progressTrackingComponent.addLevel("Temperate deciduous forest", "Lost:well");
        progressTrackingComponent.addLevel("Subtropical desert", "Lost:well");
        progressTrackingComponent.addLevel("Shrubland", "Lost:well");
        progressTrackingComponent.addLevel("Marsh", "Lost:well");
        progressTrackingComponent.addLevel("Tropical rain forest", "Lost:well");
        progressTrackingComponent.addLevel("Tropical seasonal forest", "Lost:well");
        progressTrackingComponent.addLevel("Lakeshore", "Lost:well");
        progressTrackingComponent.addLevel("Coast", "Lost:well");
        progressTrackingComponent.addLevel("Ice", "Lost:well");
        player.addComponent(progressTrackingComponent);
    }

    @ReceiveEvent
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player) {
        console.addMessage("OLD:" + event.getOldBiome().getDisplayName());
        console.addMessage("NEW:" + event.getNewBiome().getDisplayName());
        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        ProgressTrackingComponent progressTrackingComponent = player.getComponent(ProgressTrackingComponent.class);
        int searchRadius = 0;
        Vector3i ext = new Vector3i(searchRadius, 0, searchRadius);
        Vector3i desiredPos = new Vector3i(pos.getX(), 1, pos.getZ());

        // try and find somewhere in this region a spot to land
        Region3i spawnArea = Region3i.createFromCenterExtents(desiredPos, ext);
        Region worldRegion = LostWorldGenerator.world.getWorldData(spawnArea);

        GraphFacet graphs = worldRegion.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet model = worldRegion.getFacet(WhittakerBiomeModelFacet.class);
        Vector2f pos2d = new Vector2f(pos.getX(), pos.getZ());
        double d = 1000000;
        ImmutableVector2f center = null;
        String biomeName = null;
        for (Graph g : graphs.getAllGraphs()) {
            BiomeModel biomeModel = model.get(g);
            for (org.terasology.polyworld.graph.Region r : g.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(r);
                if (biome.getDisplayName().contains(event.getNewBiome().getDisplayName())) {
                    double temp = Math.pow(pos2d.x - pos.getX(), 2) + Math.pow(pos2d.y - pos.getZ(), 2);
                    if (temp < d) {
                        d = temp;
                        center = r.getCenter();
                        biomeName = biome.getDisplayName();
                    }
                }

            }
        }
        console.addMessage(center.toString());
        if ((!progressTrackingComponent.isWellFound())&&progressTrackingComponent.getLevelPrefab(biomeName).contains("well")) {
            progressTrackingComponent.foundWell = true;
        }

        if (progressTrackingComponent.isWellFound()) {
            Prefab prefab =
                    assetManager.getAsset(progressTrackingComponent.getLevelPrefab(biomeName), Prefab.class).orElse(null);
            EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
            EntityRef item = entityBuilder.build();
            int tempy = (int) Math.ceil(pos.y) + 20;
            while (true) {
                if (!(worldProvider.getBlock((int) Math.ceil(center.getX()), tempy, (int) Math.ceil(center.getY())).getURI().toString().contains("air") || worldProvider.getBlock((int) Math.ceil(center.getX()), tempy, (int) Math.ceil(center.getY())).getURI().toString().contains("air"))) {
                    break;
                }
                tempy--;
            }
            Vector3i vector3i = new Vector3i((int) Math.ceil(center.getX()), tempy, (int) Math.ceil(center.getY()));
            BlockRegionTransform b = BlockRegionTransform.createRotationThenMovement(Side.FRONT, Side.FRONT, vector3i);
            item.send(new SpawnStructureEvent(b));
        }
        player.saveComponent(progressTrackingComponent);
    }
}
