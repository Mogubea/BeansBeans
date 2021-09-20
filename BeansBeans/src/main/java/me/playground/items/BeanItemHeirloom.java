package me.playground.items;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.HeirloomInventory;

public abstract class BeanItemHeirloom extends BeanItem {
	
	protected BeanItemHeirloom(int numeric, String identifier, String name, String base64, ItemRarity rarity) {
		super(numeric, identifier, name, base64, rarity);
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
	
	/**
	 * Called whenever a player attacks an entity and they have this Heirloom in their {@link HeirloomInventory}
	 * @param e - The {@link EntityDamageByEntityEvent} instance.
	 * @param inv - The {@link HeirloomInventory} that holds the {@link BeanItemHeirloom}.
	 */
	public void onMeleeDamageToEntity(EntityDamageByEntityEvent e, HeirloomInventory inv) { }
	
	/**
	 * Called whenever a player gets attacked by an entity and they have this Heirloom in their {@link HeirloomInventory}
	 * @param e - The {@link EntityDamageByEntityEvent} instance.
	 * @param inv - The {@link HeirloomInventory} that holds the {@link BeanItemHeirloom}.
	 */
	public void onDamageFromEntity(EntityDamageByEntityEvent e, HeirloomInventory inv) { }
	
}
