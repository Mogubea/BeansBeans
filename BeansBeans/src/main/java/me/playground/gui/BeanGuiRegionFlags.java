package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.regions.flags.Flag;
import me.playground.regions.flags.FlagBoolean;
import me.playground.regions.flags.FlagFloat;
import me.playground.regions.flags.FlagMember;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import me.playground.utils.SignMenuFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiRegionFlags extends BeanGuiRegion {
	
	private static final HashMap<Integer, Flag<?>> slotToFlag = new HashMap<Integer, Flag<?>>();
	
	static {
		final List<Flag<?>> flags = Flags.getRegisteredFlags();
		final int size = flags.size();
		for (int x = -1; ++x < size;) {
			final Flag<?> flag = flags.get(x);
			final int slot = 1 + ((1 + (x / 7)) * 9) + (x % 7);
			
			slotToFlag.put(slot, flag);
		}
	}
	
	public BeanGuiRegionFlags(Player p) {
		super(p);
		
		this.name = "Regions -> Flags";
		this.presetInv = new ItemStack[] {
				rBlank,rBlank,blank,blank,null,blank,blank,rBlank,rBlank,
				rBlank,null,null,null,null,null,null,null,rBlank,
				blank,null,null,null,null,null,null,null,blank,
				blank,null,null,null,null,null,null,null,blank,
				blank,null,null,null,null,null,null,null,blank,
				rBlank,rBlank,rBlank,rBlank,goBack,rBlank,rBlank,rBlank,rBlank
		};
	}
	
	protected BeanGuiRegionFlags(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot < 10 || slot > 44) return;
		if (!getRegion().canModify(p)) return;
		
		Flag<?> flag = slotToFlag.get(slot);
		if (flag == null) return;
		
		// Reset flag
		if (e.isRightClick()) {
			if (getRegion().getFlag(flag) == null) return;
			getRegion().setFlag(flag, null);
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
		// Edit flag
		} else {
			// Boolean Flag
			if (flag instanceof FlagBoolean) {
				boolean setting = getRegion().getEffectiveFlag((FlagBoolean)flag);
				getRegion().setFlag(flag, !setting);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
			// Member Flag
			} else if (flag instanceof FlagMember) {
				new BeanGuiRegionFlagMember(p, regionIdx, (FlagMember)flag).openInventory();
				return;
			// Float Flag
			} else if (flag instanceof FlagFloat) {
				p.closeInventory();
				
				SignMenuFactory.Menu menu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7bValue for Flag", "\u00a7b" + flag.getDisplayName()), Material.WARPED_WALL_SIGN)
	            .reopenIfFail(true)
	            .response((player, strings) -> {
	                try {
	                	float newAmount = Float.parseFloat(strings[0]);
	                	getRegion().setFlag(flag, newAmount);
	                	
	                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
	                } catch (RuntimeException ex) {
	                	p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
	                }
	            	
	                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiRegionFlags(p, regionIdx).openInventory(), 1L);
	                return true;
	            });
				menu.open(p);
				return;
			} else {
				return;
			}
		}
		
		ItemStack[] contents = e.getInventory().getContents();
		contents[slot] = stackForFlag(flag);
		e.getInventory().setContents(contents);
	}
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		
		slotToFlag.forEach((slot, flag) -> {
			if (flag.needsPermission() && !p.hasPermission(flag.getPermission())) return;
			contents[slot] = stackForFlag(flag);
		});
		
		i.setContents(contents);
		super.onInventoryOpened();
	}
	
	private ItemStack stackForFlag(Flag<?> flag) {
		ItemStack item = new ItemStack(Material.CHAINMAIL_HELMET);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> lore = new ArrayList<Component>();
		
		final boolean isInherited = getRegion().getFlag(flag) == null;
		meta.displayName(Component.text(flag.getDisplayName() + (isInherited ? "\u00a77 (default)" : "")).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		
		if (flag instanceof FlagMember) {
			MemberLevel level = getRegion().getEffectiveFlag((FlagMember)flag);
			switch(level) {
			case MASTER: item.setType(Material.BARRIER); break;
			case OWNER: item.setType(Material.NETHERITE_HELMET); break;
			case OFFICER: item.setType(Material.DIAMOND_HELMET); break;
			case TRUSTED: item.setType(Material.GOLDEN_HELMET); break;
			case MEMBER: item.setType(Material.IRON_HELMET); break;
			case VISITOR: item.setType(Material.LEATHER_HELMET); break;
			case NONE: item.setType(Material.TURTLE_HELMET); break;
			}
			lore.add(Component.text("Level: \u00a7f" + level.toString()).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		} else if (flag instanceof FlagBoolean) {
			boolean setting = getRegion().getEffectiveFlag((FlagBoolean)flag);
			item.setType(setting ? Material.LIGHT_BLUE_DYE : Material.GRAY_DYE);
			lore.add(Component.text("Allow: " + (setting ? "\u00a7a" : "\u00a7c") + setting).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		} else if (flag instanceof FlagFloat) {
			float setting = getRegion().getEffectiveFlag((FlagFloat)flag);
			item.setType(Material.WARPED_SIGN);
			lore.add(Component.text("Value: \u00a7f" + setting).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		}
		
		lore.add(Component.empty());
		lore.addAll(flag.getDescription());
		
		meta.lore(lore);
		item.setItemMeta(meta);
		if (!isInherited)
			item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		return item;
	}
	
}
