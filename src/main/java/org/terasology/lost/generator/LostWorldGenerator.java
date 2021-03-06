// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lost.generator;

import org.joml.RoundingMode;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.spawner.FixedSpawner;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.viewer.picker.CirclePickerClosest;
import org.terasology.polyworld.biome.BiomeModel;
import org.terasology.polyworld.biome.WhittakerBiome;
import org.terasology.polyworld.biome.WhittakerBiomeModelFacet;
import org.terasology.polyworld.biome.WhittakerBiomeModelProvider;
import org.terasology.polyworld.biome.WhittakerBiomeProvider;
import org.terasology.polyworld.elevation.ElevationModelFacetProvider;
import org.terasology.polyworld.elevation.ElevationProvider;
import org.terasology.polyworld.elevation.FlatLakeProvider;
import org.terasology.polyworld.flora.FloraProvider;
import org.terasology.polyworld.flora.TreeProvider;
import org.terasology.polyworld.graph.Graph;
import org.terasology.polyworld.graph.GraphFacet;
import org.terasology.polyworld.graph.GraphFacetProvider;
import org.terasology.polyworld.graph.GraphRegion;
import org.terasology.polyworld.moisture.MoistureModelFacetProvider;
import org.terasology.polyworld.raster.RiverRasterizer;
import org.terasology.polyworld.raster.WhittakerRasterizer;
import org.terasology.polyworld.rivers.RiverModelFacetProvider;
import org.terasology.polyworld.rp.WorldRegionFacetProvider;
import org.terasology.polyworld.water.WaterModelFacetProvider;


@RegisterWorldGenerator(id = "lost", displayName = "Lost", description = "Generates the world for playing the 'Lost' " +
        "exploration world.")
public class LostWorldGenerator extends BaseFacetedWorldGenerator {
    public static World world;
    // Radius to search for a suitable spawn location
    private static final int SEARCH_RADIUS = 7000;

    public LostWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        int maxCacheSize = 20;
        return new WorldBuilder(CoreRegistry.get(WorldGeneratorPluginLibrary.class))
            .setSeaLevel(6)
            .addProvider(new SeaLevelProvider(6))
            .addProvider(new WorldRegionFacetProvider(maxCacheSize, 1f))
            .addProvider(new GraphFacetProvider(maxCacheSize, 0.1f, 2))
            .addProvider(new WaterModelFacetProvider(maxCacheSize))
            .addProvider(new ElevationModelFacetProvider(maxCacheSize))
            .addProvider(new ElevationProvider())
            .addProvider(new SurfaceToDensityProvider())
            .addProvider(new RiverModelFacetProvider(maxCacheSize))
            .addProvider(new FlatLakeProvider())
            .addProvider(new MoistureModelFacetProvider(maxCacheSize))
            .addProvider(new WhittakerBiomeModelProvider(maxCacheSize))
            .addProvider(new WhittakerBiomeProvider())
            .addProvider(new TreeProvider())
            .addProvider(new FloraProvider())
            .addRasterizer(new WhittakerRasterizer())
            .addRasterizer(new RiverRasterizer())
            .addRasterizer(new TreeRasterizer())
            .addRasterizer(new FloraRasterizer())
            .addPlugins();
    }

    @Override
    public Vector3fc getSpawnPosition(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition(new Vector3f());
        Vector3i ext = new Vector3i(SEARCH_RADIUS, 1, SEARCH_RADIUS);
        Vector3i desiredPos = new Vector3i(new Vector3f(pos.x(), 1, pos.z()), RoundingMode.FLOOR);

        // try and find somewhere in this region a spot to land
        BlockRegion searchArea = new BlockRegion(desiredPos).expand(ext);
        org.terasology.engine.world.generation.Region worldRegion = getWorld().getWorldData(searchArea);
        world = getWorld();
        // graphs contains all graphs relevant within a radius of 7000 blocks from (0,0,0)
        GraphFacet graphs = worldRegion.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet model = worldRegion.getFacet(WhittakerBiomeModelFacet.class);
        org.joml.Vector2f pos2d = new org.joml.Vector2f(pos.x(), pos.z());
        CirclePickerClosest<GraphRegion> picker = new CirclePickerClosest<>(pos2d);
        boolean locationFound = false;
        // searches for a spawn point such that it contains all the biomes required nearby
        for (Graph g : graphs.getAllGraphs()) {
            BiomeModel biomeModel = model.get(g);
            for (GraphRegion r : g.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(r);
                boolean ocean = false;
                boolean forest = false;
                boolean desert = false;
                if (biomeModel.getBiome(r).equals(WhittakerBiome.OCEAN)) {
                    continue;
                }
                if (isDesertBiome(r, biomeModel)) {
                    desert = true;
                }
                if (isForestBiome(r, biomeModel)) {
                    forest = true;
                }
                for (GraphRegion neighbour : r.getNeighbors()) {
                    if (biomeModel.getBiome(neighbour).equals(WhittakerBiome.OCEAN)) {
                        ocean = true;
                        break;
                    }
                    if (isDesertBiome(neighbour, biomeModel)) {
                        desert = true;
                    }
                    if (isForestBiome(neighbour, biomeModel)) {
                        forest = true;
                    }
                    for (GraphRegion neighbour2 : neighbour.getNeighbors()) {
                        if (isForestBiome(neighbour2, biomeModel)) {
                            forest = true;
                        }
                        if (isDesertBiome(neighbour2, biomeModel)) {
                            desert = true;
                        }
                    }
                }
                if (ocean || !forest || !desert) {
                    continue;
                }
                if (!biome.equals(WhittakerBiome.OCEAN) && !biome.equals(WhittakerBiome.LAKE) && !biome.equals(WhittakerBiome.BEACH)) {
                    picker.offer(r.getCenter(), r);
                    locationFound = true;
                    break;
                }
            }
            if (locationFound) {
                break;
            }
        }
        Vector2i target;
        if (picker.getClosest() != null) {
            Vector2fc hit = picker.getClosest().getCenter();
            target = new Vector2i(hit.x(), hit.y(), RoundingMode.FLOOR);
        } else {
            target = new Vector2i(desiredPos.x(), desiredPos.z());
        }

        FixedSpawner spawner = new FixedSpawner(target.x(), target.y());
        return spawner.getSpawnPosition(getWorld(), entity);
    }

    private boolean isForestBiome(GraphRegion region, BiomeModel biomeModel) {
        return biomeModel.getBiome(region).equals(WhittakerBiome.TROPICAL_SEASONAL_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_RAIN_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TROPICAL_RAIN_FOREST);
    }

    private boolean isDesertBiome(GraphRegion region, BiomeModel biomeModel) {
        return biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_DESERT) || biomeModel.getBiome(region).equals(WhittakerBiome.SUBTROPICAL_DESERT);
    }
}
