package me.playground.gui;

import java.util.*;
import java.util.Map.Entry;

import me.playground.enchants.BEnchantmentTarget;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import me.playground.enchants.BEnchantment;
import me.playground.gui.stations.BeanGuiEnchantingTable;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiEnchants extends BeanGui {
	
	private static final Map<BEnchantment, ItemStack> enchantItems = new HashMap<>();
	protected static final ItemStack gPDef = newItem(new ItemStack(Material.BROWN_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack blank = newItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), Component.text("Enchantments", BeanColor.ENCHANT));
	protected static final ItemStack goBack2 = newItem(new ItemStack(Material.ENCHANTING_TABLE), Component.text("\u00a7cGo Back"), Component.text("\u00a77Return to Enchanting Table"));
	protected static final ItemStack icon = newItem(new ItemStack(Material.ENCHANTING_TABLE), Component.text("Enchantment List", BeanColor.ENCHANT),
			Component.text("\u00a77A detailed list of all \u00a7b" + BEnchantment.size() + "\u00a77 enchantments"),
			Component.text("\u00a77and burdens on Bean's Beans!"));
	
	static {
		BEnchantment[] enchantments = BEnchantment.values();
		for (int x = -1; ++x < enchantments.length;) {
			BEnchantment enchant = enchantments[x];
			
			List<Component> lore = new ArrayList<>(enchant.getLore(enchant.getMaxLevel()));
			ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);

			book.editMeta(meta -> {
				meta.displayName(enchant.displayName().decoration(TextDecoration.ITALIC, false));
				lore.add(Component.empty());
				lore.add(Component.text("\u00a7fTargets: "));
				for (String target : enchant.getEnchantmentTarget().getValidTargetStrings())
					lore.add(Component.text("\u00a77 • \u00a7e" + target));

				lore.add(Component.empty());
				lore.add(Component.text("\u00a7fMax Level: \u00a7e" + enchant.getMaxLevel()));
				if (enchant.isCursed())
					lore.add(Component.text("\u00a7rRestores: " + (-enchant.getRunicValue(1)) + " \u269D Runic Capacity").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("\u00a7fRequirements:"));
				if (enchant.getRunicValue(1) != 0)
					if (!enchant.isCursed())
						lore.add(Component.text("\u00a77 • \u00a7r" + enchant.getRunicValue(1) + " \u269D Runic Capacity").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));

				if (enchant.inEnchantingTable() && enchant.getBookRequirement(1) > 0)
					lore.add(Component.text("\u00a77 • \u00a7d" + enchant.getBookRequirement(1) + " \u270e Enchanting Power"));
				if (enchant.getExperienceCost(1) > 0)
					lore.add(Component.text("\u00a77 • \u00a7r" + enchant.getExperienceCost(1) + " Experience Levels").colorIfAbsent(BeanColor.EXPERIENCE).decoration(TextDecoration.ITALIC, false));

				// List Conflicts
				List<Enchantment> conflicts = enchant.getConflicts();
				if (!conflicts.isEmpty()) {
					lore.add(Component.empty());
					lore.add(Component.text("\u00a7fConflicts: "));
					for (Enchantment conflict : conflicts) {
						BEnchantment conf = BEnchantment.from(conflict);
						lore.add(Component.text("\u00a77 • ").append(conf.displayName()).decoration(TextDecoration.ITALIC, false));
					}
					//lore.add(Component.empty());
				}

				meta.lore(lore);
			});
			
			enchantItems.put(enchant, book);
		}
	}
	
	private final List<ItemStack> filteredItems;

	private final String[] targetOptions = { "No Filter", "Weapons Only", "Tools Only", "Armour Only" };
	private final TextColor[] targetColors = { NamedTextColor.GRAY, NamedTextColor.YELLOW, NamedTextColor.YELLOW, NamedTextColor.YELLOW };
	private static final List<List<BEnchantmentTarget>> targetTargets = new ArrayList<>() {{
		add(Arrays.asList(BEnchantmentTarget.WEAPON, BEnchantmentTarget.BOW, BEnchantmentTarget.CROSSBOW, BEnchantmentTarget.TRIDENT));
		add(Arrays.asList(BEnchantmentTarget.AXE, BEnchantmentTarget.PICKAXE, BEnchantmentTarget.PICKAXE_AXE, BEnchantmentTarget.TOOL_NO_HOE, BEnchantmentTarget.TOOL, BEnchantmentTarget.FISHING_ROD, BEnchantmentTarget.SHOVEL, BEnchantmentTarget.SHEARS));
		add(Arrays.asList(BEnchantmentTarget.ARMOR, BEnchantmentTarget.ARMOR_TORSO, BEnchantmentTarget.ARMOR_LEGS, BEnchantmentTarget.ARMOR_HEAD, BEnchantmentTarget.ARMOR_FEET));
	}};

	private final String[] options = { "No Filter", "Enchants Only", "Burdens Only", "Astral Only" };
	private final TextColor[] colors = { NamedTextColor.GRAY, BeanColor.ENCHANT, BeanColor.ENCHANT_BURDEN, BeanColor.ENCHANT_ASTRAL };
	
	private final EnchantingTable table;
	
	private int filter = 0;
	private int targetFilter = 0;
	
	public BeanGuiEnchants(Player p, EnchantingTable table) {
		super(p);
		setName("Enchantments");
		
		this.filteredItems = new ArrayList<>(enchantItems.values());
		this.table = table;
		this.presetSize = 54;
		
		ItemStack prevGuiItem = table != null ? goBack2 : goBack;
		
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,icon,bBlank,bBlank,blank,blank,
				blank,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,blank,
				bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				blank,blank,blank,blank,prevGuiItem,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		
		if (slot == 49) {
			if (table != null)
				new BeanGuiEnchantingTable(p, table).openInventory();
			else
				close();
			return;
		}
		
		if (slot == 51) {
			filter += e.isRightClick() ? -1 : 1;
			if (filter > 3) filter = 0;
			if (filter < 0) filter = 3;
			updateList();
		} else if (slot == 52) {
			targetFilter += e.isRightClick() ? -1 : 1;
			if (targetFilter > 3) targetFilter = 0;
			if (targetFilter < 0) targetFilter = 3;
			updateList();
		}
	}
	
	@Override
	public void onInventoryOpened() {
		i.setItem(48, page > 0 ? prevPage : blank);
		i.setItem(50, filteredItems.size() > (28 * (page+1)) ? nextPage : blank);
		
		for (int x = -1; ++x < 28;) {
			if (filteredItems.size() <= x + (28 * page))
				i.setItem(x + 10 + ((x/7) * 2), gPDef);
			else
				i.setItem(x + 10 + ((x/7) * 2), filteredItems.get(x + (28 * page)));
		}

		ItemStack filterItem = new ItemStack(Material.HOPPER);
		filterItem.editMeta(meta -> {
			meta.displayName(Component.text("Enchantment Filter", BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
			
			if (this.filter != 0) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			
			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter through the list of"));
			lore.add(Component.text("\u00a77enchantments based on type."));
			lore.add(Component.empty());
			for (int x = -1; ++x < options.length;)
				lore.add(Component.text((filter == x ? "  \u00a7f\u25b6" : " \u00a78\u25b6") + "\u00a7r " + options[x]).colorIfAbsent(filter == x ? colors[x] : NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to swap!"));
			meta.lore(lore);
		});
		i.setItem(51, filterItem);

		ItemStack filterItem2 = new ItemStack(Material.HOPPER);
		filterItem2.editMeta(meta -> {
			meta.displayName(Component.text("Enchantment Target Filter", BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));

			if (this.targetFilter != 0) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}

			List<Component> lore = new ArrayList<>();
			lore.add(Component.text("\u00a77Filter through the list of"));
			lore.add(Component.text("\u00a77enchantments based on target."));
			lore.add(Component.empty());
			for (int x = -1; ++x < targetOptions.length;)
				lore.add(Component.text((targetFilter == x ? "  \u00a7f\u25b6" : " \u00a78\u25b6") + "\u00a7r " + targetOptions[x]).colorIfAbsent(targetFilter == x ? targetColors[x] : NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
			lore.add(Component.empty());
			lore.add(Component.text("\u00a76» \u00a7eClick to swap!"));
			meta.lore(lore);
		});
		i.setItem(52, filterItem2);
	}

	private void updateList() {
		filteredItems.clear();
		for (Entry<BEnchantment, ItemStack> entry : enchantItems.entrySet()) {
			BEnchantment enchantment = entry.getKey();

			if (filter == 1 && enchantment.isCursed()) continue;
			if (filter == 2 && !enchantment.isCursed()) continue;
			if (filter == 3 && !enchantment.isAstral()) continue;
			if (targetFilter > 0 && !targetTargets.get(targetFilter - 1).contains(enchantment.getEnchantmentTarget())) continue;

			ItemStack item = entry.getValue();
			filteredItems.add(item);
		}

		page = 0;
		p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 1.0F);
		onInventoryOpened();
	}

}
