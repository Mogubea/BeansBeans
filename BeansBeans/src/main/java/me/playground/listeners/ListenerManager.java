package me.playground.listeners;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import me.playground.celestia.logging.CelestiaListener;
import me.playground.enchants.BeanEnchantmentListener;
import me.playground.main.Main;

public class ListenerManager {
	
	private final Main plugin;
	
	public ListenerManager(Main pl) {
		this.plugin = pl;
		PluginManager pm = pl.getServer().getPluginManager();
		
		pm.registerEvents(new ConnectionListener(pl), pl);
		pm.registerEvents(new WorldListener(pl), pl);
		pm.registerEvents(new PlayerListener(pl), pl);
		pm.registerEvents(new EntityListener(pl), pl);
		pm.registerEvents(new ContainerListener(pl), pl);
		pm.registerEvents(new BlockListener(pl), pl);
		pm.registerEvents(new ShopListener(pl), pl);

		pm.registerEvents(new JobListener(pl), pl);
		pm.registerEvents(new BeanEnchantmentListener(pl), pl);
		
		pm.registerEvents(new CelestiaListener(pl), pl);
		pm.registerEvents(new VoteListener(pl), pl);
		
		pm.registerEvents(new PotionCauldronListener(pl), pl);
	}
	
	public void unregisterEvents() {
		HandlerList.unregisterAll(plugin);
	}
	
}
