package me.playground.listeners;

import me.playground.entity.CustomEntityListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import me.playground.celestia.logging.CelestiaListener;
import me.playground.enchants.EnchantmentListener;
import me.playground.main.Main;

public class ListenerManager {
	
	private final Main plugin;
	
	public ListenerManager(Main pl) {
		this.plugin = pl;
		PluginManager pm = pl.getServer().getPluginManager();
		pm.registerEvents(new CustomEntityListener(pl, pl.getCustomEntityManager()), pl);

		pm.registerEvents(new ConnectionListener(pl), pl);
		pm.registerEvents(new WorldListener(pl, pl.getWorldManager()), pl);
		pm.registerEvents(new PlayerListener(pl), pl);
		pm.registerEvents(new EntityListener(pl), pl);
		pm.registerEvents(new ContainerListener(pl), pl);
		pm.registerEvents(new BlockListener(pl), pl);
		pm.registerEvents(new ShopListener(pl), pl);

		pm.registerEvents(new RedstoneListener(pl, pl.getRedstoneManager()), pl);
		
		pm.registerEvents(new CelestiaListener(pl, pl.getCelestiaManager()), pl);
		pm.registerEvents(new VoteListener(pl), pl);
		
		pm.registerEvents(new PotionCauldronListener(pl), pl);
		pm.registerEvents(new EnchantmentListener(pl), pl);
	}
	
	public void unregisterEvents() {
		HandlerList.unregisterAll(plugin);
	}
	
}
