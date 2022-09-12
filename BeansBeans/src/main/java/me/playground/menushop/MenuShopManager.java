package me.playground.menushop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.playground.main.Main;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuShopManager {
	private final MenuShopDatasource datasource;

	/**
	 * TODO: Store a player's 27 recently sold items. Cleared on restarts.
	 */
	private final Map<Integer, List<ItemStack>> recentlySoldItems = new HashMap<>();

	private final Map<String, MenuShop> shopList = new HashMap<>();
	private boolean shopsEnabled = true;
	
	public MenuShopManager(Main plugin) {
		datasource = new MenuShopDatasource(plugin, this);
		datasource.loadAll();
	}

	@NotNull
	public List<PurchaseOption> getPurchaseOptions() {
		List<PurchaseOption> options = new ArrayList<>();
		for (MenuShop shop : shopList.values())
			options.addAll(shop.getPurchaseOptions());
		return options;
	}

	@NotNull
	public MenuShop getOrMakeShop(String identifier) {
		MenuShop shop = shopList.get(identifier.toLowerCase());
		if (shop == null)
			shopList.put(identifier.toLowerCase(), shop = new MenuShop(this, identifier));
		
		return shop;
	}

	@Nullable
	public MenuShop getExistingShop(String identifier) {
		return shopList.getOrDefault(identifier.toLowerCase(), null);
	}
	
	/**
	 * Reloads all {@link MenuShop}s and their {@link PurchaseOption}s
	 */
	public void reload() {
		datasource.saveAll();
		shopList.clear();
		datasource.loadAll();
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

	@NotNull
	protected MenuShopDatasource getDatasource() {
		return datasource;
	}
}
