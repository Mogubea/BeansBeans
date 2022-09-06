package me.playground.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

/**
 * A custom interface that can be instantiated by players.
 */
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
	
	protected static final ItemStack closeUI = newItem(new ItemStack(Material.BARRIER), Component.text("Close", NamedTextColor.RED));
	
	protected final Player p;
	protected final Main plugin;
	
	protected int interactCooldown = 300;
	protected int presetSize = 27;
	protected InventoryType presetType;
	protected Component name = Component.text("Inventory");
	protected Component baseName;
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
		
		playOpenSound();
	}
	
	public void onInventoryClosed(InventoryCloseEvent e) {}

	public void onInventoryOpened() {}

	public void onInventoryClicked(InventoryClickEvent e) {}

	
	/**
	 * Fired from {@link me.playground.listeners.EntityListener} whenever a player picks up an item while
	 * viewing this {@link BeanGui} instance.
	 * <p>This does not fire when an item is added to the player's inventory through other means.
	 */
	public void onItemPickup(EntityPickupItemEvent e) { }
	
	/**
	 * Fired from {@link me.playground.listeners.ContainerListener} whenever a player attempts to drag an
	 * item while viewing this {@link BeanGui} instance.
	 */
	public void onInventoryDrag(InventoryDragEvent e) {
		for (int slot : e.getRawSlots()) {
			if (slot < i.getSize()) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	/**
	 * Fires {@link BeanGui#onInventoryClicked(InventoryClickEvent)} if this returns false.
	 * @param e - The {@link InventoryClickEvent} involved.
	 * @return True if something happened during this method.
	 */
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getAction() == InventoryAction.NOTHING) return true;

		e.setCancelled(true);
		final ItemStack i = e.getCurrentItem();
		if (i == null) return true;
		
		if (interactCooldown > 0 && pp.onCdElseAdd("guiClick", interactCooldown, true)) {
			return true;
		} else if (i.isSimilar(closeUI)) {
			close();
		} else if (i.isSimilar(goBack)) {
			onBackPress();
		} else if (i.isSimilar(nextPage)) {
			pageUp();
			p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.3F, 1.0F);
		} else if (i.isSimilar(prevPage)) {
			pageDown();
			p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.3F, 1.0F);
		} else {
			return false;
		}
		
		return true;
	}

	/**
	 * Triggers when the "Go Back" Star is pressed.
	 */
	protected void onBackPress() {
		new BeanGuiMainMenu(p).openInventory();
	}
	
	public void openInventory() {
		if (this.presetType != null && presetType.isCreatable())
			this.i = Bukkit.createInventory(p, presetType, name);
		else
			this.i = Bukkit.createInventory(p, presetSize, name);
		if (presetInv != null)
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
			this.page = 0;
		onInventoryOpened();
	}
	
	public void setProfile(PlayerProfile pp) {
		this.tpp = pp;
		onInventoryOpened();
	}

	@NotNull
	public PlayerProfile getViewerProfile() {
		return pp;
	}

	@NotNull
	public PlayerProfile getViewedProfile() {
		return tpp;
	}

	@NotNull
	public Player getViewer() {
		return p;
	}

	@NotNull
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
	
	protected void setName(@NotNull Component name) {
		this.name = name;
	}
	
	protected void setName(@NotNull String name) {
		this.name = Component.text(name);
	}

	@NotNull
	public Component getName() {
		return name;
	}
	
	protected boolean isOverrideView() {
		return tpp.getId() != pp.getId();
	}
	
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.25F, 1.0F);
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, String name, String... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(Component.text(name));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		ArrayList<Component> loree = new ArrayList<>();
		if (lore != null)
			for (String line : lore)
				loree.add(Component.text(line));
		meta.lore(loree);
		i.setItemMeta(meta);
		return i;
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		i.setItemMeta(meta);
		return i;
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name, String... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		ArrayList<Component> loree = new ArrayList<>();
		if (lore != null)
			for (String line : lore)
				loree.add(Component.text(line));
		meta.lore(loree);
		i.setItemMeta(meta);
		return i;
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name, String lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		List<Component> comps = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (String s : lore.split(" ")) {
			if (cur.length() >= 34 || cur.length()+s.length() >= 36) {
				comps.add(Component.text(cur.toString()).colorIfAbsent(TextColor.color(0xafafaf)).decoration(TextDecoration.ITALIC, false));
				cur = new StringBuilder();
			}
			cur.append(s).append(" ");
		}

		if (cur.length() > 0)
			comps.add(Component.text(cur.toString()).colorIfAbsent(TextColor.color(0xafafaf)).decoration(TextDecoration.ITALIC, false));
		meta.lore(comps);
		i.setItemMeta(meta);
		return i;
	}

	@NotNull
	protected static ItemStack newItem(@NotNull ItemStack it, Component name, Component... lore) {
		ItemStack i = it.clone();
		ItemMeta meta = i.getItemMeta();
		meta.displayName(name.decoration(TextDecoration.ITALIC, false));
		meta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		ArrayList<Component> loree = new ArrayList<>();
		if (lore != null)
			Collections.addAll(loree, lore);
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
	 * Fires every 20 in-game ticks.
	 */
	public void onTick() {

	}

	/**
	 * Get all of the currently viewed instances of the specified {@link BeanGui},
	 * this method can be very useful in a forEach loop to {@link BeanGui#refresh()} with a criteria.
	 */
	@NotNull
	public static <T extends BeanGui> Collection<T> getAllViewers(@Nonnull final Class<T> clazz) {
		ArrayList<T> instances = new ArrayList<>();
		Bukkit.getOnlinePlayers().forEach((p) -> {
			PlayerProfile pp = PlayerProfile.from(p);
			if (pp.getBeanGui() != null && clazz.isInstance(pp.getBeanGui()))
				instances.add(clazz.cast(pp.getBeanGui()));
		});
		return instances;
	}
	
	@Override
	public @NotNull Main getPlugin() {
		return plugin;
	}
	
	protected final static DecimalFormat df = new DecimalFormat("#,###");
	protected final static DecimalFormat dec = new DecimalFormat("#,###.##");
	
}
