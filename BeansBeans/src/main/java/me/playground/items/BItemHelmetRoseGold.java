package me.playground.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.utils.Utils;

public class BItemHelmetRoseGold extends BItemDurable {

	public BItemHelmetRoseGold(int numeric, int modelDataInt) {
		super(numeric, "ROSE_GOLD_HELMET", "Rose Gold Helmet", Utils.getDyedLeather(Material.LEATHER_HELMET, 0xe76e79), ItemRarity.UNCOMMON, modelDataInt, 206);
		addAttribute(Attribute.GENERIC_ARMOR, 2, EquipmentSlot.HEAD);
		addAttribute(Attribute.GENERIC_MAX_HEALTH, 1, EquipmentSlot.HEAD);
		addRepairMaterial(Material.COPPER_INGOT, 16);
		setRunicCapacity(17);
	}

}
