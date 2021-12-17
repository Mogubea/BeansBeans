package me.playground.regions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import me.playground.data.Datasource;
import me.playground.main.Main;

@SuppressWarnings("rawtypes")
public class RegionManager {
	
	private final HashMap<UUID, Region> worldRegions = new HashMap<UUID, Region>();
	private final HashMap<UUID, RegionMap> worldMaps = new HashMap<UUID, RegionMap>();
	private final HashMap<String, Region> allRegionsByName = new HashMap<String, Region>();
	private final HashMap<Integer, Region> allRegionsById = new HashMap<Integer, Region>();
	
	public RegionManager() {
		for (World w : Bukkit.getWorlds())
			registerWorld(-Datasource.WorldUUIDToId.get(w.getUID()), w);
	}
	
	public void registerWorld(int negaId, World world) {
		worldMaps.put(world.getUID(), new RegionMap(world));
		Region nwr = new Region(this, negaId, world);
		worldRegions.put(world.getUID(), nwr);
		registerRegion(nwr);
	}
	
	@SuppressWarnings("unchecked")
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
		Datasource.saveDirtyRegions();
		worldRegions.clear();
		worldMaps.clear();
		allRegionsByName.clear();
		allRegionsById.clear();
		for (World w : Bukkit.getWorlds())
			registerWorld(-Datasource.WorldUUIDToId.get(w.getUID()), w);
		Datasource.loadAllRegions();
	}
	
	public void registerRegion(Region region) {
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
		Datasource.deleteRegion(region);
	}
	
	public Region getWorldRegion(World world) {
		return worldRegions.get(world.getUID());
	}
	
	public static Region getWorldRegionAt(World world) {
		return Main.getRegionManager().getWorldRegion(world);
	}
	
	public static Region getRegionAt(Location location) {
		return getRegionAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public static Region getRegionAt(World world, int x, int y, int z) {
		return Main.getRegionManager().getRegion(world, x, y, z);
	}
	
	/*public static <T extends Flag<V>, V> V getFlagAt(Location location, T flag) {
		Region reg = getRegionAt(location);
		V val = reg.getFlag(flag);
		if (val == null && !reg.isWorldRegion()) // If null, go to world region setting
			val = getWorldRegionAt(location.getWorld()).getFlag(flag);
		if (val == null) // If null, go to default setting
			val = flag.getDefault();
		return val;
	}
	
	public static <T extends Flag<V>, V> V getFlagAt(Region region, T flag) {
		V val = region.getFlag(flag);
		if (val == null && !region.isWorldRegion()) // If null, go to world region setting
			val = getWorldRegionAt(region.getWorld()).getFlag(flag);
		if (val == null) // If null, go to default setting
			val = flag.getDefault();
		return val;
	}*/
	
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
	
}
