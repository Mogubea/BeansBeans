package me.playground.gui;

import me.playground.menushop.PurchaseOption;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

public class BeanGuiRegionExpansion extends BeanGuiRegion {

    private PurchaseOption confirm;
    private final Map<BlockFace, Integer> desiredModification = new HashMap<>();

    public BeanGuiRegionExpansion(Player p, int regionIdx) {
        super(p, regionIdx);

        setName("Region -> Size");
    }

    @Override
    public void onInventoryClicked(InventoryClickEvent e) {
        int slot = e.getRawSlot();
        switch(slot) {

        }
    }

    @Override
    public void onInventoryOpened() {

    }
}
