package me.playground.playerprofile;

import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import me.playground.utils.Utils;

public abstract class DeliveryContent {
	
	public static DeliveryContent fromJSON(Delivery delivery, JSONObject contentObj) {
		if (contentObj == null || contentObj.isEmpty()) return null;
		long timeClaimed = contentObj.optLong("claimTime", 0L);
		int coins = contentObj.optInt("coins", 0);
		if (coins > 0) {
			return new DeliveryCoins(delivery, coins, timeClaimed);
		} else {
			String itemBase64 = contentObj.optString("item");
			if (itemBase64 == null) return null;
			
			try {
				return new DeliveryItem(delivery, (ItemStack) Utils.fromBase64(itemBase64), timeClaimed);
			} catch (Exception e) {
				return null;
			}
		}
	}
	
	protected final Delivery container;
	protected long claimTime;
	
	protected DeliveryContent(Delivery container) {
		this.container = container;
	}
	
	protected Delivery getDelivery() {
		return container;
	}
	
	/**
	 * Attempt to claim this content.
	 * @return whether the content claim was successful.
	 */
	public boolean claim() {
		boolean returning = doClaim();
		if (returning) {
			this.claimTime = System.currentTimeMillis();
			container.performClaim(); // Increment local counter.
		}
		return returning;
	}
	
	/**
	 * @return if this content has been claimed by the player.
	 */
	public boolean isClaimed() {
		return claimTime > 0L;
	}
	
	/**
	 * @return the millisecond time when this content was claimed by the player.
	 */
	public long getClaimLong() {
		return claimTime;
	}
	
	/**
	 * @return formatted string of time from when this content was claimed by the player.
	 */
	public String getClaimString() {
		return Utils.timeStringFromNow(claimTime);
	}
	
	public JSONObject getJson() {
		JSONObject obj = new JSONObject();
		if (claimTime > 0L)
			obj.put("claimTime", claimTime);
		return obj;
	}
	
	protected abstract boolean doClaim();
	
}
