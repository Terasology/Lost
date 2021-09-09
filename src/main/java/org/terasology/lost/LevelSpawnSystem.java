// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.lost;

import org.joml.RoundingMode;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.biomesAPI.OnBiomeChangedEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lost.generator.LostWorldGenerator;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;

import java.util.Set;

/**
 * Creates the levels/challenges for the Lost gameplay. On every biome change event it is checked whether the it is
 * suitable for a level spawn. If yes, the challenge corresponding to the biome is spawned in the centre of the voronoi
 * {@link GraphRegion} entered The challenges start spawning in their respective biomes once the book in the well has been
 * discovered
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class LevelSpawnSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(LevelSpawnSystem.class);

    @In
    private EntityManager entityManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private AssetManager assetManager;
    @In
    private WorldProvider worldProvider;

    // to prevent overlapping with hut
    private static final int MINIMUM_DISTANCE_FROM_HUT = 30;

    @ReceiveEvent
    public void onBiomeChange(OnBiomeChangedEvent event, EntityRef player,
                              ProgressTrackingComponent progressTrackingComponent) {
        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f playerLocation = loc.getWorldPosition(new Vector3f());
        playerLocation = playerLocation.add(loc.getWorldDirection(new Vector3f()).mul(3));
        // nearby area radius for which facets are fetched
        int searchRadius = 400;

        // create Region to be searched
        Vector3i extent = new Vector3i(searchRadius, 1, searchRadius);
        Vector3i desiredPos = new Vector3i(new Vector3f(playerLocation.x(), 1, playerLocation.z()), RoundingMode.FLOOR);
        BlockRegion searchRegion = new BlockRegion(desiredPos).expand(extent);

        // obtain surface height facet for the search region
        org.terasology.engine.world.generation.Region worldRegion = LostWorldGenerator.world.getWorldData(searchRegion);
        SurfacesFacet surfacesFacet = worldRegion.getFacet(SurfacesFacet.class);
        ElevationFacet elevationFacet = worldRegion.getFacet(ElevationFacet.class);

        // fetch the current voronoi region
        GraphRegion region = worldRegion.getFacet(GraphFacet.class).getWorldTriangle(Math.round(playerLocation.x),
                Math.round(playerLocation.z)).getRegion();
        Vector2fc center = region.getCenter();

        float distanceFromHut = center.distance(progressTrackingComponent.hutPosition.x,
                progressTrackingComponent.hutPosition.z);
        if (distanceFromHut < MINIMUM_DISTANCE_FROM_HUT && !event.getNewBiome().getDisplayName().contains("forest")) {
            return;
        }
        String levelURI = progressTrackingComponent.getLevelPrefab(event.getNewBiome().getDisplayName());
        if (levelURI != null && levelURI.contains("well")) {
            progressTrackingComponent.foundWell = true;
        }
        if (levelURI != null && progressTrackingComponent.isWellFound()) {
            // round center coordinates to Integers
            int x = Math.round(center.x());
            int y = Math.round(center.y());
            int height = Math.round(surfacesFacet.getPrimarySurface(elevationFacet, x, y).orElse(elevationFacet.getWorld(x, y)));

            Vector3i spawnPosition = new Vector3i(x, height, y);
            spawnLevel(levelURI, spawnPosition, assetManager, entityManager);

            // prevent level just spawned from being spawned again
            Set<String> keySet = progressTrackingComponent.biomeToPrefab.keySet();
            for (String key : keySet) {
                if (progressTrackingComponent.getLevelPrefab(key) != null && progressTrackingComponent.getLevelPrefab(key).equals(levelURI)) {
                    progressTrackingComponent.biomeToPrefab.put(key, null);
                }
            }
        }
        player.saveComponent(progressTrackingComponent);
    }

    /**
     * Spawns a Structure Template from the specified level urn in the specified position
     */
    public static void spawnLevel(String levelURI, Vector3i spawnPosition, AssetManager assetManager,
                                  EntityManager entityManager) {
        Prefab prefab = assetManager.getAsset(levelURI, Prefab.class).orElse(null);
        if (prefab == null) {
            logger.error("Level prefab for the specified URI not found. Give URI :" + levelURI);
            return;
        }
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        EntityRef item = entityBuilder.build();
        BlockRegionTransform blockRegionTransform = BlockRegionTransform.createRotationThenMovement(Side.FRONT,
                Side.FRONT, spawnPosition);
        item.send(new SpawnStructureEvent(blockRegionTransform));
    }
}
