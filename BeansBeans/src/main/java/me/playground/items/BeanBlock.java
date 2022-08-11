package me.playground.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import me.playground.listeners.BlockListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.function.Consumer;
import java.util.function.Function;

public class BeanBlock extends BeanItem {
	
	protected BeanBlock(int numeric, String identifier, String name, Material material, ItemRarity rarity, int modelDataInt) {
		super(numeric, identifier, name, material, rarity, modelDataInt, 0);
	}
	
	protected BeanBlock(int numeric, String identifier, String name, ItemStack material, ItemRarity rarity, int modelDataInt) {
		super(numeric, identifier, name, material, rarity, modelDataInt, 0);
	}
	
	/**
	 * Used for creating an instance of {@link BeanBlock} using a skull with a custom skin.
	 */
	protected BeanBlock(final int numeric, final String identifier, String name, String skullBase64, ItemRarity rarity) {
		super(numeric, identifier, name, skullBase64, rarity);
	}
	
	/**
	 * Called whenever the player interacts with this block in the world.
	 * @param e - The {@link PlayerInteractEvent} instance.
	 */
	public void onBlockInteract(PlayerInteractEvent e) { }
	
	/**
	 * Called whenever the player breaks this block in the world.
	 * @param e - The {@link BlockBreakEvent} instance.
	 */
	public void onBlockBreak(BlockBreakEvent e) { }
	
	/**
	 * Called whenever the player places this block in the world.
	 * @param e - The {@link BlockPlaceEvent} instance.
	 */
	protected void onBlockPlace(BlockPlaceEvent e) { }
	
	/**
	 * Called whenever this block drops its items.
	 * @param e - The {@link BlockDropItemEvent} instance.
	 */
	public void onBlockDropItems(BlockDropItemEvent e) { }
	
	/**
	 * Called whenever the player places this block in the world, cancels the event if the block
	 * is not an instance of {@link PersistentDataHolder}, and assigns the KEY_ID {@link NamespacedKey} otherwise.
	 * Returning null will cancel the event in {@link BlockListener}.
	 * @param e - The {@link BlockPlaceEvent} instance.
	 * @return The KEY_ID of the placed block or null.
	 */
	public String preBlockPlace(BlockPlaceEvent e) {
		if (!(e.getBlock().getState() instanceof PersistentDataHolder)) {
			e.getPlayer().sendActionBar(Component.text("You cannot place this.", NamedTextColor.RED));
			return null;
		}
		
		BlockState bs = e.getBlock().getState();
		PersistentDataHolder holder = (PersistentDataHolder) bs;
		PersistentDataContainer pdc = holder.getPersistentDataContainer();
		String id = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(KEY_ID, PersistentDataType.STRING);
		if (id == null) return null; // This is practically impossible to happen.
		pdc.set(KEY_ID, PersistentDataType.STRING, id);
		bs.update();
		
		onBlockPlace(e);
		return id;
	}

	/**
	 * Used to obtain the {@link BeanBlock} from a {@link BlockState}.
	 * @param blockState The block state
	 * @return A {@link BeanBlock} or null.
	 */
	public static BeanBlock from(BlockState blockState) {
		if (blockState instanceof PersistentDataHolder) {
			String id = ((PersistentDataHolder)blockState).getPersistentDataContainer().get(KEY_ID, PersistentDataType.STRING);
			if (id == null) return null;
			BeanItem custom = BeanItem.from(id);
			return (custom instanceof BeanBlock) ? (BeanBlock) custom : null;
		}
		return null;
	}

	/**
	 * Used to obtain the {@link BeanBlock} from a {@link BlockState}.
	 * @param blockState The block state
	 * @param consumer Optional consumer
	 * @return A {@link BeanBlock} or null.
	 */
	public static BeanBlock from(BlockState blockState, Consumer<BeanBlock> consumer) {
		BeanBlock custom = from(blockState);
		if (custom != null)
			consumer.accept(custom);
		return custom;
	}

	/**
	 * Used to obtain the {@link BeanBlock} from a {@link Block}.
	 * @param block The block
	 * @return A {@link BeanBlock} or null.
	 */
	public static BeanBlock from(Block block) {
		return from(block.getState());
	}

	/**
	 * Used to obtain the {@link BeanBlock} from a {@link Block}.
	 * @param block The block
	 * @param consumer Optional consumer
	 * @return A {@link BeanBlock} or null.
	 */
	public static BeanBlock from(Block block, Consumer<BeanBlock> consumer) {
		return from(block.getState(), consumer);
	}

	/**
	 * Calls a {@link Function} from a {@link Block} if it's a type of {@link BeanBlock}.
	 * @param block The block
	 * @param function The function
	 */
	public static boolean func(Block block, Function<BeanBlock, Boolean> function) {
		BeanBlock custom = from(block);
		return custom != null && function.apply(custom);
	}
}
