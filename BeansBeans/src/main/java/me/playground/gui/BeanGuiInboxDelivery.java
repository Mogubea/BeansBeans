package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.DeliveryCoins;
import me.playground.playerprofile.DeliveryContent;
import me.playground.playerprofile.DeliveryItem;
import net.kyori.adventure.text.Component;

public class BeanGuiInboxDelivery extends BeanGui {
	
	private final HashMap<Integer, DeliveryContent> mapping = new HashMap<Integer, DeliveryContent>();
	private final Delivery delivery;
	
	protected BeanGuiInboxDelivery(Player p, Delivery delivery, ItemStack displayStack) {
		super(p);
		this.delivery = delivery;
		
		this.name = pp.isOverridingProfile() ? p.getName() + "'s Delivery" : "Your Delivery";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,displayStack,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		DeliveryContent content = mapping.get(e.getRawSlot());
		if (content == null) return;
		if (content.isClaimed()) return;
		
		if (!content.claim()) return;
		
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6F, 0.9F);
		
		if (delivery.isContentClaimed()) {
			new BeanGuiInbox(p).openInventory();
		} else { // Update the one item's lore rather than the whole inventory.
			ItemStack stack = i.getItem(e.getRawSlot());
			List<Component> lore = stack.lore();
			lore.remove(lore.size()-1);
			lore.add(Component.text("\u00a78» Already claimed!"));
			stack.lore(lore);
			
			i.setItem(e.getRawSlot(), stack);
		}
	}
	
	@Override
	public void onInventoryOpened() {
		mapping.clear();
		final ItemStack[] contents = presetInv.clone();
		
		List<DeliveryContent> contentz = delivery.getContent();
		int size = contentz.size();
		
		for (int x = -1; ++x < size;) {
			DeliveryContent content = contentz.get(x);
			final int row = x / 7;
			
			ItemStack stack = null;
			
			if (content instanceof DeliveryCoins) {
				DeliveryCoins objCoin = (DeliveryCoins) content;
				stack = newItem(icon_money, Component.text("\u00a76" + df.format(objCoin.getCoins()) + " Coins"));
			} else if (content instanceof DeliveryItem) {
				DeliveryItem objItem = (DeliveryItem) content;
				stack = objItem.getItemStack();
			}
			
			List<Component> lore = stack.lore();
			if (lore == null) lore = new ArrayList<Component>();
			lore.add(Component.text(content.isClaimed() ? "\u00a78» Already claimed!" : "\u00a76» \u00a7eClick to claim!"));
			stack.lore(lore);
			
			
			mapping.put(10 + x + (2 * row), content);
			contents[10 + x + (2 * row)] = stack;
		}
		
		i.setContents(contents);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
			
		if (i.isSimilar(goBack)) {
			new BeanGuiInbox(p).openInventory();
			return true;
		} else if (i.isSimilar(nextPage)) {
			pageUp();
			return true;
		} else if (i.isSimilar(prevPage)) {
			pageDown();
			return true;
		}
		return false;
	}
	
}
