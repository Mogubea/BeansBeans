package me.playground.playerprofile;

import org.json.JSONObject;

public class DeliveryCoins extends DeliveryContent {
	
	private final int coins;
	
	protected DeliveryCoins(Delivery container, int coins, long time) {
		this(container, coins);
		this.claimTime = time;
	}
	
	protected DeliveryCoins(Delivery container, int coins) {
		super(container);
		this.coins = coins;
	}

	@Override
	protected boolean doClaim() {
		getDelivery().getRecipientProfile().addToBalance(coins, "Delivery");
		return true;
	}
	
	public int getCoins() {
		return coins;
	}
	
	@Override
	public JSONObject getJson() {
		JSONObject obj = super.getJson();
		obj.put("coins", getCoins());
		return obj;
	}
	
}
