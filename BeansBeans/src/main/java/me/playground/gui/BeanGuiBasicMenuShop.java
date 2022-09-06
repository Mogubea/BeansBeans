package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.playground.items.BeanItem;
import me.playground.items.tracking.ManifestationReason;
import me.playground.items.values.ItemValues;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.playground.menushop.MenuShop;
import me.playground.menushop.PurchaseOption;
import me.playground.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiBasicMenuShop extends BeanGui {
	
	private final Component baseName;
	
	private final Inventory truePlayerInventory;
	private final List<PurchaseOption> purchaseOptions;
	
	private boolean trueClosure = true;
	private int maxThisPage;
	protected int maxPerPage = 28;

	private PurchaseOption currentOption;

	private final HashMap<Integer, PurchaseOption> mappings = new HashMap<>();

	public BeanGuiBasicMenuShop(Player p, MenuShop shop) {
		super(p);
		
		purchaseOptions = shop != null ? shop.getPurchaseOptions() : new ArrayList<>();
		
		this.baseName = Component.text("Test Shop");
		this.presetSize = 54;
		this.interactCooldown = 300;
		this.presetInv = getPresetInventory(0);
		
		this.truePlayerInventory = Bukkit.createInventory(null, InventoryType.PLAYER); // XXX: maybe change to refresh inventory on closure rather than holding a clean version of the inventory?
		truePlayerInventory.setContents(p.getInventory().getContents());
		
		addSellValues(false);

		setName(purchaseOptions.size() > maxPerPage ? baseName.append(Component.text(" (1/" + (((purchaseOptions.size()-1) / maxPerPage) + 1) + ")")) : baseName);
	}
	
	public BeanGuiBasicMenuShop(Player p, NPC<?> npc) {
		this(p, npc.getMenuShop());
	}
	
	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		if (!trueClosure) return;
		
		// Update the player's inventory to be their true inventory, which is identical but without the sell price lore
		p.getInventory().setContents(truePlayerInventory.getContents());
		p.updateInventory();
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		// Selling items
		if (slot >= i.getSize()) {
			if (e.isRightClick()) return;
			
			ItemStack toSell = truePlayerInventory.getItem(e.getSlot());
			if (toSell == null) return;

			int quantity = e.isShiftClick() ? toSell.getAmount() : 1;
			double value = ItemValues.getTotalValue(toSell, e.isShiftClick());

			if (value <= 0F) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.9F);
				return;
			} else {
				p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_GOLD, 0.3F, 1.3F);
				pp.addToBalance(value);
				toSell.subtract(quantity);
				e.getClickedInventory().setItem(e.getSlot(), withSellValue(toSell));
			}
			return;
		}

		if (currentOption != null) {
			
		}

		// Buying items
		PurchaseOption opt = mappings.get(slot);
		if (opt == null) return;
		
		// Buy single
		if (e.isLeftClick()) {
			if (opt.purchase(p)) {
				ItemStack item = opt.getOriginalStack();
				BeanItem custom = opt.getCustomItem();

				AtomicInteger amount = new AtomicInteger(item.getAmount());

				if (custom != null)
					item = custom.getTrackedStack(p, ManifestationReason.SHOP, amount.get());

				truePlayerInventory.addItem(item).forEach((idx, itemStack) -> amount.addAndGet(-itemStack.getAmount()));
				//truePlayerInventory.addItem(item);
				addSellValues(true);
			}

		// Buy multiple
		} else {
			
		}
	}
	
	private ItemStack withSellValue(ItemStack item) {
		ItemStack newItem = item.clone();
		double valOfOne = ItemValues.getTotalValue(item, false);

		if (valOfOne <= 0F) return newItem;

		newItem.editMeta(meta -> {
			List<Component> lore = new ArrayList<>();
			if (meta.lore() != null && !(meta.lore().contains(null)))
				lore.addAll(meta.lore());
			lore.add(Component.text("---------------", NamedTextColor.DARK_GRAY).decoration(TextDecoration.STRIKETHROUGH, true).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.text("\u00a77Sell Value"));
			lore.add(Component.text("\u00a78 • \u00a76" + dec.format(valOfOne * item.getAmount()) + " Coins"));
			if (item.getAmount() > 1)
				lore.add(Component.text("\u00a78 • Each worth \u00a7r" + dec.format(valOfOne) + " Coins").colorIfAbsent(TextColor.color(0xcfb525)).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());

			if (item.getAmount() > 1) {
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to sell one!"));
				lore.add(Component.text("\u00a76» \u00a7eShift-Click to sell all!"));
			} else {
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to sell!"));
			}

			meta.lore(lore);
		});
		return newItem;
	}
	
	@Override
	public void onItemPickup(EntityPickupItemEvent e) {
		e.setCancelled(true);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		
		 if (interactCooldown > 0 && pp.onCdElseAdd("guiClick", interactCooldown, true)) {
			return true;
		} else if (i != null && i.isSimilar(closeUI)) {
			close();
		} else if (i != null && i.isSimilar(nextPage)) {
			reopenWithNewName(page + 1);
		} else if (i != null && i.isSimilar(prevPage)) {
			reopenWithNewName(page - 1);
		} else {
			return false;
		}
		
		return true;
	}
	
	private void setPage(int page, Component newTitle) {
		trueClosure = false;
		this.page = page;
		if (page < 0 || page > 50)
			page = 0;
		
		this.name = newTitle;
		
		this.presetInv = getPresetInventory(page);
		this.maxThisPage = Math.min(maxPerPage, purchaseOptions.size() - (page * maxPerPage));
		openInventory();
		trueClosure = true;
	}
	
	private ItemStack[] getPresetInventory(int page) {
		maxThisPage = Math.min(maxPerPage, purchaseOptions.size() - (page * maxPerPage));
		
		ItemStack[] defaults = new ItemStack[] {
			bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
			bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
			bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
			bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
			bBlank,blank,blank,blank,blank,blank,blank,blank,bBlank,
			bBlank,page > 0 ? prevPage : bBlank,bBlank,bBlank,closeUI,bBlank,bBlank,purchaseOptions.size() > maxPerPage * (page+1) ? nextPage : bBlank,bBlank,
		};
		
		mappings.clear();
		for (int x = -1; ++x < maxThisPage;) {
			int slot = 10 + x + ((x/7) * 2);
			int optIdx = (page * maxPerPage) + x;
			
			defaults[slot] = purchaseOptions.get(optIdx).getDisplayItem(p);
			mappings.put(slot, purchaseOptions.get(optIdx));
		}
		
		return defaults;
	}
	
	private void reopenWithNewName(int newPage) {
		p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.35F, 1.35F);
		setPage(newPage, purchaseOptions.size() > maxPerPage ? baseName.append(Component.text(" (1/" + (((purchaseOptions.size()-1) / maxPerPage) + 1) + ")")) : baseName);
	}
	
	/**
	 * Add the "Sell Value" lore onto all inventory items.
	 */
	private void addSellValues(boolean check) {
		for (int x = -1; ++x < truePlayerInventory.getSize();) {
			ItemStack pItem = truePlayerInventory.getItem(x);
			if (pItem == null) continue;
			if (check && p.getInventory().getItem(x) != null && (pItem.getAmount() == p.getInventory().getItem(x).getAmount())) continue;
			
			p.getInventory().setItem(x, withSellValue(pItem));
		}
	}
	
}
