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
	
	public void unloadShopEntities() {
		for (Shop s : shopList.values())
			s.unloadEntities();
	}
	
	public void loadShopEntities() {
		for (Shop s : shopList.values())
			s.loadEntities();
	}
	
	public void reloadAllShops() {
		unloadShopEntities();
		shopList.clear();
		shopList.putAll(Datasource.loadAllShops());
		loadShopEntities();
	}
	
	public void saveDirtyShops() {
		for (Shop s : this.shopList.values())
			if (s.isDirty())
				Datasource.saveDirtyShop(s);
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
