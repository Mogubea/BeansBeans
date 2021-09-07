package me.playground.warps;

import java.util.HashMap;

import org.bukkit.Location;

import me.playground.data.Datasource;

public class WarpManager {
	
	private HashMap<String, Warp> warpList = new HashMap<String, Warp>();
	
	public WarpManager() {
		warpList = Datasource.loadAllWarps();
	}
	
	public HashMap<String, Warp> getWarps() {
		return warpList;
	}
	
	public Warp createNewWarp(int playerId, String warpName, Location location) throws Throwable {
		return Datasource.saveNewWarp(playerId, warpName, location);
	}
	
	public void addNewWarp(Warp w) {
		warpList.put(w.getName().toLowerCase(), w);
	}
	
	public void renameWarp(Warp w, String newName) {
		warpList.remove(w.getName().toLowerCase());
		warpList.put(newName.toLowerCase(), w);
	}
	
	public Warp getWarp(String name) {
		return warpList.get(name.toLowerCase());
	}
	
	public boolean doesWarpExist(String name) {
		return warpList.containsKey(name.toLowerCase());
	}
	
	public void reload() {
		Datasource.saveDirtyWarps();
		warpList = Datasource.loadAllWarps();
	}
	
	public boolean deleteWarp(Warp w) {
		boolean b = false;
		if (b = Datasource.deleteWarp(w))
			warpList.remove(w.getName().toLowerCase());
		return b;
	}
	
	public int countWarps() {
		return warpList.size();
	}
	
}
