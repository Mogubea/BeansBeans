package me.playground.loot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.EntityType;

import me.playground.main.IPluginRef;
import me.playground.main.Main;

public class LootManager implements IPluginRef {

	final private Main plugin;
	final private LinkedHashMap<String, LootTable> lootTables = new LinkedHashMap<String, LootTable>();
	
	public LootManager(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public @Nonnull Main getPlugin() {
		return plugin;
	}
	
	/**
	 * Case-sensitive
	 */
	public @Nonnull LootTable getOrCreateTable(@Nonnull String name) {
		LootTable lt = lootTables.get(name);
		if (lt == null) {
			getPlugin().getLogger().info("Created LootTable: " + name);
			lt = new LootTable(this, name);
			lootTables.put(name, lt);
		}
		return lt;
	}
	
	/**
	 * Case-sensitive
	 */
	public @Nullable LootTable getLootTable(@Nonnull String name) {
		return lootTables.get(name);
	}
	
	public @Nullable LootTable getLootTable(@Nonnull EntityType type) {
		return lootTables.get("entity:"+type.name());
	}
	
	public ArrayList<LootEntry> getAllEntries() {
		ArrayList<LootEntry> entries = new ArrayList<LootEntry>();
		lootTables.forEach((identifier, table) -> {
			entries.addAll(table.getEntries());
		});
		return entries;
	}
	
	public Random getRandom() {
		return plugin.getRandom();
	}
	
}
