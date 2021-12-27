package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

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

import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.BeanColor;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import me.playground.warps.Warp;
import me.playground.warps.WarpType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiWarps extends BeanGui {
	private static final ItemStack[] panes = {
			newItem(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1), Component.text("\u00a75Your Warp Screen")),
			newItem(new ItemStack(Material.PINK_STAINED_GLASS_PANE, 1), Component.text("Public Warps Screen").color(TextColor.color(0xff66bb))),
			newItem(new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1), Component.text("\u00a7dInvited Warps Screen")),
			newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), Component.text("\u00a7cAll Warps Screen")),
	};
	
	private final static ItemStack warpSkull = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7ImlkIjoiMTkwM2NhNWE3MjgzNDExODk5NjMwYTY5OTM3MTY3NmMiLCJ0eXBlIjoiU0tJTiIsInVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSIsInByb2ZpbGVJZCI6IjgwMThhYjAwYjJhZTQ0Y2FhYzliZjYwZWY5MGY0NWU1IiwidGV4dHVyZUlkIjoiMmM5MTVkYjNmYzQwYTc5YjYzYzJjNDUzZjBjNDkwOTgxZTUyMjdjNTAyNzUwMTI4MzI3MjEzODUzM2RlYTUxOSJ9fSwic2tpbiI6eyJpZCI6IjE5MDNjYTVhNzI4MzQxMTg5OTYzMGE2OTkzNzE2NzZjIiwidHlwZSI6IlNLSU4iLCJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkiLCJwcm9maWxlSWQiOiI4MDE4YWIwMGIyYWU0NGNhYWM5YmY2MGVmOTBmNDVlNSIsInRleHR1cmVJZCI6IjJjOTE1ZGIzZmM0MGE3OWI2M2MyYzQ1M2YwYzQ5MDk4MWU1MjI3YzUwMjc1MDEyODMyNzIxMzg1MzNkZWE1MTkifSwiY2FwZSI6bnVsbH0=");
	private final static ItemStack shopSkull = Utils.getSkullWithCustomSkin(UUID.fromString("6145234d-06a3-402d-a4a0-68787735fbfa"), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=");
	
	private final static ItemStack wD_Pri = newItem(new ItemStack(Material.IRON_DOOR, 1), Component.text("\u00a75Your Warps"), "\u00a77View a list of warps you've made!");
	private final static ItemStack wD_Pub = newItem(new ItemStack(Material.OAK_DOOR, 1), Component.text("Public Warps").color(TextColor.color(0xff66bb)), "\u00a77View a list of public warps!");
	private final static ItemStack wD_Inv = newItem(new ItemStack(Material.DARK_OAK_DOOR, 1), Component.text("\u00a7dInvited Warps"), "\u00a77View a list of private warps that", "\u00a77you've been invited to!");
	private final static ItemStack wD_All = newItem(new ItemStack(Material.CRIMSON_DOOR, 1), Component.text("\u00a7cAll Warps"), "\u00a77View all the warps ever made!");
	
	private final static ItemStack wF_Name = newItem(new ItemStack(Material.CRIMSON_SIGN), Component.text("\u00a77Name Filter: \u00a7fNone"), "", "\u00a78\u00a7oClick to filter warps by Name");
	private final static ItemStack wF_Filter = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY0YzBiYWFlYTM5NDU4NjQwNWUxNTU3ZjU3ZTUwNTlmNGQ0YjAzYmYwN2FhMmJhMGYyMDkzODQ3MWQyNzFhYiJ9fX0="), Component.text("\u00a77Type Filter: \u00a7fNone"), "", "\u00a7f > \u00a77None", "\u00a78> Player Warp", "\u00a78> Shop Warp", "\u00a78> Server Warp", "\u00a78\u00a7oClick to Cycle through Warp Types");
	
	private final Map<String, Warp> warps = getPlugin().warpManager().getWarps();
	private String nameFilter = null;
	private WarpType typeFilter = null;
	
	public BeanGuiWarps(Player p) {
		super(p);
		
		this.name = "Settings";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				panes[0],panes[0],wD_Pri,panes[0],wD_Pub,panes[0],wD_Inv,panes[0],panes[0],
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				panes[0],panes[0],panes[0],wF_Name,goBack,wF_Filter,panes[0],panes[0],panes[0],
		};
	}
	
	public BeanGuiWarps(Player p, String name, WarpType type) {
		this(p);
		this.nameFilter = (name == null || name.isEmpty()) ? null : name;
		this.typeFilter = type;
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		if (pp.onCdElseAdd("warpGUI", 500))
			return;
		
		if (slot == 48) {
			if (e.isRightClick()) {
				this.nameFilter = null;
			} else {
				final WarpType wt = typeFilter;
				p.closeInventory();
				SignMenuFactory.Menu menuu = plugin.getSignMenuFactory().newMenu(Arrays.asList("","^^^^^^^^^^", "\u00a7d\u00a7lWarp Name", "\u00a7dto filter for"), Material.CRIMSON_WALL_SIGN)
	            .reopenIfFail(true)
	            .response((player, strings) -> {
	                Bukkit.getScheduler().runTaskLater(plugin, () -> new BeanGuiWarps(p, strings[0], wt).openInventory(), 1L);
	                return true;
	            });
				menuu.open(p);
				return;
			}
		} else if (slot == 50) {
			if (e.isRightClick()) {
				this.typeFilter = null;
			} else {
				if (this.typeFilter == null)
					this.typeFilter = WarpType.PLAYER;
				else if (this.typeFilter == WarpType.PLAYER)
					this.typeFilter = WarpType.SHOP;
				else if (this.typeFilter == WarpType.SHOP)
					this.typeFilter = WarpType.SERVER;
				else if (this.typeFilter == WarpType.SERVER)
					this.typeFilter = null;
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.35F, 1.0F);
			}
		} else if (slot < 36 && slot > 8) {
			String itemName = ((TextComponent)e.getCurrentItem().getItemMeta().displayName()).content();
			p.performCommand("warp " + itemName);
		} else {
			int[] door = {2,4,6};
			if (p.hasPermission("bean.cmd.warp.others"))
				door = new int[]{1,3,5,7};
			
			for (int x = 0; x < door.length; x++)
				if (data != x && slot == door[x]) {
					setData(x);
					setPage(0);
					onInventoryOpened();
					return;
				}
			return;
		}
		
		setPage(0);
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		
		final ArrayList<Warp> warpss = new ArrayList<Warp>();
		int[] door = {2,4,6};
		if (pp.hasPermission("bean.cmd.warp.*"))
			door = new int[]{1,3,5,7};
		
		for (int x = 0; x < 9; x++)
			contents[x] = panes[data];
		
		contents[door[0]] = wD_Pri.clone();
		contents[door[1]] = wD_Pub.clone();
		contents[door[2]] = wD_Inv.clone();
		if (door.length>3)
			contents[door[3]] = wD_All.clone();
		
		switch(data) {
		case 0:
			for (Warp w : warps.values())
				if (w.isOwner(pp.getId()) && isInFilter(w))
					warpss.add(w);
			contents[door[0]].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			contents[door[0]].addItemFlags(ItemFlag.HIDE_ENCHANTS);
			break;
		case 1:
			for (Warp w : warps.values())
				if (w.isPublic() && isInFilter(w))
					warpss.add(w);
			contents[door[1]].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			contents[door[1]].addItemFlags(ItemFlag.HIDE_ENCHANTS);
			break;
		case 3:
			if (p.hasPermission("bean.cmd.warp.others")) {
				for (Warp w : warps.values())
					if (isInFilter(w))
						warpss.add(w);
				contents[door[3]].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
				contents[door[3]].addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			break;
		default:
			for (Warp w : warps.values())
				if (w.isInvited(pp.getId()) && isInFilter(w))
					warpss.add(w);
			contents[door[2]].addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
			contents[door[2]].addItemFlags(ItemFlag.HIDE_ENCHANTS);
			break;
		}
		
		
		contents[45] = panes[data];
		contents[46] = panes[data];
		contents[47] = panes[data];
		contents[51] = panes[data];
		contents[52] = panes[data];
		contents[53] = panes[data];
		
		if (nameFilter != null) {
			contents[48] = newItem(wF_Name, Component.text("\u00a77Name Filter: \u00a7f" + nameFilter), "", "\u00a78\u00a7oRight click to clear!");
			contents[48].addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		} else {
			contents[48] = wF_Name;
		}
		
		if (typeFilter != null) {
			contents[50] = newItem(wF_Filter, Component.text("\u00a77Type Filter: " + typeFilter.getName()), "", "\u00a78> None", 
					(typeFilter == WarpType.PLAYER) ? "\u00a7f > " + typeFilter.getName() : "\u00a78> Player Warp", 
					(typeFilter == WarpType.SHOP) ? "\u00a7f > " + typeFilter.getName() : "\u00a78> Shop Warp", 
					(typeFilter == WarpType.SERVER) ? "\u00a7f > " + typeFilter.getName() : "\u00a78> Server Warp", 
					"\u00a78Click to Cycle through Warp Types");
			contents[50].addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		} else {
			contents[50] = wF_Filter;
		}
		
		
		for (int x = 9; x < 45; x++)
			contents[x] = null;
		
		
		contents[46] = page > 0 ? prevPage : panes[data];
		
		int warpCount = warpss.size();
		
		if (page < (Math.floor(warpCount / 36)))
			contents[52] = nextPage;
		
		for (int x = 0 + (page * 36); x < Math.min(warpCount, 36 * (page + 1)); x++) {
			Warp w = warpss.get(x);
			
			ItemStack settingItem = w.getItem() != null ? new ItemStack(w.getItem()) : (w.getType() == WarpType.SHOP ? new ItemStack(shopSkull) : new ItemStack(warpSkull));
			
			ItemMeta meta = settingItem.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS);
			meta.displayName(Component.text(w.getName()).color(w.getType().getColor()).decoration(TextDecoration.ITALIC, false));
			ArrayList<Component> lore = new ArrayList<Component>();
			if (data != 0 && w.getOwnerId() > 0)
				lore.add(Component.text("\u00a78\u00a7o(Created by ").append(PlayerProfile.getDisplayName(w.getCreatorId())).append(Component.text("\u00a78\u00a7o)")));
			if (w.getLocation().getWorld() != null) {
				lore.add(Component.text("World: ").color(TextColor.color(0xc02575)).decoration(TextDecoration.ITALIC, false)
						.append(Component.text(w.getLocation().getWorld().getName()).color(BeanColor.WORLD).decoration(TextDecoration.ITALIC, false)));
				lore.add(Component.text("X: \u00a7a" + w.getLocation().getBlockX() + "\u00a7r, Y: \u00a7a" + w.getLocation().getBlockY() + "\u00a7r, Z: \u00a7a" + w.getLocation().getBlockZ())
						.colorIfAbsent(TextColor.color(0xc02575)).decoration(TextDecoration.ITALIC, false));
				lore.add(Component.empty());
				if (data == 3)
					lore.add(Component.text(w.isPublic() ? "\u00a7aWarp is public!" : "\u00a7cWarp is private!"));
				lore.add(Component.text("\u00a76» \u00a7eClick to warp!"));
			} else {
				lore.add(Component.text("\u00a78Warp is Obstructed"));
			}
			
			meta.lore(lore);
			settingItem.setItemMeta(meta);
			
			contents[9 + x - (page * 36)] = settingItem;
		}
		
		i.setContents(contents);
	}
	
	private boolean isInFilter(Warp w) {
		if (nameFilter != null && !w.getName().toLowerCase().contains(nameFilter.toLowerCase()))
			return false;
		if (typeFilter != null && w.getType() != typeFilter)
			return false;
		return true;
	}
	
}
