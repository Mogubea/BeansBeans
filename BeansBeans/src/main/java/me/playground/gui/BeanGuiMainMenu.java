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

import me.playground.gui.player.BeanGuiPlayerMain;
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
	
	private static final ItemStack icon_wardrobe = newItem(new ItemStack(Material.LEATHER_CHESTPLATE), "\u00a7cWardrobe", "\u00a77Store and swap your armour!", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_skills = newItem(new ItemStack(Material.GOLDEN_PICKAXE), "\u00a76Your Skills", "\u00a77Skill Interface", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_home = newItem(new ItemStack(Material.DARK_OAK_DOOR), "\u00a7bWarp Home", "\u00a77Teleport to your /sethome!", "", "\u00a76» \u00a7eClick to warp!");
	private static final ItemStack icon_nohome = newItem(new ItemStack(Material.IRON_DOOR), "\u00a7cWarp Home", "\u00a77You haven't used /sethome yet!", "", "\u00a78» Cannot warp.");
	private static final ItemStack icon_shome = newItem(new ItemStack(Material.RED_BED), "\u00a7bWarp to Spawnpoint", "\u00a77Teleport to your natural spawn point!", "", "\u00a76» \u00a7eClick to warp!");
	private static final ItemStack icon_warps = newItem(new ItemStack(Material.PURPLE_CARPET), Component.text("Warps", BeanColor.WARP), "\u00a77\u00a7oStill kinda WIP warps", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_commands = newItem(new ItemStack(Material.WRITABLE_BOOK), Component.text("Server Commands", BeanColor.COMMAND), "\u00a77A list of commands you can use!", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_news = newItem(new ItemStack(Material.ENCHANTED_BOOK), Component.text("Server News", TextColor.color(0x994411)), "\u00a77Updates & Announcements", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_region = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGYxN2E2YTlhZmFhN2IwN2U0MjE4ZmU1NTVmMjgyM2IwMjg0Y2Q2OWI0OWI2OWI5N2ZhZTY3ZWIyOTc2M2IifX19"), Component.text("Region Menu").color(BeanColor.REGION), "\u00a77\u00a7oWIP");
	private static final ItemStack icon_heirlooms = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE3NmE0YzQ0NmI1NGQ1MGFlM2U1YmE4ZmU2ZjQxMzE3Njg5ZmY1YTc1MjMwMjgwOTdmNjExOTUzZDFkMTI5NyJ9fX0="), Component.text("Bag o' Heirlooms").color(BeanColor.HEIRLOOM), "\u00a77\u00a7oWIP");
	private static final ItemStack icon_bestiary = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("Bestiary", BeanColor.BESTIARY), "\u00a77\u00a7oWIP", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_support = newItem(new ItemStack(Material.CAKE), Component.text("Support Us!", BeanColor.SAPPHIRE), "\u00a77Ways of supporting the server!", "", "\u00a76» \u00a7eClick to open!");
	
	private static final ItemStack icon_resetOverride = newItem(new ItemStack(Material.BARRIER), "\u00a7cReset GUI Override", "\u00a77Go back to normal!");
	
	private static final ItemStack icon_echest = newItem(new ItemStack(Material.ENDER_CHEST), Component.text("Ender Chest", BeanColor.SAPPHIRE), "\u00a77ye", "", "\u00a76» \u00a7eClick to open!");
	private static final ItemStack icon_crafting = newItem(new ItemStack(Material.CRAFTING_TABLE), Component.text("Virtual Workbench", BeanColor.SAPPHIRE), "\u00a77ye", "", "\u00a76» \u00a7eClick to craft!");
	private static final ItemStack icon_anvil = newItem(new ItemStack(Material.ANVIL), Component.text("Virtual Anvil", BeanColor.SAPPHIRE), "\u00a77ye", "", "\u00a76» \u00a7eClick to smith!");
	private static final ItemStack icon_deliveries = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RiY2E0YjY5ZWFmOGRjYjdhYzM3MjgyMjhkZThhNjQ0NDA3ODcwMTMzNDJkZGFhYmMxYjAwZWViOGVlYzFlMiJ9fX0="), Component.text("\u00a76Deliveries"));
	
	public BeanGuiMainMenu(Player p) {
		super(p);
		
		this.name = "Main Menu";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,blank,null,blank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,null,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,null,null,null,null,null,bBlank,bBlank,
				bBlank,bBlank,null,null,null,null,null,bBlank,bBlank,
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				blank,blank,blank,blank,blank,blank,blank,blank,blank,
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = presetInv.clone();
		
		contents[0] = icon_news;
		contents[1] = icon_support;
		
		if (pp.isRank(Rank.MODERATOR)) // pp since it doesn't care if overriding or not
			contents[2] = newItem(new ItemStack(Material.ANVIL), Component.text("Moderation", BeanColor.STAFF));
		
		final World w = Bukkit.getWorlds().get(0);
		final int timeee = Calendar.getTime(w);
		contents[4] = newItem(new ItemStack(Material.CLOCK), Component.text("\u00a7eWorld Time"), Component.text("\u00a77" + Calendar.getTimeString(timeee, true) + " \u00a78(Day "+Calendar.getDay(w.getFullTime())+")"));
		
		if (pp.getPlayer().hasPermission("bean.gm.creative"))
			contents[6] = pp.getPlayer().getGameMode() != GameMode.SURVIVAL ? skull_C : skull_S;
		if (pp.getPlayer().hasPermission("bean.cmd.god"))
			contents[7] = pp.getPlayer().isInvulnerable() ? skull_Gon : skull_Goff;
		if (pp.getPlayer().hasPermission("bean.cmd.fly"))
			contents[8] = pp.getPlayer().getAllowFlight() ? skull_Fon : skull_Foff;
		
		if (isOverrideView())
			contents[9] = icon_resetOverride;
		
		contents[13] = newItem(tpp.getSkull(), tpp.getColouredName(), 
				Component.text("\u00a77Rank: ").append(tpp.getHighestRank().toComponent()).append(tpp.getDonorRank() != null ? Component.text("\u00a77 (").append(tpp.getDonorRank().toComponent()).append(Component.text("\u00a77)")) : Component.empty()),
				Component.text("\u00a77Wallet: "),
				Component.text("\u00a77 - \u00a76" + df.format(tpp.getBalance()) + " Coins"),
				Component.text("\u00a77 - ").append(Component.text(tpp.getSapphire() + " Sapphire", BeanColor.SAPPHIRE).decoration(TextDecoration.ITALIC, false)),
				Component.empty(),
				Component.text("\u00a76» \u00a7eClick to modify your profile!"));
		
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
				Component.text(tpp.getHeirlooms().getMovementBonus() == 0 ? "\u00a78\u25C8 Speed: 0" : "\u00a7r\u25C8 Speed: \u00a7f" + (int)(tpp.getHeirlooms().getMovementBonus() * 1000)).colorIfAbsent(aa).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("\u00a76» \u00a7eClick to open!")));
		int heirloomCount = tpp.getHeirlooms().size();
		if (heirloomCount > 0)
			waa.setAmount(Math.min(64, heirloomCount));
		waa.setItemMeta(waameta);
		
		contents[20] = icon_bestiary;
		contents[21] = icon_skills;
		contents[22] = waa;
		contents[23] = icon_wardrobe;
		contents[24] = tpp.getHome() != null ? icon_home : icon_nohome;
		
		ItemStack region = icon_region.clone();
		TextColor regionCol = TextColor.color(TextColor.color(0x3d3d3d | BeanColor.REGION.value()));
		ArrayList<Component> regionLore = new ArrayList<Component>();
		boolean regionCanOpen = getPlugin().regionManager().getRegions(p.getLocation()).size() < 1;
		
		if (regionCanOpen)
			regionLore.add(Component.text("\u00a7cNo region nearby.."));
		else
			regionLore.add(Component.text("Region: \u00a79" + getPlugin().regionManager().getRegion(p.getLocation()).getName()).colorIfAbsent(regionCol).decoration(TextDecoration.ITALIC, false));
		
		regionLore.addAll(Arrays.asList(
				Component.empty(), 
				Component.text("\u00a77\u00a7oView and/or Edit attributes"), 
				Component.text("\u00a77\u00a7oabout nearby Regions."),
				Component.empty(),
				Component.text(regionCanOpen ? "\u00a76» \u00a7eClick to open!" : "\u00a78» Cannot open.")));
		
		region.lore(regionLore);
		
		contents[29] = icon_commands;
		contents[30] = icon_warps;
		contents[31] = region;
		contents[32] = newItem(new ItemStack(Material.HOPPER_MINECART), Component.text("\u00a78Pickup Blacklist"), 
				Component.text("Filtering \u00a7f" + tpp.getPickupBlacklist().size() + "\u00a7r/\u00a77135\u00a7r Items").colorIfAbsent(TextColor.color(TextColor.color(0x3d3d3d | NamedTextColor.DARK_GRAY.value()))).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("\u00a77Your personal item blacklist listing the"),
				Component.text("\u00a77items you absolutely refuse to pick up!"),
				Component.empty(),
				Component.text("\u00a76» \u00a7eClick to open!"));
		contents[33] = icon_shome;
		
		if (p.hasPermission("bean.cmd.workbench"))
			contents[48] = icon_crafting;
		if (p.hasPermission("bean.cmd.enderchest"))
			contents[49] = icon_echest;
		if (p.hasPermission("bean.cmd.anvil"))
			contents[50] = icon_anvil;
		
		int ibxSize = tpp.getInbox().size();
		boolean hasD = ibxSize > 0;
		contents[52] = newItem(icon_deliveries, Component.text(hasD ? "\u00a76\u00a7l" + (ibxSize == 1 ? "1 Delivery" : ibxSize + " Deliveries") : "\u00a7cNo Deliveries"),
				Component.text(hasD ? "\u00a77Click to view and claim from your" : "\u00a77You currently do not have any"),
				Component.text(hasD ? "\u00a77current outstanding deliveries." : "\u00a77outstanding deliveries."),
				Component.empty(),
				Component.text(hasD ? "\u00a76» \u00a7eClick to open!" : "\u00a78» Cannot open."));
		if (hasD) contents[52].setAmount(ibxSize);
		
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
		case 1: // Support Us!
			new BeanGuiSupportUs(p).openInventory();
			return;
		case 2: // TODO: TEMP STAFF MENU BUTTON
			new BeanGuiStaff(p).openInventory();
			return;
		case 6: // Gamemode Button
			if (p.hasPermission("bean.gm.creative"))
				p.setGameMode(p.getGameMode() != GameMode.SURVIVAL ? GameMode.SURVIVAL : GameMode.CREATIVE);
			break;
		case 7: // Godmode Button
			if (p.hasPermission("bean.cmd.god"))
				p.setInvulnerable(!p.isInvulnerable());
			break;
		case 8: // Flight Button
			if (p.hasPermission("bean.cmd.fly"))
				p.setAllowFlight(!p.getAllowFlight());
			break;
		case 9: // Cancel Override Button
			if (override)
				pp.clearOverridingProfile();
			break;
		case 13: // Player Menu Button
			new BeanGuiPlayerMain(p).openInventory();
			return;
		case 20: // Bestiary Button
			new BeanGuiBestiaryEntity(p).openInventory();
			return;
		case 21: // Skills Button
			new BeanGuiSkills(p).openInventory();
			return;
		case 22: // Heirloom Button
			new BeanGuiHeirlooms(p).openInventory();
			return;
		case 23: // Wardrobe Button
			if (tpp.isOnline())
				new BeanGuiWardrobe(p).openInventory();
			return;
		case 24: // Home Button
			if (tpp.getHome() != null) {
				p.closeInventory();
				p.teleport(tpp.getHome(), TeleportCause.COMMAND);
				return;
			}
			break;
		case 29: // Command Button
			new BeanGuiCommands(p).openInventory();
			return;
		case 30: // Warps Button
			new BeanGuiWarps(p).openInventory();
			return;
		case 31: // Region Button
			new BeanGuiRegionMain(p).openInventory();
			return;
		case 32: // Blacklist Button
			new BeanGuiPickupfilter(p).openInventory();
			return;
		case 33: // Spawnpoint Button
			p.closeInventory();
			if (tpp.getOfflinePlayer().getBedSpawnLocation() != null)
				p.teleport(tpp.getOfflinePlayer().getBedSpawnLocation(), TeleportCause.COMMAND);
			else
				p.performCommand("warp Spawn");
			return;
		case 48: // Workbench Button
			if (p.hasPermission("bean.cmd.workbench"))
				p.performCommand("workbench");
			return;
		case 49: // Enderchest Button
			if (p.hasPermission("bean.cmd.enderchest")) // TEMP
				p.performCommand("enderchest");
			return;
		case 50: // Anvil Button
			if (p.hasPermission("bean.cmd.anvil"))
				p.performCommand("anvil");
			return;
		case 52: // Delivery Button
			if (tpp.getInbox().size() > 0)
				new BeanGuiInbox(p).openInventory();
			return;
		default:
			return;
		}
		
		onInventoryOpened();
	}

}
