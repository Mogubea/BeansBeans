package me.playground.worlds.generation;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Test chunk generator for learning generation
 */
public class TestChunkGenerator extends ChunkGenerator {

    public TestChunkGenerator() {

    }

    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        for (int x = -1; ++x < 16;) {
            for (int z = -1; ++z < 16;) {



                // Bedrock layers
                for (int a = 0; ++a < 4;)
                    chunkData.setBlock(x, worldInfo.getMinHeight() + a, z, random.nextInt(10) > (a + 1) ? Material.BEDROCK : Material.DEEPSLATE);
                chunkData.setBlock(x, worldInfo.getMinHeight(), z, Material.BEDROCK);
            }
        }
    }

    @Override
    public void generateCaves(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {

    }

    public boolean shouldGenerateNoise() {
        return true;
    }

    public boolean shouldGenerateCaves() {
        return true;
    }

    public boolean shouldGenerateDecorations() {
        return true;
    }

    public boolean shouldGenerateMobs() {
        return false;
    }

    public boolean shouldGenerateStructures() {
        return false;
    }

}
