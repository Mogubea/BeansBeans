package me.playground.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import net.kyori.adventure.text.Component;

import java.io.Serial;

public class RegionPermissionException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 6293204585885190346L;
	
	public RegionPermissionException(Player p, Cancellable e, String reason) {
		super(reason == null ? "" : reason);
		e.setCancelled(true);
		if (reason != null)
			p.sendActionBar(Component.text("\u00a74\u2716 \u00a7c" + reason + " \u00a74\u2716"));
	}
	
}
