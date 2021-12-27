package me.playground.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import me.playground.main.Main;

public class RegionManager {
	private final RegionDatasource datasource;
	
	private final HashMap<UUID, Region> worldRegions = new HashMap<UUID, Region>();
	private final HashMap<UUID, RegionMap<Region>> worldMaps = new HashMap<UUID, RegionMap<Region>>();
	private final HashMap<String, Region> allRegionsByName = new HashMap<String, Region>();
	private final HashMap<Integer, Region> allRegionsById = new HashMap<Integer, Region>();
	
	public RegionManager(Main plugin) {
		datasource = new RegionDatasource(plugin, this);
		datasource.loadAll();
	}
	
	public void initWorldRegion(World world) {
		worldMaps.put(world.getUID(), new RegionMap<Region>(world));
		Region nwr = new Region(this, -datasource.getWorldManager().getWorldId(world), world);
		worldRegions.put(world.getUID(), nwr);
		registerRegion(nwr);
	}
	
	public RegionMap<Region> getRegionMap(World world) {
		return worldMaps.get(world.getUID());
	}
	
	public Region getRegion(String name) {
		return allRegionsByName.get(name);
	}
	
	public Region getRegion(int id) {
		return allRegionsById.get(id);
	}
	
	public void reload() {
		datasource.saveAll();
		worldRegions.clear();
		worldMaps.clear();
		allRegionsByName.clear();
		allRegionsById.clear();
		datasource.loadAll();
	}
	
	/**
	 * Create a new region.
	 */
	public Region createRegion(int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		return datasource.createNewRegion(creator, priority, parent, name, world, min, max);
	}
	
	protected void registerRegion(Region region) {
		if (!region.isWorldRegion() && region.getWorld() != null)
			getRegionMap(region.getWorld()).add(region);
		
		this.allRegionsByName.put(region.getName().toLowerCase(), region);
		this.allRegionsById.put(region.getRegionId(), region);
	}
	
	public void refreshRegion(Region region) {
		getRegionMap(region.getWorld()).update(region);
	}
	
	public void renameRegion(String name, String newName) {
		Region r = allRegionsByName.get(name.toLowerCase());
		allRegionsByName.remove(name.toLowerCase());
		allRegionsByName.put(newName.toLowerCase(), r);
	}
	
	protected void removeRegion(Region region) {
		getRegionMap(region.getWorld()).remove(region);
		allRegionsByName.remove(region.getName().toLowerCase());
		allRegionsById.remove(region.getRegionId());
		datasource.deleteRegion(region);
	}
	
	public Region getWorldRegion(World world) {
		return worldRegions.get(world.getUID());
	}
	
	public Region getRegion(Location location) {
		return getRegion(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public Region getRegion(World world, int x, int y, int z) {
		Region dominantRegion = getWorldRegion(world);
		List<Region> regions = getRegions(world, x, y, z);
		if (!regions.isEmpty()) {
			Collections.sort(regions);
			dominantRegion = regions.get(0);
		}
		
		return dominantRegion;
	}
	
	public List<Region> getRegions(Location location) {
		return getRegions(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public List<Region> getRegions(World world, int x, int y, int z) {
		return getRegionMap(world).getRegions(x, y, z);
	}
	
	public ArrayList<Region> getAllRegions() {
		return new ArrayList<Region>(allRegionsByName.values());
	}
	
	public int countRegions() {
		return allRegionsByName.size();
	}
	
	/**
	 * @return the Datasource responsible for managing Region data.
	 */
	protected RegionDatasource getDatasource() {
		return datasource;
	}
	
}
