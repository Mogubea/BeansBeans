package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.playground.items.BeanBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import me.playground.items.BItemLivingHopper;
import me.playground.items.BeanItem;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class BeanGuiLivingHopper extends BeanGui {
	
	private static final Map<Location, ArrayList<Player>> viewers = new HashMap<>();
	private static final ItemStack filter = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u00a7aInput Filter Slot"), Component.text("\u00a77Placing an item here will allow"), Component.text("\u00a77it to enter into this hopper."));
	private static final ItemStack noFilters = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u00a7aInput Filter Slot"), Component.text("\u00a77Placing an item here will allow"), Component.text("\u00a77it to enter into this hopper."), Component.empty(), Component.text("\u00a7eDue to having no filters, any item"), Component.text("\u00a7ecan currently enter into this"), Component.text("\u00a7ehopper."));
	private static final ItemStack filtered = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u00a7aInput Filter Slot"), Component.text("\u00a77This item is permitted to enter"), Component.text("\u00a77into this hopper."));
	private static final ItemStack hopperIcon = newItem(BeanItem.LIVING_HOPPER.getItemStack(), Component.text("\u00a7aLiving Hopper"), Component.text("\u00a77Inventory and Input Filter"));
	
	private final BItemLivingHopper livingHopper;
	private Hopper hopper;
	
	private ItemStack[] filterInventory;
	
	public BeanGuiLivingHopper(Player p, @NotNull Hopper hopper) {
		super(p);
		
		this.hopper = hopper;
		BeanBlock custom = BeanBlock.from(hopper);
		if (!(custom instanceof BItemLivingHopper livingHopper))
			throw new IllegalArgumentException("The Hopper provided was not a custom hopper!");
		
		this.livingHopper = livingHopper;
		
		this.presetSize = 54;
		this.filterInventory = livingHopper.getFilterContents(hopper);
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,hopperIcon,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,null,null,null,null,null,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,filter,filter,filter,filter,filter,filter,filter,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,closeUI,bBlank,bBlank,bBlank,bBlank,
		};
		
		setName("Living Hopper");
		
		// Set viewers.
		ArrayList<Player> list = viewers.getOrDefault(hopper.getLocation(), new ArrayList<>());
		list.add(p);
		viewers.put(hopper.getLocation(), list);
	}
	
	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		viewers.get(hopper.getLocation()).remove(p);
		if (viewers.get(hopper.getLocation()).isEmpty())
			viewers.remove(hopper.getLocation());
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		boolean updateHopperInv = false;
		if (slot >= i.getSize()) {
			e.setCancelled(false);
			if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || e.getAction() == InventoryAction.COLLECT_TO_CURSOR)
				updateHopperInv = true;
		} else if (slot >= 11 && slot <= 15) {
			e.setCancelled(false);
			updateHopperInv = true;
		} else if (slot >= 28 && slot <= 34) {
			if (e.isShiftClick()) return; // Do nothing
			
			if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
				if (filterInventory[slot - 28] == null) return;
				filterInventory[slot - 28] = null;
				i.setItem(slot, filter);
			} else {
				BeanItem custom = BeanItem.from(e.getCursor());
				filterInventory[slot - 28] = custom != null ? custom.getItemStack() : new ItemStack(e.getCursor().getType());
			}
			
			// Save new filter and update the inventory for all users
			hopper = getHopper();
			livingHopper.saveFilterInventory(hopper, filterInventory);
			return;
		} else {
			return;
		}
		
		// Update the inventory for all users if flagged
		if (updateHopperInv) {
			hopper = getHopper();
			
			if (hopper.hasMetadata("noHopper")) {
				e.setCancelled(true);
				return;
			}
			
			hopper.setMetadata("noHopper", new FixedMetadataValue(getPlugin(), true));
			Bukkit.getScheduler().runTask(getPlugin(), () -> {
				for (int x = -1; ++x < 5;)
					hopper.getInventory().setItem(x, i.getItem(11 + x));
				
				updateViewerInventory(hopper, filterInventory);
				hopper.removeMetadata("noHopper", getPlugin());
			});
		}
	}
	
	@Override
	public void onInventoryOpened() {
		for (int x = -1; ++x < 5;)
			i.setItem(11 + x, hopper.getInventory().getItem(x));
		
		boolean hasFilter = false;
		
		for (int x = -1; ++x < 7;) {
			if (filterInventory[x] == null) continue;
			hasFilter = true;
			
			ItemStack heck = BeanItem.formatItem(filterInventory[x].clone());
			heck.editMeta(meta -> meta.lore(filtered.lore())); // Append an informative lore to prevent stacking
			i.setItem(28 + x, heck);
		}
		
		// Set filter slots
		for (int x = -1; ++x < 7;) {
			if (filterInventory[x] != null) continue;
			i.setItem(28 + x, hasFilter ? filter : noFilters);
		}
	}
	
	@Override
	public void onInventoryDrag(InventoryDragEvent e) {
		if (hopper.hasMetadata("noHopper")) return;
		
		hopper = getHopper();
		e.getNewItems().forEach((slot, item) -> {
			if (!(slot >= 11 && slot <= 15)) return;
			hopper.getInventory().setItem(slot - 11, item);
		});
		
		updateViewerInventory(hopper, null, p);
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		
		 if (interactCooldown > 0 && pp.onCdElseAdd("guiClick", interactCooldown, true)) {
			return true;
		} else if (i != null && i.isSimilar(closeUI)) {
			close();
		} else {
			return false;
		}
		
		return true;
	}
	
	public Hopper getHopper() {
		return (Hopper)(hopper.getLocation().getBlock().getState());
	}
	
	public static void updateViewerInventory(Hopper hopper, ItemStack[] items) {
		updateViewerInventory(hopper, items, null);
	}
	
	private static void updateViewerInventory(Hopper hopper, ItemStack[] items, Player exclusion) {
		if (!viewers.containsKey(hopper.getLocation())) return;
		
		for (Player vp : viewers.get(hopper.getLocation())) {
			if (exclusion != null && vp == exclusion) continue;
			
			PlayerProfile vpp = PlayerProfile.from(vp);
			if (vpp.getBeanGui() instanceof BeanGuiLivingHopper) {
				if (items != null)
					((BeanGuiLivingHopper)vpp.getBeanGui()).filterInventory = items;
				vpp.getBeanGui().onInventoryOpened();
			}
		}
	}
	
}
