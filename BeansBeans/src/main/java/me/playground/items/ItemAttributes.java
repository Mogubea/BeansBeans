package me.playground.items;

import java.util.*;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.playground.enchants.BEnchantment;

public enum ItemAttributes {
	
	NIL(Material.AIR, 0, 0, 0, 0),
	
	FISHING_ROD(Material.FISHING_ROD, 15, 0, 0),
	SHEARS(Material.SHEARS, 15, 0, 0),
	
	WOODEN_SWORD(Material.WOODEN_SWORD, 12, 4, 1.6),
	GOLDEN_SWORD(Material.GOLDEN_SWORD, 22, 4, 1.6),
	STONE_SWORD(Material.STONE_SWORD, 13, 5, 1.6),
	IRON_SWORD(Material.IRON_SWORD, 14, 6, 1.6),
	DIAMOND_SWORD(Material.DIAMOND_SWORD, 15, 7, 1.6),
	NETHERITE_SWORD(Material.NETHERITE_SWORD, 16, 8, 1.6),
	WOODEN_AXE(Material.WOODEN_AXE, 12, 7, 0.8),
	GOLDEN_AXE(Material.GOLDEN_AXE, 22, 7, 1.0),
	STONE_AXE(Material.STONE_AXE, 13, 9, 0.8),
	IRON_AXE(Material.IRON_AXE, 14, 9, 0.9),
	DIAMOND_AXE(Material.DIAMOND_AXE, 15, 9, 1.0),
	NETHERITE_AXE(Material.NETHERITE_AXE, 16, 10, 1.0),
	WOODEN_PICKAXE(Material.WOODEN_PICKAXE, 12, 2, 1.2),
	GOLDEN_PICKAXE(Material.GOLDEN_PICKAXE, 22, 2, 1.2),
	STONE_PICKAXE(Material.STONE_PICKAXE, 13, 3, 1.2),
	IRON_PICKAXE(Material.IRON_PICKAXE, 14, 4, 1.2),
	DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, 15, 5, 1.2),
	NETHERITE_PICKAXE(Material.NETHERITE_PICKAXE, 16, 6, 1.2),
	WOODEN_SHOVEL(Material.WOODEN_SHOVEL, 12, 2.5, 1),
	GOLDEN_SHOVEL(Material.GOLDEN_SHOVEL, 22, 2.5, 1),
	STONE_SHOVEL(Material.STONE_SHOVEL, 13, 3.5, 1),
	IRON_SHOVEL(Material.IRON_SHOVEL, 14, 4.5, 1),
	DIAMOND_SHOVEL(Material.DIAMOND_SHOVEL, 15, 5.5, 1),
	NETHERITE_SHOVEL(Material.NETHERITE_SHOVEL, 16, 6.5, 1),
	WOODEN_HOE(Material.WOODEN_HOE, 12, 1, 1),
	GOLDEN_HOE(Material.GOLDEN_HOE, 22, 1, 1),
	STONE_HOE(Material.STONE_HOE, 13, 1, 2),
	IRON_HOE(Material.IRON_HOE, 14, 1, 3),
	DIAMOND_HOE(Material.DIAMOND_HOE, 15, 1, 4),
	NETHERITE_HOE(Material.NETHERITE_HOE, 16, 1, 4),
	TRIDENT(Material.TRIDENT, 15, 9, 1.1),
	
	LEATHER_HELMET(Material.LEATHER_HELMET, 12, 1, 0, 0),
	LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, 12, 3, 0, 0),
	LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, 12, 2, 0, 0),
	LEATHER_BOOTS(Material.LEATHER_BOOTS, 12, 1, 0, 0),
	GOLDEN_HELMET(Material.GOLDEN_HELMET, 22, 2, 0, 0),
	GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, 22, 5, 0, 0),
	GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, 22, 4, 0, 0),
	GOLDEN_BOOTS(Material.GOLDEN_BOOTS, 22, 1, 0, 0),
	CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, 13, 2, 0, 0),
	CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, 13, 5, 0, 0),
	CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, 13, 4, 0, 0),
	CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, 13, 1, 0, 0),
	IRON_HELMET(Material.IRON_HELMET, 14, 2, 0, 0),
	IRON_CHESTPLATE(Material.IRON_CHESTPLATE, 14, 6, 0, 0),
	IRON_LEGGINGS(Material.IRON_LEGGINGS, 14, 5, 0, 0),
	IRON_BOOTS(Material.IRON_BOOTS, 14, 2, 0, 0),
	DIAMOND_HELMET(Material.DIAMOND_HELMET, 15, 3, 2, 0),
	DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, 15, 8, 2, 0),
	DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, 15, 6, 2, 0),
	DIAMOND_BOOTS(Material.DIAMOND_BOOTS, 15, 3, 2, 0),
	NETHERITE_HELMET(Material.NETHERITE_HELMET, 16, 3, 3, 1),
	NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, 16, 8, 3, 1),
	NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, 16, 6, 3, 1),
	NETHERITE_BOOTS(Material.NETHERITE_BOOTS, 16, 3, 3, 1),
	TURTLE_HELMET(Material.TURTLE_HELMET, 15, 2, 0, 0),
	;
	
	private final Material material;
	private final int runicCapacity;
	private final Map<Attribute, Double> values = new HashMap<>();
	private Set<BEnchantment> allowedEnchantments = new HashSet<>();
	
	/**
	 * Attack Damage and Attack Speed for tools
	 */
	ItemAttributes(Material material, int runicCapacity, double attackDamage, double attackSpeed) {
		this.material = material;
		this.runicCapacity = runicCapacity;
		this.values.put(Attribute.GENERIC_ATTACK_DAMAGE, attackDamage);
		this.values.put(Attribute.GENERIC_ATTACK_SPEED, attackSpeed);
		finish();
	}

	/**
	 * Defense Points, Armour Toughness and Knockback Resistance for armour
	 */
	ItemAttributes(Material material, int runicCapacity, double defense, double toughness, double knockbackResistance) {
		this.material = material;
		this.runicCapacity = runicCapacity;
		this.values.put(Attribute.GENERIC_ARMOR, defense);
		this.values.put(Attribute.GENERIC_ARMOR_TOUGHNESS, toughness);
		this.values.put(Attribute.GENERIC_KNOCKBACK_RESISTANCE, knockbackResistance);
		finish();
	}

	private void finish() {
		BEnchantment[] enchants = BEnchantment.values();
		final int size = enchants.length;
		for (int x = -1; ++x < size;) {
			BEnchantment enchant = enchants[x];
			if (enchant == Enchantment.BINDING_CURSE || enchant == Enchantment.VANISHING_CURSE)
				continue;
			if (enchant.getEnchantmentTarget().includes(getMaterial()))
				addValidEnchantment(enchant);
		}
		lockSet();
	}

	public int getRunicCapacity() { return runicCapacity; }

	public double getAttackDamage() {
		return values.getOrDefault(Attribute.GENERIC_ATTACK_DAMAGE, 0D);
	}
	
	public double getDefensePoints() {
		return values.getOrDefault(Attribute.GENERIC_ARMOR, 0D);
	}
	
	public double getAttackSpeed() {
		return values.getOrDefault(Attribute.GENERIC_ATTACK_SPEED, 0D);
	}
	
	public double getArmourToughness() {
		return values.getOrDefault(Attribute.GENERIC_ARMOR_TOUGHNESS, 0D);
	}
	
	public double getKnockbackResistance() {
		return values.getOrDefault(Attribute.GENERIC_KNOCKBACK_RESISTANCE, 0D);
	}

	public double getAttribute(Attribute attribute) { return values.getOrDefault(attribute, 0D); }

	public boolean isTool() {
		return values.containsKey(Attribute.GENERIC_ATTACK_SPEED);
	}
	
	public static ItemAttributes fromItem(ItemStack i) {
		return fromMaterial(i.getType());
	}
	
	public static ItemAttributes fromMaterial(Material m) {
		ItemAttributes ia;
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
