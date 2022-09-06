package me.playground.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BItemPickierAxe extends BItemPickyAxe {

	protected BItemPickierAxe(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, material, rarity, modelDataInt, durability);
		getAttributes().clear();
		addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0.004, EquipmentSlot.HAND);
		addAttribute(Attribute.GENERIC_ATTACK_SPEED, -3, EquipmentSlot.HAND);
		addAttribute(Attribute.GENERIC_ATTACK_DAMAGE, 10, EquipmentSlot.HAND);
		setRunicCapacity(16);
	}

	@Override
	protected void onFavouriteMined(BlockBreakEvent e) {

	}

}
