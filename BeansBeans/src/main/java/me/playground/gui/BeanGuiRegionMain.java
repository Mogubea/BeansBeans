package me.playground.gui;

import java.util.Arrays;

import me.playground.entity.EntityRegionCrystal;
import me.playground.items.BeanItem;
import me.playground.items.lore.Lore;
import me.playground.regions.flags.Flags;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import me.playground.utils.SignMenuFactory;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class BeanGuiRegionMain extends BeanGuiRegion {
	
	protected static final ItemStack icon_name = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("Name", BeanColor.REGION));
	protected static final ItemStack icon_members = newItem(new ItemStack(Material.ACACIA_DOOR), Component.text("Members", BeanColor.REGION), "\u00a77Members of the Region");
	protected static final ItemStack icon_flags = newItem(new ItemStack(Material.LIGHT_BLUE_BANNER), Component.text("Flags", BeanColor.REGION), "\u00a77View the Region Flags");
	protected static final ItemStack icon_priority = newItem(new ItemStack(Material.CRIMSON_SIGN), Component.text("Priority", BeanColor.REGION));
	protected static final ItemStack icon_size = newItem(new ItemStack(Material.FILLED_MAP), Component.text("Size", BeanColor.REGION));
	private static final ItemStack removeCrystal = newItem(BeanItem.REGION_CRYSTAL.getItemStack(), Component.text("Drop Region Crystal", NamedTextColor.RED), Lore.getBuilder("Click to drop this &fRegion Crystal&r as an item.").build().getLoree());

	public BeanGuiRegionMain(Player p) {
		super(p);
		
		setName("Region");
		this.presetInv = new ItemStack[] {
				rBlank,rBlank,bBlank,bBlank,regionSkull,bBlank,bBlank,rBlank,rBlank,
				rBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,rBlank,
				bBlank,icon_name,blanc,icon_members,blanc,icon_flags,blanc,icon_priority,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				rBlank,rBlank,rBlank,whatIsThis,goBack,rBlank,rBlank,rBlank,rBlank
		};
	}
	
	protected BeanGuiRegionMain(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
		updateRegionItems();
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot > 54) return;
		final boolean canEdit = getRegion().canModify(p) && !getRegion().isWorldRegion();
		
		switch(slot) {
		case 19: // Rename Button - Must be a Senator? to rename your region.
			if (!canEdit) return;

			if (p.hasPermission("bean.region.modifyothers") || p.hasPermission("bean.region.rename")) {
				// can
			} else {
				p.sendActionBar(Component.text("\u00a7cYou don't have permission to rename regions."));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				return;
			}
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList(Component.empty(), Component.text("^^^^^^^^^^"), Component.text("\u00a7bNew Name for"), Component.text("\u00a7b" + getRegion().getName())), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	String regionName = strings[0];
                	
                	if (regionName.length() < 3)
        				throw new RuntimeException("The region name must contain at least 3 characters.");
        			
        			if (!regionName.matches("^[a-zA-ZÀ-ÖØ-öø-ÿ0-9-_]+$"))
        				throw new RuntimeException("The region name '"+regionName+"' is invalid (a-z, À-Ö, 0-9, _, -)!");
        			
        			if (rm.getRegion(regionName) != null)
        				throw new RuntimeException("The region '"+regionName+"' already exists!");
        			
        			String oldName = getRegion().getName();
        			getRegion().setName(p, regionName);

        			p.sendMessage(Component.text("\u00a77Renamed \u00a7f"+oldName+" \u00a77to ").append(getRegion().toComponent()));
        			refreshRegionViewers();
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.sendActionBar(Component.text("\u00a7c" + ex.getMessage()));
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiRegionMain(p, regionIdx).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			return;
		case 21: // Member Button
			new BeanGuiRegionMembers(p, regionIdx).openInventory();
			break;
		case 23: // Flag Button
			new BeanGuiRegionFlags(p, regionIdx).openInventory();
			break;
		case 25: // Priority Button - Can adjust region priority.
			if (!canEdit) return;
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu2 = plugin.getSignMenuFactory().newMenu(Arrays.asList(Component.empty(), Component.text("^^^^^^^^^^"), Component.text("\u00a7bRegion Priority for"), Component.text("\u00a7b" + getRegion().getName())), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	int newPriority = Integer.parseInt(strings[0]);
                	
                	if (newPriority < 0) newPriority = 0;
                	else if (newPriority > 20) newPriority = 20;
                	
                	getRegion().setPriority(p, newPriority);
                	
        			p.sendMessage(getRegion().toComponent().append(Component.text("\u00a77's region priority is now \u00a7f" + newPriority)));
        			refreshRegionViewers();
                	
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiRegionMain(p, regionIdx).openInventory(), 1L);
                return true;
            });
			menu2.open(p);
			return;
			/*case 31: // Size Button
				new BeanGuiRegionExpansion(p, regionIdx).openInventory();
				break;*/
			case 52: // Remove Crystal
				if (!getRegion().doesPlayerBypass(p, Flags.BUILD_ACCESS)) return;

				if (getCrystal() != null && !getCrystal().isRemoved()) {
					p.getInventory().addItem(BeanItem.REGION_CRYSTAL.getItemStack()).forEach((idx, item) -> getCrystal().getBukkitEntity().getWorld().dropItem(getCrystal().getBukkitEntity().getLocation(), item));
					getCrystal().remove(Entity.RemovalReason.DISCARDED);
					getAllViewers(BeanGuiRegion.class).forEach((gui) -> {
						if (gui.getRegion() == getRegion() && gui.getCrystal() == getCrystal()) {
							setCrystal(null);
							refresh();
						}
					});
					p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25F, 0.8F);
				}
				return;
		default:
			super.onInventoryClicked(e);
			return;
		}
		
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		final boolean isOwner = getRegion().getMember(p).higherThan(MemberLevel.OFFICER);
		
		ItemStack iName = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("Name", getRegion().getColour()));
		iName.lore(Arrays.asList(Component.text("\u00a7f" + getRegion().getName())));
		
		contents[19] = iName;
		
		contents[21] = newItem(new ItemStack(Material.ACACIA_DOOR), Component.text("Members", getRegion().getColour()), "\u00a77Members of the Region");
		
		contents[23] = newItem(new ItemStack(Material.LIGHT_BLUE_BANNER), Component.text("Flags", getRegion().getColour()), "\u00a77View the Region Flags");
		
		ItemStack iPriority = icon_priority.clone();
		if (isOwner && !getRegion().isWorldRegion())
			iPriority.lore(Arrays.asList(Component.text("\u00a7f" + getRegion().getPriority()), Component.empty(), Component.text("\u00a77\u00a7oClick to change")));
		else
			iPriority.lore(Arrays.asList(Component.text("\u00a7f" + getRegion().getPriority())));
		
		contents[25] = iPriority;
		contents[52] = getCrystal() != null && getRegion().doesPlayerBypass(p, Flags.BUILD_ACCESS) ? removeCrystal : rBlank;

		i.setContents(contents);
		super.onInventoryOpened();
	}

	@Override
	public BeanGuiRegion setCrystal(@Nullable EntityRegionCrystal crystal) {
		this.crystal = crystal;
		return this;
	}

	@Override
	public void onBackPress() {
		new BeanGuiMainMenu(p).openInventory();
	}
	
}
