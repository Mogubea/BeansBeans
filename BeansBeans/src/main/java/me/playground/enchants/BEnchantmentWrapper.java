package me.playground.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import me.playground.main.Main;

public class BEnchantmentWrapper extends Enchantment {
	
	final private String name;
	final private BEnchantmentTarget target;
	final private int maxLevel;
	final private int minLevel;
	final private boolean isCursed;
	final private boolean isTreasure;
	final private boolean isLegendary;
	final private List<Enchantment> conflicts;
	
	final private int runicCost;
	final private int runicCostPerLevel;
	
	final private int xpCost;
	final private int xpCostPerLevel;
	
	protected BEnchantmentWrapper(String keyName, String engName, BEnchantmentTarget target, int minLevel, int maxLevel, boolean isCurse, boolean isTreasure, boolean isLegendary, int runicCost, int runicIncrease, int xpCost, int xpIncrease, Enchantment... conflicts) {
        super(Main.getInstance().getKey(keyName));
        this.name = engName;
        this.target = target;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.isCursed = isCurse;
        this.isTreasure = isTreasure;
        this.isLegendary = isLegendary;
        this.runicCost = runicCost;
        this.runicCostPerLevel = runicIncrease;
        this.xpCost = xpCost;
        this.xpCostPerLevel = xpIncrease;
        this.conflicts = Arrays.asList(conflicts);
    }

    /**
     * Gets the enchantment bound to this wrapper
     *
     * @return Enchantment
     */
    @NotNull
    public Enchantment getEnchantment() {
        return Enchantment.getByKey(getKey());
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getStartLevel() {
        return minLevel;
    }
    
    public boolean isLegendary() {
    	return isLegendary;
    }
    
    public int getRunicCost(int level) {
    	return runicCost + (runicCostPerLevel * (level-1));
    }
    
    public int getExperienceCost(int level) {
    	return xpCost + (xpCostPerLevel * (level-1));
    }
    
    @NotNull
    @Override
    @Deprecated
    public EnchantmentTarget getItemTarget() {
        return null;
    }
    
    public BEnchantmentTarget getEnchantTarget() {
        return target;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return target.includes(item);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTreasure() {
        return isTreasure;
    }

    @Override
    public boolean isCursed() {
        return isCursed;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return conflicts.contains(other);
    }
    // Paper start
    @NotNull
    @Override
    public net.kyori.adventure.text.Component displayName(int level) {
        return getEnchantment().displayName(level);
    }

    @Override
    @Deprecated
    public @NotNull String translationKey() {
        return getEnchantment().translationKey();
    }

    @Override
    public boolean isTradeable() {
        return getEnchantment().isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        return getEnchantment().isDiscoverable();
    }

    @NotNull
    @Override
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        return getEnchantment().getRarity();
    }

    @Override
    public float getDamageIncrease(int level, @NotNull org.bukkit.entity.EntityCategory entityCategory) {
        return getEnchantment().getDamageIncrease(level, entityCategory);
    }

    @NotNull
    @Override
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        return getEnchantment().getActiveSlots();
    }
    // Paper end
}