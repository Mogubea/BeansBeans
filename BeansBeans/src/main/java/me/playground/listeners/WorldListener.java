package me.playground.listeners;

import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldSaveEvent;

import me.playground.data.Datasource;
import me.playground.main.Main;

public class WorldListener extends EventListener {
	
	public WorldListener(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onWorldSave(WorldSaveEvent e) {
		if (e.getWorld().getEnvironment() == Environment.NETHER)
			Datasource.saveAll();
	}
	
}
