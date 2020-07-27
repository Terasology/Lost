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
import org.terasology.logic.console.Console;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.structureTemplates.events.SpawnStructureEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import static org.terasology.lost.LevelSpawnSystem.getGroundHeight;

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
        progressTrackingComponent.addLevel("Lost:well","Beach","Coast","Lakeshore");
        progressTrackingComponent.addLevel("Lost:templePlasmaOfFire","Tropical seasonal forest","Tropical rain forest","Temperate deciduous forest","Temperate rain forest");
        progressTrackingComponent.addLevel("Lost:stonehengeWipedOutKey","Marsh","Shrubland","Grassland");
        progressTrackingComponent.addLevel("Lost:pyramidBladesOfTruth","Tundra","Bare","Scorched","Temperate desert","Subtropical desert");
        player.addComponent(progressTrackingComponent);

        LocationComponent loc = player.getComponent(LocationComponent.class);
        Vector3f playerLocation = loc.getWorldPosition();
        Prefab prefab =
                assetManager.getAsset("Lost:hut", Prefab.class).orElse(null);
        EntityBuilder entityBuilder = entityManager.newBuilder(prefab);
        EntityRef item = entityBuilder.build();
        int x = (int) Math.round(playerLocation.getX()) + 5;
        int y = (int) Math.round(playerLocation.getZ()) + 5;
        Vector3i spawnPosition = new Vector3i(x, getGroundHeight(x, y, Math.round(playerLocation.y),worldProvider), y);
        BlockRegionTransform b = BlockRegionTransform.createRotationThenMovement(Side.FRONT, Side.FRONT,
                spawnPosition);
        item.send(new SpawnStructureEvent(b));
    }
}
