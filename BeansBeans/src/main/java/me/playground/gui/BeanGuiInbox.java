package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.DeliveryType;
import me.playground.playerprofile.ProfileStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public class BeanGuiInbox extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1), Component.text("\u00a76Your Mailbox"));
	protected static final ItemStack chest = DeliveryType.TREASURE.getDisplayStack();
	private final HashMap<Integer, Delivery> mapping = new HashMap<Integer, Delivery>();
	
	public BeanGuiInbox(Player p) {
		super(p);
		
		setName(pp.isOverridingProfile() ? p.getName() + "'s Inbox" : "Your Inbox");
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
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		Delivery delivery = mapping.get(e.getRawSlot());
		if (delivery == null) return;
		
		p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.35F, 1);
		new BeanGuiInboxDelivery(p, delivery).openInventory();
	}
	
	@Override
	public void onInventoryOpened() {
		mapping.clear();
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(chest, Component.text("\u00a76Your Mailbox"));
		
		List<Delivery> deliveries = tpp.getInbox();
		int size = deliveries.size();
		
		for (int x = -1; ++x < size;) {
			Delivery delivery = deliveries.get(x);
			// Skip deliveries that shouldn't be visible.
			// This allows for non-expiring deliveries to remain in the player's mailbox for as long as they want to keep it.
			if (delivery.canExpire() && (delivery.hasExpired() || delivery.isContentClaimed())) continue;
			
			final int row = x / 7;
			DeliveryType type = delivery.getDeliveryType();
			ItemStack stack = type.getDisplayStack().clone();
			List<Component> lore = new ArrayList<Component>();
			lore.add(Component.text("\u00a77From: ").append(ProfileStore.from(delivery.getSenderId()).getColouredName()).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("\u00a77\""+ChatColor.translateAlternateColorCodes('&', delivery.getMessage())+"\u00a77\""));
			lore.add(Component.empty());
			
			if (delivery.getContentClaimed() == 0) {
				lore.add(Component.text("\u00a77This "+type.getName()+" contains \u00a76" + delivery.getContentSize() + " items\u00a77."));
			} else if (delivery.isContentClaimed()) {
				lore.add(Component.text("\u00a77All "+delivery.getContentClaimed()+" items from this "+type.getName()));
				lore.add(Component.text("\u00a77have been claimed."));
			} else {
				lore.add(Component.text("\u00a77You have claimed \u00a7e" + delivery.getContentClaimed() + "\u00a77 of the \u00a76" + delivery.getContentSize() + " items"));
				lore.add(Component.text("\u00a77contained within this "+type.getName()+"."));
			}
			
			lore.add(Component.empty());
			lore.add(Component.text("\u00a77Received " + delivery.getReceiveString() + " ago."));
			if (delivery.canExpire())
				lore.add(Component.text("\u00a7cExpires in " + delivery.getExpiryString() + "."));
				
			lore.add(Component.text("\u00a76» \u00a7eClick to " + (delivery.isOpened() ? "view" : "open") + "!"));
			stack.editMeta(meta -> {
				meta.displayName(Component.text(delivery.getTitle(), type.getTitleColour()).decoration(TextDecoration.ITALIC, false));
				meta.lore(lore);
			});
			
			mapping.put(10 + x + (2 * row), delivery);
			contents[10 + x + (2 * row)] = stack;
		}
		
		contents[48] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), "\u00a7eWhat are Deliveries?",
				"\u00a76Deliveries\u00a77 are packages/gifts filled with",
				"\u00a77various things like items, coins, experience etc.",
				"\u00a77Clicking on a \u00a76Delivery\u00a77 will open it up.",
				"",
				"\u00a7cMost deliveries will expire past a certain date",
				"\u00a7cso be sure to claim everything before they do!");
		
		i.setContents(contents);
	}
	
}
