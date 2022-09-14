package me.playground.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.listeners.events.CustomBlockBreakEvent;

public class BlockDestructionSequence {
	
	private final static Random rand = new Random();
	
	private final Player player;
	private final Block block;
	private Enchantment enchantCause;
	private ItemStack playerItem;
	private boolean triggerCustomItems = true;
	private boolean triggerCustomBlocks = true;
	private boolean playEffect = true;
	private boolean dropItems = true;
	private int itemDamage = 1;
	
	public BlockDestructionSequence(Player p, Block b) {
		this.player = p;
		this.block = b;
		this.playerItem = p.getInventory().getItemInMainHand();
	}
	
	public BlockDestructionSequence(Player p, Block b, Enchantment cause, boolean customItems, boolean customBlocks) {
		this(p, b);
		this.enchantCause = cause;
		this.triggerCustomItems = customItems;
		this.triggerCustomBlocks = customBlocks;
	}
	
	/**
	 * Fire all the required events, effects, item drops etc. As if it was a regular block break. This method fires
	 * {@link CustomBlockBreakEvent}, {@link PlayerItemDamageEvent} and {@link BlockDropItemEvent}, taking into consideration
	 * the player's permissions, item enchantments, custom item functionality etc.
	 * 
	 * @return True if the player had permission to break this block
	 */
	public boolean fireSequence() {
		if (!new CustomBlockBreakEvent(block, player, enchantCause, triggerCustomBlocks, triggerCustomItems).callEvent()) return false;
		// Fire Item Damage Event to consider Custom Damage System.
		if (itemDamage > 0)
			new PlayerItemDamageEvent(player, playerItem, itemDamage, itemDamage).callEvent();
		
		// Play Sounds and Effects
		if (playEffect)
			block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
		//else if (playSound)
		//	block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), block.getSoundGroup().getBreakSound(), 0.75F, 1F);
		
		BlockState bs = block.getState();
		// Fire Block Drop Item Event to consider Skills and Enchantments.
		if (dropItems) {
			List<Item> drops = new ArrayList<>();
			for (ItemStack i : block.getDrops())
				drops.add(block.getWorld().dropItem(block.getLocation().add(0.5, 0, 0.5), i));
			block.setType(Material.AIR);
			new BlockDropItemEvent(block, bs, player, drops).callEvent();
		}
		return true;
	}
	
	public BlockDestructionSequence setItemDamage(int damage) {
		this.itemDamage = damage;
		return this;
	}
	
	public BlockDestructionSequence setEnchantmentCause(Enchantment enchant) {
		this.enchantCause = enchant;
		return this;
	}
	
	public BlockDestructionSequence setItemStack(ItemStack itemStack) {
		this.playerItem = itemStack;
		return this;
	}
	
	public BlockDestructionSequence setFireCustomItemEvents(boolean trigger) {
		this.triggerCustomItems = trigger;
		return this;
	}
	
	public BlockDestructionSequence setFireCustomBlockChecks(boolean trigger) {
		this.triggerCustomBlocks = trigger;
		return this;
	}
	
	public BlockDestructionSequence setPlayEffect(boolean toggle) {
		this.playEffect = toggle;
		return this;
	}
	
}
