package me.playground.data;

import java.sql.Connection;

import org.bukkit.World;

import me.playground.main.Main;

public abstract class PrivateDatasource {
	
	protected final Main plugin;
	protected final DatasourceCore dc;
	
	protected PrivateDatasource(Main plugin) {
		this.plugin = plugin;
		this.dc = plugin.getDatasourceCore();
		this.dc.registerDatasource(this);
	}
	
	protected Connection getNewConnection() {
		return dc.getNewConnection();
	}
	
	protected Main getPlugin() {
		return plugin;
	}
	
	/**
	 * Shortcut to {@link WorldManager#getWorldId(World world)}.
	 * @return database ID of the specified world.
	 */
	public int getWorldId(World world) {
		return plugin.getWorldManager().getWorldId(world);
	}
	
	/**
	 * Shortcut to {@link WorldManager#getWorld(int id)}.
	 * @return World with the specified database ID.
	 */
	public World getWorld(int databaseId) {
		return plugin.getWorldManager().getWorld(databaseId);
	}
	
	/**
	 * Load all of the relevant objects this datasource is managing the data of.
	 */
	public abstract void loadAll();
	
	/**
	 * Save all of the relevant objects this datasource is managing the data of.
	 */
	public abstract void saveAll();
	
}
