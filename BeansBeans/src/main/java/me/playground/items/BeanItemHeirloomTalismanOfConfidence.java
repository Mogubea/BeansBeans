package me.playground.items;

import me.playground.items.lore.Lore;
import me.playground.playerprofile.HeirloomInventory;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BeanItemHeirloomTalismanOfConfidence extends BeanItemHeirloom {

	protected BeanItemHeirloomTalismanOfConfidence(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFhZjdkMGExMjU2YzA3NzhhOWIwYmRlOTU3OGYzNjg4NTNkYzdmMDdmMDAyY2Y4MDdlYTUzNjgxYThmZTg1YSJ9fX0=", rarity);
		setDefaultLore(Lore.getBuilder("Increases the damage taken from all enemies by &c100%&r and increases your melee damage by &a20%&r.", "", "&8&oCould you add a hard-mode? - The Leg").build().getLore());
	}

	@Override
	public void onMeleeDamageToEntity(EntityDamageByEntityEvent e, HeirloomInventory inv) {
		e.setDamage(e.getDamage() * 1.2);
	}

	@Override
	public void onDamageFromEntity(EntityDamageByEntityEvent e, HeirloomInventory inv) {
		e.setDamage(e.getDamage() * 2);
	}
	
}
