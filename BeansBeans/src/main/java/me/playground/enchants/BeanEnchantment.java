package me.playground.enchants;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;

public class BeanEnchantment extends Enchantment {
	
	final public static ArrayList<BeanEnchantment> enchants = new ArrayList<BeanEnchantment>();
	
	final public static BeanEnchantment MOLTEN_TOUCH = new BeanEnchantment(NamespacedKey.minecraft("molten_touch"), "Molten Touch", EnchantmentTarget.TOOL, 1, 35, false, false) {
		public boolean canEnchantItem(ItemStack arg0) {
			return false;
		}

		public boolean conflictsWith(Enchantment arg0) {
			return arg0.equals(Enchantment.SILK_TOUCH);
		}
	};
	
	/*final public static BeanEnchantment SWIFT_STRIKE = new BeanEnchantment(NamespacedKey.minecraft("swiftstrike"), "Swift Strike", EnchantmentTarget.WEAPON, 5, 35, false, false) {
		public boolean canEnchantItem(ItemStack arg0) {
			return false;
		}

		public boolean conflictsWith(Enchantment arg0) {
			return arg0.equals(BeanEnchantment.HEAVY_STRIKE);
		}
	};
	
	final public static BeanEnchantment HEAVY_STRIKE = new BeanEnchantment(NamespacedKey.minecraft("heavystrike"), "Heavy Strike", EnchantmentTarget.WEAPON, 5, 35, false, false) {
		public boolean canEnchantItem(ItemStack arg0) {
			return false;
		}

		public boolean conflictsWith(Enchantment arg0) {
			return arg0.equals(BeanEnchantment.SWIFT_STRIKE);
		}
	};*/
	
	// XXX: Class begin
	
	final private String name;
	final private EnchantmentTarget target;
	final private int maxLevel;
	final private int startEnchantLevel;
	final private boolean isCursed;
	final private boolean isTreasure;
	
	public BeanEnchantment(NamespacedKey key, String name, EnchantmentTarget target, int maxLevel, int startLevel, boolean isCursed, boolean isTreasure) {
		super(key);
		this.name = name;
		this.target = target;
		this.maxLevel = maxLevel;
		this.startEnchantLevel = startLevel;
		this.isCursed = isCursed;
		this.isTreasure = isTreasure;
		enchants.add(this);
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return target;
	}

	@Override
	public int getMaxLevel() {
		return maxLevel;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getStartLevel() {
		return startEnchantLevel;
	}

	@Override
	public boolean isCursed() {
		return isCursed;
	}

	@Override
	public boolean isTreasure() {
		return isTreasure;
	}

	@Override
	public boolean canEnchantItem(ItemStack arg0) {
		return false;
	}

	@Override
	public boolean conflictsWith(Enchantment arg0) {
		return false;
	}

	@Override
	public @Nonnull Component displayName(int arg0) {
		return Component.text(getName());
	}

	@Override
	public @Nonnull Set<EquipmentSlot> getActiveSlots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getDamageIncrease(int arg0, @Nonnull EntityCategory arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @Nonnull EnchantmentRarity getRarity() {
		return null;
	}

	@Override
	public boolean isDiscoverable() {
		return false;
	}

	@Override
	public boolean isTradeable() {
		return false;
	}

	@Override
	public @NotNull String translationKey() {
		return this.name;
	}
	
	
}
