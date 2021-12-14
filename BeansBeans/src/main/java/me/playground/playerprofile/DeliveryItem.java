package me.playground.playerprofile;

import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import me.playground.utils.Utils;

public class DeliveryItem extends DeliveryContent {
	
	private final ItemStack item;
	
	protected DeliveryItem(Delivery container, ItemStack item, long time) {
		this(container, item);
		this.claimTime = time;
	}
	
	protected DeliveryItem(Delivery container, ItemStack item) {
		super(container);
		this.item = item;
	}

	@Override
	protected boolean doClaim() {
		PlayerProfile pp = getDelivery().getRecipientProfile();
		if (!pp.isOnline()) return false;
		pp.getPlayer().getInventory().addItem(item).forEach((index, item) -> { pp.getPlayer().getWorld().dropItem(pp.getPlayer().getLocation(), item); });
		return true;
	}
	
	public ItemStack getItemStack() {
		return item;
	}

	@Override
	public JSONObject getJson() {
		JSONObject obj = super.getJson();
		obj.put("item", Utils.toBase64(item));
		return obj;
	}
	
}
