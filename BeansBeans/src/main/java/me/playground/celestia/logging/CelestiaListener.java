package me.playground.celestia.logging;

import com.google.gson.Gson;
import me.playground.items.BeanBlock;
import me.playground.items.BeanItem;
import me.playground.listeners.EventListener;
import me.playground.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public class CelestiaListener extends EventListener {

	private final CelestiaManager manager;

	public CelestiaListener(Main plugin, CelestiaManager manager) {
		super(plugin);
		this.manager = manager;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerLoginEvent e) {
		if (e.getResult() == PlayerLoginEvent.Result.ALLOWED)
			manager.log(e.getPlayer(), CelestiaAction.JOIN, e.getRealAddress().getHostAddress());
		else
			manager.log(e.getPlayer(), CelestiaAction.FAIL_JOIN, e.getRealAddress().getHostAddress() + " ("+e.getResult().name()+")");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e) {
		InetSocketAddress address = e.getPlayer().getAddress(); // Not often that this will be null but this is just a precaution.
		manager.log(e.getPlayer(), CelestiaAction.QUIT, address == null ? "Unknown IP" : address.getAddress().getHostAddress());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled())
			manager.log(e.getPlayer(), CelestiaAction.COMMAND, e.getMessage());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!e.isCancelled()) {
			BeanBlock custom = BeanBlock.from(e.getBlock());
			String name = custom != null ? custom.getIdentifier() : e.getBlock().getType().name();
			manager.log(e.getPlayer(), CelestiaAction.BLOCK_PLACE, e.getBlock().getLocation(), name);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		if (!e.isCancelled()) {
			BeanBlock custom = BeanBlock.from(e.getBlock());
			String name = custom != null ? custom.getIdentifier() : e.getBlock().getType().name();
			manager.log(e.getPlayer(), CelestiaAction.BLOCK_BREAK, e.getBlock().getLocation(), name);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(EntityPickupItemEvent e) {
		if (!e.isCancelled() && e.getEntity() instanceof Player player)
			manager.log(player, CelestiaAction.ITEM_PICKUP, toJson(e.getItem().getItemStack()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemDrop(PlayerDropItemEvent e) {
		if (!e.isCancelled()) {
			String json = toJson(e.getItemDrop().getItemStack());
			manager.log(e.getPlayer(), CelestiaAction.ITEM_DROP, json);
		}
	}

	/**
	 * Convert the ItemStack to JSON, however, only keep information that's necessary. We do not care about the ItemStack version
	 * as we're not using Legacy ItemStacks. Additionally, we're mainly looking for PersistentDataContainer data for custom items.
	 */
	private String toJson(ItemStack i) {
		Map<String, Object> map = new LinkedHashMap<>(i.serialize());
		BeanItem custom = BeanItem.from(i);

		map.remove("v"); // We don't care. We don't use LEGACY items.

		boolean renamed = BeanItem.hasBeenRenamed(i);

		if (i.hasItemMeta()) {
			ItemMeta meta = i.getItemMeta();
			Map<String, Object> serializedMeta = new LinkedHashMap<>(meta.serialize());

			// Remove anything that's unnecessary to store due to it being re-applied in BeanItem formatting.
			// This includes things like ItemFlags, Custom Model Data and other various unique meta things that are unlikely to be utilised.
			if (custom != null) {
				ItemMeta newMeta = Bukkit.getItemFactory().getItemMeta(i.getType());
				if (renamed)
					newMeta.displayName(meta.displayName());
				meta.getEnchants().forEach((enchantment, integer) -> newMeta.addEnchant(enchantment, integer, true)); // Keep enchantments

				// Clone PersistentDataContainer (PublicBukkitValues). Anything here is 100% important.
				if (serializedMeta.containsKey("PublicBukkitValues")) {
					Object publicBukkitValues = serializedMeta.get("PublicBukkitValues"); // Map of NamespacedKeys and Objects
					serializedMeta = new LinkedHashMap<>(newMeta.serialize());
					if (publicBukkitValues != null)
						serializedMeta.put("PublicBukkitValues", publicBukkitValues);
				}
			} else if (meta instanceof Damageable) {
				serializedMeta.remove("lore");
				serializedMeta.remove("ItemFlags");
				serializedMeta.remove("Damage"); // PersistentDataContainer holds damage.
			}

			if (!renamed) // Remove display name of non renamed.
				serializedMeta.remove("display-name");

			map.put("meta", serializedMeta);
		}

		Gson gson = new Gson();
		return gson.toJson(map);
	}

	/*private ItemStack fromJson(String json) {
		Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.INTEGER_OR_DOUBLE).create();
		LinkedHashMap<String, Object> obj = gson.fromJson(json, LinkedHashMap.class);
		ItemMeta itemMeta = null;
		if (obj.containsKey("meta")) {
			Map<String, Object> metaa = new LinkedHashMap<>((Map<? extends String, ?>) obj.get("meta"));
			metaa.put("==", "ItemMeta");
			itemMeta = (ItemMeta) ConfigurationSerialization.deserializeObject(metaa);
		}

		obj.put("v", 2500);
		ItemStack test = ItemStack.deserialize(obj);
		if (itemMeta != null)
			test.setItemMeta(itemMeta);
		return BeanItem.formatItem(test);
	}*/

}
