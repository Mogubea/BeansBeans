package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class BeanGuiRegionMembers extends BeanGuiRegion {
	final List<MemberLevel> visibleLevels = Arrays.asList(MemberLevel.OWNER, MemberLevel.OFFICER, MemberLevel.TRUSTED, MemberLevel.MEMBER, MemberLevel.VISITOR);
	final Material[] levelIcons = {Material.NETHERITE_HELMET, Material.DIAMOND_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.LEATHER_HELMET};
	int[] page = {0,0,0,0,0};
	
	public BeanGuiRegionMembers(Player p) {
		super(p);
		
		this.name = "Regions -> Members";
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}
	
	protected BeanGuiRegionMembers(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		final MemberLevel myLevel = getRegion().getMember(p);
		
		switch(slot) {
		case 0: case 9: case 18: case 27: case 36:
			if (myLevel.lowerThan(MemberLevel.OFFICER)) return;
			
			int index = slot/9;
			final MemberLevel toAddLevel = visibleLevels.get(index);
			
			if (myLevel.lowerOrEqTo(toAddLevel)) return;
			
			p.closeInventory();
			
			SignMenuFactory.Menu menu = Main.getInstance().getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7aAdd as a", "\u00a7a" + toAddLevel.toString()), Material.WARPED_WALL_SIGN)
            .reopenIfFail(true)
            .response((player, strings) -> {
                try {
                	final ProfileStore ps = ProfileStore.from(strings[0], true);
                	
                	if (ps == null) {
                		p.sendActionBar(Component.text("\u00a7cCouldn't find player '" + strings[0] + "'!"));
                		throw new RuntimeException();
                	} else if (getRegion().isMember(ps.getDBID())) {
                		p.sendActionBar(ps.getColouredName().append(Component.text("\u00a7c is already part of this region!")));
                		throw new RuntimeException();
                	}
                	
                	getRegion().addMember(ps.getDBID(), toAddLevel);
                	refreshRegionViewers();
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 0.8F);
                } catch (RuntimeException ex) {
                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
                }
            	
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> new BeanGuiRegionMembers(p, regionIdx).openInventory(), 1L);
                return true;
            });
			menu.open(p);
			break;
		default: // Kick, Demote and Promote players
			if (myLevel.lowerThan(MemberLevel.OFFICER)) return;
			
			if (slot < 44 && slot % 9 > 0 && slot % 9 < 8) {
				index = Math.floorDiv(slot, 9);
				final boolean canPromote = index -1 > -1 && visibleLevels.get(index-1).lowerThan(myLevel);
				final boolean canDemote = visibleLevels.get(index).lowerThan(myLevel);
				final boolean isKick = e.isShiftClick() || visibleLevels.get(index) == MemberLevel.VISITOR;
				final int id = ProfileStore.from(((TextComponent)e.getInventory().getItem(slot).getItemMeta().displayName()).content(), false).getDBID();
				if (id < 1) return;
				
				if (canPromote && e.isLeftClick()) {
					getRegion().addMember(id, visibleLevels.get(index-1));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 0.8F);
					refreshRegionViewers();
				} else if (canDemote && e.isRightClick()) {
					if (!isKick) {
						getRegion().addMember(id, visibleLevels.get(index+1));
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
						refreshRegionViewers();
					} else {
						getRegion().removeMember(id);
						refreshRegionViewers();
					}
				}
			} else {
				return;
			}
			break;
		}
		
		resetPages();
		onInventoryOpened();
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		
		// Some of this is done this way for readability rather than optimisation, maybe improve it somehow in future.
		
		final MemberLevel myLevel = getRegion().getMember(p);
		
		// Count the quantities and place the player skulls
		final HashMap<MemberLevel, Integer> memberCounts = new HashMap<MemberLevel, Integer>(5);
		for (Entry<Integer, MemberLevel> ent : getRegion().getMembers().entrySet()) {
			final MemberLevel theirLevel = ent.getValue();
			memberCounts.put(theirLevel, memberCounts.getOrDefault(theirLevel, 0) + 1);
			
			final ProfileStore ps = ProfileStore.from(ent.getKey());
			final ItemStack peep = newItem(Utils.getSkullFromPlayer(ps.getUniqueId()), ps.getColouredName());
			final ItemMeta peepo = peep.getItemMeta();
			final ArrayList<Component> lore = new ArrayList<Component>();
			final int index = visibleLevels.indexOf(theirLevel);
			
			if (myLevel.higherThan(theirLevel) && myLevel.higherThan(MemberLevel.TRUSTED)) {
				final boolean canPromote = index -1 > -1 && visibleLevels.get(index-1).lowerThan(myLevel);
				final boolean canDemote = visibleLevels.get(index).lowerThan(myLevel);
				
				if (canPromote || canDemote)
					lore.add(Component.empty());
				if (canPromote)
					lore.add(Component.text("\u00a78\u00a7oLeft Click to \u00a72\u00a7oPromote \u00a78\u00a7oto " + visibleLevels.get(index-1).toString()));
				if (canDemote) {
					if (!(index+1 >= visibleLevels.size())) {
						lore.add(Component.text("\u00a78\u00a7oRight Click to \u00a7c\u00a7oDemote \u00a78\u00a7oto " + visibleLevels.get(index+1).toString()));
						lore.add(Component.text("\u00a78\u00a7oShift Right Click to \u00a74\u00a7oKick"));
					} else {
						lore.add(Component.text("\u00a78\u00a7oRight Click to \u00a74\u00a7oKick"));
					}
				}
			}
			
			peepo.lore(lore);
			peep.setItemMeta(peepo);
			contents[(index * 9) + (memberCounts.get(theirLevel)) - (getPage(index) * 7)] = peep;
		}
		
		for (int x = 0; x < 5; x++) {
			MemberLevel mem = visibleLevels.get(x);
			ArrayList<String> lore = new ArrayList<String>();
			lore.add("\u00a77Quantity: \u00a7f" + memberCounts.getOrDefault(mem, 0));
			lore.add("");
			
			if (mem == MemberLevel.OWNER)
				lore.addAll(Arrays.asList("\u00a7c* Can rename the region.", "\u00a7c* Can modify region flags."));
			
			if (mem.higherThan(MemberLevel.TRUSTED))
				lore.addAll(Arrays.asList("\u00a7c* Can promote, demote and kick any", "\u00a7c  member below the rank of " + visibleLevels.get(x).toString()));
			
			if (mem.higherThan(MemberLevel.VISITOR))
				lore.add("\u00a77* Bypasses outsider flags.");
			
			// Build Access basically gives access to everything.
			if (getRegion().getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(visibleLevels.get(x))) {
				lore.add("\u00a77* Has access to everything.");
			} else {
				if (getRegion().getEffectiveFlag(Flags.CROP_ACCESS).lowerOrEqTo(visibleLevels.get(x)))
					lore.add("\u00a77* Has access to crops.");
				if (getRegion().getEffectiveFlag(Flags.DOOR_ACCESS).lowerOrEqTo(visibleLevels.get(x)))
					lore.add("\u00a77* Has access to doors.");
				if (getRegion().getEffectiveFlag(Flags.CONTAINER_ACCESS).lowerOrEqTo(visibleLevels.get(x)))
					lore.add("\u00a77* Has access to containers.");
				if (getRegion().getEffectiveFlag(Flags.VILLAGER_ACCESS).lowerOrEqTo(visibleLevels.get(x)))
					lore.add("\u00a77* Has access to villager trading.");
			}
			
			if (myLevel.higherThan(mem) && myLevel.higherThan(MemberLevel.TRUSTED))
				lore.addAll(Arrays.asList("", "\u00a78\u00a7oLeft Click to invite."));
			
			contents[x * 9] = newItem(new ItemStack(levelIcons[x]), Component.text("\u00a7b"+visibleLevels.get(x).toString()+"\u00a77(s) of ").append(getRegion().getColouredName()), lore.toArray(new String[lore.size()]));
			if (myLevel == mem || myLevel == MemberLevel.MASTER && mem == MemberLevel.OWNER)
				contents[x * 9].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		}
		
		i.setContents(contents);
		super.onInventoryOpened();
	}
	
	private void resetPages() {
		this.page = new int[]{0,0,0,0,0};
	}
	
	private int getPage(int id) {
		return page[id];
	}
	
}
