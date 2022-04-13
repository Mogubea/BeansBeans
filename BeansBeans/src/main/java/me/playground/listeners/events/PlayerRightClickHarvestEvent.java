package me.playground.listeners.events;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.playground.playerprofile.PlayerProfile;

public class PlayerRightClickHarvestEvent extends PlayerEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private final PlayerProfile pp;
	private final ItemStack handItem;
	private final Block block;
	private boolean isCancelled;
	
	public PlayerRightClickHarvestEvent(@Nonnull Player who, @Nullable ItemStack item, @Nullable Block clickedBlock, @Nonnull BlockFace clickedFace) {
		super(who);
		this.pp = PlayerProfile.from(who);
		pp.pokeAFK();
		this.handItem = item;
		this.block = clickedBlock;
	}
	
	@Nonnull
	public PlayerProfile getProfile() {
		return pp;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
	
	public static @NotNull HandlerList getHandlerList() {
		return handlers;
	}

	public ItemStack getItemInHand() {
		return handItem;
	}

	public Block getClickedBlock() {
		return block;
	}
	
	public Material getMaterial() {
		return block.getType();
	}

}
