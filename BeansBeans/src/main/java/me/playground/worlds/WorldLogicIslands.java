package me.playground.worlds;

import me.playground.worlds.generation.ChunkGeneratorEmpty;
import org.bukkit.util.BlockVector;

public class WorldLogicIslands extends WorldLogic {

    private final int MAX_ISLAND_SIZE = 16 * 16; // Maximum island size a player can obtain in blocks, X and Z
    private final int ISLAND_GAP = 16 * 8; // Distance between islands

    protected WorldLogicIslands(BeanWorld world) {
        super(world);

        if (!(world.getBukkitWorld().getGenerator() instanceof ChunkGeneratorEmpty))
            throw new RuntimeException("No.");

        world.getBukkitWorld().getWorldBorder().setWarningDistance(1);
        world.getBukkitWorld().getWorldBorder().setSize(50000);
    }

    private BlockVector[] getIslandPosition(int position) {
        if (position <= -1) position = 0;

        int layer = 0;
        int layerSize = 1;
        for (int x = 1; x < 100; x+=2) {
            layer++;
            layerSize = x;
            if (position < x * x) // 1, 9, 25, 49, 81
                break;
        }

        int posX = -layer + 1, posZ = -layer + 1;

        int curPos = layerSize * layerSize;
        for (int x = -layer; ++x < layer;) {
            if (curPos == position) break; // Just break

            for (int z = -layer; ++z < layer;) {
                if (z != -layer + 1 && z != layer - 1 && x != -layer + 1 && x != layer - 1) continue; // Only count that specific layer
                if (++curPos == position) { // Update position and check if it matches
                    posX = x;
                    posZ = z;
                    break;
                }
            }
        }

        BlockVector min = new BlockVector(posX * (MAX_ISLAND_SIZE + ISLAND_GAP), 0, posZ * (MAX_ISLAND_SIZE + ISLAND_GAP));
        BlockVector max = min.clone().add(new BlockVector(MAX_ISLAND_SIZE, world.getBukkitWorld().getMaxHeight(), MAX_ISLAND_SIZE)).toBlockVector();
        return new BlockVector[] {min , max};
    }

}
