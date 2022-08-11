package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import me.playground.items.BItemBigChest;
import me.playground.items.BeanBlock;
import me.playground.items.BeanItem;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;

public class BeanGuiBigChest extends BeanGui {
	
	private static final Map<Location, ArrayList<Player>> viewers = new HashMap<>();
	
	private final ItemStack accidentInfo = newItem(new ItemStack(Material.GRAY_DYE), Component.text("\u00a7bBreak Protection"), Component.text("\u00a77Prevent this container from"), Component.text("\u00a77being broken accidentally."), Component.empty());
	
	private final BItemBigChest customChest;
	private final Chest chest;
	private final int maxStorage;
	
	private ItemStack[] chestInventory;
	
	private boolean trueClosure = true;
	private int maxThisPage;
	private int maxPerPage;
	
	public BeanGuiBigChest(Player p, Chest chest) {
		super(p);
		
		this.chest = chest;
		
		BeanItem custom = BeanBlock.from(chest);
		if (custom == null)
			throw new IllegalArgumentException("The Chest provided was not a custom chest!");
		
		customChest = (BItemBigChest) custom;
		int maxSize = customChest.getStorageSize();
		
		this.baseName = chest.customName().color(null);
		
		this.presetSize = maxSize < 54 ? maxSize+9 : 54;
		
		this.chestInventory = customChest.getInventoryContents(chest);
		this.maxStorage = customChest.getStorageSize();
		this.interactCooldown = 125;
		
		this.maxPerPage = Math.min(maxStorage, 45);
		this.maxThisPage = Math.min(maxPerPage, maxStorage - (page * maxPerPage));
		
		this.presetInv = getPresetInventory(0);
		
		setName(maxStorage > 54 ? baseName.append(Component.text(" (1/" + (((maxStorage-1) / 45) + 1) + ")")) : baseName);
		if (!p.getGameMode().equals(GameMode.SPECTATOR))
			chest.open();
		
		// Set viewers.
		ArrayList<Player> list = viewers.getOrDefault(chest.getLocation(), new ArrayList<>());
		list.add(p);
		viewers.put(chest.getLocation(), list);
	}
	
	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		if (!trueClosure) return;
		
		// Set viewers.
		viewers.get(chest.getLocation()).remove(p);
		if (viewers.get(chest.getLocation()).isEmpty())
			viewers.remove(chest.getLocation());
		
		saveToArray();
		customChest.saveInventory(chest, chestInventory, true);
		
		if (!p.getGameMode().equals(GameMode.SPECTATOR) && !(viewers.containsKey(chest.getLocation())))
			chest.close();
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		if (slot < i.getSize() && chest.hasMetadata("noHopper")) return;
		if (slot == (maxThisPage+2)) {
			toggleAccidentProtection(chest);
			return; 
		}
		if (slot >= maxThisPage && slot < i.getSize()) return;
		e.setCancelled(false);
		
		// Due to the inability of efficiently and reliably predicting the outcome of an InventoryClickEvent, just prevent any and all interactions
		// while an interaction is going on. This is a tiny price to pay for users for essentially live updating and multi-viewable custom containers.
		chest.setMetadata("noHopper", new FixedMetadataValue(getPlugin(), true));
		
