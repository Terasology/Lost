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
package org.terasology.lost.generator;

import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.spawner.FixedSpawner;
import org.terasology.math.Region3i;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
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
import org.terasology.polyworld.graph.Region;
import org.terasology.polyworld.moisture.MoistureModelFacetProvider;
import org.terasology.polyworld.raster.RiverRasterizer;
import org.terasology.polyworld.raster.WhittakerRasterizer;
import org.terasology.polyworld.rivers.RiverModelFacetProvider;
import org.terasology.polyworld.rp.WorldRegionFacetProvider;
import org.terasology.polyworld.water.WaterModelFacetProvider;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generation.World;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.viewer.picker.CirclePickerClosest;


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
    public Vector3f getSpawnPosition(EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        Vector3i ext = new Vector3i(SEARCH_RADIUS, 1, SEARCH_RADIUS);
        Vector3i desiredPos = new Vector3i(pos.getX(), 1, pos.getZ());

        // try and find somewhere in this region a spot to land
        Region3i searchArea = Region3i.createFromCenterExtents(desiredPos, ext);
        org.terasology.world.generation.Region worldRegion = getWorld().getWorldData(searchArea);
        world = getWorld();
        // graphs contains all graphs relevant within a radius of 7000 blocks from (0,0,0)
        GraphFacet graphs = worldRegion.getFacet(GraphFacet.class);
        WhittakerBiomeModelFacet model = worldRegion.getFacet(WhittakerBiomeModelFacet.class);
        Vector2f pos2d = new Vector2f(pos.getX(), pos.getZ());
        CirclePickerClosest<Region> picker = new CirclePickerClosest<>(pos2d);
        boolean locationFound = false;
        // searches for a spawn point such that it contains all the biomes required nearby
        for (Graph g : graphs.getAllGraphs()) {
            BiomeModel biomeModel = model.get(g);
            for (Region r : g.getRegions()) {
                WhittakerBiome biome = biomeModel.getBiome(r);
                boolean ocean = false;
                boolean forest = false;
                boolean desert = false;
                if (biomeModel.getBiome(r).equals(WhittakerBiome.TROPICAL_SEASONAL_FOREST) || biomeModel.getBiome(r).equals(WhittakerBiome.TEMPERATE_RAIN_FOREST) || biomeModel.getBiome(r).equals(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST) || biomeModel.getBiome(r).equals(WhittakerBiome.TROPICAL_RAIN_FOREST)) {
                    forest = true;
                }
                for (Region neighbour : r.getNeighbors()) {
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
                    for (Region neighbour2 : neighbour.getNeighbors()) {
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
            if(locationFound){
                break;
            }
        }
        Vector2i target;
        if (picker.getClosest() != null) {
            ImmutableVector2f hit = picker.getClosest().getCenter();
            target = new Vector2i(hit.getX(), hit.getY());
        } else {
            target = new Vector2i(desiredPos.getX(), desiredPos.getZ());
        }

        FixedSpawner spawner = new FixedSpawner(target.getX(), target.getY());
        return spawner.getSpawnPosition(getWorld(), entity);
    }

    private boolean isForestBiome(Region region, BiomeModel biomeModel) {
        return biomeModel.getBiome(region).equals(WhittakerBiome.TROPICAL_SEASONAL_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_RAIN_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_DECIDUOUS_FOREST) || biomeModel.getBiome(region).equals(WhittakerBiome.TROPICAL_RAIN_FOREST);
    }

    private boolean isDesertBiome(Region region, BiomeModel biomeModel) {
        return biomeModel.getBiome(region).equals(WhittakerBiome.TEMPERATE_DESERT) || biomeModel.getBiome(region).equals(WhittakerBiome.SUBTROPICAL_DESERT);
    }

}
