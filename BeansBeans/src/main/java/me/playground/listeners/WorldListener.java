package me.playground.listeners;

import me.playground.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldSaveEvent;

import me.playground.main.Main;

// TODO: More world things, improve saving
public class WorldListener extends EventListener {

	private final WorldManager manager;

	public WorldListener(Main plugin, WorldManager manager) {
		super(plugin);
		this.manager = manager;
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
