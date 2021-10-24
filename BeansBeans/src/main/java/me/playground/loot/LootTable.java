package me.playground.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * LootTable instances are not saved at all, but rather are created on Server Startup 
 * every time there's a new table name addressed when loading Loot Entries.
 * */
public class LootTable {
	
	private final String name;
	private final LootManager manager;
	private final ArrayList<LootEntry> entries = new ArrayList<LootEntry>();
	
	public LootTable(LootManager lm, String name) {
		this.manager = lm;
		this.name = name;
	}
	
	public LootManager getManager() {
		return manager;
	}
	
	public String getName() {
		return name;
	}
	
	public LootTable setEntries(ArrayList<LootEntry> entries) {
		this.entries.clear();
		setEntries(entries);
		totalChances.clear();
		getTotalChance(0);
		return this;
	}
	
	public LootTable addEntry(LootEntry entry) {
		if (entry.getTable() != this) throw new IllegalArgumentException("Entry's designated loot table is not equal to this loot table.");
		this.entries.add(entry);
		totalChances.clear();
		getTotalChance(0);
		return this;
	}
	
	private final Map<Float, Integer> totalChances = new HashMap<Float, Integer>();
	public float getTotalChance(float luck) {
		if (totalChances.containsKey(luck)) 
			return totalChances.get(luck);
		
		int total = 0;
		int size = entries.size();
		for (int x = -1; ++x < size;)
			total += entries.get(x).getChance(luck);
		totalChances.put(luck, total);
		return total;
	}
	
	/**
	 * Any changes made to the entries in this table will appear in the Loot Table.
	 * @return list of entries straight from the Loot Table.
	 */
	public ArrayList<LootEntry> getEntries() {
		return this.entries;
	}
	
}
