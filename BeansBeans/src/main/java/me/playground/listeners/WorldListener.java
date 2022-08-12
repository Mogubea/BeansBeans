package me.playground.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldSaveEvent;

import me.playground.data.Datasource;
import me.playground.main.Main;

// TODO: More world things, improve saving
public class WorldListener extends EventListener {
	
	public WorldListener(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		if (e.getWorld().getEnvironment() == Environment.NETHER) {
			if (getPlugin().isEnabled())
				Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> getPlugin().saveAll());
			else
				getPlugin().saveAll();
		}
	}
	
}
