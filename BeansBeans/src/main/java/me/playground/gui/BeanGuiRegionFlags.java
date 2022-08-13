package me.playground.gui;

import java.util.*;

import me.playground.regions.flags.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiRegionFlags extends BeanGuiRegion {

	private static final int categorySize = Flag.FlagCategory.values().length;

	private final List<Flag<?>> visibleFlags = new ArrayList<>();
	private final Map<Integer, Flag<?>> mappings = new HashMap<>();
	private final int filterSlot;

	private Flag.FlagCategory currentView;
	
	public BeanGuiRegionFlags(Player p) {
		super(p);

		for (Flag<?> flag : Flags.getRegisteredFlags()) {
			if (flag.needsPermission() && !tpp.hasPermission(flag.getPermission())) continue;
			visibleFlags.add(flag);
		}

		this.filterSlot = presetSize - 3;
		preparePresetInventory(0);
	}
	
	protected BeanGuiRegionFlags(Player p, int regionIdx) {
		this(p);
		this.regionIdx = regionIdx;
		updateRegionItems();
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();

		// Filter
		if (slot == filterSlot) {
			data = (e.isRightClick() ? data - 1 : data + 1);
			if (data > categorySize) data = 0;
			else if (data < 0) data = categorySize;
			p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
			preparePresetInventory(0);
			openInventory();
			return;
		}

		Flag<?> flag = mappings.get(slot);
		if (flag != null) {
			if (!getRegion().canModify(p)) return;

			if (e.isRightClick()) { // Reset flag
				if (getRegion().getFlag(flag, true) == null) return;
				getRegion().setFlag(flag, null);
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
				i.setItem(slot, getFlagItem(flag));
				refreshRegionViewers();
			} else { // Edit flag
				if (flag instanceof FlagBoolean flagBoolean) {
					boolean setting = getRegion().getFlag(flagBoolean);
					getRegion().setFlag(flag, !setting);
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5F, 0.8F);
					i.setItem(slot, getFlagItem(flag));
				} else if (flag instanceof FlagMember) {
					new BeanGuiRegionFlagMember(p, regionIdx, (FlagMember)flag).openInventory();
				} else if (flag instanceof FlagFloat) {
					getPlugin().getSignMenuFactory().requestSignResponse(p, Material.WARPED_WALL_SIGN, (strings -> getRegion().setFlag(flag, Float.parseFloat(strings[0]))), true, "Value for Flag", flag.getDisplayName());
				} else if (flag instanceof FlagString) {
					getPlugin().getSignMenuFactory().requestSignResponse(p, Material.WARPED_WALL_SIGN, (strings -> getRegion().setFlag(flag, strings[0]+strings[1])), true, flag.getDisplayName() + " Value");
				}
			}
			return;
		}
		super.onInventoryClicked(e);
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

	/**
	 * Set the new Inventory for when the inventory gets opened again.
	 */
	private void preparePresetInventory(int page) {
		ItemStack[] newInv = new ItemStack[] {
				rBlank,rBlank,bBlank,bBlank,regionSkull,bBlank,bBlank,rBlank,rBlank,
				rBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,rBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				bBlank,blanc,blanc,blanc,blanc,blanc,blanc,blanc,bBlank,
				rBlank,rBlank,rBlank,rBlank,goBack,rBlank,rBlank,rBlank,rBlank
		};

		// We are using data as a helper here to easily sift between these categories.
		currentView = (data < 1 || data > categorySize) ? null : Flag.FlagCategory.values()[data - 1];

		mappings.clear();

		List<Flag<?>> filteredFlags = new ArrayList<>();
		for (Flag<?> flag : visibleFlags)
			if (currentView == null || flag.getCategory() == currentView)
				filteredFlags.add(flag);

		int maxPerPage = 28;
		int size = filteredFlags.size();
		int maxThisPage = Math.min(size - (maxPerPage * page), maxPerPage);
		this.page = Math.min(page, size/maxPerPage);
		int maxPages = size/maxPerPage + 1;

		newInv[48] = page > 0 ? prevPage : whatIsThis;
		newInv[50] = size > (maxPerPage * (page+1)) ? nextPage : rBlank;

		// Loop through all visible commands while respecting the current page and max page limits.
		for (int rx = maxThisPage, idx = (maxPerPage * page); rx > 0; ++idx) {
			int slot = (maxThisPage-rx + 10 + (((maxThisPage-rx) / 7) * 2)); // Calculate the slot position
			if (idx >= size) break; // If exceeds the array size, break

			Flag<?> flag = filteredFlags.get(idx);

			newInv[slot] = getFlagItem(flag);
			mappings.put(slot, flag);
			rx--;
		}

		String title = (getRegion().isWorldRegion() ? "World" : "Region") + " -> " + (currentView != null ? currentView.getTitle() : "All Flags") + (maxPages > 1 ? " ("+(page+1)+"/"+maxPages+")" : "");
		setName(title);

		newInv[filterSlot] = getFilterItem();
		this.presetInv = newInv;
	}

	private ItemStack getFilterItem() {
		ItemStack filterItem = new ItemStack(Material.HOPPER);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Flag Category Filter", BeanColor.REGION).decoration(TextDecoration.ITALIC, false));

			if (this.currentView != null) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter through the list of"));
			lore.add(Component.text("\u00a77region flag categories."));
			lore.add(Component.empty());
			lore.add(Component.text((currentView == null ? "  \u00a7f\u25b6" : " \u00a78\u25b6") + "\u00a7r No Filter").colorIfAbsent(currentView == null ? NamedTextColor.GRAY : NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			for (Flag.FlagCategory category : Flag.FlagCategory.values())
				lore.add(Component.text((currentView == category ? "  \u00a7f\u25b6" : " \u00a78\u25b6") + "\u00a7r " + category.getTitle()).colorIfAbsent(currentView == category ? NamedTextColor.AQUA : NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to swap!"));
			meta.lore(lore);
		});

		return filterItem;
	}

	private ItemStack getFlagItem(Flag<?> flag) {
		ItemStack item = new ItemStack(Material.CHAINMAIL_HELMET);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		ArrayList<Component> lore = new ArrayList<>();
		
		final boolean isInherited = getRegion().getFlag(flag, true) == null;
		meta.displayName(Component.text(flag.getDisplayName() + (isInherited ? "\u00a78 (default)" : "")).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		
		if (flag instanceof FlagMember) {
			MemberLevel level = getRegion().getEffectiveFlag((FlagMember)flag);
			switch (level) {
				case MASTER -> item.setType(Material.BARRIER);
				case OWNER -> item.setType(Material.NETHERITE_HELMET);
				case OFFICER -> item.setType(Material.DIAMOND_HELMET);
				case TRUSTED -> item.setType(Material.GOLDEN_HELMET);
				case MEMBER -> item.setType(Material.IRON_HELMET);
				case VISITOR -> item.setType(Material.LEATHER_HELMET);
				case NONE -> item.setType(Material.TURTLE_HELMET);
			}
			lore.add(Component.text("Level: \u00a7f" + level).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		} else if (flag instanceof FlagBoolean) {
			boolean setting = getRegion().getEffectiveFlag((FlagBoolean)flag);
			item.setType(setting ? Material.LIGHT_BLUE_DYE : Material.GRAY_DYE);
			lore.add(Component.text("Allow: " + (setting ? "\u00a7a" : "\u00a7c") + setting).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		} else if (flag instanceof FlagFloat) {
			float setting = getRegion().getEffectiveFlag((FlagFloat)flag);
			item.setType(Material.COMMAND_BLOCK_MINECART);
			lore.add(Component.text("Value: \u00a7f" + setting).colorIfAbsent(BeanColor.REGION).decoration(TextDecoration.ITALIC, false));
		} else if (flag instanceof FlagString flagString) {
			Component setting = flagString.getComponentValue(getRegion().getEffectiveFlag(flagString));
			item.setType(Material.WRITABLE_BOOK);
			lore.add(Component.text("Value: ", BeanColor.REGION).append(setting).decoration(TextDecoration.ITALIC, false));
		}
		
		lore.add(Component.empty());
		lore.addAll(flag.getDescription());

		if (getRegion().canModify(p)) {
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to modify!"));
		}
		
		meta.lore(lore);
		item.setItemMeta(meta);
		if (!isInherited)
			item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		return item;
	}
	
}
