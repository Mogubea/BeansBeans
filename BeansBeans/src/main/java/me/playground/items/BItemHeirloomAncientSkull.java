package me.playground.items;

import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.playground.playerprofile.HeirloomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemHeirloomAncientSkull extends BeanItemHeirloom {
	
	public BItemHeirloomAncientSkull(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk3MjNlNTIwYjMwNzAwZTNiYzUzZTg1MjYyMDdlMjJhZjdmZjhmOTY2ODk3OTVmYzk0OWZhYmQ4ZDk4NDcxNSJ9fX0=", rarity);
		setDefaultLore(
				Component.text("An incredibly old, forgotten, gold", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("encrusted Skeleton skull.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("You deal ", NamedTextColor.GRAY).append(Component.text("10%", NamedTextColor.GREEN)).append(Component.text(" increased melee", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false),
				Component.text("damage to all species of Skeleton.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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
