package me.playground.items;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.HeirloomInventory;

public abstract class BeanItemHeirloom extends BeanItem {
	
	protected BeanItemHeirloom(String identifier, String name, ItemStack item, ItemRarity rarity) {
		super(identifier, name, item, rarity, 0);
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		e.setCancelled(true);
	}
	
	public static BeanItemHeirloom from(ItemStack item) {
		BeanItem bi = BeanItem.from(item);
		if (bi instanceof BeanItemHeirloom)
			return (BeanItemHeirloom) bi;
		return null;
	}
	
	/**
	 * Called whenever a player successfully consumes an item and they have this Heirloom in their {@link HeirloomInventory}
	 * @param e - The {@link PlayerItemConsumeEvent} instance.
	 * @param inv - The {@link HeirloomInventory} that holds the {@link BeanItemHeirloom}.
	 */
	public void onConsumeItem(PlayerItemConsumeEvent e, HeirloomInventory inv) { }
	
}
