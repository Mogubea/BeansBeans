package me.playground.gui;

import me.playground.items.BeanItem;
import me.playground.items.ItemRarity;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.items.tracking.ItemTrackingManager;
import me.playground.items.tracking.ManifestationReason;
import me.playground.items.values.ItemValueManager;
import me.playground.ranks.Permission;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BeanGuiAdminItemValues extends BeanGui {

	protected static final ItemStack blanc = newItem(new ItemStack(Material.BROWN_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack blank = newItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE), Component.text("Item Sell Values", BeanColor.STAFF));
	protected static final ItemStack icon = newItem(new ItemStack(Material.CHAIN_COMMAND_BLOCK), Component.text("Item Sell Values", BeanColor.STAFF));
	protected static final ItemStack recalculate = newItem(new ItemStack(Material.COMPARATOR), Component.text("Recalculate Recipe Values", BeanColor.STAFF), Component.text("\u00a77Recalculates the \u00a76Coin Value\u00a77 of"), Component.text("\u00a77all craftable items in the game."), Component.empty(), Component.text("\u00a76» \u00a7eClick to Recalculate Values!"));

	private static final List<Component> itemCategoryFilters = Arrays.asList(
			Component.text("No Filter", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.BUILDING_BLOCKS.translationKey(), NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.DECORATIONS.translationKey(), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.REDSTONE.translationKey(), NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.TRANSPORTATION.translationKey(), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.MISC.translationKey(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.FOOD.translationKey(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.TOOLS.translationKey(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.COMBAT.translationKey(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
			Component.translatable(CreativeCategory.BREWING.translationKey(), NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false),
			Component.text("Special", ItemRarity.UNTOUCHABLE.getColour()).decoration(TextDecoration.ITALIC, false),
			Component.text("Custom", ItemRarity.RARE.getColour()).decoration(TextDecoration.ITALIC, false));

	private static final List<Component> valueFilterFilters = Arrays.asList(
			Component.text("Larger Than", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false),
			Component.text("Equal To", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
			Component.text("Smaller Than", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
	);

	private static final List<Component> enforceFilters = Arrays.asList(
			Component.text("No Filter", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
			Component.text("Enforced Values", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
			Component.text("Calculated Values", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false),
			Component.text("No Value Set", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)
	);

	private final ItemValueManager valueManager;
	private final ItemTrackingManager trackingManager;
	private final Map<Integer, String> mappings = new HashMap<>();

	private int itemCategoryFilter; // Filter based on item category
	private String nameFilter; // Filter based on item name
	private double valueFilter = -1; // Works with valueFilterFilter
	private int valueFilterFilter = 1; // Filter the valueFilter based on equal to, larger than or smaller than
	private int enforceFilter; // Filter between enforced, not enforced or all

	private final List<ItemStack> allItemStacks = new ArrayList<>();

	public BeanGuiAdminItemValues(Player p, ItemValueManager manager, ItemTrackingManager manager2) {
		super(p);

		for (Material material : Material.values()) {
			if (material.isLegacy() || material.isAir()) continue;
			if (material.isItem() || material.isBlock())
				allItemStacks.add(new ItemStack(material));
		}

		for (BeanItem beanItem : BeanItem.values())
			allItemStacks.add(beanItem.getItemStack());

		this.valueManager = manager;
		this.trackingManager = manager2;
		presetSize = 54;
		preparePresetInventory(0);
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		if (slot >= i.getSize()) return;

		switch(slot) {
			case 45 -> { // Value Filter
				if (e.isRightClick()) {
					if (valueFilter == -1) return;

					this.valueFilter = -1;
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 0.2F, 1.0F);
					preparePresetInventory(0);
					openInventory();
				} else {
					getPlugin().getSignMenuFactory().requestSignResponse(p, Material.SPRUCE_WALL_SIGN, (strings -> {
						valueFilter = Double.parseDouble(strings[0]);
						p.playSound(p.getLocation(), Sound.ITEM_BOOK_PUT, 0.2F, 1.0F);
						preparePresetInventory(0);
					}), true, "\u00a7fEnter value", "\u00a7fto search for");
				}
			}
			case 46 -> { // Category Filter
				valueFilterFilter += (e.isRightClick() ? -1 : 1);
				if (valueFilterFilter >= valueFilterFilters.size()) valueFilterFilter = 0;
				else if (valueFilterFilter < 0) valueFilterFilter = valueFilterFilters.size() - 1;

				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
				preparePresetInventory(0);
				openInventory();
			}
			case 47 -> { // Category Filter
				enforceFilter += (e.isRightClick() ? -1 : 1);
				if (enforceFilter >= enforceFilters.size()) enforceFilter = 0;
				else if (enforceFilter < 0) enforceFilter = enforceFilters.size() - 1;

				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
				preparePresetInventory(0);
				openInventory();
			}
			case 51 -> { // Recalculate
				if (!pp.hasPermission(Permission.EDIT_ITEM_VALUES)) return;

				if (System.currentTimeMillis() - valueManager.getLastRecalculation() < 1000 * 15) {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.9F);
				} else {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 1.0F);
					p.sendMessage(Component.text("\u00a78Recalculating item recipe result values..."));
					valueManager.calculateItemValues();
					getAllViewers(BeanGuiAdminItemValues.class).forEach(ui -> { ui.preparePresetInventory(ui.page); ui.openInventory(); });
					p.sendMessage(Component.text("\u00a77Done. There are " + valueManager.countPendingChanges() + " total changes."));
				}
			}
			case 52 -> { // Category Filter
				if (e.isShiftClick()) { // Shift Click to quickly go to top or bottom
					itemCategoryFilter = (e.isRightClick() ? 0 : itemCategoryFilters.size() - 1);
				} else { // Regular click to cycle
					itemCategoryFilter += (e.isRightClick() ? -1 : 1);
					if (itemCategoryFilter >= itemCategoryFilters.size()) itemCategoryFilter = 0;
					else if (itemCategoryFilter < 0) itemCategoryFilter = itemCategoryFilters.size() - 1;
				}

				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
				preparePresetInventory(0);
				openInventory();
			}
			case 53 -> { // Search Filter
				if (e.isRightClick()) {
					if (nameFilter == null) return;

					this.nameFilter = null;
					p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 0.2F, 1.0F);
					preparePresetInventory(0);
					openInventory();
				} else {
					getPlugin().getSignMenuFactory().requestSignResponse(p, Material.SPRUCE_WALL_SIGN, (strings -> {
						nameFilter = strings[0] == null ? null : strings[0].toLowerCase();
						p.playSound(p.getLocation(), Sound.ITEM_BOOK_PUT, 0.2F, 1.0F);
						preparePresetInventory(0);
					}), true, "\u00a7fEnter identifier", "\u00a7fto search for");
				}
			}
			default -> { // Edit existing item value
				if (!pp.hasPermission(Permission.EDIT_ITEM_VALUES)) return;

				String identifier = mappings.get(slot);
				if (identifier == null) return;

				getPlugin().getSignMenuFactory().requestSignResponse(p, Material.SPRUCE_WALL_SIGN, (strings -> {
					double value = Double.parseDouble(strings[0]);
					valueManager.setValue(identifier, value, pp.getId());
					ItemStack newItem = getDisplayItem(identifier);
					this.presetInv[slot] = newItem;
					getAllViewers(BeanGuiAdminItemValues.class).forEach(ui -> { if (ui.page == page) { ui.i.setItem(slot, newItem);} });
				}), true, "\u00a7fValue for " + identifier, "\u00a7fPrev: \u00a76" + dec.format(valueManager.getValue(identifier)) + " Coins");
			}
		}
	}

	@Override
	public void pageUp() {
		preparePresetInventory(++this.page);
		openInventory();
	}

	@Override
	public void pageDown() {
		preparePresetInventory(--this.page);
		openInventory();
	}

	@Override
	public void openInventory() {
		if (!pp.hasPermission(Permission.VIEW_ITEM_VALUES)) {
			close();
			return;
		}
		super.openInventory();
	}

	@Override
	public void onTick() {
		long secs = (System.currentTimeMillis() - valueManager.getLastRecalculation()) / 1000L;
		if (secs < 30) {
			ItemStack heck = i.getItem(51);
			if (heck == null) return;

			heck.editMeta(meta -> {
				List<Component> lore = meta.lore();
				lore.set(lore.size() - 1, Component.text("\u00a7c» Please wait " + (30 - secs) + " seconds..."));
				meta.lore(lore);
			});
			i.setItem(51, heck);
		} else if (secs == 30) {
			i.setItem(51, recalculate.clone());
		}
	}

	/**
	 * Set the new Inventory for when the inventory gets opened again.
	 */
	private void preparePresetInventory(int page) {
		if (!pp.hasPermission(Permission.VIEW_ITEM_VALUES)) return;

		ItemStack[] newInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,icon,bBlank,bBlank,blank,blank,
				blank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,blank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				blank,blank,blank,blank,closeUI,blank,recalculate.clone(),blank,blank
		};

		mappings.clear();

		List<ItemStack> filteredItems = new ArrayList<>();
		int fSize = allItemStacks.size();

		CreativeCategory currentFilter = itemCategoryFilter > 0 && itemCategoryFilter < CreativeCategory.values().length + 1 ? CreativeCategory.values()[itemCategoryFilter - 1] : null;
		int otherOpt = itemCategoryFilter - CreativeCategory.values().length;

		for (int x = -1; ++x < fSize;) {
			ItemStack itemStack = allItemStacks.get(x);

			// Category Filter
			if (currentFilter != null) {
				try {
					CreativeCategory itemCategory = itemStack.getType().getCreativeCategory();
					if (itemCategory == null || itemCategory != currentFilter) continue;
				} catch (Exception ignored) { // Bukkit is weird
					continue;
				}
			} else if (otherOpt == 1) {
				try {
					CreativeCategory itemCategory = itemStack.getType().getCreativeCategory();
					if (itemCategory != null) continue;
				} catch (Exception ignored) { // Bukkit is weird
					continue;
				}
			} else if (otherOpt == 2) {
				if (BeanItem.from(itemStack) == null) continue;
			}

			// Value Filter
			if (valueFilter > -1) {
				double itemValue = valueManager.getValue(itemStack);
				switch(valueFilterFilter) {
					case 0 -> { if (!(itemValue > valueFilter)) continue; }
					case 2 -> { if (!(itemValue < valueFilter)) continue; }
					default -> { if (!(itemValue == valueFilter)) continue; }
				}
			}

			// How the Value is enforced Filter
			if (enforceFilter > 0) {
				String identifier = BeanItem.getIdentifier(itemStack);

				switch(enforceFilter) {
					case 1 -> { if (!valueManager.isEnforced(identifier)) continue; } // Only enforced
					case 2 -> { if (!valueManager.isCalculated(identifier)) continue; } // Only calculated
					default -> { if (valueManager.isEnforced(identifier) || valueManager.isCalculated(identifier)) continue; } // Only neither
				}
			}

			// Name Filter
			BeanItem custom = BeanItem.from(itemStack);
			String name = (custom != null ? custom.getDisplayName().content() : itemStack.getI18NDisplayName()).toLowerCase();
			if (nameFilter != null && !(nameFilter.contains(name) || name.contains(nameFilter))) continue;

			filteredItems.add(itemStack);
		}

		int maxPerPage = 28;
		int size = filteredItems.size();
		int maxThisPage = Math.min(size - (maxPerPage * page), maxPerPage);
		this.page = Math.min(page, size/maxPerPage);
		int maxPages = size/maxPerPage + 1;

		newInv[48] = page > 0 ? prevPage : blank;
		newInv[50] = size > (maxPerPage * (page+1)) ? nextPage : blank;

		// Loop through all visible commands while respecting the current page and max page limits.
		for (int rx = maxThisPage, idx = (maxPerPage * page); rx > 0; ++idx) {
			int slot = (maxThisPage-rx + 10 + (((maxThisPage-rx) / 7) * 2)); // Calculate the slot position
			if (idx >= size) break; // If exceeds the array size, break

			String identifier = BeanItem.getIdentifier(filteredItems.get(idx));

			newInv[slot] = getDisplayItem(identifier);
			mappings.put(slot, identifier);
			rx--;
		}

		String title = "Edit Item Values" + (maxPages > 1 ? " ("+(page+1)+"/"+maxPages+")" : "") + " [" + size + "]";
		setName(title);

		newInv[45] = getValueFilterItem();
		newInv[46] = getValueFilterItem2();
		newInv[47] = getEnforceFilterItem();
		newInv[52] = getCategoryFilterItem();
		newInv[53] = getSearchFilterItem();
		this.presetInv = newInv;
	}

	private ItemStack getDisplayItem(String identifier) {
		ItemStack itemStack = null;

		try {
			Material material = Material.valueOf(identifier);
			itemStack = BeanItem.formatItem(new ItemStack(material));
		} catch (Exception ignored) {
			BeanItem custom = BeanItem.from(identifier);
			if (custom != null)
				itemStack = custom.getItemStack();
		}

		if (itemStack == null) return new ItemStack(Material.AIR);

		double value = valueManager.getValue(identifier);
		boolean isEnforced = valueManager.isEnforced(identifier);
		boolean isCalculated = !isEnforced && valueManager.isCalculated(identifier);

		itemStack.editMeta(meta -> {
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a78\u00a7m--------------------"));
			if (value > 0) {
				lore.add(Component.text("\u00a77This item can be sold by"));
				lore.add(Component.text("\u00a77players at NPCs for:"));
			} else {
				lore.add(Component.text("\u00a77This item cannot be sold by"));
				lore.add(Component.text("\u00a77players at NPCs as there is:"));
			}
			lore.add(Component.empty());
			lore.add(Component.text(value > 0 ? "\u00a76" + dec.format(value) + " Coins" : "\u00a7cNo Value Set"));
			if (isEnforced || isCalculated)
				lore.add(Component.text("\u00a78" + (isEnforced ? "Manually Set Value" : "Calculated Value")));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a78\u00a7m--------------------"));
			lore.add(Component.text("\u00a78 • \u00a77Times Manifested: \u00a7a" + df.format(trackingManager.getTimesManifested(identifier, ManifestationReason.TOTAL))));
			lore.add(Component.text("\u00a78 • \u00a77Times Demanifested: \u00a7c" + df.format(trackingManager.getTimesDemanifested(identifier, DemanifestationReason.TOTAL))));
			if (pp.hasPermission(Permission.EDIT_ITEM_VALUES)) {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eClick to enforce a value!"));
			}
			meta.lore(lore);
		});

		return itemStack;
	}

	private ItemStack getCategoryFilterItem() {
		ItemStack filterItem = new ItemStack(Material.HOPPER);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Item Category Filter", BeanColor.STAFF).decoration(TextDecoration.ITALIC, false));

			if (this.itemCategoryFilter > 0) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter items based on their"));
			lore.add(Component.text("\u00a77creative inventory category."));
			lore.add(Component.empty());
			for (int x = -1; ++x < itemCategoryFilters.size();)
				if (itemCategoryFilter == x) {
					lore.add(Component.text("  \u00a7f\u25b6 ").append(itemCategoryFilters.get(x)));
				} else {
					lore.add(Component.text(" \u00a78\u25b6 ").append(itemCategoryFilters.get(x).color(NamedTextColor.DARK_GRAY)));
				}
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eLeft/Right-Click to cycle!"));
			lore.add(Component.text("\u00a76» \u00a7eShift-Click to swap ends!"));
			meta.lore(lore);
		});

		return filterItem;
	}

	private ItemStack getSearchFilterItem() {
		ItemStack filterItem = new ItemStack(Material.SPRUCE_SIGN);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Name Search Filter", BeanColor.STAFF).decoration(TextDecoration.ITALIC, false));

			if (this.nameFilter != null) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter items based on"));
			lore.add(Component.text("\u00a77their name."));
			if (nameFilter != null) {
				lore.add(Component.empty());
				lore.add(Component.text("  \u00a7f\u25b6 \u00a7f\"" + nameFilter + "\""));
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to edit!"));
				lore.add(Component.text("\u00a76» \u00a7eRight-Click to clear!"));
			} else {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to search!"));
			}

			meta.lore(lore);
		});

		return filterItem;
	}

	private ItemStack getValueFilterItem() {
		ItemStack filterItem = new ItemStack(Material.SPRUCE_SIGN);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Value Search Filter", BeanColor.STAFF).decoration(TextDecoration.ITALIC, false));

			if (this.valueFilter > -1) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter items based on"));
			lore.add(Component.text("\u00a77their \u00a76Coin Value\u00a77."));
			if (valueFilter > -1) {
				lore.add(Component.empty());
				lore.add(Component.text("  \u00a7f\u25b6 \u00a76" + valueFilter + " Coins"));
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to edit!"));
				lore.add(Component.text("\u00a76» \u00a7eRight-Click to clear!"));
			} else {
				lore.add(Component.empty());
				lore.add(Component.text("\u00a76» \u00a7eLeft-Click to search!"));
			}

			meta.lore(lore);
		});

		return filterItem;
	}

	private ItemStack getValueFilterItem2() {
		ItemStack filterItem = new ItemStack(Material.CHEST_MINECART);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Value Search Filter II", BeanColor.STAFF).decoration(TextDecoration.ITALIC, false));

			if (this.valueFilterFilter != 1) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter items based on"));
			lore.add(Component.text("\u00a77their \u00a76Coin Value\u00a77."));
			lore.add(Component.empty());
			for (int x = -1; ++x < valueFilterFilters.size();)
				if (valueFilterFilter == x) {
					lore.add(Component.text("  \u00a7f\u25b6 ").append(valueFilterFilters.get(x)));
				} else {
					lore.add(Component.text(" \u00a78\u25b6 ").append(valueFilterFilters.get(x).color(NamedTextColor.DARK_GRAY)));
				}
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eLeft/Right-Click to cycle!"));
			meta.lore(lore);
		});

		return filterItem;
	}

	private ItemStack getEnforceFilterItem() {
		ItemStack filterItem = new ItemStack(Material.COMMAND_BLOCK_MINECART);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Value Cause Filter", BeanColor.STAFF).decoration(TextDecoration.ITALIC, false));

			if (this.enforceFilter > 0) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter items based on the"));
			lore.add(Component.text("\u00a77cause of their \u00a76Coin Value\u00a77."));
			lore.add(Component.empty());
			for (int x = -1; ++x < enforceFilters.size();)
				if (enforceFilter == x) {
					lore.add(Component.text("  \u00a7f\u25b6 ").append(enforceFilters.get(x)));
				} else {
					lore.add(Component.text(" \u00a78\u25b6 ").append(enforceFilters.get(x).color(NamedTextColor.DARK_GRAY)));
				}
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eLeft/Right-Click to cycle!"));
			meta.lore(lore);
		});

		return filterItem;
	}

}
