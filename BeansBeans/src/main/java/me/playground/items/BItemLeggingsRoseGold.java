package me.playground.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.utils.Utils;

public class BItemLeggingsRoseGold extends BItemDurable {

	protected BItemLeggingsRoseGold(int numeric, int modelDataInt) {
		super(numeric, "ROSE_GOLD_LEGGINGS", "Rose Gold Leggings", Utils.getDyedLeather(Material.LEATHER_LEGGINGS, 0xe76e79), ItemRarity.UNCOMMON, modelDataInt, 281);
		addAttribute(Attribute.GENERIC_ARMOR, 4, EquipmentSlot.LEGS);
		addAttribute(Attribute.GENERIC_MAX_HEALTH, 1, EquipmentSlot.LEGS);
		addRepairMaterial(Material.COPPER_INGOT, 16);
		setRunicCapacity(17);
	}

}
