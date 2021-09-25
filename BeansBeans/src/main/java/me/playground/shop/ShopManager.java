package me.playground.shop;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;

import me.playground.data.Datasource;

public class ShopManager {
	
	private final HashMap<Integer, Shop> shopList;
	private boolean shopsEnabled = true;
	
	public ShopManager() {
		shopList = Datasource.loadAllShops();
	}
	
	public ArrayList<Shop> getShops() {
		return new ArrayList<Shop>(shopList.values());
	}
	
	public Shop getShop(int id) {
		return shopList.get(id);
	}
	
	public Shop createNewShop(int playerId, Location location) throws Throwable {
		final Shop s = Datasource.createNewShop(playerId, location);
		shopList.put(s.getShopId(), s);
		return s;
	}
	
	private void unloadShopEntities(boolean loadChunks) {
		for (Shop s : shopList.values()) {
			if (loadChunks)
				s.getLocation().getChunk().load();
			if (s.getLocation().isChunkLoaded())
				s.unloadEntities();
		}
	}
	
	private void loadShopEntities(boolean loadChunks) {
		for (Shop s : shopList.values()) {
			if (loadChunks)
				s.getLocation().getChunk().load();
			if (s.getLocation().isChunkLoaded())
				s.loadEntities();
		}
	}
	
	/**
	 * Reloads all shops
	 * @param loadChunks - Forceably load chunks to refresh ALL entities, not just loaded ones.
	 */
	public void reload(boolean loadChunks) {
		Datasource.saveDirtyShops();
		unloadShopEntities(loadChunks);
		shopList.clear();
		shopList.putAll(Datasource.loadAllShops());
		loadShopEntities(loadChunks);
	}
	
	public void deleteShop(Shop s, int deletedBy) {
		if (Datasource.deleteShop(s)) {
			s.unloadEntities();
			this.shopList.remove(s.getShopId());
			logAction(s.getShopId(), deletedBy, "deleted this shop", null);
		}
	}
	
	public void logAction(int shopId, int playerId, String comment, String data) {
		Datasource.logShopAction(shopId, playerId, comment, data);
	}
	
	public void disable() {
		this.shopsEnabled = false;
	}
	
	public void enable() {
		this.shopsEnabled = true;
	}
	
	public boolean isEnabled() {
		return this.shopsEnabled;
	}
	
	public int countShops() {
		return shopList.size();
	}
	
}
