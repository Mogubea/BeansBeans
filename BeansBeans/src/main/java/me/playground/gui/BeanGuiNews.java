package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BeanGuiNews extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.BROWN_STAINED_GLASS_PANE, 1), "");
	private final HashMap<Integer, UpdateEntry> mappings = new HashMap<Integer, UpdateEntry>();
	private final ArrayList<UpdateEntry> entries = UpdateEntry.getUpdateEntries();
	
	public BeanGuiNews(Player p) {
		super(p);
		
		this.name = "Server News";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,newItem(new ItemStack(Material.ENCHANTED_BOOK), Component.text("Server News", TextColor.color(0x994411))),bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		UpdateEntry entry = mappings.get(e.getRawSlot());
		if (entry == null) return;
		
		p.openBook(entry.getBook());
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		final int size = entries.size() - (page * 7 * 4);
		
		if (page > 0) contents[46] = prevPage;
		if (size >= 36) contents[52] = nextPage;
		
		for (int x = -1; ++x < size;) {
			final int y = x + (7 * 4 * page);
			final int row = x / 7;
			UpdateEntry entry = entries.get(y);
			
			contents[10 + x + (2 * row)] = entry.getCover();
			mappings.put(10 + x + (2 * row), entry);
		}
		
		i.setContents(contents);
	}
	
}
