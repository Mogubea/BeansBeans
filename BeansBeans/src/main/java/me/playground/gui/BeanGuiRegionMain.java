package me.playground.gui;

import java.util.Arrays;

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

public class BeanGuiRegionMain extends BeanGuiRegion {
	
	protected static final ItemStack icon_name = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("Name", BeanColor.REGION));
	protected static final ItemStack icon_members = newItem(new ItemStack(Material.ACACIA_DOOR), Component.text("Members", BeanColor.REGION), "\u00a77Members of the Region");
	protected static final ItemStack icon_flags = newItem(new ItemStack(Material.LIGHT_BLUE_BANNER), Component.text("Flags", BeanColor.REGION), "\u00a77View the Region Flags");
	protected static final ItemStack icon_priority = newItem(new ItemStack(Material.CRIMSON_SIGN), Component.text("Priority", BeanColor.REGION));
	
	public BeanGuiRegionMain(Player p) {
		super(p);
		
		setName("Regions");
		this.presetInv = new ItemStack[] {
				rBlank,rBlank,bBlank,bBlank,null,bBlank,bBlank,rBlank,rBlank,
				rBlank,null,null,null,null,null,null,null,rBlank,
				bBlank,icon_name,null,icon_members,null,icon_flags,null,icon_priority,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				rBlank,rBlank,rBlank,rBlank,goBack,rBlank,rBlank,rBlank,rBlank
		};
	}
	
	protected BeanGuiRegionMain(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot < 19 || slot > 25) return;
		final boolean canEdit = getRegion().canModify(p) && !getRegion().isWorldRegion();
		
		switch(slot) {
		case 19: // Rename Button - Costs 500 coins, requires a permission so it's revokable if someone's a twat.
			if (!canEdit) return;
			if (p.hasPermission("bean.region.modifyothers")) {
				// can
			} else if (p.hasPermission("bean.region.rename")) {
				if (pp.getBalance() < 500) {
					p.sendActionBar(Component.text("\u00a7cYou don't have enough coins to rename the region!"));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
					return;
				}
			} else {
				p.sendActionBar(Component.text("\u00a7cYou don't have permission to rename regions."));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				return;
			}
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7bNew Name for", "\u00a7b" + getRegion().getName()), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	String regionName = strings[0];
                	
                	if (pp.getBalance() < 500) // just in case they delay it and their coin count changes
    					throw new RuntimeException("You don't have enough coins to rename a region!");
                	
                	if (regionName.length() < 3)
        				throw new RuntimeException("The region name must contain at least 3 characters.");
        			
        			if (!regionName.matches("^[a-zA-ZÀ-ÖØ-öø-ÿ0-9-_]+$"))
        				throw new RuntimeException("The region name '"+regionName+"' is invalid (a-z, À-Ö, 0-9, _, -)!");
        			
        			if (rm.getRegion(regionName) != null)
        				throw new RuntimeException("The region '"+regionName+"' already exists!");
        			
        			String oldName = getRegion().getName();
        			getRegion().setName(p, regionName);
        			
        			if (!p.hasPermission("bean.region.modifyothers"))
        				pp.addToBalance(-500, "Renamed region '"+oldName+"'" + " to '"+regionName+"'");
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
			
			SignMenuFactory.Menu menu2 = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7bRegion Priority for", "\u00a7b" + getRegion().getName()), Material.WARPED_WALL_SIGN)
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
		default:
			return;
		}
		
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		final boolean isOwner = getRegion().getMember(p).higherThan(MemberLevel.OFFICER);
		
		ItemStack iName = newItem(new ItemStack(Material.WARPED_SIGN), Component.text("Name", getRegion().getColour()));
		if (isOwner && !getRegion().isWorldRegion())
			iName.lore(Arrays.asList(Component.text("\u00a7f" + getRegion().getName()), Component.empty(), Component.text("\u00a77\u00a7oClick to change" + (p.hasPermission("bean.region.modifyothers") ? "" : " for \u00a76\u00a7o500 Coins"))));
		else
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
		
		i.setContents(contents);
		super.onInventoryOpened();
	}
	
}
