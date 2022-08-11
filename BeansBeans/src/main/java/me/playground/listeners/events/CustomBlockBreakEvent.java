package me.playground.listeners.events;

import javax.annotation.Nonnull;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import me.playground.playerprofile.PlayerProfile;

public class CustomBlockBreakEvent extends BlockBreakEvent {
	private final PlayerProfile pp;
	private final boolean checkCustomBlocks;
	private final boolean checkCustomItemUse;
	private final Enchantment enchantmentCause;
	private boolean earnedSkillXP = false;
	
	public CustomBlockBreakEvent(@NotNull Block block, @NotNull Player who, boolean checkCustomBlocks, boolean checkCustomItemUse) {
		super(block, who);
		this.pp = PlayerProfile.from(who);
		this.checkCustomBlocks = checkCustomBlocks;
		this.checkCustomItemUse = checkCustomItemUse;
		this.enchantmentCause = null;
	}
	
	public CustomBlockBreakEvent(@NotNull Block block, @NotNull Player who, @NotNull Enchantment enchantCause, boolean checkCustomBlocks, boolean checkCustomItemUse) {
		super(block, who);
		this.pp = PlayerProfile.from(who);
		this.checkCustomBlocks = checkCustomBlocks;
		this.checkCustomItemUse = checkCustomItemUse;
		this.enchantmentCause = enchantCause;
	}
	
	public CustomBlockBreakEvent(@NotNull Block block, @NotNull Player who) {
		this(block, who, true, true);
	}
	
	@Nonnull
	public PlayerProfile getProfile() {
		return pp;
	}
	
	/**
	 * Returns true if we are bothering to check custom block id tags.
	 */
	public boolean isCheckingCustomBlocks() {
		return checkCustomBlocks;
	}
	
	/**
	 * Returns true if we are firing custom item events.
	 */
	public boolean isActivatingCustomItems() {
		return checkCustomItemUse;
	}
	
	public boolean isDueToEnchantment() {
		return enchantmentCause != null;
	}
	
	public Enchantment getEnchantmentCause() {
		return enchantmentCause;
	}
	
	public void setEarnedSkillXP(boolean value) {
		this.earnedSkillXP = value;
	}
	
	public boolean earnedSkillXP() {
		return earnedSkillXP;
	}
	
}
