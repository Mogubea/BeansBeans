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
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiInbox extends BeanGui {
	
	protected static final ItemStack blank2 = newItem(new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1), Component.text("\u00a76Your Mailbox"));
	protected static final ItemStack chest = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RiY2E0YjY5ZWFmOGRjYjdhYzM3MjgyMjhkZThhNjQ0NDA3ODcwMTMzNDJkZGFhYmMxYjAwZWViOGVlYzFlMiJ9fX0="), Component.text("\u00a7fTest"));
	private final HashMap<Integer, Delivery> mapping = new HashMap<Integer, Delivery>();
	
	public BeanGuiInbox(Player p) {
		super(p);
		
		this.name = pp.isOverridingProfile() ? p.getName() + "'s Inbox" : "Your Inbox";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank2,blank2,bBlank,bBlank,null,bBlank,bBlank,blank2,blank2,
				blank2,null,null,null,null,null,null,null,blank2,
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
		
		p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.45F, 1);
		new BeanGuiInboxDelivery(p, delivery, i.getItem(e.getRawSlot())).openInventory();
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
			if (delivery.hasExpired() || delivery.isContentClaimed()) continue;
			
			final int row = x / 7;
			
			ItemStack stack = chest.clone();
			List<Component> lore = new ArrayList<Component>();
			lore.add(Component.text("\u00a77From: ").append(ProfileStore.from(delivery.getSenderId()).getColouredName()).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("\u00a77\""+delivery.getMessage()+"\""));
			lore.add(Component.empty());
			
			if (delivery.getContentClaimed() == 0) {
				lore.add(Component.text("\u00a77This delivery contains \u00a76" + delivery.getContentSize() + " items\u00a77."));
			} else {
				lore.add(Component.text("\u00a77You have claimed \u00a7e" + delivery.getContentClaimed() + "\u00a77 of the \u00a76" + delivery.getContentSize() + " items"));
				lore.add(Component.text("\u00a77contained within this delivery."));
			}
			
			lore.add(Component.empty());
			if (delivery.canExpire())
				lore.add(Component.text((System.currentTimeMillis()+60000*60*24 >= delivery.getExpiryDate() ? "\u00a7c" : "\u00a78") + "Expires in " + delivery.getExpiryString() + "."));
			lore.add(Component.text("\u00a77Received " + delivery.getReceiveString() + " ago."));
				
			lore.add(Component.text("\u00a76» \u00a7eClick to open!"));
			stack.lore(lore);
			
			mapping.put(10 + x + (2 * row), delivery);
			contents[10 + x + (2 * row)] = stack;
		}
		
		i.setContents(contents);
	}
	
}
