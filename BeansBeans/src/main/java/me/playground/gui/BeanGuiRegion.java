package me.playground.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.playground.entity.EntityRegionCrystal;
import me.playground.items.BeanItem;
import me.playground.items.lore.Lore;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.world.entity.Entity;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BeanGuiRegion extends BeanGui {

	protected static final ItemStack blanc = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.empty());
	protected final ItemStack regionSkull = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYxN2E2YTlhZmFhN2IwN2U0MjE4ZmU1NTVmMjgyM2IwMjg0Y2Q2OWI0OWI2OWI5N2ZhZTY3ZWIyOTc2M2IifX19");
	protected static final ItemStack whatIsThis = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is this?", BeanColor.REGION), 
			"\u00a77This is the \u00a79Region Menu\u00a77. Here, you are able", 
			"\u00a77to view various attributes about the \u00a79Regions", 
			"\u00a77that are currently overlapping your location.",
			"",
			"\u00a77The \u00a79Region Owner\u00a77 and \u00a7bStaff \u00a77are able to modify",
			"\u00a77various attributes about the \u00a79Region\u00a77 such as;",
			"\u00a77name, priority, flags and members from this menu.");

	private static final ItemStack removeCrystal = newItem(BeanItem.REGION_CRYSTAL.getItemStack(), Component.text("Drop Region Crystal", NamedTextColor.RED), Lore.getBuilder("Click to drop this &fRegion Crystal&r as an item.").setCompact().setLineLimit(32).build().getLore().get(0));
	protected final ItemStack rBlank = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
	protected final RegionManager rm;
	protected int regionIdx;
	final protected List<Region> localRegions;
	private EntityRegionCrystal crystal; // The crystal used to access this GUI.

	public BeanGuiRegion(Player p) {
		super(p);
		
		this.rm = getPlugin().regionManager();
		this.localRegions = rm.getRegions(p.getLocation());
		Collections.sort(localRegions);
		if (tpp.hasPermission("bean.region.override"))
			this.localRegions.add(rm.getWorldRegion(p.getWorld()));

		if (localRegions.size() < 1) return;

		setName("Region");
		this.presetSize = 54;

		updateRegionItems();
		updateCrystalItem();
	}
	
	protected BeanGuiRegion(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		if (e.getRawSlot() == 4) {
			if (getRegions().size() <= 1) return;
			regionIdx = (++regionIdx >= getRegions().size() ? 0 : regionIdx);
			p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
			setPage(0);
			updateRegionItems();
			openInventory();
		}

		// Drop crystal
		else if (e.getRawSlot() == 53) {
			if (getCrystal() != null) {
				p.getInventory().addItem(BeanItem.REGION_CRYSTAL.getItemStack()).forEach((idx, item) -> crystal.getBukkitEntity().getWorld().dropItem(crystal.getBukkitEntity().getLocation(), item));
				crystal.remove(Entity.RemovalReason.DISCARDED);
				updateCrystalItem();
				p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25F, 0.8F);
			}
		}
	}

	@Override
	public void onBackPress() {
		new BeanGuiRegionMain(p, regionIdx).openInventory();
	}

	@Override
	public void openInventory() {
		if (!getRegions().isEmpty())
			super.openInventory();
		else
			p.sendActionBar(Component.text("\u00a7cThere are no regions here!"));
	}

	protected void updateRegionItems() {
		List<Component> lore = new ArrayList<>();
		if (tpp.hasPermission("bean.region.override"))
			lore.add(Component.text("Permissions: ", NamedTextColor.GRAY).append(Component.text("Overridden", NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false).append(Component.text(" (" + getRegion().getTrueMemberLevel(tpp.getId()).toString() + ")", NamedTextColor.DARK_GRAY)));
		else
			lore.add(Component.text("Permissions: ", NamedTextColor.GRAY).append(Component.text(getRegion().getTrueMemberLevel(tpp.getId()).toString(), NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false));

		if (getRegions().size() > 1) {
			lore.add(Component.empty());

			for (Region region : getRegions()) {
				boolean match = getRegion().getRegionId() == region.getRegionId();
				lore.add(Component.text((match ? "  \u00a7f\u25b6" : " \u00a78\u25b6") + "\u00a7r ").append(region.getColouredName().color(match ? region.getColour() : NamedTextColor.DARK_GRAY)).decoration(TextDecoration.ITALIC, false));
			}

			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to swap regions!"));
		}

		regionSkull.editMeta(meta -> {
			meta.displayName(getRegion().getColouredName().decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
		});

		rBlank.setType(getRegion().isWorldRegion() ? Material.GREEN_STAINED_GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE);
		rBlank.editMeta(meta -> meta.displayName(getRegion().getColouredName().decoration(TextDecoration.ITALIC, false)));
	}

	private void updateCrystalItem() {
		i.setItem(53, getCrystal() != null ? removeCrystal : rBlank);
	}

	/**
	 * Get the dominant region currently encapsulating the viewers' location.
	 */
	@NotNull
	public Region getRegion() {
		if (regionIdx >= getRegions().size())
			regionIdx = getRegions().size() - 1;
		return getRegions().get(regionIdx);
	}

	/**
	 * Get the regions currently encapsulating the viewers' location.
	 */
	@NotNull
	public List<Region> getRegions() {
		return localRegions;
	}

	public BeanGuiRegion setCrystal(@Nullable EntityRegionCrystal crystal) {
		this.crystal = crystal;
		return this;
	}

	@Nullable
	private EntityRegionCrystal getCrystal() {
		return crystal;
	}

	/**
	 * TODO: improve this method to be more specific as to what to update rather than updating the entire inventory.
	 */
	protected void refreshRegionViewers() {
		getAllViewers(BeanGuiRegion.class).forEach((gui) -> {
			if (gui.getViewer() != p && gui.getRegion() == getRegion()) {
				gui.refresh(); 
			}
		});
	}
	
}
