package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class BeanGuiNews extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.BROWN_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiNews(Player p) {
		super(p);
		
		this.name = "Updates & Announcements";
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
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		
		if (item == null)
			return;
		
		if (slot < 54 - 9) {
			close();
			p.openBook(UpdateEntry.getUpdateEntries().get(slot + (page * 45)).getBook());
		}
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[46] = page > 0 ? prevPage : blank;
		
		int x = 0 + (page * 45);
		for (UpdateEntry entry : UpdateEntry.getUpdateEntries()) {
			if (x > (44 + (page * 45))) {
				contents[52] = nextPage;
				break;
			}
			
			contents[x - (page * 45)] = entry.getCover();
			x++;
		}
		i.setContents(contents);
	}
	
}
