package me.playground.enchants;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.playground.main.Main;
import net.kyori.adventure.text.Component;

public class BeanEnchantment extends Enchantment {
	
	private final static Set<BeanEnchantment> enchants = new HashSet<BeanEnchantment>();
	
	final public static BeanEnchantment MOLTEN_TOUCH = new BeanEnchantment(Main.key("molten_touch"), "Molten Touch", EnchantmentTarget.TOOL, 1, 1, 40, false, false) {
		public boolean canEnchantItem(ItemStack arg0) {
			return false;
		}

		public boolean conflictsWith(Enchantment arg0) {
			return arg0.equals(Enchantment.SILK_TOUCH);
		}
	};
	
	final public static BeanEnchantment REINFORCED = new BeanEnchantment(Main.key("reinforced"), "Reinforced", EnchantmentTarget.BREAKABLE, 3, 1, 24, false, false) {
		public boolean conflictsWith(Enchantment arg0) {
			return arg0.equals(Enchantment.DURABILITY);
		}
	};
	
	final public static BeanEnchantment SEARING = new BeanEnchantment(Main.key("searing"), "Searing", EnchantmentTarget.FISHING_ROD, 1, 1, 26, false, false);
	
	// XXX: Class begin
	
	final private String name;
	final private EnchantmentTarget target;
	final private int maxLevel;
	final private int minLevel;
	final private boolean isCursed;
	final private boolean isTreasure;
	final private int minimumEnchantLevel;
	
	public BeanEnchantment(NamespacedKey key, String name, EnchantmentTarget target, int maxLevel, int startLevel, int minimumEnchantLevel, boolean isCursed, boolean isTreasure) {
		super(key);
		this.name = name;
		this.target = target;
		this.maxLevel = maxLevel;
		this.minLevel = startLevel;
		this.minimumEnchantLevel = minimumEnchantLevel;
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
		return minLevel;
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
	public @Nonnull String translationKey() {
		return this.name;
	}
	
	public int getLevelRequirement() {
		return this.minimumEnchantLevel;
	}
	
	public static Set<BeanEnchantment> getCustomEnchants() {
		return enchants;
	}
	
}
