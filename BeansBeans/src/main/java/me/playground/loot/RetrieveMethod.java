package me.playground.loot;

public enum RetrieveMethod {
	
	/**
	 * Add together the total {@link LootEntry#getChance(float)} from every {@link LootEntry},
	 * generate a number between 0 and the cumulative value to determine the reward. Each loop
	 * will return <b>ONE</b> item.
	 * <p>If multiple loops are used, multiple of the same reward could generate.
	 * <p>Default loops: 1.
	 */
	CUMULATIVE_CHANCE,
	/**
	 * Add together the total {@link LootEntry#getChance(float)} from every {@link LootEntry},
	 * generate a number between 0 and the cumulative value to determine the reward. Each loop
	 * will return <b>ONE</b> item.
	 * <p>Assuming the size of the {@link LootTable} is larger than the specified loop count, 
	 * the system will attempt to not give the same item twice.
	 * <p>Default loops: 1.
	 */
	@Deprecated
	UNIQUE_CUMULATIVE_CHANCE,
	/**
	 * The potential amount of items generated is equal to the amount of items in the specified {@link LootTable}.
	 * Each {@link LootEntry} has their own chance to appear and a random float between <b>0.0</b> and <b>100.0</b>
	 * is generated for each entry to determine if it should be added to the returning collection.
	 * <p>As implied, every entry should have a chance between <b>0.0</b> and <b>100.0</b> as a representation 
	 * of their odds. Any entry with lower or higher odds will be treated similarly to the minimum and maximum values.
	 */
	INDIVIDUAL_CHANCE;
	
}
