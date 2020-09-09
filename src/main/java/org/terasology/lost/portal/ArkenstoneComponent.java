// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lost.portal;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

@Replicate
public class ArkenstoneComponent implements Component {

    // Stores whether block is activated
    public boolean activated = false;
}
