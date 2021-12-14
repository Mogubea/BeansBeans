package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.data.Datasource;
import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.DeliveryCoins;
import me.playground.playerprofile.DeliveryContent;
import me.playground.playerprofile.DeliveryItem;
import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiInboxDelivery extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1), Component.text("\u00a76Your Mailbox"));
	private static final ItemStack deleteDelivery = newItem(new ItemStack(Material.BARRIER), 
			"\u00a7cDelete Delivery",
			"\u00a77You can delete this delivery once all",
			"\u00a77of its contents have been claimed.");
	private static final ItemStack claimAll = newItem(new ItemStack(Material.CHEST_MINECART), 
			"\u00a7aClaim All",
			"\u00a77Instantly claim everything from this delivery.",
			"\u00a77Anything that can't fit in into your inventory",
			"\u00a77will be dropped at your feet.");
	
	private final HashMap<Integer, DeliveryContent> mapping = new HashMap<Integer, DeliveryContent>();
	private final Delivery delivery;
	
	protected BeanGuiInboxDelivery(Player p, Delivery delivery) {
		super(p);
		this.delivery = delivery;
		
		this.name = pp.isOverridingProfile() ? p.getName() + "'s Delivery" : "Your Delivery";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		// If the delivery is flagged to be removed, save to database and update now rather than waiting for the 15 minute update cycle.
		if (delivery.toBeRemoved()) {
			Datasource.updateDelivery(delivery);
			tpp.getInbox().remove(delivery);
			p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.35F, 1.0F);
		}
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		if (slot == 46 && delivery.isContentClaimed()) {
			final BeanGuiInboxDelivery instance = this;
			new BeanGuiConfirm(p, Arrays.asList(
					Component.text("\u00a77Confirm to delete the " + delivery.getDeliveryType().getName() + ":"),
					Component.text(delivery.getTitle(), delivery.getDeliveryType().getTitleColour()).decoration(TextDecoration.ITALIC, false))) {

				@Override
				public void onAccept() {
					delivery.flagRemoval();
					Datasource.updateDelivery(delivery);
					tpp.getInbox().remove(delivery);
					p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.35F, 1.0F);
					if (tpp.getInbox().size() - 1 <= 0)
						new BeanGuiInbox(p).openInventory();
				}

				@Override
				public void onDecline() {
					instance.openInventory();
				}
				
			}.openInventory();
			return;
		}
		
		if (slot == 52 && !delivery.isContentClaimed() && pp.hasPermission(Permission.DELIVERY_CLAIMALL)) {
			mapping.forEach((map, content) -> {
				if (content == null || content.isClaimed()) return;
				content.claim();
			});
			
			if (delivery.canExpire())
				new BeanGuiInbox(p).openInventory();
			return;
		}
		
		DeliveryContent content = mapping.get(slot);
		if (content == null) return;
		
		if (content.isClaimed()) {
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.9F);
			return;
		}
		
		if (!content.claim()) return;
		
		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6F, 0.9F);
		
		if (delivery.canExpire() && delivery.isContentClaimed()) {
			new BeanGuiInbox(p).openInventory();
		} else { // Update the one item's lore rather than the whole inventory.
			ItemStack stack = i.getItem(slot);
			List<Component> lore = stack.lore();
			lore.remove(lore.size()-1);
			lore.add(Component.text("\u00a78» Already claimed!"));
			stack.lore(lore);
			
			i.setItem(slot, stack);
		}
	}
	
	@Override
	public void onInventoryOpened() {
		mapping.clear();
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(delivery.getDeliveryType().getDisplayStack(), Component.text(delivery.getTitle(), delivery.getDeliveryType().getTitleColour()).decoration(TextDecoration.ITALIC, false));
		
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
		
		contents[46] = deleteDelivery;
		
		contents[48] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), "\u00a7eWhat are these?",
				"\u00a77The \u00a7fcontents \u00a77of a \u00a76Delivery\u00a77 can include",
				"\u00a77various things like items, coins, experience etc.",
				"\u00a77You can click a piece of \u00a7fcontent \u00a77to claim it forever!",
				"",
				"\u00a7cDeliveries with expiration dates will delete themselves",
				"\u00a7conce all of their contents have been collected.");
		
		if (pp.hasPermission(Permission.DELIVERY_CLAIMALL))
			contents[52] = claimAll;
		
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
