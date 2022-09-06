package me.playground.gui;

import me.playground.entity.EntityHologram;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BeanGuiHologram extends BeanGui {

	private final EntityHologram hologram;

	public BeanGuiHologram(Player p, EntityHologram hologram) {
		super(p);

		this.hologram = hologram;
		this.presetSize = 54;
		this.interactCooldown = 300;
		this.presetInv = new ItemStack[] {bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
				bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
				bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
				bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
				bBlank,bBlank,bBlank,bBlank,closeUI,bBlank,bBlank,bBlank,bBlank};

		setName("Hologram Menu");
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();

	}
}
