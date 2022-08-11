package me.playground.listeners.events;

import me.playground.playerprofile.PlayerProfile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Identical to CreatureSpawnEvent but only fires for Spawn Eggs.
 */
public class PlayerSpawnCreatureEvent extends CreatureSpawnEvent {
	private final PlayerProfile pp;
	private final ItemStack itemStack;

	public PlayerSpawnCreatureEvent(LivingEntity entity, Player p, ItemStack itemStack) {
		super(entity, SpawnReason.SPAWNER_EGG);
		this.pp = PlayerProfile.from(p);
		this.itemStack = itemStack;
	}
	
	@NotNull
	public PlayerProfile getProfile() {
		return pp;
	}

	@NotNull
	public ItemStack getItemStack() {
		return itemStack;
	}

}
