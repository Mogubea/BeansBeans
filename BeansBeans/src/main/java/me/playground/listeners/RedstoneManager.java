package me.playground.listeners;

import me.playground.main.Main;
import org.bukkit.Chunk;

import java.util.Map;
import java.util.TreeMap;

public class RedstoneManager {

    private final short maxRedstoneActions = 4000; // The maximum amount of actions per second, per chunk, we permit before stopping the Redstone
    private final Map<Long, Integer> averageChunkRedstoneActions = new TreeMap<>();
    private final Map<Long, int[]> chunkRedstoneActions = new TreeMap<>();
    private final Main plugin;

    private int currentIdx = 0;
    private final int secondsToAverage = 5;

    public RedstoneManager(Main plugin) {
        this.plugin = plugin;
        redstoneDeductionLoop();
    }

    private void redstoneDeductionLoop() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            currentIdx += 1;
            if (currentIdx >= secondsToAverage)
                currentIdx = 0;

            for (Map.Entry<Long, int[]> entry : chunkRedstoneActions.entrySet()) {
                long id = entry.getKey();
                int[] actions = entry.getValue();

                int average = 0;
                for (int x = -1; ++x < secondsToAverage;)
                    average += actions[x];

                average = (int) ((float)average / (float)secondsToAverage);

                if (average < 1) {
                    averageChunkRedstoneActions.remove(id);
                    chunkRedstoneActions.remove(id);
                    break;
                } else {
                    averageChunkRedstoneActions.put(id, average);
                    actions[currentIdx] = 0; // Reset the count for this second
                }
            }
        }, 0L, 20L);
    }

    public int getAverageRedstoneActions(Chunk chunk) {
        return averageChunkRedstoneActions.getOrDefault(chunk.getChunkKey(), 0);
    }

    private int[] getRedstoneActions(Chunk chunk) {
        return chunkRedstoneActions.getOrDefault(chunk.getChunkKey(), new int[5]);
    }

    protected void incrementAction(Chunk chunk, int amount) {
        int[] actions = getRedstoneActions(chunk);
        actions[currentIdx] += amount;

        chunkRedstoneActions.put(chunk.getChunkKey(), actions);
    }

    public int getMaximumActions() {
        return maxRedstoneActions;
    }

}
