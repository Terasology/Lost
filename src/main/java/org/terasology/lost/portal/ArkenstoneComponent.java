// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lost.portal;

import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

@Replicate
public class ArkenstoneComponent implements Component<ArkenstoneComponent> {

    // Stores whether block is activated
    public boolean activated = false;

    @Override
    public void copyFrom(ArkenstoneComponent other) {
        this.activated = other.activated;
    }
}
