package me.playground.main;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class specifically designed to track if a block has been placed by a player as efficiently as possible
 * 12 Bits are allocated to Y co-ordinate.
 * 26 Bits are allocated to X co-ordinate.
 * 26 Bits are allocated to Z co-ordinate.
 */
public class BlockTracker {

    private final Main plugin;
    private final Map<World, LongSet> trackedBlocks = new HashMap<>();

    protected BlockTracker(Main plugin) {
        this.plugin = plugin;

        if (!plugin.getDataFolder().exists())
            if (!plugin.getDataFolder().mkdir())
                plugin.getSLF4JLogger().error("There was a problem creating the plugin Data Folder.");

        load();
    }

    /**
     * Check if this block is being tracked, and therefore has been placed before.
     * @return true or false
     */
    public boolean isBlockNatural(Block block) {
        return !trackedBlocks.get(block.getWorld()).contains(getBlockKey(block));
    }

    /**
     * Start tracking this block.
     */
    public void trackBlock(Block block) {
        trackedBlocks.get(block.getWorld()).add(getBlockKey(block));
    }

    /**
     * Stop tracking this block.
     */
    public void untrackBlock(Block block) {
        trackedBlocks.get(block.getWorld()).remove(getBlockKey(block));
    }

    protected void save() {
        File trackingFile = getTrackingFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(trackingFile);
        Map<World, LongSet> clone = Map.copyOf(trackedBlocks);
        clone.forEach((world, set) -> cfg.set("worlds." + world.getUID() + ".blocks", set));
        try {
            cfg.save(trackingFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void load() {
        File trackingFile = getTrackingFile();

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(trackingFile);
        plugin.getWorldManager().getWorlds().forEach(world -> trackedBlocks.put(world, new LongOpenHashSet(cfg.getLongList("worlds." + world.getUID() + ".blocks"))));
    }

    private long getBlockKey(Block block) {
        return ((long) block.getY() << 52) | ((block.getX() & 0xFFFFFFFL) << 26) | (block.getZ() & 0xFFFFFFFL);
    }

    private File getTrackingFile() {
        File trackingFile = new File(plugin.getDataFolder() + "/PlacedBlocks.yml");
        if (!trackingFile.exists()) {
            try {
                if (trackingFile.createNewFile())
                    plugin.getSLF4JLogger().info("Created PlacedBlocks.yml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return trackingFile;
    }

}
