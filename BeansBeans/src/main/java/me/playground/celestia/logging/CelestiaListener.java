package me.playground.celestia.logging;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.playground.data.Datasource;
import me.playground.listeners.EventListener;
import me.playground.main.Main;

public class CelestiaListener extends EventListener {
	
	public CelestiaListener(Main plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerLoginEvent e) {
		Datasource.logCelestia(CelestiaAction.JOIN, e.getPlayer(), e.getPlayer().getLocation(), e.getRealAddress().getHostAddress() + " ("+e.getResult().name()+")");
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent e) {
		Datasource.logCelestia(CelestiaAction.QUIT, e.getPlayer(), e.getPlayer().getLocation(), e.getPlayer().getAddress().getAddress().getHostAddress());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled())
			Datasource.logCelestia(CelestiaAction.COMMAND, e.getPlayer(), e.getPlayer().getLocation(), e.getMessage());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!e.isCancelled())
			Datasource.logCelestia(CelestiaAction.BLOCK_PLACE, e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getType().name());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e) {
		if (!e.isCancelled())
			Datasource.logCelestia(CelestiaAction.BLOCK_BREAK, e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getType().name());
	}
	
	/*@EventHandler(priority = EventPriority.MONITOR)
	public void onItemPickup(EntityPickupItemEvent e) {
		if (!e.isCancelled() && e.getEntity() instanceof Player)
			Datasource.logCelestia(CelestiaAction.ITEM_PICKUP, (Player)e.getEntity(), e.getEntity().getLocation(), toString(e.getItem().getItemStack()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onItemDrop(PlayerDropItemEvent e) {
		if (!e.isCancelled())
			Datasource.logCelestia(CelestiaAction.ITEM_DROP, e.getPlayer(), e.getPlayer().getLocation(), toString(e.getItemDrop().getItemStack()));
	}
	
	private String toString(ItemStack i) {
		BeanItem bi = BeanItem.from(i);
		JsonObject json = new JsonObject();
		json.addProperty("i", bi != null ? bi.getIdentifier() : i.getType().name());
		if (i.getAmount() > 1)
			json.addProperty("a", i.getAmount());
		if (i.getType().getMaxDurability() > 1) {
			json.addProperty("d", BeanItem.getDurability(i));
			json.addProperty("md", BeanItem.getMaxDurability(i));
		}
		/*if (i.hasItemMeta() && !i.getItemMeta().getPersistentDataContainer().isEmpty()) {
			JsonObject jsonCon = new JsonObject();
			for (Entry<>)
		}
			json.add("p", new JsonObject()
					);
		
		return json.toString();
	}*/
	
}
