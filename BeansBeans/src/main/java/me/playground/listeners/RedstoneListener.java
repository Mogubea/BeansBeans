package me.playground.listeners;

import me.playground.main.Main;
import me.playground.regions.flags.Flags;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class RedstoneListener extends EventListener {

    private final RedstoneManager manager;

    public RedstoneListener(Main plugin, RedstoneManager redstoneManager) {
        super(plugin);
        this.manager = redstoneManager;
    }

    @EventHandler
    public void onRedstoneUpdate(BlockRedstoneEvent e) {
        manager.incrementAction(e.getBlock().getChunk(), 1); // Count as 1
        if (manager.getAverageRedstoneActions(e.getBlock().getChunk()) > manager.getMaximumActions()) {
            if (rand.nextInt(5) == 0) // Low chance to break the block as a forced obnoxious counter measure to potential lag machines
                e.getBlock().breakNaturally();
            e.setNewCurrent(0); // Shut it down
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent e) {
        if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.PISTONS)) {
            e.setCancelled(true);
            return;
        }
        manager.incrementAction(e.getBlock().getChunk(), 9 + e.getBlocks().size() * 3);
        if (manager.getAverageRedstoneActions(e.getBlock().getChunk()) > manager.getMaximumActions())
            e.setCancelled(true); // Shut it down
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPiston(BlockPistonRetractEvent e) {
        if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.PISTONS)) {
            e.setCancelled(true);
            return;
        }

        manager.incrementAction(e.getBlock().getChunk(), 9 + e.getBlocks().size() * 3);
        if (manager.getAverageRedstoneActions(e.getBlock().getChunk()) > manager.getMaximumActions())
            e.setCancelled(true); // Shut it down
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHopper(InventoryMoveItemEvent e) {
        manager.incrementAction(e.getSource().getLocation().getChunk(), 8);
        if (manager.getAverageRedstoneActions(e.getSource().getLocation().getChunk()) > manager.getMaximumActions())
            e.setCancelled(true); // Shut it down
    }

}
