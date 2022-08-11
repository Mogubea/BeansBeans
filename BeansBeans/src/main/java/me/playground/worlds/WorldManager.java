package me.playground.worlds;

import java.util.Set;

import org.bukkit.World;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import me.playground.main.Main;

public class WorldManager {
	private final WorldDatasource datasource;
	private final BiMap<Integer, World> idToWorld = HashBiMap.create();
	
	public WorldManager(Main plugin) {
		datasource = new WorldDatasource(plugin, this);
		datasource.loadAll();
	}
	
	/**
	 * @return A World's database ID.
	 */
	public int getWorldId(World w) {
		return idToWorld.inverse().get(w);
	}
	
	/**
	 * @return A World from its database ID.
	 */
	public World getWorld(int id) {
		return idToWorld.get(id);
	}
	
	protected void addWorldToMap(int id, World world) {
		idToWorld.put(id, world);
	}
	
	/**
	 * Register the specified world into the database and set up the World Region.
	 */
	public void registerWorld(World world) {
		datasource.registerWorld(world);
	}
	
	public Set<World> getWorlds() {
		return idToWorld.values();
	}
	
}
