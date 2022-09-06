package me.playground.warps;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import me.playground.main.Main;
import org.jetbrains.annotations.NotNull;

public class WarpManager {
	private final WarpDatasource datasource;
	
	private final Map<String, Warp> warpList = new HashMap<>();
	
	public WarpManager(Main plugin) {
		datasource = new WarpDatasource(plugin, this);
		datasource.loadAll();
	}
	
	public Map<String, Warp> getWarps() {
		return warpList;
	}
	
	public Warp createWarp(int playerId, String warpName, Location location) {
		return datasource.createWarp(playerId, warpName, location);
	}
	
	public void addNewWarp(Warp w) {
		warpList.put(w.getName().toLowerCase(), w);
		datasource.updateMarker(w);
	}
	
	public void renameWarp(Warp w, String newName) {
		warpList.remove(w.getName().toLowerCase());
		warpList.put(newName.toLowerCase(), w);
		datasource.updateMarker(w);
	}
	
	public Warp getWarp(String name) {
		return warpList.get(name.toLowerCase());
	}
	
	public boolean doesWarpExist(String name) {
		return warpList.containsKey(name.toLowerCase());
	}

	@NotNull
	public Map<String, Warp> getWarpsOwnedBy(int id) {
		Map<String, Warp> warps = new HashMap<>();
		warpList.forEach((name, warp) -> {
			if (warp.isOwner(id))
				warps.put(name, warp);
		});

		return warps;
	}
	
	public void reload() {
		datasource.saveAll();
		warpList.clear();
		datasource.loadAll();
	}
	
	/**
	 * Attempt to delete the specified {@link Warp}
	 * @return whether the deletion was successful or not.
	 */
	public boolean deleteWarp(Warp w) {
		return datasource.deleteWarp(w) && warpList.remove(w.getName().toLowerCase()) != null;
	}
	
	public int countWarps() {
		return warpList.size();
	}
	
}
