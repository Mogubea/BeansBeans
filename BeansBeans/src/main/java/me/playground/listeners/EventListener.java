package me.playground.listeners;

import org.bukkit.Location;
import org.bukkit.event.Listener;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.regions.Region;

public abstract class EventListener implements Listener, IPluginRef {
	
	private final Main plugin;
	
	public EventListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	public Region getRegionAt(Location loc) {
		return plugin.regionManager().getRegion(loc);
	}
	
}
