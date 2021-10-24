package me.playground.listeners.events;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import me.playground.npc.NPC;
import me.playground.playerprofile.PlayerProfile;

public class PlayerInteractNPCEvent extends PlayerInteractEntityEvent {
	private final PlayerProfile pp;
	private final NPC<?> npc;
	
	public PlayerInteractNPCEvent(@NotNull Player who, @NotNull NPC<?> clickedNPC) {
		super(who, clickedNPC.getEntity().getBukkitEntity());
		this.pp = PlayerProfile.from(who);
		this.npc = clickedNPC;
	}
	
	@Nonnull
	public PlayerProfile getProfile() {
		return pp;
	}
	
	@Nonnull
	public NPC<?> getNPC() {
		return npc;
	}

}
