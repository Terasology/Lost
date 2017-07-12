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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LostPortalSystem extends BaseComponentSystem {


    private static final Logger logger = LoggerFactory.getLogger(LostPortalSystem.class);

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
        activatePortal(event.getTargetLocation());
    }

    private void activatePortal(Vector3f keyLocation) {
        logger.info("Activating Portal with keystone at: " + keyLocation);
    }
}
