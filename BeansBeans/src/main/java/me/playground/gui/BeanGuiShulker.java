package me.playground.gui;

import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import me.playground.items.BeanItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

/**
 * Open a {@link ShulkerBox} from the inventory.
 */
public class BeanGuiShulker extends BeanGui {
	
	private final ShulkerBox shulker;
	private final int shulkerSlot;
	
	public BeanGuiShulker(Player p, ItemStack itemStack, int shulkerSlot) {
		super(p);
		
		if (!(itemStack.getItemMeta() instanceof BlockStateMeta))
			throw new IllegalArgumentException("The ItemStack provided for BeanGuiShulker was not a Shulker Box!");
		BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
		if (!(meta.getBlockState() instanceof ShulkerBox))
			throw new IllegalArgumentException("The ItemStack provided for BeanGuiShulker was not a Shulker Box!");
		ShulkerBox box = (ShulkerBox) meta.getBlockState();
		
		this.shulker = box;
		setName(meta.hasDisplayName() ? meta.displayName().color(TextColor.color(0x3a3a3a)) : Component.translatable(itemStack));
		this.presetType = InventoryType.SHULKER_BOX;
		this.presetSize = presetType.getDefaultSize();
		this.presetInv = shulker.getInventory().getContents();
		this.shulkerSlot = shulkerSlot;
	}
	
	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		ItemStack itemStack = p.getInventory().getItem(shulkerSlot);
		BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
		ShulkerBox box = (ShulkerBox) meta.getBlockState();
		
		box.getInventory().setContents(getInventory().getContents());
		meta.setBlockState(box);
		itemStack.setItemMeta(meta);
		BeanItem.formatItem(itemStack);
		p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.25F, 1F);
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		if (e.getClickedInventory().getType() != InventoryType.SHULKER_BOX)
			if (e.getSlot() == shulkerSlot) return; // Don't allow the player to move the active shulker.
		e.setCancelled(false);
	}
	
	@Override
	public void onInventoryOpened() {
		p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.25F, 1F);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (super.preInventoryClick(e)) {
			final ItemStack i = e.getCurrentItem();
			if (i == null) return e.getCursor() == null; // Only cancel if both cursor and slot are null.
		}
		return false;
	}
	
	public int getShulkerSlot() {
		return shulkerSlot;
	}
	
}
