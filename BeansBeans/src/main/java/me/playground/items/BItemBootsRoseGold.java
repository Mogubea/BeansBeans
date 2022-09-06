package me.playground.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.utils.Utils;

public class BItemBootsRoseGold extends BItemDurable {

	protected BItemBootsRoseGold(int numeric, int modelDataInt) {
		super(numeric, "ROSE_GOLD_BOOTS", "Rose Gold Boots", Utils.getDyedLeather(Material.LEATHER_BOOTS, 0xe76e79), ItemRarity.UNCOMMON, modelDataInt, 243);
		addAttribute(Attribute.GENERIC_ARMOR, 2, EquipmentSlot.FEET);
		addAttribute(Attribute.GENERIC_MAX_HEALTH, 1, EquipmentSlot.FEET);
		addRepairMaterial(Material.COPPER_INGOT, 16);
		setRunicCapacity(17);
	}

}
