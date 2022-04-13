package me.playground.listeners;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import me.playground.celestia.logging.CelestiaListener;
import me.playground.enchants.BeanEnchantmentListener;
import me.playground.main.Main;
import me.playground.playerprofile.skills.BxpListener;

public class ListenerManager {
	
	public ListenerManager(Main pl) {
		PluginManager pm = Bukkit.getPluginManager();
		
		pm.registerEvents(new ConnectionListener(pl), pl);
		pm.registerEvents(new WorldListener(pl), pl);
		pm.registerEvents(new PlayerListener(pl), pl);
		pm.registerEvents(new EntityListener(pl), pl);
		pm.registerEvents(new ContainerListener(pl), pl);
		pm.registerEvents(new BlockListener(pl), pl);
		pm.registerEvents(new ShopListener(pl), pl);

		pm.registerEvents(new BxpListener(pl), pl);
		pm.registerEvents(new JobListener(pl), pl);
		pm.registerEvents(new BeanEnchantmentListener(), pl);
		
		pm.registerEvents(new CelestiaListener(), pl);
		pm.registerEvents(new VoteListener(pl), pl);
		
		pm.registerEvents(new PotionCauldronListener(pl), pl);
	}
	
}
