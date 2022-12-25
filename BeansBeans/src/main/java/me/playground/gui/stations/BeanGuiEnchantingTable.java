package me.playground.gui.stations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.playground.items.BItemDurable;
import me.playground.items.lore.Lore;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.menushop.PurchaseOption;
import me.playground.ranks.Permission;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.playground.enchants.BEnchantment;
import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiEnchants;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.skills.Skill;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiEnchantingTable extends BeanGui {
	
	protected static final ItemStack confirmEmpty = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjUyMjljMzQ4NTRhNzI2MTg1ZTg0YzcwMzQ0MGU2ZGMzNDA3NDc1NDA3YWRhNWYxYjI3YmIyYzExNTZlYWMyZSJ9fX0="),
			Component.text("No Pending Changes..", NamedTextColor.DARK_GRAY), Component.text("\u00a77Click the enhancement categories above"), Component.text("\u00a77to add pending changes to your"), Component.text("\u00a77item."));
	protected static final ItemStack confirmCant = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZmMTg5OGYxZTg0ODA1Njk0NTQ0OTQ0ZjhiNDljNzAwNzYyMmEyZDliMmJiNTlhMjc4NTE5YTY4OTkxYWM2OSJ9fX0="),
			Component.text("Cannot Apply Runic Enhancements", NamedTextColor.RED));
	protected static final ItemStack confirmCan = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU2YmIyNDNmY2JkYTc4MDNlZGUxNmYwMmI2MTU2MzZkZDJkNzI1MmUxN2RkZTkxMzE0MjRjNjhhNGQ1YWNhOSJ9fX0="),
			Component.text("Apply Runic Enhancements", NamedTextColor.GREEN));
	protected static final ItemStack pageRunes = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2IxMWZiOTBkYjdmNTdiZWI0MzU5NTQwMTNiMWM3ZWY3NzZjNmJkOTZjYmYzMzA4YWE4ZWJhYzI5NTkxZWJiZCJ9fX0="),
			Component.text("Runes", NamedTextColor.GOLD),
			Component.text("\u00a77Work in progress. \u00a76Runes \u00a77can be infused"),
			Component.text("\u00a77into your equipment to enhance it with"),
			Component.text("\u00a77various bonuses."));
	
	protected static final ItemStack pageEnchantsDetails = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGJlMmJhZjQwZmQ4NWViNTczZmU1YjJlNWI2Yzg4MTdjZjUwZjg4M2Q5NTc2OTQxNTgwN2FiMDcyODhhNDdjZCJ9fX0="), 
			Component.text("Enchantments", BeanColor.ENCHANT),
			Component.text("\u00a77The list of positive enchantments"), Component.text("\u00a77that can be added to your item. Each"), Component.text("\u00a77enchantment applied will deplete some"),
			Component.text("\u269D Runic Capacity \u00a77from your item.").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false), Component.empty(), 
			Component.text("\u00a77Removing and downgrading Enchantments"),
			Component.text("\u00a77is \u00a7aFree\u00a77."));
	
	protected static final ItemStack pageBurdensDetails = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNhNjljM2NhYTMxMzA0ZTk5NTIzMjhjNzJjZWUwYjU3YjJhMmJkNDZjZTljNWNiODhjMDdkMTI2NjI3N2Q2YSJ9fX0="), 
			Component.text("Burdens", BeanColor.ENCHANT_BURDEN),
			Component.text("\u00a77The list of burdens that can be"), Component.text("\u00a77added to your item. Each \u00a7rBurden").colorIfAbsent(BeanColor.ENCHANT_BURDEN).decoration(TextDecoration.ITALIC, false), 
			Component.text("\u00a77applied will restore some \u00a7r\u269D Runic").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false),
			Component.text("Capacity \u00a77to your item, allowing").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false), 
			Component.text("\u00a77for more positive enchantments."), Component.empty(),
			Component.text("\u00a7cRemoving burdens requires your"),
			Component.text("\u00a7citem to be at full durability."));
	
	protected static final ItemStack confNone = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confRed = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confGreen = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.empty());
	
	protected static final ItemStack allEnchantList = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("Enchantment List", BeanColor.ENCHANT),
			Component.text("\u00a77A detailed list of all \u00a7b" + BEnchantment.size(false) + "\u00a77 enchantments"),
			Component.text("\u00a77and burdens on Bean's Beans!"),
			Component.empty(),
			Component.text("\u00a76» \u00a7eClick to view!"));
	
	protected static final ItemStack enchTable = newItem(new ItemStack(Material.ENCHANTING_TABLE), 
			Component.text("Enchanting Table", BeanColor.ENCHANT),
			Component.text("\u00a77Place the equipment you wish to"),
			Component.text("\u00a77modify the runic enhancements of"),
			Component.text("\u00a77in the slot above!"));
	
	protected static final ItemStack gPDef = newItem(new ItemStack(Material.BROWN_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack gPDefEnch = newItem(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE), Component.text("Enchant Slot", NamedTextColor.WHITE), Component.text("\u00a77Place the item you wish to \u00a7benchant\u00a77,"), Component.text("\u00a73disenchant \u00a77or modify the \u00a76Runes \u00a77of here."));
	
//	protected static final ItemStack gPRuneEmpty = newItem(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE), Component.text("Empty Rune Slot", NamedTextColor.WHITE), Component.text("\u00a77Place a \u00a76Rune\u00a77 here to infuse it"), Component.text("\u00a77with your item."));
	
	protected static final ItemStack backToList = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), Component.text("Go Back", NamedTextColor.RED), Component.text("\u00a77To List of Enchants"));
	protected static final ItemStack backToListB = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), Component.text("Go Back", NamedTextColor.RED), Component.text("\u00a77To List of Burdens"));
	protected static final ItemStack backToFront = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0="), Component.text("Go Back", NamedTextColor.RED), Component.text("\u00a77To Enhancement Options"));
	
	protected static final ItemStack lapisChest = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM3Yjk2YTQ1YThmMGM2ZmQ3ZGZhYjJhODVlMWEzODRkYWNhNGI4MDZlYjg5MmE4N2UyN2Y1MmE5ZjkxYzA4NCJ9fX0=");
	
	public static final NamespacedKey KEY_LAPIS = Main.getInstance().getKey("ENCHANTING_TABLE_LAPIS");
	public static final NamespacedKey KEY_LAPIS_LEVEL = Main.getInstance().getKey("ENCHANTING_TABLE_LAPIS_LEVEL");
	
	private static final String[] lapisTitles = { "Tiny", "Small", "Medium", "Large", "Huge", "Gigantic", "Massive" };
	
	private final EnchantingTable table;
	private final int enchantingSlot = 19;
	private final int confirmationSlot = 41;
	private BEnchantment selEnch;
	private ItemStack itemToModify = null;
	private ItemStack displayItem = null;
	private BItemDurable customItem = null;

	private PurchaseOption confirm = null;
	
	private int bookPower = 0;
	private byte lapisLevel = 0;
	private int lapis = 0;
	
	private int lapisCost = 0;
	private int xpCost = 0;
	
	private int remainingRunicScore;
	
	private final Map<Enchantment, Integer> pendingChanges = new LinkedHashMap<>();
	private final Map<Integer, BEnchantment> mapping = new HashMap<>();
	
	public BeanGuiEnchantingTable(Player p, EnchantingTable table) {
		super(p);
		setName("Enchanting Table");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank,bBlank,bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				bBlank,null,bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				bBlank,enchTable,bBlank,gPDef,gPDef,gPDef,gPDef,gPDef,bBlank,
				allEnchantList,bBlank,bBlank,bBlank,bBlank,confirmEmpty,bBlank,bBlank,bBlank,
				lapisChest,bBlank,confNone,confNone,confNone,closeUI,confNone,confNone,confNone,
		};
		
		this.table = table;

		if (p.getGameMode() == GameMode.CREATIVE)
			bookPower += 1000;

		// Book Power
		if (table != null) {
			int oX = table.getX();
			int oY = table.getY();
			int oZ = table.getZ();
			for (int x = -3; ++x < 3;) {
				for (int y = -1; ++y < 2;)
					for (int z = -3; ++z < 3;) {
						if (Math.abs(x) < 2 && Math.abs(z) < 2) continue;
						if (table.getWorld().getBlockAt(oX + x, oY + y, oZ + z).getType() == Material.BOOKSHELF)
							bookPower += 2;
					}
			}
			
			// Grab lapis values, set them if the enchantment table doesn't have them.
			PersistentDataContainer pdc = table.getPersistentDataContainer();
			if (!pdc.has(KEY_LAPIS)) {
				pdc.set(KEY_LAPIS, PersistentDataType.SHORT, (short)0);
				table.update();
			} else
				lapis = table.getPersistentDataContainer().getOrDefault(KEY_LAPIS, PersistentDataType.SHORT, (short)0);
			
			if (!pdc.has(KEY_LAPIS_LEVEL)) {
				pdc.set(KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
				table.update();
			} else
				lapisLevel = table.getPersistentDataContainer().getOrDefault(KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
		}
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		ItemStack[] contents = e.getInventory().getContents();
		if (contents[enchantingSlot] != null && !contents[enchantingSlot].equals(gPDefEnch))
			e.getPlayer().getInventory().addItem(contents[enchantingSlot]).forEach((index, item) -> e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item));
	}
	
	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		if (slot == enchantingSlot) {
			ItemStack incomingItem = e.getCursor().getType() == Material.AIR ? null : e.getCursor();
			if (e.getHotbarButton() > -1)
				incomingItem = p.getInventory().getItem(e.getHotbarButton());
			
			if (!e.isShiftClick() && incomingItem != null && !isEnchantable(incomingItem)) {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
				return;
			}
			
			e.setCancelled(false);
			updateEnchantSlot();
			return;
		} else if (slot > i.getSize()) {
			if (e.isShiftClick() && itemToModify == null) {
				if (e.getCurrentItem() == null) return;

				if (!isEnchantable(e.getCurrentItem())) {
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
					return;
				}
				updateEnchantSlot();
			}
			e.setCancelled(false);
			return;
		}

		switch (slot) {
			case 36 -> { new BeanGuiEnchants(p, table).openInventory(); return; }
			case 46 -> { new BeanGuiEnchantingTableLapis(p, table).openInventory(); return; }
			case 50 -> { close(); return; }
		}
		
		if (itemToModify == null) return;
		
		if (slot == confirmationSlot) {
			if (confirm != null && confirm.purchase(p, false)) {
				i.setItem(enchantingSlot, displayItem);

				if (p.getGameMode() != GameMode.CREATIVE) {
					pp.getSkills().addExperience(Skill.ENCHANTING, xpCost * 41 + lapisCost * 100); // Only give XP if not in Creative Mode.
					useLapisLazuli(lapisCost);
				} else if (!pp.hasPermission(Permission.BYPASS_COSTS_CREATIVE))
					useLapisLazuli(lapisCost);

				updateEnchantSlot(); // Done by global gui update
				p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, table != null ? table.getLocation() : p.getEyeLocation(), 12, 0.5, 0.5, 0.5);
				p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
			} else {
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
			}
			return;
		}
		
		if (this.data == 0) {
			if (slot == 21) { // Enchantments
				data = 1;
				i.setItem(5, pageEnchantsDetails);
				i.setItem(4, backToFront);
				showEnchantments();
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			} else if (slot == 23) { // Burdens
				data = 2;
				i.setItem(5, pageBurdensDetails);
				i.setItem(4, backToFront);
				showBurdens();
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			} else if (slot == 25) { // Runes
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, 0.8F);
				//data = 3;
			}
		// Specific enchantment level screen
		} else if (selEnch != null) {
			// Go back to enchantment list
			if (slot == 4) {
				selEnch = null;
				if (data == 1)
					showEnchantments();
				else
					showBurdens();
				i.setItem(4, backToFront);
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			} else if (slot >= 12 && slot < 17 && e.getCurrentItem().getType() == Material.ENCHANTED_BOOK) {
				int oldLvl = itemToModify.getItemMeta().getEnchantLevel(selEnch);
				int pendLvl = pendingChanges.getOrDefault(selEnch, -1);
				int lvSlot = slot-12 + 1;
				
				// Remove existing change if exists and removal is clicked
				if (pendLvl == lvSlot || pendLvl == oldLvl || pendLvl == 0 && (oldLvl == lvSlot)) {
					pendingChanges.remove(selEnch);
				// Update existing change or add a new change
				} else {
					// Remove enchantment
					if (oldLvl == lvSlot)
						pendingChanges.put(selEnch, 0);
					// Change existing enchantment level
					else if (bookPower >= selEnch.getBookRequirement(lvSlot)) {
						pendingChanges.put(selEnch, lvSlot);
						// Remove all pending changes for existing conflicts
						for (Enchantment conflict : selEnch.getActiveConflicts(displayItem))
							pendingChanges.remove(conflict);
					} else {
						p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.3F, 1.0F);
						return;
					}
				}
				
				selEnch = null;
				updateConfirmationItems();
				
				if (data == 1)
					showEnchantments();
				else
					showBurdens();
				
				p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.15F, 1.5F);
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			}
		// Set up specific enchantment level screen
		} else if (mapping.containsKey(slot)) {
			BEnchantment enchant = mapping.get(slot);
			int oldLvl = itemToModify.getItemMeta().getEnchantLevel(enchant);
			selEnch = enchant;
			resetBrowns();
			p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			
			for (int level = 0; ++level < selEnch.getMaxLevel() + 1;) {
				List<Component> lore = new ArrayList<>(enchant.getLore(level));
				int enchSlot = 12 + (level-1) + (4 * ((level-1)/5));
				ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta meta = book.getItemMeta();

				if (selEnch.isCursed() && !BeanItem.isFullyRepaired(itemToModify) && oldLvl >= level) { // Burden removal but the item is not fully repaired
					book.setType(Material.BOOK);
					meta = book.getItemMeta();
					meta.displayName(selEnch.displayName(level).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
					lore.addAll(Lore.fastBuild(true, 40, "\n&c\u26a0 Your item must be fully repaired in order to remove this burden!"));
				} else if (bookPower >= selEnch.getBookRequirement(level) || oldLvl == level) { // Enough book power or the item already has this enchantment level
					meta.displayName(selEnch.displayName(level).decoration(TextDecoration.ITALIC, false));
					
					int oldLevel = pendingChanges.getOrDefault(enchant, oldLvl);
					boolean addingEnch = false;
					String note = "modify!";
					int runicCost;
					
					if (oldLevel > 0) {
						if (oldLevel == level) {
							runicCost = -(selEnch.getRunicValue(oldLevel));
							note = "\u00a7cremove\u00a7b!";
						} else if (oldLevel < level) {
							runicCost = (selEnch.getRunicValue(level-oldLevel));
							addingEnch = true;
						} else {
							runicCost = -selEnch.getRunicValue(oldLevel-level);
						}
					} else {
						runicCost = selEnch.getRunicValue(level);
						addingEnch = true;
						note = "add!";
					}
					
					if (addingEnch) {
						if (oldLvl < level) {
							lore.add(Component.empty());
							lore.add(Component.text("\u00a77 • Lapis Cost: \u00a7r" + level + " Lapis Lazuli").colorIfAbsent(BeanColor.ENCHANT_LAPIS).decoration(TextDecoration.ITALIC, false));
							lore.add(Component.text("\u00a77 • Exp Cost: \u00a7r" + selEnch.getExperienceCost(level) + " Experience Levels").colorIfAbsent(BeanColor.EXPERIENCE).decoration(TextDecoration.ITALIC, false));
						}
						// List Conflicts that are being removed.
						List<Enchantment> conflicts = selEnch.getActiveConflicts(displayItem);
						if (!conflicts.isEmpty()) {
							lore.add(Component.empty());
							lore.add(Component.text("\u00a7c\u26a0 \u00a7cThese Enchantments will be removed:"));
							for (Enchantment conflict : conflicts) {
								lore.add(Component.text("\u00a7c • ").append(Component.translatable(conflict.translationKey(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
								runicCost -= BEnchantment.from(conflict).getRunicValue(displayItem.getItemMeta().getEnchantLevel(conflict));
							}
						}
					}
					
					lore.add(Component.empty());
					if (runicCost == 0) {
						lore.add(Component.text("\u00a77 • No changes to \u00a7r\u269D Runic Capacity").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
					} else if (runicCost > 0) {
						lore.add(Component.text("\u00a77 • Utilises \u00a7r" + runicCost + " \u269D Runic Capacity").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
					} else {
						lore.add(Component.text("\u00a77 • Restores \u00a7r" + (-runicCost) + " \u269D Runic Capacity").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
					}
					
					lore.add(Component.text("\u00a73» \u00a7bClick to " + note));
				} else {
					book.setType(Material.BOOK);
					meta = book.getItemMeta();
					meta.displayName(selEnch.displayName(level).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
					lore.add(Component.empty());
					lore.add(Component.text("\u00a7c\u26a0 \u00a7cThis enchanting table needs \u00a7d"+(selEnch.getBookRequirement(level)-bookPower)));
					lore.add(Component.text("\u00a7c more \u00a7d\u270e Enchanting Power\u00a7c in order"));
					lore.add(Component.text("\u00a7c to apply this enchantment!"));
				}
				
				meta.lore(lore);
				book.setItemMeta(meta);
				
				i.setItem(enchSlot, book);
			}
			
			i.setItem(4, data == 2 ? backToListB : backToList);
		// Back to front
		} else if (slot == 4) {
			if (data != 0) {
				data = 0;
				page = 0;
				mapping.clear();
				i.setItem(5, bBlank);
				i.setItem(4, bBlank);
				
				resetBrowns();
				i.setItem(21, pageEnchantsDetails);
				i.setItem(23, pageBurdensDetails);
				i.setItem(25, pageRunes);
				p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.5F, 1.0F);
			}
		}
	}
	
	@Override
	public void onInventoryOpened() {
		ItemStack bookshelfPower = new ItemStack(Material.BOOKSHELF);
		bookshelfPower.editMeta(meta -> {
			if (bookPower > 0) {
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			meta.displayName(Component.text("\u00a7d\u270e Enchanting Power"));
			meta.lore(Arrays.asList(Component.text("\u00a77This value represents the cumulative power"), Component.text("\u00a77value of the Bookshelves surrounding this"),
									Component.text("\u00a77Enchanting Table. The higher this value is,"), Component.text("\u00a77the more enchantment options you have."), Component.empty(),
					Component.text("\u00a77Enchanting Power: " + (bookPower > 0 ? "\u00a7d" : "\u00a78") + bookPower)));
		});
		
		i.setItem(45, bookshelfPower);
		updateLapisItem();
	}
	
	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		e.setCancelled(true);
		return ((!(e.getRawSlot() > i.getSize()) || e.isShiftClick() && itemToModify == null) && pp.onCdElseAdd("guiClick", 200, true));
	}
	
	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.55F, 1.0F);
	}
	
	private void showEnchantments() {
		resetBrowns();
		int x = 0;
		for (BEnchantment ench : BEnchantment.values()) {
			if (customItem != null && customItem.getForbiddenEnchantments().contains(ench)) continue;
			if (!ench.inEnchantingTable()) continue;
			if (ench.isCursed()) continue;
			if (ench.isTreasure()) continue;
			if (ench.canEnchantItem(itemToModify)) {
				List<Component> lore = new ArrayList<>(ench.getLore(ench.getMaxLevel()));
				int enchSlot = 12 + x + (4 * (x/5));
				mapping.put(enchSlot, ench);
				ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta meta = book.getItemMeta();

				if (bookPower >= ench.getBookRequirement(1)) {
					meta.displayName(ench.displayName().color(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));
				} else {
					book.setType(Material.BOOK);
					meta = book.getItemMeta();
					meta.displayName(ench.displayName().color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
					lore.add(Component.empty());
					lore.add(Component.text("\u00a7c\u26a0 \u00a7cThis enchanting table needs \u00a7d"+(ench.getBookRequirement(1)-bookPower)));
					lore.add(Component.text("\u00a7c more \u00a7d\u270e Enchanting Power\u00a7c in order"));
					lore.add(Component.text("\u00a7c to apply this enchantment!"));
				}
				
				lore.add(Component.empty());
				// List Conflicts
				List<Enchantment> conflicts = ench.getActiveConflicts(displayItem);
				if (!conflicts.isEmpty()) {
					lore.add(Component.text("\u00a7c\u26a0 \u00a7cEnchantment Conflicts:"));
					for (Enchantment conflict : conflicts)
						lore.add(Component.text("\u00a7c • ").append(Component.translatable(conflict.translationKey(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
					lore.add(Component.empty());
				}
				
				lore.add(Component.text("\u00a78Maximum Level: " + ench.getMaxLevel()));
				
				int oldLevel = pendingChanges.getOrDefault(ench, itemToModify.getItemMeta().getEnchantLevel(ench));
				if (oldLevel > 0) {
					lore.add(Component.text("\u00a73» \u00a7bClick to modify!"));
				} else {
					lore.add(Component.text("\u00a73» \u00a7bClick to view!"));
				}
				
				meta.lore(lore);
				book.setItemMeta(meta);
				
				i.setItem(enchSlot, book);
				if (++x > 14) break;
			}
		}
	}
	
	private void showBurdens() {
		resetBrowns();
		int x = 0;
		for (BEnchantment ench : BEnchantment.values()) {
			if (!ench.inEnchantingTable()) continue;
			if (!ench.isCursed()) continue;
			if (ench.isTreasure()) continue;
			if (ench.canEnchantItem(itemToModify)) {
				List<Component> lore = new ArrayList<>(ench.getLore(ench.getMaxLevel()));
				int enchSlot = 12 + x + (4 * (x/5));
				mapping.put(enchSlot, ench);
				ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta meta = book.getItemMeta();
				
				if (bookPower >= ench.getBookRequirement(1)) {
					meta.displayName(ench.displayName().color(BeanColor.ENCHANT_BURDEN).decoration(TextDecoration.ITALIC, false));
				} else {
					book.setType(Material.BOOK);
					meta = book.getItemMeta();
					meta.displayName(ench.displayName().color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
					lore.add(Component.empty());
					lore.add(Component.text("\u00a7c\u26a0 \u00a7cThis enchanting table needs \u00a7d"+(ench.getBookRequirement(1)-bookPower)));
					lore.add(Component.text("\u00a7c more \u00a7d\u270e Enchanting Power\u00a7c in order"));
					lore.add(Component.text("\u00a7c to apply this burden!"));
				}
				
				lore.add(Component.empty());
				// List Conflicts
				List<Enchantment> conflicts = ench.getActiveConflicts(displayItem);
				if (!conflicts.isEmpty()) {
					lore.add(Component.text("\u00a7c\u26a0 \u00a7cEnchantment Conflicts:"));
					for (Enchantment conflict : conflicts)
						lore.add(Component.text("\u00a7c • ").append(Component.translatable(conflict.translationKey(), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
					lore.add(Component.empty());
				}
				
				lore.add(Component.text("\u00a78Maximum Level: " + ench.getMaxLevel()));
				
				int oldLevel = pendingChanges.getOrDefault(ench, itemToModify.getItemMeta().getEnchantLevel(ench));
				if (oldLevel > 0) {
					lore.add(Component.text("\u00a73» \u00a7bClick to modify!"));
				} else {
					lore.add(Component.text("\u00a73» \u00a7bClick to view!"));
				}
				
				meta.lore(lore);
				book.setItemMeta(meta);
				
				i.setItem(enchSlot, book);
				if (++x > 14) break;
			}
		}
	}
	
	private void useLapisLazuli(int count) {
		if (table == null) return;
		int remaining = lapis - count;
		if (remaining < 0) {
			p.getInventory().removeItem(new ItemStack(Material.LAPIS_LAZULI, -remaining));
			remaining = 0;
		}

		table.getPersistentDataContainer().set(KEY_LAPIS, PersistentDataType.SHORT, (short)remaining);
		getPlugin().getItemTrackingManager().incrementDemanifestationCount(new ItemStack(Material.LAPIS_LAZULI), DemanifestationReason.FUEL, count);
		forceUpdateLapis(table);
	}
	
	private void updateLapisItem() {
		ItemStack lapis = lapisChest;
		lapis.editMeta(meta -> {
			meta.displayName(Component.text("Lazuli Storage Compartment™", BeanColor.ENCHANT_LAPIS).decoration(TextDecoration.ITALIC, false));
			String title = lapisTitles[lapisLevel >= lapisTitles.length ? lapisTitles.length : lapisLevel];
			meta.lore(Arrays.asList(Component.text("\u00a77A convenient and upgradable lapis"),
					Component.text("\u00a77storage for all of your enchanting"),
					Component.text("\u00a77needs! Right-Click the Enchanting"),
					Component.text("\u00a77Table to add lapis."),
					Component.empty(),
					Component.text("\u00a77Lapis Stored: " + (this.lapis <= 0 ? "\u00a78" : "\u00a7b") + this.lapis + "\u00a77/\u00a7r" + df.format(128 + (lapisLevel*lapisLevel) * 64)).colorIfAbsent(BeanColor.ENCHANT_LAPIS).decoration(TextDecoration.ITALIC, false),
					Component.text("\u00a77Compartment Size: \u00a7e" + title),
					Component.empty(),
					Component.text("\u00a76» \u00a7eClick to view upgrades!")));
		});
		
		i.setItem(46, lapis);
	}
	
	private void updateConfirmationItems() {
		ItemStack item = confirmEmpty;
		ItemStack glassItem = confNone;

		confirm = null;

		remainingRunicScore = 15;
		xpCost = 0;
		lapisCost = 0;
		
		if (itemToModify != null) {
			displayItem = itemToModify.clone();
			if (!pendingChanges.isEmpty()) {
				List<TextComponent> lore = new ArrayList<>();
				lore.add(Component.text("\u00a77Pending changes: "));
				
				pendingChanges.forEach((ench, level) -> {
					BEnchantment bEnch = BEnchantment.from(ench);
					BeanColor color = bEnch.isCursed() ? BeanColor.ENCHANT_BURDEN : BeanColor.ENCHANT;
					int oldLevel = itemToModify.getItemMeta().getEnchantLevel(ench);
					if (oldLevel > 0) {
						if (level <= 0) {
							lore.add(Component.text("\u00a78 - \u00a77Remove ").append(bEnch.displayName(oldLevel)).decoration(TextDecoration.ITALIC, false));
							displayItem.removeEnchantment(ench);
						} else {
							lore.add(Component.text("\u00a78 • \u00a77Modify ").append(bEnch.displayName()).append(Component.text("\u00a77 " + Utils.toRoman(oldLevel) + " \u00a77\u00a7l\u2192\u00a7r " + Utils.toRoman(level))).colorIfAbsent(color).decoration(TextDecoration.ITALIC, false));
							displayItem.addUnsafeEnchantment(ench, level);
							if (level > oldLevel) {
								xpCost += bEnch.getExperienceCost(level);
								lapisCost += level;
							}
						}
					} else {
						lore.add(Component.text("\u00a78 + \u00a77Add ").append(bEnch.displayName(level).decoration(TextDecoration.ITALIC, false)));
						
						// Remove all conflicts
						displayItem.addUnsafeEnchantment(ench, level);
						if (level > oldLevel) {
							xpCost += bEnch.getExperienceCost(level);
							lapisCost += level;
						}
						List<Enchantment> conflicts = bEnch.getActiveConflicts(displayItem);
						if (!conflicts.isEmpty()) {
							for (Enchantment conflict : conflicts) {
								lore.add(Component.text("\u00a78 - \u00a77Remove ").append(conflict.displayName(displayItem.getItemMeta().getEnchantLevel(conflict)).append(Component.text("\u00a78 (Conflict)"))).decoration(TextDecoration.ITALIC, false));
								displayItem.removeEnchantment(conflict);
							}
						}
					}
				});
				
				displayItem.getEnchantments().forEach((enchantment, level) -> remainingRunicScore -= BEnchantment.from(enchantment).getRunicValue(level));

				// TODO: Add more information about Runic Capacity

				// Confirmation Item
				confirm = new PurchaseOption(item, item.getItemMeta().displayName(), new Lore(lore));
				confirm.setDemanifestationReason(DemanifestationReason.ENCHANTING);
				confirm.setPurchaseWord("enchant");
				confirm.setSubtext("Enchanting");
				confirm.addFakeCost(Component.text(remainingRunicScore + " \u269D Remaining Runic Capacity", BeanColor.ENCHANT), remainingRunicScore >= 0);
				confirm.addExperienceCost(xpCost);

				// Allow the usage of Lapis Lazuli inside the player's inventory
				boolean can = true;
				if (lapisCost > lapis)
					can = p.getInventory().containsAtLeast(new ItemStack(Material.LAPIS_LAZULI), lapisCost - lapis);

				if (lapisCost > 0)
					confirm.addFakeCost(Component.text("" + lapisCost + " Lapis Lazuli", BeanColor.ENCHANT_LAPIS), can);

				//lore.add(Component.text((remainingRunicScore < 0 ? "\u00a7c\u274c " : "") + "\u00a77Your item has \u00a7r" + remainingRunicScore + " \u269D Runic Capacity\u00a77 remaining.").colorIfAbsent(BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false));

				if (!confirm.canPurchase(p)) {
					item = confirmCant;
					glassItem = confRed;
				} else {
					item = confirmCan;
					glassItem = confGreen;
				}

				confirm.setDisplayName(item.getItemMeta().displayName());
				item = confirm.getDisplayItem(p);
			}
			
			i.setItem(confirmationSlot + 1, BeanItem.formatItem(displayItem));
		} else { // No item to modify
			i.setItem(confirmationSlot + 1, bBlank);
		}

		for (int x = 46; ++x < 54;)
			i.setItem(x, glassItem);
		i.setItem(50, closeUI);

		i.setItem(confirmationSlot, item);
	}
	
	private void resetBrowns() {
		for (int x = -1; ++x < 5;)
			for (int y = -1; ++y < 3;)
				i.setItem(x + 12 + y * 9, gPDef);
		mapping.clear();
	}
	
	private void updateEnchantSlot() {
		new BukkitRunnable() {

			@Override
			public void run() {
				itemToModify = i.getItem(enchantingSlot);
				displayItem = itemToModify == null ? null : itemToModify.clone();
				customItem = BeanItem.from(itemToModify, BItemDurable.class);
				confirm = null;
				
				data = 0;
				page = 0;
				xpCost = 0;
				lapisCost = 0;
				selEnch = null;
				mapping.clear();
				pendingChanges.clear();
				
				i.setItem(5, bBlank);
				i.setItem(4, bBlank);
				
				resetBrowns();
				
				if (itemToModify != null) {
					p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
					i.setItem(21, pageEnchantsDetails);
					i.setItem(23, pageBurdensDetails);
					i.setItem(25, pageRunes);
				}
				
				updateConfirmationItems();
			}
		}.runTaskLater(plugin, 1L);
	}
	
	private boolean isEnchantable(ItemStack item) {
		if (item == null) return false;
		BeanItem customItem = BeanItem.from(item);
		return Enchantment.DURABILITY.canEnchantItem(item) && (customItem == null || customItem.isEnchantable()/* && !item.getItemMeta().hasEnchant(BEnchantment.BURDEN_IRREVOCABLE)*/);
	}
	
	/**
	 * Update the lapis container for all active users of this enchanting table, so they are fed
	 * the correct information when they try to upgrade their item.
	 */
	public static void forceUpdateLapis(EnchantingTable table) {
		if (table == null) return;
		table.update(true);
		int lapisCount = table.getPersistentDataContainer().getOrDefault(KEY_LAPIS, PersistentDataType.SHORT, (short)0);
		
		getAllViewers(BeanGuiEnchantingTable.class).forEach(gui -> {
			if (gui.table.getLocation().equals(table.getLocation())) {
				gui.lapis = lapisCount;
				gui.updateLapisItem();
				gui.updateConfirmationItems();
			}
		});
	}

	public static String getLapisStorageTitle(int level) {
		if (level < 0) level = 0;
		else if (level >= lapisTitles.length) level = lapisTitles.length - 1;
		return lapisTitles[level];
	}
	
}
