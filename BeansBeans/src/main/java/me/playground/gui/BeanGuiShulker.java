package me.playground.gui;

import me.playground.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import me.playground.items.BeanItem;
import net.kyori.adventure.text.Component;

/**
 * Open a {@link ShulkerBox} from the inventory.
 */
public class BeanGuiShulker extends BeanGui {

	private static boolean safetyDisable;

	private final int shulkerSlot;
	
	public BeanGuiShulker(Player p, ItemStack itemStack, int shulkerSlot) {
		super(p);
		
		if (!(itemStack.getItemMeta() instanceof BlockStateMeta meta))
			throw new IllegalArgumentException("The ItemStack provided for BeanGuiShulker was not a Shulker Box!");
		if (!(meta.getBlockState() instanceof ShulkerBox shulker))
			throw new IllegalArgumentException("The ItemStack provided for BeanGuiShulker was not a Shulker Box!");

		Component displayName = meta.displayName();
		setName((displayName != null ? displayName : Component.translatable(itemStack)).color(null));
		this.presetType = InventoryType.SHULKER_BOX;
		this.presetSize = presetType.getDefaultSize();
		this.presetInv = shulker.getInventory().getContents();
		this.shulkerSlot = shulkerSlot;
		this.interactCooldown = 0;
	}
	
	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 0.2F, 1F);
		ItemStack itemStack = p.getInventory().getItem(shulkerSlot);
		if (itemStack == null) {
			safetyDisable = true;
			Utils.notifyAllStaff(Component.text("Inventory shortcut for Shulker Boxes has been disabled due to a bug. The player ").append(pp.getComponentName()).append(Component.text(" may have lost or duplicated items.")), "Shulker Boxes Disabled", "A serious bug was detected with the Inventory shortcut for Shulker Boxes and has been disabled.");
			return;
		}

		itemStack.editMeta(meta -> {
			BlockStateMeta blockMeta = (BlockStateMeta) meta;
			ShulkerBox box = (ShulkerBox) blockMeta.getBlockState();
			box.getInventory().setContents(getInventory().getContents());
			blockMeta.setBlockState(box);
		});
		BeanItem.formatItem(itemStack);
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent e) {
		e.setCancelled(false);
	}

	@Override
	public void openInventory() {
		if (safetyDisable) {
			p.sendActionBar(Component.text("\u00a7cThis feature has been temporarily disabled due to a bug."));
		} else {
			super.openInventory();
		}
	}

	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		// Don't allow the player to move the active shulker.
		e.setCancelled(e.getRawSlot() >= i.getSize() && e.getSlot() == shulkerSlot);
		return true;
	}
	
	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.2F, 1F);
	}
	
	public int getShulkerSlot() {
		return shulkerSlot;
	}
	
}
