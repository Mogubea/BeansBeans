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
 * 16 Bits are allocated to Y co-ordinate.
 * 24 Bits are allocated to X co-ordinate.
 * 24 Bits are allocated to Z co-ordinate.
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
        trackedBlocks.forEach((world, set) -> cfg.set("worlds." + world.getUID() + ".blocks", set.toLongArray()));
        try {
            cfg.save(trackingFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the list of block keys that have been saved previously
     */
    private void load() {
        File trackingFile = getTrackingFile();
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(trackingFile);
        plugin.getWorldManager().getWorlds().forEach(world -> trackedBlocks.put(world, new LongOpenHashSet(cfg.getLongList("worlds." + world.getUID() + ".blocks"))));
    }

    /**
     * Get the block key for the provided block, this is unique for every single block at any position
     * @return a long key.
     */
    private long getBlockKey(Block block) {
        long yBits = block.getY() & 0xFFFL; // 16
        long xBits = block.getX() & 0xFFFFFFL; // 24
        long zBits = block.getZ() & 0xFFFFFFL; // 24, each F represents 4 bits
        return (yBits << 48) | (xBits << 24) | (zBits);
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

    /**
     * The maximum vertical world size allowed
     */
    public static int getMaximumVertical() {
        return 0xFFFF - 1;
    }

    /**
     * The maximum horizontal world size allowed
     */
    public static int getMaximumHorizontal() {
        return 0xFFFFFF - 1;
    }

}
