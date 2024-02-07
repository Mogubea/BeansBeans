package me.playground.worlds;

import java.util.*;

import org.bukkit.World;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import me.playground.main.Main;

public class WorldManager {
	private final WorldDatasource datasource;
	private final BiMap<Integer, BeanWorld> idToWorld = HashBiMap.create();
	private final BiMap<Integer, World> idToBukkitWorld = HashBiMap.create();

	public WorldManager(Main plugin) {
		datasource = new WorldDatasource(plugin, this);
		datasource.loadAll();
	}
	
	/**
	 * @return A World's database ID.
	 */
	public int getWorldId(World w) {
		return idToBukkitWorld.inverse().get(w);
	}
	
	/**
	 * @return A World from its database ID.
	 */
	public World getWorld(int id) {
		return idToBukkitWorld.get(id);
	}

	protected void addWorldToMap(int id, World world) {
		idToBukkitWorld.put(id, world);
		idToWorld.put(id, new BeanWorld(world));
	}

	/**
	 * Register the specified world into the database and set up the World Region.
	 */
	public void registerWorld(World world) {
		datasource.registerWorld(world);
	}
	
	public Set<World> getWorlds() {
		return idToBukkitWorld.values();
	}

	public int size() {
		return idToBukkitWorld.size();
	}

}
