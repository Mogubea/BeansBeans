package me.playground.gui;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.gui.staff.BeanGuiStaff;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import me.playground.utils.Calendar;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiMainMenu extends BeanGui {
	
	private static final ItemStack skull_S = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkOWQ5NmMzOTNlMzhhMzZiNjFhYTNjODU5ZWRlNWViNzQ0ZWYxZTg0NmQ0ZjdkMGVjYmQ2NTg4YTAyMSJ9fX0="), "\u00a7aGamemode: \u00a72Survival", "\u00a77\u00a7oChange Gamemode to Creative!");
	private static final ItemStack skull_C = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI2ZmIxNjlkM2Q3Yjg0OWYwYmI5YTcwZWI3YjQ4MWQ2ZjcxZTk3YzhlY2IxMzU0NmVjOTcyMzgxNDkxNyJ9fX0="), "\u00a7aGamemode: \u00a79Creative", "\u00a77\u00a7oChange Gamemode to Survival!");
	private static final ItemStack skull_Goff = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThjMzM2ZGVkZmUxOTdiNDM0YjVhYjY3OTg4Y2JlOWMyYzlmMjg1ZWMxODcxZmRkMWJhNDM0ODU1YiJ9fX0="), "\u00a7eGod Mode: \u00a7cDisabled");
	private static final ItemStack skull_Gon = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1NzViNTU3N2NjYjMyZTQyZDU0MzA0YTFlZjVmMjNhZDZiYWQ1YTM0NTYzNDBhNDkxMmE2MmIzNzk3YmI1In19fQ=="), "\u00a7eGod Mode: \u00a7aEnabled");
	private static final ItemStack skull_Foff = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ3MTRiYWZiMGI1YWI5Y2ZhN2RiMDJlZmM4OTI3YWVkMWVmMjk3OTdhNTk1ZGEwNjZlZmM1YzNlZmRjOSJ9fX0="), "\u00a7eFlying: \u00a7cDisabled");
	private static final ItemStack skull_Fon = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzBkYzk0MjBjMTRmY2FiOThkY2Q2ZjVhZDUxZThlYmUyYmI5Nzg5NTk3NmNhYTcwNTc4ZjczYzY2ZGZiZCJ9fX0="), "\u00a7eFlying: \u00a7aEnabled");
	
	private static final ItemStack icon_wardrobe = newItem(new ItemStack(Material.LEATHER_CHESTPLATE), "\u00a7cWardrobe", "\u00a77\u00a7oStore and swap your armour!");
	private static final ItemStack icon_skills = newItem(new ItemStack(Material.GOLDEN_PICKAXE), "\u00a76Your Skills", "\u00a77\u00a7oView your skills!");
	private static final ItemStack icon_settings = newItem(new ItemStack(Material.REDSTONE_TORCH), "\u00a7cYour Settings", "\u00a77\u00a7oEdit your settings.");
	private static final ItemStack icon_time = newItem(new ItemStack(Material.CLOCK), "\u00a7aServer Time", "\u00a770:00AM");
	private static final ItemStack icon_home = newItem(new ItemStack(Material.DARK_OAK_DOOR), "\u00a7b/home", "\u00a77\u00a7oTeleport to your /sethome!");
	private static final ItemStack icon_nohome = newItem(new ItemStack(Material.IRON_DOOR), "\u00a7c/home", "\u00a77\u00a7oYou haven't used /sethome yet!");
	private static final ItemStack icon_shome = newItem(new ItemStack(Material.RED_BED), "\u00a7b/shome", "\u00a77\u00a7oTeleport to your natural spawn point!");
	private static final ItemStack icon_warps = newItem(new ItemStack(Material.PURPLE_CARPET), "\u00a7dWarp Menu", "\u00a77\u00a7oTest warp menu?");
	private static final ItemStack icon_echest = newItem(new ItemStack(Material.ENDER_CHEST), "\u00a75Ender Chest", "\u00a77\u00a7oOpen your Ender Chest!");
	private static final ItemStack icon_blacklist = newItem(new ItemStack(Material.HOPPER_MINECART), "\u00a78Pickup Blacklist", "\u00a77\u00a7oWhich items do you absolutely", "\u00a77\u00a7orefuse to pick up?");
	private static final ItemStack icon_commands = newItem(new ItemStack(Material.WRITABLE_BOOK), "\u00a7aCommands", "\u00a77\u00a7oView the list of commands!");
	private static final ItemStack icon_news = newItem(new ItemStack(Material.ENCHANTED_BOOK), Component.text("Server News").color(TextColor.color(0x994411)), "\u00a77\u00a7oUpdates & Announcements");
	private static final ItemStack icon_region = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYxN2E2YTlhZmFhN2IwN2U0MjE4ZmU1NTVmMjgyM2IwMjg0Y2Q2OWI0OWI2OWI5N2ZhZTY3ZWIyOTc2M2IifX19"), Component.text("Region Menu").color(BeanColor.REGION), "\u00a77\u00a7oWIP");
	private static final ItemStack icon_heirlooms = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE3NmE0YzQ0NmI1NGQ1MGFlM2U1YmE4ZmU2ZjQxMzE3Njg5ZmY1YTc1MjMwMjgwOTdmNjExOTUzZDFkMTI5NyJ9fX0="), Component.text("Bag o' Heirlooms").color(BeanColor.HEIRLOOM), "\u00a77\u00a7oWIP");
	private static final ItemStack icon_bestiary = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("Bestiary", BeanColor.BESTIARY), "\u00a77\u00a7oWIP");
	private static final ItemStack icon_support = newItem(new ItemStack(Material.LIGHT_BLUE_CANDLE), Component.text("Support Us!", BeanColor.SAPPHIRE), "\u00a77\u00a7oHow to support the server..");
	
	private static final ItemStack icon_resetOverride = newItem(new ItemStack(Material.BARRIER), "\u00a7cReset GUI Override", "\u00a77Go back to normal!");
	
	public BeanGuiMainMenu(Player p) {
		super(p);
		
		this.name = "Main Menu";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,blank,blank,blank,blank,blank,blank,
				blank,blank,blank,null,null,null,blank,blank,blank,
				blank,blank,null,null,null,null,null,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				blank,blank,blank,blank,blank,blank,blank,blank,blank,
				blank,blank,blank,null,null,null,blank,icon_support,blank,
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = presetInv.clone();
		
		contents[0] = icon_news;
		
		if (isOverrideView())
			contents[1] = icon_resetOverride;
		
		if (pp.getPlayer().hasPermission("bean.gm.creative"))
			contents[8] = pp.getPlayer().getGameMode() != GameMode.SURVIVAL ? skull_C : skull_S;
		if (pp.getPlayer().hasPermission("bean.cmd.god"))
			contents[17] = pp.getPlayer().isInvulnerable() ? skull_Gon : skull_Goff;
		if (pp.getPlayer().hasPermission("bean.cmd.fly"))
			contents[26] = pp.getPlayer().getAllowFlight() ? skull_Fon : skull_Foff;
		
		contents[13] = newItem(tpp.getSkull(), tpp.getColouredName(), 
				Component.text("\u00a77Rank: ").append(tpp.getHighestRank().toComponent()).append(tpp.getDonorRank() != null ? Component.text("\u00a77 (").append(tpp.getDonorRank().toComponent()).append(Component.text("\u00a77)")) : Component.empty()),
				Component.text("\u00a77Wallet: "),
				Component.text("\u00a77 - \u00a76" + df.format(tpp.getBalance()) + " Coins"),
				Component.text("\u00a77 - ").append(Component.text(tpp.getSapphire() + " Sapphire", BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false)),
				Component.empty(),
				Component.text("\u00a78Click to modify your profile!"));
		
		if (pp.isRank(Rank.MODERATOR)) // pp since it doesn't care if overriding or not
			contents[14] = newItem(new ItemStack(Material.ANVIL), Component.text("Temp Staff Menu"));
		
		contents[20] = icon_skills;
		
		ItemStack region = icon_region.clone();
		TextColor regionCol = TextColor.color(TextColor.color(0x3d3d3d | BeanColor.REGION.value()));
		ArrayList<Component> regionLore = new ArrayList<Component>();
		
		if (getPlugin().regionManager().getRegions(p.getLocation()).size() < 1)
			regionLore.add(Component.text("\u00a7cNo region nearby.."));
		else
			regionLore.add(Component.text("Region: \u00a79" + getPlugin().regionManager().getRegion(p.getLocation()).getName()).colorIfAbsent(regionCol).decoration(TextDecoration.ITALIC, false));
		
		regionLore.addAll(Arrays.asList(
				Component.empty(), 
				Component.text("\u00a77\u00a7oView and/or Edit attributes"), 
				Component.text("\u00a77\u00a7oabout nearby Regions.")));
		
		region.lore(regionLore);
		
		contents[21] = region;
		
		contents[23] = icon_warps;
		contents[24] = icon_wardrobe;
		contents[28] = isOverrideView() ? icon_echest : null;
		
		ItemStack waa = icon_heirlooms.clone();
		ItemMeta waameta = waa.getItemMeta();
		TextColor aa = TextColor.color(TextColor.color(0x3d3d3d | BeanColor.HEIRLOOM.value()));
		
		// TODO: make better
		waameta.lore(Arrays.asList(
				Component.text("Using \u00a7f" + tpp.getHeirlooms().size() + "\u00a7r/\u00a77" + tpp.getHeirlooms().getMaxHeirlooms() + "\u00a7r Slots").colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false), 
				Component.text(""),
				Component.text("\u00a77\u00a7oStore and utilise the effects of"),
				Component.text("\u00a77\u00a7oyour Heirlooms in this magical bag."),
				Component.text(""),
				Component.text("Stat Modifiers:", BeanColor.HEIRLOOM).decoration(TextDecoration.ITALIC, false),
				Component.text(tpp.getHeirlooms().getLuckBonus() == 0 ? "\u00a78\u25C8 Luck: 0" : "\u00a7r\u25C8 Luck: \u00a7f" + (tpp.getHeirlooms().getLuckBonus())).colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false),
				Component.text(tpp.getHeirlooms().getDamageBonus() == 0 ? "\u00a78\u25C8 Damage: 0" : "\u00a7r\u25C8 Damage: \u00a7f" + (tpp.getHeirlooms().getDamageBonus())).colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false),
				Component.text(tpp.getHeirlooms().getHealthBonus() == 0 ? "\u00a78\u25C8 Health: 0" : "\u00a7r\u25C8 Health: \u00a7f" + (tpp.getHeirlooms().getHealthBonus())).colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false),
				Component.text(tpp.getHeirlooms().getMovementBonus() == 0 ? "\u00a78\u25C8 Movement Speed: 0" : "\u00a7r\u25C8 Movement Speed: \u00a7f" + (int)(tpp.getHeirlooms().getMovementBonus() * 1000)).colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false)));
		int heirloomCount = tpp.getHeirlooms().size();
		if (heirloomCount > 0)
			waa.setAmount(Math.min(64, heirloomCount));
		waa.setItemMeta(waameta);
		//waa.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		
		contents[29] = waa;
		contents[30] = icon_bestiary;
		contents[32] = icon_commands;
		
		ItemStack blacklist = icon_blacklist.clone();
		TextColor blacklistCol = TextColor.color(TextColor.color(0x3d3d3d | NamedTextColor.DARK_GRAY.value()));
		blacklist.lore(Arrays.asList(
				Component.text("Using \u00a7f" + tpp.getPickupBlacklist().size() + "\u00a7r/\u00a77135\u00a7r Slots").colorIfAbsent(blacklistCol).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("\u00a77\u00a7oWhich items do you absolutely"),
				Component.text("\u00a77\u00a7orefuse to pick up?")));
		
		contents[33] = blacklist;
		contents[34] = icon_settings;
		
		ItemStack time = icon_time.clone();
		ItemMeta timee = time.getItemMeta();
		final World w = Bukkit.getWorlds().get(0);
		final int timeee = Calendar.getTime(w);
		timee.lore(Arrays.asList(Component.text("\u00a77" + Calendar.getTimeString(timeee, true) + " \u00a78(Day "+Calendar.getDay(w.getFullTime())+")")));
		time.setItemMeta(timee);
		
		contents[48] = tpp.getHome() != null ? icon_home : icon_nohome;
		contents[49] = time;
		contents[50] = icon_shome;
		
		i.setContents(contents);
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		e.setCancelled(true);
		
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		final boolean override = PlayerProfile.from(p).isOverridingProfile();
		
		switch (slot) {
		case 0: // News Button
			new BeanGuiNews(p).openInventory();
			break;
		case 1: // Cancel Override Button
			if (override)
				pp.clearOverridingProfile();
			break;
		case 8: // Gamemode Button
			if (p.hasPermission("bean.gm.creative"))
				p.setGameMode(p.getGameMode() != GameMode.SURVIVAL ? GameMode.SURVIVAL : GameMode.CREATIVE);
			break;
		case 13: // Player Menu Button
			new BeanGuiPlayer(p).openInventory();
			return;
		case 14: // TODO: TEMP STAFF MENU BUTTON
			new BeanGuiStaff(p).openInventory();
			return;
		case 17: // Godmode Button
			if (p.hasPermission("bean.cmd.god"))
				p.setInvulnerable(!p.isInvulnerable());
			break;
		case 20: // Skills Button
			new BeanGuiSkills(p).openInventory();
			return;
		case 21: // Region Button
			new BeanGuiRegionMain(p).openInventory();
			return;
		case 23: // Warps Button
			new BeanGuiWarps(p).openInventory();
			return;
		case 24: // Wardrobe Button
			if (tpp.isOnline())
				new BeanGuiWardrobe(p).openInventory();
			return;
		case 26: // Flight Button
			if (p.hasPermission("bean.cmd.fly"))
				p.setAllowFlight(!p.getAllowFlight());
			break;
		case 28: // Enderchest Button
			if (p.hasPermission("bean.cmd.enderchest")) // TEMP
				p.openInventory(p.getEnderChest());
			return;
		case 29: // Heirloom Button
			new BeanGuiHeirlooms(p).openInventory();
			return;
		case 30: // Bestiary Button
			new BeanGuiBestiaryEntity(p).openInventory();
			return;
		case 32: // Command Button
			new BeanGuiCommands(p).openInventory();
			return;
		case 33: // Blacklist Button
			new BeanGuiPickupfilter(p).openInventory();
			return;
		case 34: // Settings Button
			new BeanGuiSettings(p).openInventory();
			return;
		case 48: // Home Button
			if (tpp.getHome() != null) {
				p.closeInventory();
				p.teleport(tpp.getHome(), TeleportCause.COMMAND);
				return;
			}
			break;
		case 50: // Spawnpoint Button
			p.closeInventory();
			p.teleport(tpp.getOfflinePlayer().getBedSpawnLocation(), TeleportCause.COMMAND);
			return;
		case 52: // Support Us! Button
			new BeanGuiSupportUs(p).openInventory();
			return;
		default:
			return;
		}
		
		onInventoryOpened();
	}

}
