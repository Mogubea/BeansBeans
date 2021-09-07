package me.playground.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ItemAttributes {
	
	NIL(0, 0, 0),
	
	WOODEN_SWORD(4, 1.6),
	GOLDEN_SWORD(4, 1.6),
	STONE_SWORD(5, 1.6),
	IRON_SWORD(6, 1.6),
	DIAMOND_SWORD(7, 1.6),
	NETHERITE_SWORD(8, 1.6),
	WOODEN_AXE(7, 0.8),
	GOLDEN_AXE(7, 1.0),
	STONE_AXE(9, 0.8),
	IRON_AXE(9, 0.9),
	DIAMOND_AXE(9, 1.0),
	NETHERITE_AXE(10, 1.0),
	WOODEN_PICKAXE(2, 1.2),
	GOLDEN_PICKAXE(2, 1.2),
	STONE_PICKAXE(3, 1.2),
	IRON_PICKAXE(4, 1.2),
	DIAMOND_PICKAXE(5, 1.2),
	NETHERITE_PICKAXE(6, 1.2),
	WOODEN_SHOVEL(2.5, 1),
	GOLDEN_SHOVEL(2.5, 1),
	STONE_SHOVEL(3.5, 1),
	IRON_SHOVEL(4.5, 1),
	DIAMOND_SHOVEL(5.5, 1),
	NETHERITE_SHOVEL(6.5, 1),
	WOODEN_HOE(1, 1),
	GOLDEN_HOE(1, 1),
	STONE_HOE(1, 2),
	IRON_HOE(1, 3),
	DIAMOND_HOE(1, 4),
	NETHERITE_HOE(1, 4),
	TRIDENT(9, 1.1),
	
	LEATHER_HELMET(1, 0, 0),
	LEATHER_CHESTPLATE(3, 0, 0),
	LEATHER_LEGGINGS(2, 0, 0),
	LEATHER_BOOTS(1, 0, 0),
	GOLDEN_HELMET(2, 0, 0),
	GOLDEN_CHESTPLATE(5, 0, 0),
	GOLDEN_LEGGINGS(4, 0, 0),
	GOLDEN_BOOTS(1, 0, 0),
	CHAINMAIL_HELMET(2, 0, 0),
	CHAINMAIL_CHESTPLATE(5, 0, 0),
	CHAINMAIL_LEGGINGS(4, 0, 0),
	CHAINMAIL_BOOTS(1, 0, 0),
	IRON_HELMET(2, 0, 0),
	IRON_CHESTPLATE(6, 0, 0),
	IRON_LEGGINGS(5, 0, 0),
	IRON_BOOTS(2, 0, 0),
	DIAMOND_HELMET(3, 2, 0),
	DIAMOND_CHESTPLATE(8, 2, 0),
	DIAMOND_LEGGINGS(6, 2, 0),
	DIAMOND_BOOTS(3, 2, 0),
	NETHERITE_HELMET(3, 3, 1),
	NETHERITE_CHESTPLATE(8, 3, 1),
	NETHERITE_LEGGINGS(6, 3, 1),
	NETHERITE_BOOTS(3, 3, 1),
	TURTLE_SHELL(2, 0, 0),
	;
	
	final double values[];
	
	/**
	 * Attack Damage and Attack Speed for tools
	 * Defense Points, Armour Toughness and Knockback Resistance for armour
	 */
	ItemAttributes(double...values) {
		this.values = values;
	}
	
	public double getAttackDamage() {
		return values[0];
	}
	
	public double getDefensePoints() {
		return values[0];
	}
	
	public double getAttackSpeed() {
		return values[1];
	}
	
	public double getArmourToughness() {
		return values[1];
	}
	
	public double getKnockbackResistance() {
		return values[2];
	}
	
	public boolean isTool() {
		return values.length == 2;
	}
	
	public static ItemAttributes fromItem(ItemStack i) {
		return fromMaterial(i.getType());
	}
	
	public static ItemAttributes fromMaterial(Material m) {
		ItemAttributes ia = null;
		try {
			ia = valueOf(m.toString());
		} catch (Exception e) {
			return NIL;
		}
		return ia;
	}
	
}
