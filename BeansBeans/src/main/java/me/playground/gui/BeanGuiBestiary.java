package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.loot.LootEnchantEntry;
import me.playground.loot.LootEntry;
import me.playground.loot.LootTable;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiBestiary extends BeanGui {
	
	/*private static LoadingCache<Integer, Integer> progressCache = CacheBuilder.from("maximumSize=100,expireAfterAccess=15m")
			.build(
					new CacheLoader<Integer, Integer>() {
						public Integer load(Integer playerId) throws Exception { // if the key doesn't exist, request it via this method
							PlayerProfile prof = PlayerProfile.fromIfExists(playerId);
							
							
							return prof;
						}
					});
	Cache % progress in future to save memory on calculating it every time the gui is opened. Will require a custom dirty flag system in stats/playerprofile to make this accurate though.
	*/
	
	protected final ItemStack blank = newItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1), Component.text("Bestiary", BeanColor.BESTIARY));
	protected final ItemStack pageEntities = newItem(new ItemStack(Material.ZOMBIE_HEAD, 1), Component.text("Creatures", BeanColor.BESTIARY));
	protected final ItemStack pageFishing = newItem(new ItemStack(Material.FISHING_ROD, 1), Component.text("Fishing", BeanColor.BESTIARY));
	protected final ItemStack notUnlocked = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM3MGNhNDdiNjE3M2FiNThlNmE4MDE4NDg2ZTJmOGJhYTgzOTdhYjYxNGFlMmU2OTY4NDkxOTZiYWE3YyJ9fX0=");
	protected final ItemStack missingLoot = newItem(notUnlocked, "\u00a7c???", "\u00a78Unlock information about this unknown", "\u00a78piece of loot by earning it first!");
	protected final ItemStack blank2 = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "");
	protected final ItemStack whatIsThis = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), Component.text("What is the Bestiary?", BeanColor.BESTIARY), "", 
			"\u00a77The \u00a72Bestiary\u00a77 is an interface where",
			"\u00a77you can view information about various",
			"\u00a77things you've encountered in your adventure!");
	
	public BeanGuiBestiary(Player p) {
		super(p);
		
		setName("Bestiary");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,pageEntities,null,pageFishing,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,whatIsThis,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		
		if (!slotToLootId.isEmpty()) {
			if (p.hasPermission("bean.loot")) {
				if (slotToLootId.containsKey(e.getRawSlot())) {
					LootEntry entry = slotToLootId.get(slot);
					if (entry == null) return; // The odds of a LootEntry vanishing while you're looking at it is unlikely, but not zero.
					ItemStack i = p.getInventory().getItemInMainHand();
					p.getInventory().addItem(entry.generateReward(
							i.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS), 
							pp.getLuck(), 
							i.getItemMeta().getEnchantLevel(Enchantment.FIRE_ASPECT) > 0 || i.getItemMeta().getEnchantLevel(Enchantment.ARROW_FIRE) > 0));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 0.8F);
				}
			}
		} else {
			switch (slot) {
			case 21: 
				new BeanGuiBestiaryEntity(p).openInventory();
				break;
			case 23:
				new BeanGuiBestiaryFishing(p).openInventory();
				break;
			}
		}
	}
	
	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		contents[48] = whatIsThis;
		i.setContents(contents);
	}
	
	protected PlayerStats getStats() {
		return tpp.getStats();
	}
	
	protected HashMap<Integer, LootEntry> slotToLootId = new HashMap<Integer, LootEntry>();
	protected ItemStack[] showLoot(ItemStack[] contents, LootTable table, boolean system1, int rows, int column) {
		if (table == null) return contents;
		slotToLootId.clear();
		
		int loots = table.getEntries().size();
		int offset = (loots > 1) ? 21 : 22;
		offset = 21 - (Math.floorDiv(loots-1, rows * 2) * 1);
		
		float luckLevel = pp.getLuck();
		if (table.getName().equals("fishing"))
			luckLevel += p.getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LUCK);
		
		for (int e = -1; ++e < loots;) {
			ItemStack entryDisplay;
			ArrayList<Component> lore = new ArrayList<Component>();
			int displaySlot = (e%column) + (Math.floorDiv(e, column)*9) + offset;
			int obtained = getStats().getStat(StatType.LOOT_EARNED, table.getEntries().get(e).getId()+"");
			LootEntry entry = table.getEntries().get(e);
			slotToLootId.put(displaySlot, entry);
			
			if (obtained > 0 || p.hasPermission("bean.loot")) {
				entryDisplay = entry.getDisplayStack().clone();
				if (entry.hasDescription()) {
					for (String s : entry.getDescription().split("\n"))
						lore.add(Component.text(s));
				} else {
					int maxStack = entry.getMaxStackSize();
					int lootingMax = (entry.allowsLooting() ? maxStack + p.getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS) : maxStack);
					
					float chance, luckChance;
					if (system1) {
						chance = (entry.getChance()/table.getTotalChance(0)) * 100;
						luckChance = (entry.getChance(luckLevel)/table.getTotalChance(luckLevel)) * 100;
					} else {
						chance = (float)entry.getChance();
						luckChance = (float)entry.getChance(luckLevel);
					}
				
					float diff = luckChance-chance;
					
					lore.add(Component.text("\u00a77Chance: \u00a7f" + dec.format(luckChance) + "% " + (diff != 0 ? (diff > 0 ? "\u00a7a(+"+dec.format(diff)+"%)" : "\u00a7c("+dec.format(diff)+"%)") : "")));
					
					if (entry.hasTableRedirect()) {
						lore.add(Component.text("\u00a77One of \u00a7f" + entry.getTableRedirect().getEntries().size() + "\u00a77 items."));
					} else {
						lore.add(Component.text("\u00a77Quantity: \u00a7e" + entry.getMinStackSize() + "\u00a77 - \u00a7e" + lootingMax
								+ (lootingMax > maxStack ? " \u00a7r(+"+(lootingMax-maxStack)+")" : "")).decoration(TextDecoration.ITALIC, false).colorIfAbsent(BeanColor.ENCHANT));
						
						if (entryDisplay.getType().getMaxDurability() > 0) {
							if (entry.getMinDurability() > 0) {
								if (entry.hasDurabilityRange()) // Has a set range of durability in the loot entry
									lore.add(Component.text("\u00a77Durability: \u00a7c" + entry.getMinDurability() + "\u00a77 - \u00a7a" + entry.getMaxDurability()));
								else // Has a set durability in the loot entry
									lore.add(Component.text("\u00a77Durability: \u00a7f" + entry.getMinDurability()));
							}
						}
						
						if (entry.hasPossibleEnchants()) {
							int size = entry.getPossibleEnchants().size();
							lore.add(Component.text("\u00a77Enchants: "));
							for (int a = -1; ++a < size;) {
								LootEnchantEntry ench = entry.getPossibleEnchants().get(a);
								lore.add(Component.text("\u00a77 * ").append(Component.translatable(ench.getEnchantment().translationKey(), BeanColor.ENCHANT).decoration(TextDecoration.ITALIC, false)).append(Component.text(" \u00a77(\u00a7f"+dec.format(ench.getChance(luckLevel))+"%\u00a77)")));
								
								Component hell = Component.text("\u00a77  Levels: ");
								
								// TODO: update
								for (int lvl = -1; ++lvl < ench.getEnchantment().getMaxLevel();)
									hell = hell.append(Component.text("\u00a77[\u00a7r" + Utils.toRoman(lvl+1) + " \u00a77(\u00a7f" + dec.format(ench.getChanceOfLvl(lvl+1, luckLevel)) + "%\u00a77]").decoration(TextDecoration.ITALIC, false).color(BeanColor.ENCHANT));
								lore.add(hell);
							}
						}
					}
				}
				
				lore.add(Component.text(""));
				lore.add(Component.text("\u00a78\u00a7oObtained " + obtained + " times."));
				if (entry.hasCookedVariant())
					lore.add(Component.text("\u00a78\u00a7oCan be cooked with Enchantments."));
			} else {
				entryDisplay = missingLoot;
				if (entry.requiresChargedCreeper() || entry.requiresSkeletonShot()) // Only bother with the slow .clone() method if there's a reason for it.
					entryDisplay = missingLoot.clone();
				lore.addAll(missingLoot.lore());
			}
			
			if (entry.requiresChargedCreeper())
				lore.add(0, Component.text("\u00a77Requires a \u00a7bCharged Creeper"));
			else if (entry.requiresSkeletonShot())
				lore.add(0, Component.text("\u00a77Requries a \u00a7fSkeleton Arrow"));
			
			entryDisplay.lore(lore);
			contents[displaySlot] = entryDisplay;
		}
		return contents;
	}
}
