package me.playground.gui;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;

/**
 * TODO: Add functions to this and, in general, make it better.
 */
public abstract class BeanGuiConfirm extends BeanGui {
	
	protected static final ItemStack confirm = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1), "\u00a7aConfirm Action");
	protected static final ItemStack cancel = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), "\u00a7cCancel Action");
	
	protected final List<Component> confirmationInfo;

	public BeanGuiConfirm(Player p, List<Component> confirmationInfo) {
		super(p);
		
		this.confirmationInfo = confirmationInfo;
		setName("Confirm Action");
		this.presetSize = 27;
		this.presetInv = new ItemStack[] {
				confirm,confirm,confirm,blank,null,blank,cancel,cancel,cancel,
				confirm,confirm,confirm,blank,null,blank,cancel,cancel,cancel,
				confirm,confirm,confirm,blank,blank,blank,cancel,cancel,cancel,
		};
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		e.setCancelled(true);
		
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		if (e.getRawSlot() % 9 < 3) {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
			close();
			onAccept();
		} else if (e.getRawSlot() % 9 > 5) {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
			close();
			onDecline();
		}
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
		ItemMeta meta = book.getItemMeta();
		meta.displayName(Component.text("\u00a7fInformation"));
		meta.lore(confirmationInfo);
		book.setItemMeta(meta);
		
		contents[4] = newItem(pp.getSkull(), pp.getColouredName());
		contents[13] = book;
		
		i.setContents(contents);
	}
	
	public abstract void onAccept();
	public abstract void onDecline();
	
}
