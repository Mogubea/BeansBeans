package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.playground.celestia.logging.Celestia;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.utils.SignMenuFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiPlayerLoginMessage extends BeanGui {
	
	final private int lockedCol = 0x323233;
	protected static final ItemStack blankbl = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiPlayerLoginMessage(Player p) {
		super(p);
		
		this.name = pp.isOverridingProfile() ? p.getName() + "'s Name Colour" : "Your Name Colour";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blankbl,blankbl,blankbl,blankbl,null,blankbl,blankbl,blankbl,blankbl,
				blankbl,null,null,null,null,null,null,null,blankbl,
				blankbl,null,null,null,null,null,null,null,blankbl,
				blankbl,null,null,null,null,null,null,null,blankbl,
				blankbl,blankbl,blankbl,blankbl,null,blankbl,blankbl,blankbl,blankbl,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		if (e.getCurrentItem().getType() != Material.LEATHER_CHESTPLATE) return;
		LeatherArmorMeta meta = (LeatherArmorMeta) e.getCurrentItem().getItemMeta();
		if (!pp.isOverridingProfile() && tpp.onCooldown("nameColour")) { // Cooldown
			p.sendActionBar(Component.text("\u00a7cYou'll confuse people if you change your name colour that fast!"));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
		} else if (e.getRawSlot() == 34) { // Custom
			if (!pp.hasPermission(Permission.NAMECOLOUR_CUSTOM)) { // If no access to custom colour
				p.sendActionBar(Component.text("\u00a7cJust before you could apply the colour, your hands melt."));
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				return;
			}
			
			close();
			
			SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","\u00a78^^^^^^^^^^", "\u00a7fEnter HEX Value for your new", "\u00a7fName Colour (eg. #ff33af)!"), Material.OAK_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	String sCol = strings[0];
                	if (!sCol.startsWith("#"))
                		sCol = "#" + sCol;
                	
                	int col = Integer.decode(strings[0]);
                	NamedTextColor baseCols = NamedTextColor.nearestTo(TextColor.color(col));
                	
                	if (!pp.hasPermission(Permission.NAMECOLOUR_OVERRIDE))
                		if (baseCols == NamedTextColor.AQUA || baseCols == NamedTextColor.BLACK || baseCols == NamedTextColor.BLUE || baseCols == NamedTextColor.DARK_BLUE)
                			throw new RuntimeException("Colour is too close to a shade of blue or black.");
                	
                	// Sync open
                	Bukkit.getScheduler().runTaskLater(plugin, () -> showConfirmation(col), 1L);
                } catch (RuntimeException ex) {
                	if (ex.getMessage() != null)
                		p.sendActionBar(Component.text("\u00a7c" + ex.getMessage()));
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
                	Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiPlayerLoginMessage(p).openInventory(), 1L);
                }
                return true;
            });
			menu.open(p);
			return;
		} else if (meta.getColor().asRGB() == lockedCol) { // Locked
			p.sendActionBar(Component.text("\u00a7cJust before you could apply the colour, your hands melt."));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
		} else if (meta.getColor().asRGB() == tpp.getNameColour().value()) { // Same Colour
			p.sendActionBar(Component.text("\u00a7cWhy re-apply the same coat of colour? You numpty!"));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
		} else { // Rank Colour Selected
			showConfirmation(meta.getColor().asRGB());
		}
	}
	
	private void showConfirmation(int col) {
		ArrayList<Component> confirmationLore = new ArrayList<Component>();
		confirmationLore.add(Component.text("\u00a77Confirm to change ").append(pp.isOverridingProfile() ? tpp.getColouredName().append(Component.text("\u00a77's")) : Component.text("\u00a77your")).append(Component.text("\u00a77 name colour to:")));
		confirmationLore.add(Component.text(tpp.getDisplayName(), TextColor.color(col)));
		final boolean enactCd = !pp.isOverridingProfile() && !tpp.hasPermission(Permission.BYPASS_COOLDOWNS);
		if (enactCd) {
			confirmationLore.add(Component.empty());
			confirmationLore.add(Component.text("\u00a7cThere is a 30 minute cooldown"));
			confirmationLore.add(Component.text("\u00a7cbetween name colour changes."));
		}
		
		BeanGuiConfirm confirm = new BeanGuiConfirm(p, confirmationLore) {
			@Override
			public void onAccept() {
				tpp.setNameColour(col);
				Celestia.logModify(pp.getId(), "Changed %ID"+tpp.getId()+"'s Name Colour to " + Long.toHexString(tpp.getNameColour().value()));
				if (enactCd)
					tpp.addCooldown("nameColour", 1000 * 60 * 30);
				new BeanGuiPlayerLoginMessage(p).openInventory();
			}

			@Override
			public void onDecline() {
				new BeanGuiPlayerLoginMessage(p).openInventory();
			}
		};
		confirm.openInventory();
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		contents[4] = newItem(tpp.getSkull(), tpp.getColouredName(), 
				Component.text("\u00a77Colour: #").append(Component.text(Long.toHexString(tpp.getNameColour().value()), tpp.getNameColour())).decoration(TextDecoration.ITALIC, false));
		
		boolean hasListed = false;
		
		// Rank Colours
		final boolean pOverride = pp.hasPermission(Permission.NAMECOLOUR_OVERRIDE);
		final boolean pRankCols = pOverride || pp.hasPermission(Permission.NAMECOLOUR_RANKS);
		final boolean pRankStaffCols = pOverride || pp.hasPermission(Permission.NAMECOLOUR_STAFFRANKS);
		
		final Rank[] ranks = Rank.values();
		for (int x = -1; ++x < ranks.length;) {
			final Rank rank = ranks[x];
			if (rank.isStaffRank() && !(pRankStaffCols || tpp.isRank(rank))) continue; // Hide staff ranks from those who aren't those specific ranks
			final boolean isUnlocked = pRankCols || tpp.isRank(rank);
			final boolean isColour = tpp.getNameColour().value() == rank.getRankHex();
			final int row = x / 7;
			
			ItemStack chestCol = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta meta = (LeatherArmorMeta) chestCol.getItemMeta();
			meta.displayName(isUnlocked ? rank.toComponent() : rank.toComponent().color(NamedTextColor.GRAY).append(Component.text("\u00a78 (Unavailable)")));
			
			ArrayList<Component> lore = new ArrayList<Component>();
			lore.add(Component.text("\u00a77Colour: #")
					.append(Component.text(Long.toHexString(rank.getRankHex())).color(rank.getRankColour()).decoration(TextDecoration.ITALIC, false)));
			lore.add(Component.text("\u00a77Preview: ")
					.append(Component.text(tpp.getDisplayName()).color(rank.getRankColour()).decoration(TextDecoration.ITALIC, false)));
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_DYE);
			if (isColour) {
				meta.addEnchant(Enchantment.DURABILITY, 1, false);
				hasListed = true;
			}
				
			if (isUnlocked) {
				meta.setColor(Color.fromRGB(rank.getRankHex()));
				lore.add(Component.empty());
				lore.add(Component.text(isColour ? "\u00a7aCurrently Selected!" : "\u00a78Click to Change Colour"));
			} else {
				meta.setColor(Color.fromRGB(lockedCol));
				lore.add(Component.empty());
				lore.add(Component.text("\u00a78\u00a7oRequires the " + rank.lowerName() + " rank"));
			}
			
			meta.lore(lore);
			chestCol.setItemMeta(meta);
			
			contents[10 + x + (2 * row)] = chestCol;
		}
		
		// Cooldown Item
		if (!pp.hasPermission(Permission.BYPASS_COOLDOWNS))
			contents[40] = newItem(new ItemStack(Material.CLOCK), Component.text("\u00a77There is a 30 minute cooldown"), Component.text("\u00a77between changing name colours!"));
		else
			contents[40] = newItem(new ItemStack(Material.CLOCK), Component.text("\u00a77You don't have a cooldown"), Component.text("\u00a77when changing name colours!"));
		
		// Custom Colour
		final boolean isUnlocked = pOverride || pp.hasPermission(Permission.NAMECOLOUR_CUSTOM);
		
		if (!isUnlocked && hasListed) return;
		
		ItemStack chestCol = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) chestCol.getItemMeta();
		meta.displayName(Component.text("Custom Colour").color(tpp.getNameColour()).decoration(TextDecoration.ITALIC, false)
				.append(Component.text(!isUnlocked ? "\u00a78 (Unavailable)" : "")));
		ArrayList<Component> lore = new ArrayList<Component>();
		
		if (!isUnlocked) { // Not Unlocked
			if (!hasListed) { // Used but Not Unlocked, likely due to being special
				lore.add(Component.text("\u00a77Current Colour: #")
						.append(Component.text(Long.toHexString(tpp.getNameColour().value()), tpp.getNameColour()).decoration(TextDecoration.ITALIC, false)));
			}
			meta.setColor(Color.fromRGB(lockedCol));
		} else if (!hasListed) { // Unlocked and Used
			lore.add(Component.text("\u00a77Current Colour: #")
					.append(Component.text(Long.toHexString(tpp.getNameColour().value()), tpp.getNameColour()).decoration(TextDecoration.ITALIC, false)));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a78Click to Change"));
			meta.setColor(Color.fromRGB(tpp.getNameColour().value()));
			meta.addEnchant(Enchantment.DURABILITY, 1, false);
		} else { // Unlocked but not used
			lore.add(Component.text("\u00a77Click to set a \u00a7fCustom Name Colour"));
			if (!pOverride) {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a78Note: Custom Name Colours cannot be"));
				lore.add(Component.text("\u00a78similar to shades of blue or black."));
			}
			meta.setColor(Color.fromRGB(tpp.getNameColour().value()));
		}
		
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES,ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_DYE);
		meta.lore(lore);
		
		chestCol.setItemMeta(meta);
		
		contents[34] = chestCol;
		
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
			new BeanGuiPlayer(p).openInventory();
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
