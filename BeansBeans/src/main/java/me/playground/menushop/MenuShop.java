package me.playground.menushop;

import java.util.ArrayList;
import java.util.List;

public class MenuShop {
	
	private final MenuShopManager manager;
	private final List<PurchaseOption> purchaseOptions = new ArrayList<PurchaseOption>();
	private final String identifier;
	
	protected MenuShop(MenuShopManager manager, String identifier) {
		this.identifier = identifier;
		this.manager = manager;
	}
	
	public List<PurchaseOption> getPurchaseOptions() {
		return List.copyOf(purchaseOptions);
	}
	
	protected MenuShop addPurchaseOption(PurchaseOption option) {
		this.purchaseOptions.add(option);
		option.setMenuShop(this);
		if (option.getDBID() < 1)
			manager.getDatasource().register(option);
		return this;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
}
