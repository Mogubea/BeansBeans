package me.playground.gui;

import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.main.Main;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public abstract class BeanGuiRegion extends BeanGui {
	
	protected static final ItemStack whatIsThis = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is this?", BeanColor.REGION), 
			"\u00a77This is the \u00a79Region Menu\u00a77. Here, you are able", 
			"\u00a77to view various attributes about the \u00a79Regions", 
			"\u00a77that are currently overlapping your location.",
			"",
			"\u00a77The \u00a79Region Owner\u00a77 and \u00a7bStaff \u00a77are able to modify",
			"\u00a77various attributes about the \u00a79Region\u00a77 such as;",
			"\u00a77name, priority, flags and members from this menu.");
	
	protected ItemStack rBlank;
	protected final RegionManager rm;
	protected int regionIdx;
	final protected List<Region> localRegions;
	
	public BeanGuiRegion(Player p) {
		super(p);
		
		this.rm = Main.getRegionManager();
		this.localRegions = rm.getRegions(p.getLocation());
		Collections.sort(localRegions);
		if (p.hasPermission("bean.region.override"))
			this.localRegions.add(rm.getWorldRegion(p.getWorld()));
		this.regionIdx = 0;
		this.name = "Regions";
		this.presetSize = 54;
		this.rBlank = newItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1), getRegion().getColouredName());
	}
	
	protected BeanGuiRegion(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = i.getContents();
		
		contents[48] = whatIsThis;
		
		this.rBlank = newItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1), getRegion().getColouredName());
		for (int x = presetSize-9; x < presetSize; x++) {
			if (x == 48 || x == 49 || x == 50) continue;
			contents[x] = rBlank;
		}
		
		if (getRegions().size() > 1) {
			String[] lore = new String[2 + getRegions().size()];
			lore[0] = "";
			for (int x = 1; x < lore.length - 1; x++) {
				final Region r = getRegions().get(x - 1);
				lore[x] = (getRegion().getRegionId() == r.getRegionId()) ? "\u00a7f > \u00a79" + r.getName() : "\u00a78> " + r.getName();
			}
			lore[lore.length - 1] = "\u00a78\u00a7oClick to Cycle through Local Regions";
				
			contents[50] = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0YzBiYWFlYTM5NDU4NjQwNWUxNTU3ZjU3ZTUwNTlmNGQ0YjAzYmYwN2FhMmJhMGYyMDkzODQ3MWQyNzFhYiJ9fX0="), 
					Component.text("\u00a77Region: \u00a7r").append(getRegion().getColouredName()), lore);
		} else {
			contents[50] = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0YzBiYWFlYTM5NDU4NjQwNWUxNTU3ZjU3ZTUwNTlmNGQ0YjAzYmYwN2FhMmJhMGYyMDkzODQ3MWQyNzFhYiJ9fX0="), 
					Component.text("\u00a77Region: \u00a7r").append(getRegion().getColouredName()));
		}
		
		i.setContents(contents);
	}
	
	public Region getRegion() {
		if (regionIdx >= getRegions().size())
			regionIdx = getRegions().size() - 1;
		return getRegions().get(regionIdx);
	}
	
	public List<Region> getRegions() {
		return localRegions;
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
			
		if (e.getRawSlot() == 50) {
			if (getRegions().size() <= 1) return true;
			regionIdx = (++regionIdx >= getRegions().size() ? 0 : regionIdx);
			p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.35F, 1.0F);
			setPage(0);
			onInventoryOpened();
			return true;
		} else if (i.isSimilar(goBack)) {
			if (this instanceof BeanGuiRegionMain)
				new BeanGuiMainMenu(p).openInventory();
			else
				new BeanGuiRegionMain(p, regionIdx).openInventory();
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
	
	@Override
	public void openInventory() {
		if (!getRegions().isEmpty())
			super.openInventory();
		else
			p.sendActionBar(Component.text("\u00a7cThere are no regions here!"));
	}
	
	protected void refreshRegionViewers() {
		getAllViewers(BeanGuiRegion.class).forEach((gui) -> {
			if (gui.getViewer() != p && gui.getRegion() == getRegion()) {
				gui.refresh(); 
			}
		});
	}
	
}
