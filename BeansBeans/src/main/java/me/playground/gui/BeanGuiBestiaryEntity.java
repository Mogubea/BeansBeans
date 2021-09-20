package me.playground.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.loot.LootEnchantEntry;
import me.playground.loot.LootEntry;
import me.playground.loot.LootTable;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BeanGuiBestiaryEntity extends BeanGuiBestiary {
	
	// TODO: move all skulls to a class that holds them in non-static references maybe?
	private final static LinkedHashMap<EntityType, ItemStack> creatureHeads = new LinkedHashMap<EntityType, ItemStack>();
	private final static EntityType[] creatures;
	private final static ItemStack notUnlocked = Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM3MGNhNDdiNjE3M2FiNThlNmE4MDE4NDg2ZTJmOGJhYTgzOTdhYjYxNGFlMmU2OTY4NDkxOTZiYWE3YyJ9fX0=");
	private final static ItemStack missingKill = newItem(notUnlocked, "\u00a7c???", "\u00a78Find and kill this mob to", "\u00a78add it to your Bestiary!");
	private final static ItemStack missingLoot = newItem(notUnlocked, "\u00a7c???", "\u00a78Unlock information about this unknown", "\u00a78piece of loot by earning it first!");
	private final static ItemStack blank2 = newItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "");
	
	static {
		creatureHeads.put(EntityType.BLAZE, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ=="));
		creatureHeads.put(EntityType.CHICKEN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ=="));
		creatureHeads.put(EntityType.COW, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2RmYTBhYzM3YmFiYTJhYTI5MGU0ZmFlZTQxOWE2MTNjZDYxMTdmYTU2OGU3MDlkOTAzNzQ3NTNjMDMyZGNiMCJ9fX0="));
		creatureHeads.put(EntityType.CREEPER, new ItemStack(Material.CREEPER_HEAD));
		creatureHeads.put(EntityType.DROWNED, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0ZGY3OWM0OTEwNGIxOThjZGFkNmQ5OWZkMGQwYmNmMTUzMWM5MmQ0YWI2MjY5ZTQwYjdkM2NiYmI4ZTk4YyJ9fX0="));
		creatureHeads.put(EntityType.ENDERMAN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMGIzNmQ1M2ZmZjY5YTQ5YzdkNmYzOTMyZjJiMGZlOTQ4ZTAzMjIyNmQ1ZTgwNDVlYzU4NDA4YTM2ZTk1MSJ9fX0="));
		creatureHeads.put(EntityType.HUSK, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NGM2M2M4ZGI1ZjRjYTYyOGQ2OWEzYjFmOGEzNmUyOWQ4ZmQ3NzVlMWE2YmRiNmNhYmI0YmU0ZGIxMjEifX19"));
		creatureHeads.put(EntityType.PHANTOM, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQ2ODMwZGE1ZjgzYTNhYWVkODM4YTk5MTU2YWQ3ODFhNzg5Y2ZjZjEzZTI1YmVlZjdmNTRhODZlNGZhNCJ9fX0="));
		creatureHeads.put(EntityType.PIG, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIxNjY4ZWY3Y2I3OWRkOWMyMmNlM2QxZjNmNGNiNmUyNTU5ODkzYjZkZjRhNDY5NTE0ZTY2N2MxNmFhNCJ9fX0="));
		creatureHeads.put(EntityType.PIGLIN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBiYzlkYmI0NDA0YjgwMGY4Y2YwMjU2MjIwZmY3NGIwYjcxZGJhOGI2NjYwMGI2NzM0ZjRkNjMzNjE2MThmNSJ9fX0="));
		creatureHeads.put(EntityType.PIGLIN_BRUTE, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzMDBlOTAyNzM0OWM0OTA3NDk3NDM4YmFjMjllM2E0Yzg3YTg0OGM1MGIzNGMyMTI0MjcyN2I1N2Y0ZTFjZiJ9fX0="));
		creatureHeads.put(EntityType.ZOMBIFIED_PIGLIN, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzNTg0MmFmNzY5MzgwZjc4ZThiOGE4OGQxZWE2Y2EyODA3YzFlNTY5M2MyY2Y3OTc0NTY2MjA4MzNlOTM2ZiJ9fX0="));
		creatureHeads.put(EntityType.SHEEP, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMxZjljY2M2YjNlMzJlY2YxM2I4YTExYWMyOWNkMzNkMThjOTVmYzczZGI4YTY2YzVkNjU3Y2NiOGJlNzAifX19"));
		creatureHeads.put(EntityType.SKELETON, new ItemStack(Material.SKELETON_SKULL));
		creatureHeads.put(EntityType.SPIDER, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0="));
		creatureHeads.put(EntityType.STRAY, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM1MDk3OTE2YmMwNTY1ZDMwNjAxYzBlZWJmZWIyODcyNzdhMzRlODY3YjRlYTQzYzYzODE5ZDUzZTg5ZWRlNyJ9fX0="));
		creatureHeads.put(EntityType.WITHER, Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RmNzRlMzIzZWQ0MTQzNjk2NWY1YzU3ZGRmMjgxNWQ1MzMyZmU5OTllNjhmYmI5ZDZjZjVjOGJkNDEzOWYifX19"));
		creatureHeads.put(EntityType.WITHER_SKELETON, new ItemStack(Material.WITHER_SKELETON_SKULL));
		creatureHeads.put(EntityType.ZOMBIE, new ItemStack(Material.ZOMBIE_HEAD));
		
		creatures = creatureHeads.keySet().toArray(new EntityType[0]);
	}
	
	private EntityType entity;
	//private boolean passive;
	
	public BeanGuiBestiaryEntity(Player p) {
		super(p);
		
		this.name = "Bestiary";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,blank,blank,null,blank,blank,blank,blank,
				blank2,blank2,blank2,blank,blank,blank,blank2,blank2,blank2,
				blank2,null,null,null,null,null,null,null,blank2,
				blank2,null,null,null,null,null,null,null,blank2,
				blank2,blank2,blank2,blank2,blank2,blank2,blank2,blank2,blank2,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final ItemStack item = e.getClickedInventory().getItem(e.getSlot());
		if (item == null) return;
		
		if (getEntityType() != null) {
			if (p.hasPermission("bean.loot")) {
				if (slotToLootId.containsKey(e.getRawSlot())) {
					LootEntry entry = slotToLootId.get(e.getRawSlot());
					if (entry == null) return; // The odds of a LootEntry vanishing while you're looking at it is unlikely, but not zero.
					p.getInventory().addItem(entry.generateReward(p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS), pp.getLuck()));
					p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.4F, 0.8F);
				}
			}
		} else {
			if (e.getRawSlot() >= creatures.length) return;
			setEntityType(creatures[e.getRawSlot()]);
		}
	}
	
	// Purely for clicking to generate the loot lol, could maybe be done better but I think hashing slots -> lootEntry would be easier than doing wacky math every click.
	private HashMap<Integer, LootEntry> slotToLootId = new HashMap<Integer, LootEntry>();
	
	@Override
	public void onInventoryOpened() {
		final ItemStack[] contents = presetInv.clone();
		
		// XXX: Statistics and information about the entity in question.
		if (getEntityType() != null) {
			slotToLootId.clear();
			contents[4] = newItem(creatureHeads.get(getEntityType()), Component.translatable(getEntityType().translationKey()).color(TextColor.color(0x13bf27)));
			
			LootTable table = getPlugin().lootManager().getLootTable(getEntityType());
			if (table != null) {
				int loots = table.getEntries().size();
				int offset = (loots > 1) ? 21 : 22;
				offset = 21 - (Math.floorDiv(loots-1, 4) * 1);
				
				float luckLevel = pp.getLuck();
				
				for (int e = -1; ++e < loots;) {
					ItemStack entryDisplay;
					ArrayList<Component> lore = new ArrayList<Component>();
					int displaySlot = (e%7) + (Math.floorDiv(e, 7)*9) + offset;
					int obtained = getStats().getStat(StatType.LOOT_EARNED, table.getEntries().get(e).getId()+"");
					LootEntry entry = table.getEntries().get(e);
					slotToLootId.put(displaySlot, entry);
					
					if (obtained > 0) {
						int maxStack = entry.getMaxStackSize();
						int lootingMax = (entry.allowsLooting() ? maxStack + p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) : maxStack);
						
						entryDisplay = entry.getDisplayStack().clone();
						
						float chance = (float)entry.getChance()/10000;
						float luckChance = (float)entry.getChance(entry.allowsLuck() ? luckLevel : 0)/10000;
						float diff = luckChance-chance;
						
						lore.add(Component.text("\u00a77Chance: \u00a7f" + luckChance + "% " + (diff != 0 ? (diff > 0 ? "\u00a7a(+"+diff+")" : "\u00a7c(-"+diff+")") : "")));
						
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
									lore.add(Component.text("\u00a77 * ").append(Component.translatable(ench.getEnchantment().translationKey()).decoration(TextDecoration.ITALIC, false).color(BeanColor.ENCHANT)).append(Component.text(" \u00a77(\u00a7f"+oneDec.format(ench.getChance(luckLevel))+"%\u00a77)")));
									
									Component hell = Component.text("\u00a77  Levels: ");
									
									// TODO: update
									for (int lvl = -1; ++lvl < ench.getEnchantment().getMaxLevel();)
										hell = hell.append(Component.text("\u00a77[\u00a7r" + Utils.toRoman(lvl+1) + " \u00a77(\u00a7f" + oneDec.format(ench.getChanceOfLvl(lvl+1, luckLevel)) + "%\u00a77]").decoration(TextDecoration.ITALIC, false).color(BeanColor.ENCHANT));
									lore.add(hell);
								}
							}
						}
						
						lore.add(Component.text(""));
						lore.add(Component.text("\u00a78\u00a7oObtained " + obtained + " times."));
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
			}
			
		// XXX: A list of the entities available to examine in the Beastiary.
		} else {
			int size = creatures.length;
			for (int x = -1; ++x < size;) {
				int kills = getStats().getStat(StatType.KILLS, creatures[x].name());
				if (kills > 0) { // Unlocked
					int obtained = 0, loots = 0;
					LootTable table = getPlugin().lootManager().getLootTable(creatures[x]);
					if (table != null) {
						loots = table.getEntries().size();
						for (int e = -1; ++e < loots;)
							if (getStats().getStat(StatType.LOOT_EARNED, table.getEntries().get(e).getId()+"") > 0)
								obtained++;
					}
					
					ItemStack displayItem = newItem(creatureHeads.getOrDefault(creatures[x], notUnlocked), Component.translatable(creatures[x].translationKey()).color(TextColor.color(0x13bf27)), "",
							"\u00a77Kills: \u00a7a" + df.format(getStats().getStat(StatType.KILLS, creatures[x].name())),
							"\u00a77Loot Found: " + "\u00a7a" + obtained + "\u00a77/\u00a72" + loots,
							Utils.getProgressBar('-', 16, obtained, loots, ChatColor.DARK_GRAY, ChatColor.GREEN) +  (obtained>=loots ? "\u00a76 " : "\u00a7a ") + oneDec.format((((float)obtained/(float)loots) * 100F)) + "%");
					contents[x] = displayItem;
				} else { // Not
					ItemStack displayItem = missingKill;
					contents[x] = displayItem;
				}
			}
		}
		
		contents[50] = newItem(new ItemStack(Material.KNOWLEDGE_BOOK), "\u00a72What is the Bestiary?", "", 
				"\u00a77The \u00a72Bestiary\u00a77 is an interface where",
				"\u00a77you can view information about various",
				"\u00a77things you've encountered in your adventure!",
				"",
				"\u00a77When viewing loot information, your \u00a7aLuck Level",
				"\u00a77and \u00a7bLooting Enchantments\u00a77 will affect what's shown!",
				"\u00a77 * \u00a7aLuck Level\u00a77 affects the \u00a7fChances\u00a77.",
				"\u00a77 * \u00a7bLooting\u00a77 affects the \u00a7eMaximum Quantity\u00a77.");
		
		i.setContents(contents);
	}
	
	private EntityType getEntityType() {
		return entity;
	}
	
	private void setEntityType(EntityType type) {
		this.entity = type;
		onInventoryOpened();
	}
	
	@Override
	public boolean checkPageClick(InventoryClickEvent e) {
		e.setCancelled(true);
		final ItemStack i = e.getClickedInventory().getItem(e.getSlot());
		if (i != null) {
			if (pp.onCdElseAdd("guiClick", 300))
				return true;
			
			if (i.isSimilar(goBack)) {
				if (getEntityType() != null)
					setEntityType(null);
				else
					new BeanGuiMainMenu(p).openInventory();
				return true;
			} else if (i.isSimilar(nextPage)) {
				pageUp();
				return true;
			} else if (i.isSimilar(prevPage)) {
				pageDown();
				return true;
			}
		}
		return false;
	}
	
}
