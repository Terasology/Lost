// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lost.generator;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

/**
 * Utility class that can be used to access blocks from the Core Registry's block manager.
 */
public final class Blocks {
    private Blocks() {
    }

    /**
     * Get a block with a given ID.
     *
     * @param blockId The ID of the block
     * @return The block with the given ID
     */
    public static Block getBlock(String blockId) {
        return CoreRegistry.get(BlockManager.class).getBlock(blockId);
    }
}
