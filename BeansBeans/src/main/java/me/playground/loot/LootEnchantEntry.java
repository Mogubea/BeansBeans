package me.playground.loot;

import org.bukkit.enchantments.Enchantment;

/**
 * This is fucking chaos
 */
public class LootEnchantEntry {
	
	private final LootEntry entry;
	private final Enchantment enchant;
	private float chanceOfEnchant; // Chance of the enchantment even being added 0.0 - 100.0
	private float[] levelChances; // Max level is based on length, a level can be completely avoided by making the chance 0, chosen based on cumulative odds.
	private float[] levelLuckEffectiveness; // Per luck level, add the amount specified.
//	private float totalLevelChances;
	
	public LootEnchantEntry(LootEntry entry, Enchantment enchant, float chanceOfEnchant, float[] levelChances, float[] luckEffects) {
		this.entry = entry;
		this.enchant = enchant;
		this.chanceOfEnchant = chanceOfEnchant;
		this.levelChances = levelChances;
		this.levelLuckEffectiveness = luckEffects;
	}
	
	/**
	 * Generates a random float between 0.0 and 100.0, which will determine if the enchantment will even be added.
	 * After this, a random float between 0.0 and (int){@link #generateCumulativeChance(float)} will be generated and a level
	 * will be selected.
	 * @return level -1 if the chances fail.
	 */
	public int getEnchantmentLevel(float luck) {
		if (getChance(luck) <= nextFloat(100)) return -1;
		
		float levelRand = nextFloat(generateCumulativeChance(luck));
		final int size = levelChances.length;
		for (int x = -1; ++x < size;)
			if ((levelRand -= generateChance(x, luck)) <= 0)
				return x;
		return -1;
	}
	
	/**
	 * Set the chance of this enchantment appearing on the item.
	 * <p>Between 0.0 and 100.0 where 34.5 means 34.5% chance.
	 * @return the LootEnchantEntry.
	 */
	public LootEnchantEntry setChance(float chance) {
		if (chance > 100F) chance = 100F;
		if (chance < 0F) chance = 0F;
		
		chanceOfEnchant = chance;
		setDirty(true);
		return this;
	}
	
	protected LootEnchantEntry setLevelChances(float...chances) {
		int size = chances.length;
//		totalLevelChances = 0;
		for (int x = -1; ++x < size;) {
			if (chances[x] < 0) chances[x] = 0;
//			totalLevelChances += chances[x]; 
		}
		levelChances = chances;
		setDirty(true);
		return this;
	}
	
	public LootEnchantEntry setLevelLuckEffect(int level, float effect) {
		if ((level-1) >= levelChances.length) return this; // TODO: Make it adapt
		levelLuckEffectiveness[level-1] = effect;
		return this;
	}
	
	public LootEnchantEntry setLevelChance(int level, int chance) {
		if ((level-1) >= levelChances.length) return this; // TODO: Make it adapt
		if (chance < 0) chance = 0;
//		float old = levelChances[level-1];
		levelChances[level-1] = chance;
//		totalLevelChances -= (old - chance);
		setDirty(true);
		return this;
	}
	
	protected float getFlatChance() {
		return this.chanceOfEnchant;
	}
	
	protected float[] getFlatLvlChances() {
		return this.levelChances;
	}
	
	protected float[] getFlatLvlLuckEffects() {
		return this.levelLuckEffectiveness;
	}
	
	/**
	 * Luck will improve the odds of an enchantment by 15% of the enchantment's odds to appear per luck level
	 * <p>Odds of 
	 * @return A 0% - 100% float value
	 */
	public float getChance(float luck) {
		float chance = chanceOfEnchant * (1 + (0.15F * luck));
		if (chance > 100F) chance = 100F;
		
		return chance;
	}
	
	/**
	 * @return A 0% - 100% float value
	 */
	public float getChanceOfLvl(int level, float luck) {
		if ((level-1) >= levelChances.length) return 0.0F;
		float chance = (generateChance(level-1, luck) / generateCumulativeChance(luck)) * 100;
		if (chance > 100F) chance = 100F;
		if (chance < 0F) chance = 0F;
		
		return chance;
	}
	
	private float generateCumulativeChance(float luck) {
		int total = 0;
		int size = levelChances.length;
		for (int x = -1; ++x < size;)
			total += generateChance(x, luck);
		return total;
	}
	
	private float generateChance(int idx, float luck) {
		return levelChances[idx] + (luck * levelLuckEffectiveness[idx]);
	}
	
	public Enchantment getEnchantment() {
		return enchant;
	}
	
	private void setDirty(boolean dirty) {
		this.entry.setDirty(dirty);
	}
	
	/**
	 * For slightly better accuracy than doing a nextInt((int) float).
	 */
	private float nextFloat(float bound) {
		if (bound <= 0) return 0f;
		return (float)entry.getRandom().nextInt((int)(bound * 100F)) / 100F;
	}
	
}
