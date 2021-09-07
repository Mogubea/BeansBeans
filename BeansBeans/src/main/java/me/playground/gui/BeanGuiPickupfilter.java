package me.playground.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;
import net.kyori.adventure.text.Component;

public class BeanGuiPickupfilter extends BeanGui {
	
	protected static final ItemStack blankbl = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiPickupfilter(Player p) {
		super(p);
		
		this.name = "Pickup Blacklist";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blankbl,null,blankbl,blankbl,goBack,blankbl,blankbl,null,blankbl
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		
		if (item == null)
			return;
		
		if (slot > presetSize) {
			if (tpp.getPickupBlacklist().size() > 135) {
				p.sendActionBar(Component.text("\u00a7cYou can't add anymore blacklisted items!"));
				return;
			}
			tpp.addToPickupBlacklist(item);
			p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
		} else if (slot < presetSize-9) {
			tpp.removeFromPickupBlacklist(item);
			p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.2F, 0.8F);
		}
		
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		// Update Contents
		final ItemStack[] contents = presetInv.clone();
		final ArrayList<String> blacklist = tpp.getPickupBlacklist();
		// 0 - 44
		for (int x = 0 + (page * 45); x < Math.min(45 + (page * 45), blacklist.size()); x++) {
			ItemStack item = toItem(blacklist.get(x));
			if (item == null)
				continue;
			contents[x - (page * 45)] = item;
		}
		
		contents[46] = page > 0 ? prevPage : blankbl;
		contents[52] = (page < 2 && (blacklist.size()+1)>(45*(page+1))) ? nextPage : blankbl;
		
		contents[48] = newItem(new ItemStack(Material.ENDER_CHEST), "\u00a78Blacklist Limit", "", "\u00a77You have used \u00a7f" + blacklist.size() + "\u00a77 of your", "\u00a77135 available blacklist slots!");
		contents[50] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), "\u00a78What is this?", "", 
				"\u00a77Any item which you have added to this list", 
				"\u00a77\u00a7ccan no longer\u00a77 be picked up by you,", 
				"\u00a77regardless of enchants, durability or data.",
				"\u00a77Please be careful of what you put in here.",
				"",
				"\u00a77To \u00a78Blacklist\u00a77 something, click a copy of the", "\u00a77item in your inventory!",
				"",
				"\u00a77To \u00a7fWhitelist\u00a77 something again, click an item", "\u00a77that is within the \u00a78Blacklist\u00a77 interface!");
		
		i.setContents(contents);
	}

	private ItemStack toItem(String entry) {
		BeanItem bi = BeanItem.from(entry);
		return bi != null ? bi.getItemStack() : new ItemStack(Material.valueOf(entry));
	}
	
}
