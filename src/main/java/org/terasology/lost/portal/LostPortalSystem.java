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
package org.terasology.lost.portal;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.notifications.NotificationMessageEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LostPortalSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(LostPortalSystem.class);
    @In
    private BlockEntityRegistry blockEntityRegistry;

    @ReceiveEvent(components = {ArkenstoneComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef player = event.getInstigator();
        CharacterHeldItemComponent characterHeldItemComponent = player.getComponent(CharacterHeldItemComponent.class);
        EntityRef heldItem = characterHeldItemComponent.selectedItem;
        if (!heldItem.hasComponent(BlockItemComponent.class)) {
            return;
        }
        if (!heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString()
            .equalsIgnoreCase("Lost:ObsidianTorch")) {
            return;
        }
        activatePortal(event.getTargetLocation(), event.getInstigator());
    }

    private void activatePortal(Vector3f keyLocation, EntityRef player) {
        EntityRef block;
        // Check for surrounding 8 blocks of shattered plasma
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                block = blockEntityRegistry.getBlockEntityAt(new Vector3f(i, 0, j).add(keyLocation));
                if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                    .equalsIgnoreCase("Lost:ShatteredPlasma")) {
                    return;
                }
            }
        }

        // Checking for orientation of facade
        block = blockEntityRegistry.getBlockEntityAt(new Vector3f(1, 1, 0).add(keyLocation));
        if (block.getComponent(BlockComponent.class).getBlock().getURI().toString()
            .equalsIgnoreCase("Lost:FacadeOfTruth")) {

            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(-1, 1, 0).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(1, 2, 0).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(-1, 2, 0).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            for (int i = -1; i <= 1; i++) {
                block = blockEntityRegistry.getBlockEntityAt(new Vector3f(i, 3, 0).add(keyLocation));
                if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                    .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                    return;
                }
            }
        } else {
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(0, 1, 1).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(0, 1, -1).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(0, 2, 1).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            block = blockEntityRegistry.getBlockEntityAt(new Vector3f(0, 2, -1).add(keyLocation));
            if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                return;
            }
            for (int i = -1; i <= 1; i++) {
                block = blockEntityRegistry.getBlockEntityAt(new Vector3f(0, 3, i).add(keyLocation));
                if (!block.getComponent(BlockComponent.class).getBlock().getURI().toString()
                    .equalsIgnoreCase("Lost:FacadeOfTruth")) {
                    return;
                }
            }
        }
        Vector3f playerWorldLocation = player.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        Vector3i roundedKeyPosition = new Vector3i(new Vector3f(keyLocation.x, keyLocation.y, keyLocation.z), RoundingMode.FLOOR);
        Vector3i roundedPlayerPosition = new Vector3i(new Vector3f(playerWorldLocation.x, playerWorldLocation.y, playerWorldLocation.z), RoundingMode.FLOOR);
        if (roundedPlayerPosition.equals(new Vector3i(0, 1, 0).add(roundedKeyPosition)) ||
            roundedPlayerPosition.equals(new Vector3i(0, 2, 0).add(roundedKeyPosition))) {
            return;
        }
        logger.info("Portal active");
        EntityRef client = player.getComponent(CharacterComponent.class).controller;
        client.send(new NotificationMessageEvent("Portal activated!", client));
    }
}
