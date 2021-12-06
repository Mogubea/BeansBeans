package me.playground.items;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemHeirloomShungite extends BeanItemHeirloom {
	
	public BItemHeirloomShungite(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg4YmNlNDk3Y2ZhNWY2MTE4MzlmNmRhMjFjOTVkMzRlM2U3MjNjMmNjNGMzYzMxOWI1NjI3NzNkMTIxNiJ9fX0=", rarity);
		addAttribute(Attribute.GENERIC_LUCK, 0.1, EquipmentSlot.FEET);
		setDefaultLore(
				Component.text("A luckier chunk of Shungite!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
	}
	
}