		Bukkit.getScheduler().runTask(getPlugin(), () -> {
			saveToArray();
			
			updateViewerInventory(chest, chestInventory, p, page);
			customChest.saveInventory(chest, chestInventory, false);
			chest.removeMetadata("noHopper", getPlugin());
		});
	}
	
	@Override
	public void onInventoryOpened() {
		for (int x = -1; ++x < maxThisPage;)
			i.setItem(x, chestInventory[page * maxPerPage + x]);
	}
	
	@Override
	public void onInventoryDrag(InventoryDragEvent e) {
		if (chest.hasMetadata("noHopper")) return;
		
		// INSTANT identical changes due to InventoryDragEvent being super helpful and friendly, providing us with all the valid inventory changes before they happen.
		e.getNewItems().forEach((slot, item) -> {
			if (slot >= i.getSize()) return;
			chestInventory[(page * maxPerPage) + slot] = item;
		});
		
		updateViewerInventory(chest, chestInventory, p, page);
		customChest.saveInventory(chest, chestInventory, false);
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
	
	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.BLOCK_METAL_BREAK, 0.25F, 1.0F);
	}
	
	private void setPage(int page, Component newTitle) {
		saveToArray();
		
		trueClosure = false;
		this.page = page;
		if (page < 0 || page > 50)
			page = 0;
		
		this.name = newTitle;
		
		this.presetInv = getPresetInventory(page);
		this.maxPerPage = Math.min(maxStorage, 45);
		this.maxThisPage = Math.min(maxPerPage, maxStorage - (page * maxPerPage));
		openInventory();
		trueClosure = true;
	}
	
	private ItemStack[] getPresetInventory(int page) {
		int maxThisPage = Math.min(maxPerPage, maxStorage - (page * maxPerPage));
		
		ItemStack[] defaults = new ItemStack[presetSize];
		
		for (int x = -1; ++x < 9;)
			defaults[maxThisPage + x] = x == 4 ? closeUI : bBlank;
		
		if (page > 0)
			defaults[maxThisPage + 1] = prevPage;
		
		if (maxStorage > maxPerPage * (page+1))
			defaults[maxThisPage + 7] = nextPage;
		
		defaults[maxThisPage + 2] = getAccidentItem();
		return defaults;
	}
	
	private void reopenWithNewName(int newPage) {
		p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.35F, 1.35F);
		setPage(newPage, maxStorage > 54 ? baseName.append(Component.text(" (" + (newPage + 1) + "/" + (((maxStorage-1) / 45) + 1) + ")")) : baseName);
	}
	
	private void saveToArray() {
		for (int x = -1; ++x < maxThisPage;)
			chestInventory[page * maxPerPage + x] = i.getItem(x);
	}
	
	public static ArrayList<Player> getViewers(Chest chest) {
		ArrayList<Player> viewerz = new ArrayList<>();
		if (chest != null)
			viewerz = viewers.getOrDefault(chest.getLocation(), viewerz);
		
		return viewerz;
	}
	
	public static void updateViewerInventory(Chest chest, ItemStack[] items) {
		updateViewerInventory(chest, items, null, -1);
	}
	
	private static void updateViewerInventory(Chest chest, ItemStack[] items, Player exclusion, int pageMatch) {
		if (!viewers.containsKey(chest.getLocation())) return;
		
		for (Player vp : viewers.get(chest.getLocation())) {
			if (exclusion != null && vp == exclusion) continue;
			
			PlayerProfile vpp = PlayerProfile.from(vp);
			if (vpp.getBeanGui() instanceof BeanGuiBigChest bigChest) {
				bigChest.chestInventory = items;
				if (pageMatch < 0 || vpp.getBeanGui().getPage() == pageMatch)
					bigChest.onInventoryOpened();
			}
		}
	}
	
	private void toggleAccidentProtection(Chest chest) {
		customChest.toggleProtection(chest);
		ItemStack accidentItem = getAccidentItem();
		
		for (Player vp : viewers.get(chest.getLocation())) {
			PlayerProfile vpp = PlayerProfile.from(vp);
			vp.playSound(vp.getLocation(), Sound.BLOCK_CHAIN_BREAK, 0.2F, 1.1F);
			if (vpp.getBeanGui() instanceof BeanGuiBigChest)
				vpp.getBeanGui().i.setItem(((BeanGuiBigChest)vpp.getBeanGui()).maxThisPage + 2, accidentItem);
		}
	}
	
	private ItemStack getAccidentItem() {
		ItemStack accident = accidentInfo.clone();
		accident.editMeta(meta -> {
			boolean enabled = customChest.hasAccidentProtection(chest);
			
			if (enabled) {
				accident.setType(Material.LIGHT_BLUE_DYE);
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			}
			
			List<Component> lore = new ArrayList<>(accidentInfo.lore());
			
			lore.add(Component.text("\u00a77Enabled: " + (enabled ? "\u00a7aYes" : "\u00a7cNo")));
			meta.lore(lore);
		});
		return accident;
	}
	
}
