package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class BeanGuiRegionMain extends BeanGuiRegion {
	
	protected static final ItemStack icon_members = newItem(new ItemStack(Material.ACACIA_DOOR), "\u00a7bRegion Members", "\u00a77\u00a7oMembers of the Region");
	
	public BeanGuiRegionMain(Player p) {
		super(p);
		
		this.name = "Regions";
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,icon_members,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}
	
	protected BeanGuiRegionMain(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		switch(slot) {
		case 22: // Member Button
			new BeanGuiRegionMembers(p, regionIdx).openInventory();
			break;
		default:
			return;
		}
		
		setPage(0);
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		super.onInventoryOpened();
	}
	
}
