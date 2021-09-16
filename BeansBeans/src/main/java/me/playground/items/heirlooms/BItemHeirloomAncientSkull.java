package me.playground.items.heirlooms;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItemHeirloom;
import me.playground.items.ItemRarity;
import me.playground.playerprofile.HeirloomInventory;
import net.kyori.adventure.text.Component;

public class BItemHeirloomAncientSkull extends BeanItemHeirloom {
	
	public BItemHeirloomAncientSkull(String identifier, String name, ItemStack item, ItemRarity rarity) {
		super(identifier, name, item, rarity);
	}
	
	@Override
	public ArrayList<Component> getCustomLore(ItemStack item) {
		final ArrayList<Component> lore = new ArrayList<Component>();
		lore.addAll(Arrays.asList(
				Component.text("\u00a77An incredibly old, forgotten, gold"),
				Component.text("\u00a77encrusted Skeleton skull."),
				Component.text(""),
				Component.text("\u00a77You deal \u00a7a10%\u00a77 increased melee"),
				Component.text("\u00a77damage to all species of Skeleton.")));
		return lore;
	}
	
	@Override
	public void onMeleeDamageToEntity(EntityDamageByEntityEvent e, HeirloomInventory inv) {
		EntityType type = e.getEntityType();
		switch (type) {
		case SKELETON: case WITHER_SKELETON: case SKELETON_HORSE: case STRAY: case WITHER:
			e.setDamage(e.getDamage() * 1.1);
			break;
		default: return;
		}
	}
	
}
