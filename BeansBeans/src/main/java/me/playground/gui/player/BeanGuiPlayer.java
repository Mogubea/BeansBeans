package me.playground.gui.player;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiMainMenu;

public abstract class BeanGuiPlayer extends BeanGui {
	
	public BeanGuiPlayer(Player p) {
		super(p);
		
		this.presetSize = 54;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {}

	@Override
	public void onInventoryOpened() {}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
			
		if (i.isSimilar(goBack)) {
			if (this instanceof BeanGuiPlayerMain)
				new BeanGuiMainMenu(p).openInventory();
			else
				new BeanGuiPlayerMain(p).openInventory();
			return true;
		} else if (i.isSimilar(nextPage)) {
			pageUp();
			return true;
		} else if (i.isSimilar(prevPage)) {
			pageDown();
			return true;
		}
		return false;
	}
	
}
