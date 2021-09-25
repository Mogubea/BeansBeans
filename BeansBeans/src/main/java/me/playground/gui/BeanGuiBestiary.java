package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.stats.PlayerStats;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class BeanGuiBestiary extends BeanGui {
	
	protected ItemStack blank = newItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), Component.text("Bestiary", BeanColor.BESTIARY));
	protected ItemStack pageEntities = newItem(new ItemStack(Material.ZOMBIE_HEAD, 1), Component.text("Creatures", BeanColor.BESTIARY));
	protected ItemStack pageFishing = newItem(new ItemStack(Material.FISHING_ROD, 1), Component.text("Fishing", BeanColor.BESTIARY));
	
	public BeanGuiBestiary(Player p) {
		super(p);
		
		this.name = "Bestiary";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,pageEntities,null,pageFishing,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		switch (slot) {
		case 21: 
			new BeanGuiBestiaryEntity(p).openInventory();
			break;
		case 23:
			break;
		}
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
