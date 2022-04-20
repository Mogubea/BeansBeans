package me.playground.gui.debug;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.gui.BeanGui;

public class BeanGuiDebug extends BeanGui {
	
	protected ItemStack blank;
	
	public BeanGuiDebug(Player p) {
		super(p);
		setName("Debug");
		this.presetSize = 54;
	}
	
	protected BeanGuiDebug(Player p, int regionIdx) {
		this(p);
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
	}

	@Override
	public void onInventoryOpened() {
	}
	
}
