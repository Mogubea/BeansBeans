package me.playground.celestia.logging;

import org.bukkit.entity.Player;

import me.playground.data.Datasource;

/**
 * Simple class to forward logs to {@link Datasource#logCelestia(CelestiaAction, int, org.bukkit.Location, String)}
 */
public final class Celestia {
	
	public static void logRegionChange(Player player, String information) {
		Datasource.logCelestia(CelestiaAction.REGION_CHANGE, player, player.getLocation(), information);
	}
	
}
