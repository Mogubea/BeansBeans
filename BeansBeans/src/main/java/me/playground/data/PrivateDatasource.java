package me.playground.data;

import java.sql.Connection;
import java.sql.SQLException;

import me.playground.worlds.WorldManager;
import org.bukkit.World;

import me.playground.main.Main;

/**
 * The base abstract class for Data Sources. Allows for nice organisation of Data Source files.
 */
public abstract class PrivateDatasource {
	
	protected final Main plugin;
	protected final DatasourceCore dc;
	
	protected PrivateDatasource(Main plugin) {
		this.plugin = plugin;
		this.dc = plugin.getDatasourceCore();
		this.dc.registerDatasource(this);
	}
	
	protected Connection getNewConnection() throws SQLException {
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
	 * Register a {@link PrivateLogger} to the {@link DatasourceCore} for automatic routine saving of logs.
	 */
	protected void registerLogger(PrivateLogger<?> logger) {
		this.dc.registerLogger(logger);
	}

	/**
	 * Fires after this {@link PrivateDatasource} has been registered by the {@link DatasourceCore}.
	 */
	protected void postCreation() {

	}

	/**
	 * Load all the relevant objects this datasource is managing the data of.
	 */
	public abstract void loadAll();
	
	/**
	 * Save all the relevant objects this datasource is managing the data of.
	 */
	public abstract void saveAll() throws Exception;

	protected void close(Object...c) {
		dc.close(c);
	}

}
