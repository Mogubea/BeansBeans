package me.playground.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;

public class BeanGuiBeanItems extends BeanGui {
	
	public BeanGuiBeanItems(Player p) {
		super(p);
		
		setName("Custom Items");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blankop,null,null,null,null,null,null,null,blankop,
				blankop,null,null,null,null,null,null,null,blankop,
				blankop,null,null,null,null,null,null,null,blankop,
				blankop,null,null,null,null,null,null,null,blankop,
				blankop,null,null,null,null,null,null,null,blankop,
				blankop,null,null,null,null,null,null,null,blankop
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		e.setCancelled(true);
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		if (e.getCurrentItem() != null && !e.getCurrentItem().equals(blankop)) {
			p.getInventory().addItem(e.getCurrentItem());
		}
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		//final int page = pp.currentlyViewedGUIPage;
		
		for (int y = 0; y < 6; y++) {
			for (int x = 0; x < 7; x++) {
				int entry = (y*7) + x;
				
				if (entry >= BeanItem.values().length) {
					i.setContents(contents);
					return;
				}
				
				ItemStack test = BeanItem.values()[entry].getOriginalStack();
				
				contents[1 + x + (y*9)] = test;
				
			}
		}
		
		//contents[45] = page == 1 ? icon_prev : blank;
		//contents[53] = page == 0 ? icon_next : blank;
	}
	
}
