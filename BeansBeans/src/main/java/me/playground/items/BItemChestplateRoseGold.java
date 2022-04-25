package me.playground.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.utils.Utils;

public class BItemChestplateRoseGold extends BItemDurable {

	protected BItemChestplateRoseGold(int numeric, int modelDataInt) {
		super(numeric, "ROSE_GOLD_CHESTPLATE", "Rose Gold Chestplate", Utils.getDyedLeather(Material.LEATHER_CHESTPLATE, 0xe76e79), ItemRarity.UNCOMMON, modelDataInt, 300);
		addAttribute(Attribute.GENERIC_ARMOR, 3, EquipmentSlot.CHEST);
		addAttribute(Attribute.GENERIC_MAX_HEALTH, 1, EquipmentSlot.CHEST);
		addRepairMaterial(Material.COPPER_INGOT, 16);
	}

}
