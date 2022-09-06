package me.playground.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;
import me.playground.items.BeanItemHeirloom;
import me.playground.playerprofile.HeirloomInventory;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class BeanGuiHeirlooms extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1), Component.text("Heirloom Bag").color(BeanColor.HEIRLOOM));
	protected static final ItemStack locked = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), Component.text("Locked Slot"));

	public BeanGuiHeirlooms(Player p) {
		super(p);
		
		setName("Your Heirlooms");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,locked,locked,locked,locked,locked,locked,locked,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) { }

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		
		if (item == null) return;
		
		final BeanItem bitem = BeanItem.from(item);
		
		if (!(bitem instanceof BeanItemHeirloom)) return;
		
		if (slot > presetSize) {
			if (tpp.getHeirlooms().size() >= tpp.getHeirlooms().getMaxHeirlooms()) {
				p.sendActionBar(Component.text("\u00a7cYou can't equip any more heirlooms!"));
				return;
			}
			if (tpp.getHeirlooms().addHeirloom(item)) {
				e.getClickedInventory().getItem(e.getSlot()).setAmount(item.getAmount()-1);
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, 0.8F);
			}
		} else if (slot < presetSize-9) {
			if (p.getInventory().firstEmpty() > -1) {
				if (tpp.getHeirlooms().removeHeirloom(item)) {
					p.getInventory().addItem(item);
					p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, 0.8F);
				}
			} else {
				p.sendActionBar(Component.text("\u00a7cYour inventory is full!"));
				return;
			}
		}
		
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		// Update Contents
		final ItemStack[] contents = presetInv.clone();
		final HeirloomInventory hinv = tpp.getHeirlooms();
		ArrayList<ItemStack> stacc = hinv.getContents();

		for (int x = (page * 14); x < Math.min(14 + (page * 14), hinv.size()); x++) {
			int a = x - (page * 14);
			int slot = 10 + a + ((a / 7) * 3);

			contents[slot] = stacc.get(x);
			if (contents[slot].getAmount() > 1) {
				contents[slot].setAmount(1);
				BeanItem.formatItem(contents[slot]);
			}
		}
		
		contents[46] = page > 0 ? prevPage : blank;
		contents[52] = (page < 2 && (hinv.size()+1)>(14*(page+1))) ? nextPage : blank;
		
		contents[50] = newItem(new ItemStack(Material.ENDER_CHEST), "\u00a7fHeirloom Bag", "", "\u00a77You have used \u00a7f" + hinv.size() + "\u00a77 of your", "\u00a77" + hinv.getMaxHeirlooms() + " available \u00a7fheirloom \u00a77slots!");
		contents[48] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), "\u00a7fWhat is this?", "", 
				"\u00a77This is your \u00a7fHeirloom Bag\u00a77! You can store any", 
				"\u00a7fHeirlooms \u00a77you may have picked up on your", 
				"\u00a77adventure. You gain the full effect of any \u00a7fHeirlooms",
				"\u00a77that you have stored in here!",
				"",
				"\u00a77To \u00a7aStore\u00a77 an \u00a7fHeirloom\u00a77, click the \u00a7fHeirloom", "\u00a77in your inventory!",
				"",
				"\u00a77To \u00a7cRemove\u00a77 an \u00a7fHeirloom\u00a78, click the \u00a7fHeirloom", "\u00a77that is within the \u00a7fHeirloom Bag\u00a77 interface!");

		//ItemStack stat_damage = newItem(new ItemStack(Material.IRON_SWORD), Component.text("Damage"), Lore.fastBuild(true, 40, "Test"));
		//contents[37] = stat_damage;

		i.setContents(contents);
	}
	
	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, 1.0F);
	}
	
}
