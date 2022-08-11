package me.playground.items;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.playground.enchants.BEnchantment;

public enum ItemAttributes {
	
	NIL(Material.AIR, 0, 0, 0),
	
	FISHING_ROD(Material.FISHING_ROD, 0, 0),
	SHEARS(Material.SHEARS, 0, 0),
	
	WOODEN_SWORD(Material.WOODEN_SWORD, 4, 1.6),
	GOLDEN_SWORD(Material.GOLDEN_SWORD, 4, 1.6),
	STONE_SWORD(Material.STONE_SWORD, 5, 1.6),
	IRON_SWORD(Material.IRON_SWORD, 6, 1.6),
	DIAMOND_SWORD(Material.DIAMOND_SWORD, 7, 1.6),
	NETHERITE_SWORD(Material.NETHERITE_SWORD, 8, 1.6),
	WOODEN_AXE(Material.WOODEN_AXE, 7, 0.8),
	GOLDEN_AXE(Material.GOLDEN_AXE, 7, 1.0),
	STONE_AXE(Material.STONE_AXE, 9, 0.8),
	IRON_AXE(Material.IRON_AXE, 9, 0.9),
	DIAMOND_AXE(Material.DIAMOND_AXE, 9, 1.0),
	NETHERITE_AXE(Material.NETHERITE_AXE, 10, 1.0),
	WOODEN_PICKAXE(Material.WOODEN_PICKAXE, 2, 1.2),
	GOLDEN_PICKAXE(Material.GOLDEN_PICKAXE, 2, 1.2),
	STONE_PICKAXE(Material.STONE_PICKAXE, 3, 1.2),
	IRON_PICKAXE(Material.IRON_PICKAXE, 4, 1.2),
	DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, 5, 1.2),
	NETHERITE_PICKAXE(Material.NETHERITE_PICKAXE, 6, 1.2),
	WOODEN_SHOVEL(Material.WOODEN_SHOVEL, 2.5, 1),
	GOLDEN_SHOVEL(Material.GOLDEN_SHOVEL, 2.5, 1),
	STONE_SHOVEL(Material.STONE_SHOVEL, 3.5, 1),
	IRON_SHOVEL(Material.IRON_SHOVEL, 4.5, 1),
	DIAMOND_SHOVEL(Material.DIAMOND_SHOVEL, 5.5, 1),
	NETHERITE_SHOVEL(Material.NETHERITE_SHOVEL, 6.5, 1),
	WOODEN_HOE(Material.WOODEN_HOE, 1, 1),
	GOLDEN_HOE(Material.GOLDEN_HOE, 1, 1),
	STONE_HOE(Material.STONE_HOE, 1, 2),
	IRON_HOE(Material.IRON_HOE, 1, 3),
	DIAMOND_HOE(Material.DIAMOND_HOE, 1, 4),
	NETHERITE_HOE(Material.NETHERITE_HOE, 1, 4),
	TRIDENT(Material.TRIDENT, 9, 1.1),
	
	LEATHER_HELMET(Material.LEATHER_HELMET, 1, 0, 0),
	LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, 3, 0, 0),
	LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, 2, 0, 0),
	LEATHER_BOOTS(Material.LEATHER_BOOTS, 1, 0, 0),
	GOLDEN_HELMET(Material.GOLDEN_HELMET, 2, 0, 0),
	GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, 5, 0, 0),
	GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, 4, 0, 0),
	GOLDEN_BOOTS(Material.GOLDEN_BOOTS, 1, 0, 0),
	CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, 2, 0, 0),
	CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, 5, 0, 0),
	CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, 4, 0, 0),
	CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, 1, 0, 0),
	IRON_HELMET(Material.IRON_HELMET, 2, 0, 0),
	IRON_CHESTPLATE(Material.IRON_CHESTPLATE, 6, 0, 0),
	IRON_LEGGINGS(Material.IRON_LEGGINGS, 5, 0, 0),
	IRON_BOOTS(Material.IRON_BOOTS, 2, 0, 0),
	DIAMOND_HELMET(Material.DIAMOND_HELMET, 3, 2, 0),
	DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, 8, 2, 0),
	DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, 6, 2, 0),
	DIAMOND_BOOTS(Material.DIAMOND_BOOTS, 3, 2, 0),
	NETHERITE_HELMET(Material.NETHERITE_HELMET, 3, 3, 1),
	NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, 8, 3, 1),
	NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, 6, 3, 1),
	NETHERITE_BOOTS(Material.NETHERITE_BOOTS, 3, 3, 1),
	TURTLE_HELMET(Material.TURTLE_HELMET, 2, 0, 0),
	;
	
	private final Material material;
	private final double values[];
	private Set<BEnchantment> allowedEnchantments = new HashSet<>();
	
	/**
	 * Attack Damage and Attack Speed for tools
	 * Defense Points, Armour Toughness and Knockback Resistance for armour
	 */
	ItemAttributes(Material material, double...values) {
		this.material = material;
		this.values = values;
		
		BEnchantment[] enchants = BEnchantment.values();
		final int size = enchants.length;
		for (int x = -1; ++x < size;) {
			BEnchantment enchant = enchants[x];
			if (enchant == BEnchantment.BINDING_CURSE || enchant == BEnchantment.VANISHING_CURSE)
				continue;
			if (enchant.getEnchantmentTarget().includes(getMaterial()))
				addValidEnchantment(enchant);
		}
		lockSet();
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
	
	/**
	 * @return An unmodifiable set of valid {@link Enchantment}s for this item.
	 */
	public Set<BEnchantment> getValidEnchantments() {
		return allowedEnchantments;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	private void addValidEnchantment(BEnchantment enchant) {
		this.allowedEnchantments.add(enchant);
	}
	
	private void lockSet() {
		this.allowedEnchantments = Collections.unmodifiableSet(allowedEnchantments);
	}
	
}
