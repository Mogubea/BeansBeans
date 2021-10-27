package me.playground.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.npc.NPCHuman;

public class BeanGuiNPCEdit extends BeanGui {
	
	final NPCHuman npc;
	
	public BeanGuiNPCEdit(Player p, NPCHuman npc) {
		super(p);
		this.name = "Edit NPC \""+npc.getEntity().getName()+"\"";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null
		};
		this.npc = npc;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		ItemStack[] blah = new ItemStack[6];
		for (int x = -1; ++x < 6;)
			blah[x] = e.getInventory().getItem(54 - (x * 9) - 1);
		npc.setEquipment(blah);
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot > i.getSize()) {
			if (e.isShiftClick()) return;
			e.setCancelled(false);
		} else if (slot % 9 == 8) {
			e.setCancelled(false);
		}
			
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		ItemStack[] items = npc.getEquipment();
		for (int x = 6; --x > -1;) {
			contents[x * 9 + 7] = blank;
			contents[x * 9 + 8] = items[5 - x];
		}
		
		i.setContents(contents);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
		
		final ItemStack i = e.getCurrentItem();
		if (i == null) return false;
		
		if (i.isSimilar(goBack)) {
			new BeanGuiMainMenu(p).openInventory();
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
