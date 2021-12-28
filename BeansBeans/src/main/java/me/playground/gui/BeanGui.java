package me.playground.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class BeanGui implements IPluginRef {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1), "");
	protected static final ItemStack bBlank = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack nextPage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19"), "\u00a78Next Page");
	protected static final ItemStack prevPage = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), "\u00a78Previous Page");
	public static final ItemStack goBack = newItem(new ItemStack(Material.NETHER_STAR), "\u00a7cBack");
	
	protected static final ItemStack blankop = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), "");
	protected static final ItemStack icon_money = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM2ZTk0ZjZjMzRhMzU0NjVmY2U0YTkwZjJlMjU5NzYzODllYjk3MDlhMTIyNzM1NzRmZjcwZmQ0ZGFhNjg1MiJ9fX0="), "");
	
	public static final ItemStack icon_dataU = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19"), "\u00a78Right");
	public static final ItemStack icon_dataD = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), "\u00a78Left");
	
	public static final ItemStack menuItem = newItem(new ItemStack(Material.NETHER_STAR), "\u00a7aPlayer Menu", "\u00a77\u00a7oClick to open!");
	
	protected final Player p;
	protected final Main plugin;
	
	protected int presetSize = 27;
	protected InventoryType presetType;
	protected String name = "Error";
	protected ItemStack[] presetInv;
	/**
	 * The PlayerProfile of the inventory viewer.
	 */
	protected PlayerProfile pp; // Real
	/**
	 * The previewed profile, otherwise the same as pp.
	 */
	protected PlayerProfile tpp; // Target
	protected int page;
	protected int data;
	protected Inventory i;
	
	public BeanGui(Player p) {
		this(p, 0, 0);
	}
	
	public BeanGui(Player p, int page, int data) {
		this.p = p;
		this.plugin = Main.getInstance();
		PlayerProfile ack = PlayerProfile.from(p);
		if (!ack.profileOverride.equals(ack.getUniqueId()))
			tpp = PlayerProfile.from(ack.profileOverride);
		else
			tpp = ack;
		pp = ack;
		this.page = page;
		this.data = data;
	}
	
	public abstract void onInventoryClosed(InventoryCloseEvent e);
	public abstract void onInventoryOpened();
	public abstract void onInventoryClicked(InventoryClickEvent e);
	
	/**
	 * Fires {@link BeanGui#onInventoryClicked(InventoryClickEvent)} if this returns false.
	 * @param e - The {@link InventoryClickEvent} involved.
	 * @return True if something happened during this method.
	 */
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (pp.onCdElseAdd("guiClick", 300))
			return true;
			
		if (i.isSimilar(goBack)) {
			new BeanGuiMainMenu(p).openInventory();
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
	
	public void openInventory() {
		if (this.presetType != null && presetType.isCreatable())
			this.i = Bukkit.createInventory(p, presetType, Component.text(name));
		else
			this.i = Bukkit.createInventory(p, presetSize, Component.text(name));
		i.setContents(presetInv);
		p.openInventory(i);
		pp.setBeanGui(this);
		
		onInventoryOpened();
	}
	
	public void pageUp() {
		this.page++;
		if (page > 50)
			page = 50;
		onInventoryOpened();
	}
	
	public void pageDown() {
		this.page--;
		if (page < 0)
			page = 0;
		onInventoryOpened();
	}
	
	public void setPage(int page) {
		this.page = page;
		if (page < 0 || page > 50)
			page = 0;
		onInventoryOpened();
	}
	
	public void setProfile(PlayerProfile pp) {
		this.tpp = pp;
		onInventoryOpened();
	}
	
	public PlayerProfile getViewerProfile() {
		return pp;
	}
	
	public PlayerProfile getViewedProfile() {
		return tpp;
	}
	
	public Player getViewer() {
		return p;
	}
	
	public Inventory getInventory() {
		return i;
	}
	
	public int getPage() {
		return page;
	}
	
	public int getData() {
		return data;
	}
	
	public int setData(int data) {
		return this.data = data;
	}
	
	public int upData() {
		return this.data++;
	}
	
	public int downData() {
		return this.data--;
	}
	
	protected boolean isOverrideView() {
		return tpp.getId() != pp.getId();
	}
	
	final protected static ItemStack newItem(ItemStack it, String name, String... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(Component.text(name));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> loree = new ArrayList<Component>();
		if (lore != null)
			for (String line : lore)
				loree.add(Component.text(line));
		meta.lore(loree);
		i.setItemMeta(meta);
		return i;
	}
	
	final protected static ItemStack newItem(ItemStack it, Component name) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		i.setItemMeta(meta);
		return i;
	}
	
	final protected static ItemStack newItem(ItemStack it, Component name, String... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> loree = new ArrayList<Component>();
		if (lore != null)
			for (String line : lore)
				loree.add(Component.text(line));
		meta.lore(loree);
		i.setItemMeta(meta);
		return i;
	}
	
	final protected static ItemStack newItem(ItemStack it, Component name, Component... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> loree = new ArrayList<Component>();
		if (lore != null)
			for (Component line : lore)
				loree.add(line);
		meta.lore(loree);
		i.setItemMeta(meta);
		return i;
	}
	
	public void close() {
		p.closeInventory();
	}
	
	public void refresh() {
		onInventoryOpened();
	}
	
	/**
	 * Get all of the currently viewed instances of the specified {@link BeanGui},
	 * this method can be very useful in a forEach loop to {@link BeanGui#refresh()} with a criteria.
	 */
	@SuppressWarnings("unchecked") // It is checked lol?
	public static <T extends BeanGui> Collection<T> getAllViewers(@Nonnull final Class<T> clazz) {
		ArrayList<T> instances = new ArrayList<T>();
		Bukkit.getOnlinePlayers().forEach((p) -> {
			PlayerProfile pp = PlayerProfile.from(p);
			if (pp.getBeanGui() != null && clazz.isInstance(pp.getBeanGui()))
				instances.add((T) pp.getBeanGui());
		});
		return instances;
	}
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	protected final DecimalFormat dec = new DecimalFormat("#.##");
	
}
