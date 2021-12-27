package me.playground.shop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import me.playground.main.Main;

public class ShopManager {
	private final ShopDatasource datasource;
	
	private final Map<Integer, Shop> shopList = new HashMap<Integer, Shop>();
	private boolean shopsEnabled = true;
	
	public ShopManager(Main plugin) {
		datasource = new ShopDatasource(plugin, this);
		datasource.loadAll();
	}
	
	public Map<Integer, Shop> getShops() {
		return shopList;
	}
	
	public Shop getShop(int id) {
		return shopList.get(id);
	}
	
	public Shop createShop(int playerId, Location location) {
		return datasource.createShop(playerId, location);
	}
	
	protected void addNewShop(Shop s) {
		this.shopList.put(s.getShopId(), s);
		datasource.updateMarker(s);
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
		datasource.saveAll();
		unloadShopEntities(loadChunks);
		shopList.clear();
		datasource.loadAll();
		loadShopEntities(loadChunks);
	}
	
	public boolean deleteShop(Shop s, int deletedBy) {
		boolean success = datasource.deleteShop(s);
		if (success) {
			s.unloadEntities();
			this.shopList.remove(s.getShopId());
			logAction(s.getShopId(), deletedBy, "deleted this shop", null);
		}
		return success;
	}
	
	public void logAction(int shopId, int playerId, String comment, String data) {
		datasource.logShopAction(shopId, playerId, comment, data);
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
	
	protected ShopDatasource getDatasource() {
		return datasource;
	}
}
