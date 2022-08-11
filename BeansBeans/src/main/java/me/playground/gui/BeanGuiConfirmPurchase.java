package me.playground.gui;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.menushop.PurchaseOption;
import net.kyori.adventure.text.Component;

public abstract class BeanGuiConfirmPurchase extends BeanGuiConfirm {
	
	protected static final ItemStack confirm = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), 
			Component.text("\u00a7aComplete Purchase"),
			Component.text("\u00a77Click to complete the purchase."));
	protected static final ItemStack cancel = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.text("\u00a7cCancel Purchase"),
			Component.text("\u00a77Click to cancel the purchase."));
	
	private final PurchaseOption option;
	
	public BeanGuiConfirmPurchase(Player p, PurchaseOption option) {
		super(p, null);
		
		this.option = option;
		
		setName("Confirm Purchase");
		this.presetSize = 36;
		this.presetInv = new ItemStack[] {
				confirm,confirm,confirm,bBlank,bBlank,bBlank,cancel,cancel,cancel,
				confirm,confirm,confirm,bBlank,option.getDisplayItem(p, null, true, false),bBlank,cancel,cancel,cancel,
				confirm,confirm,confirm,bBlank,bBlank,bBlank,cancel,cancel,cancel,
				bBlank,bBlank,bBlank,bBlank,closeUI,bBlank,bBlank,bBlank,bBlank
		};
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		int side = slot % 9;
		
		if (slot >= (i.getSize()-9)) return;
		
		if (side < 3) {
			if (option.purchase(p)) {
				onAccept();
			} else {
				p.sendActionBar(Component.text("\u00a7cThere was a problem completing your purchase."));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				onDecline();
			}
		} else if (side > 5) {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
			onDecline();
		}
	}
	
}
