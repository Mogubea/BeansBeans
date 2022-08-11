package me.playground.loot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.EntityType;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import org.jetbrains.annotations.NotNull;

public class LootManager implements IPluginRef {

	private final Main plugin;
	private final LinkedHashMap<String, LootTable> lootTables = new LinkedHashMap<>();
	private final LootDatasource datasource;
	
	public LootManager(Main plugin) {
		this.plugin = plugin;
		this.datasource = new LootDatasource(plugin, this);

		datasource.loadAll();
	}
	
	/**
	 * Case-sensitive
	 */
	public @Nonnull LootTable getOrCreateTable(@Nonnull String name) {
		LootTable lt = lootTables.get(name);
		if (lt == null) {
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
		ArrayList<LootEntry> entries = new ArrayList<>();
		lootTables.forEach((identifier, table) -> {
			entries.addAll(table.getEntries());
		});
		return entries;
	}
	
	public Random getRandom() {
		return plugin.getRandom();
	}

	@Override
	@NotNull
	public Main getPlugin() {
		return plugin;
	}

	public void reload() {
		try {
			datasource.saveAll();
		} catch (Exception ignored) {
		}
		lootTables.clear();
		datasource.loadAll();
	}
	
}
