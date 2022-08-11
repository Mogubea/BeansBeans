package me.playground.listeners;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.regions.Region;
import me.playground.regions.flags.Flag;
import org.jetbrains.annotations.NotNull;

public abstract class EventListener implements Listener, IPluginRef {
	
	private final Main plugin;
	protected final Random rand;
	
	public EventListener(Main plugin) {
		this.plugin = plugin;
		this.rand = plugin.getRandom();
	}
	
	@Override
	@NotNull
	public Main getPlugin() {
		return plugin;
	}
	
	/**
	 * Helper for getting the highest priority region at the specified location.
	 */
	@NotNull
	public Region getRegionAt(Location loc) {
		return plugin.regionManager().getRegion(loc);
	}
	
	/**
	 * Check if the player has this {@link Flag} permission in the provided {@link Region}.
	 * @return whether the player has the flag permission
	 */
	protected final boolean checkRegionPermission(Region r, Cancellable e, Player p, Flag<?> flag) {
		return enactRegionPermission(r, e, p, flag, null, false);
	}
	
	/**
	 * Check if the player has this {@link Flag} permission in the provided {@link Region}.
	 * and cancels the {@link Cancellable} Event if the player does not have permission.
	 * @throws RegionPermissionException if no permission
	 * @return whether the player has the flag permission
	 */
	protected final boolean enactRegionPermission(Region r, Cancellable e, Player p, Flag<?> flag, String reason) {
		return enactRegionPermission(r, e, p, flag, reason, true);
	}
	
	/**
	 * Similar to {@link #enactRegionPermission(Region, Cancellable, Player, Flag, String)} except we already have the permission provided as 
	 * the first argument. Throws a {@link RegionPermissionException} and cancels the {@link Cancellable} Event if the player does not have permission.
	 * @throws RegionPermissionException if no permission
	 * @return whether the player has the flag permission
	 */
	protected boolean enactRegionPermission(boolean hasPermission, Cancellable e, Player p, String reason) {
		return enactRegionPermission(hasPermission, e, p, reason, true);
	}
	
	private boolean enactRegionPermission(boolean hasPermission, Cancellable e, Player p, String reason, boolean throwException) {
		try {
			if (!hasPermission) {
				if (throwException)
					throw new RegionPermissionException(p, e, reason == null ? null : "You don't have permission to " + reason + " here.");
				return false;
			}
			return true;
		} catch (RegionPermissionException ex) {
			return false;
		}
	}
	
	private boolean enactRegionPermission(Region r, Cancellable e, Player p, Flag<?> flag, String reason, boolean throwException) {
		return enactRegionPermission(r.doesPlayerBypass(p, flag), e, p, reason, throwException);
	}
	
}
