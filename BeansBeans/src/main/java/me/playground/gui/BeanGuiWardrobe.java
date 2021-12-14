package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class BeanGuiWardrobe extends BeanGui {
	
	public BeanGuiWardrobe(Player p) {
		super(p);
		
		this.name = "Wardrobe";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,null,null,null,null,null,blank,
				blank,null,blank,null,null,null,null,null,blank,
				blank,null,blank,null,null,null,null,null,blank,
				blank,null,blank,null,null,null,null,null,blank,
				blank,null,blank,null,null,null,null,null,blank,
				blank,blank,blank,null,null,null,null,null,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		if (slot < 0 || slot >= e.getInventory().getSize()) return;
		
		final Material mat = e.getInventory().getItem(slot).getType();
		
		if (slot < 44 && slot > 38) {
			if (mat != Material.MINECART) {
				tpp.performWardrobeSwap(slot - 38);
				onInventoryOpened();
			}
		} else
		if (slot < 53 && slot > 47) {
			if (mat != Material.ENDER_CHEST) {
				tpp.withdrawWardrobe(slot - 47);
				onInventoryOpened();
			}
		}
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		final ItemStack[] armour = tpp.getArmourWardrobe();
		
		boolean hasCurrentSet = false;
		ItemStack[] currentSet = tpp.getPlayer().getInventory().getArmorContents();
		for (int x = 0; x < 4; x++) {
			contents[10 + (x * 9)] = currentSet[3 - x];
			if (currentSet[x] != null)
				hasCurrentSet = true;
		}
		
		
		for (int x = 0; x < 5; x++) {
			boolean noSet = true;
			for (int y = 0; y < 4; y++) {
				ItemStack piece = armour[(x * 4) + (3 - y)];
				if (piece != null)
					noSet = false;
				contents[(3 + x) + (9 * y)] = piece;
			}
			
			if (noSet) {
				if (hasCurrentSet) {
					contents[(3 + x) + (9 * 4)] = newItem(new ItemStack(Material.HOPPER_MINECART), "\u00a7bStore Armour", "\u00a77\u00a7oStore your Armour Set!");
				} else {
					contents[(3 + x) + (9 * 4)] = newItem(new ItemStack(Material.MINECART), "\u00a7cUnavailable", "\u00a77\u00a7oYou're not wearing anything!");
				}
				contents[(3 + x) + (9 * 5)] = newItem(new ItemStack(Material.ENDER_CHEST), "\u00a7cUnavailable", "\u00a77\u00a7oThere's nothing to collect!");
			} else { 
				if (hasCurrentSet) {
					contents[(3 + x) + (9 * 4)] = newItem(new ItemStack(Material.CHEST_MINECART), "\u00a7bSwap Armour", "\u00a77\u00a7oSwap this Armour Set with", "\u00a77\u00a7othe one you're wearing!");
				} else {
					contents[(3 + x) + (9 * 4)] = newItem(new ItemStack(Material.CHEST_MINECART), "\u00a7bWear Armour", "\u00a77\u00a7oWear this Armour Set!");
				}
				contents[(3 + x) + (9 * 5)] = newItem(new ItemStack(Material.CHEST), "\u00a7aCollect Armour", "\u00a77\u00a7oReturn this Armour Set", "\u00a77\u00a7oback to your inventory!");
			}
			
		}
		
		i.setContents(contents);
	}
	
}
