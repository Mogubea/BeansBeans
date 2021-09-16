package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.stats.PlayerStats;

public class BeanGuiBestiary extends BeanGui {
	
	protected ItemStack blank = newItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), "\u00a72Bestiary");
	
	public BeanGuiBestiary(Player p) {
		super(p);
		
		this.name = "Bestiary";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		/*final int slot = e.getRawSlot();
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		
		if (item == null)
			return;*/
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = i.getContents();
		
		i.setContents(contents);
	}
	
	protected PlayerStats getStats() {
		return tpp.getStats();
	}
	
}
