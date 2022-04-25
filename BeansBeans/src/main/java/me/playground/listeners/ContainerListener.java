package me.playground.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;

import me.playground.enchants.BeanEnchantment;
import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiMainMenu;
import me.playground.gui.BeanGuiShulker;
import me.playground.items.BItemDurable;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillInfo;
import me.playground.playerprofile.skills.SkillType;
import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;

public class ContainerListener extends EventListener {
	
	public ContainerListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler
	public void onCraft(PrepareResultEvent e) {
		final Inventory inv = e.getInventory();
		if (inv instanceof AnvilInventory) return; // Handled later in the class.
		
		// Cancel the cleansing of tools that can't be cleansed.
		if (inv instanceof GrindstoneInventory) {
			BeanItem bi = BeanItem.from(e.getResult());
			if (bi instanceof BItemDurable && !((BItemDurable)bi).isCleansable()) {
				e.setResult(null);
				return;
			}
		}
		
		ItemStack i = e.getResult();
		if (i == null) return;
		if (!(inv instanceof SmithingInventory)) {
			BeanItem.resetItemFormatting(i);
			return;
		}
		
		SmithingRecipe sr = ((SmithingRecipe)((SmithingInventory)inv).getRecipe());
		
		if (sr.willCopyNbt()) {
			// Dumb that I gotta do this. willCopyNbt is necessary but removes nbt from the result before overriding, so we need to force a "merge" of nbt.
			BeanItem bi = BeanItem.from(sr.getResult());
			if (bi != null && bi != BeanItem.from(i))
				i = BeanItem.convert(i, bi);
			
			final Material type = i.getType();
			
			ItemStack original = inv.getContents()[0];
			
			// If tool's name is base item name, update it to new item name - Only smithing
			if (original != null && original.getType() != i.getType()) {
				final Component translatable = Component.translatable(i);
				if (!BeanItem.hasBeenRenamed(i))
					i.editMeta((meta) -> {meta.displayName(bi != null ? bi.getDisplayName() : translatable);});
			}
			
			if (type.getMaxDurability() > 1) {
				int maxDura = BeanItem.getMaxDurability(i);
				float duraPerc = (float)BeanItem.getDurability(original) / (float)BeanItem.getMaxDurability(original);
				BeanItem.setDurability(i, (int)((float)maxDura * duraPerc), maxDura);
			}
		}
		
		BeanItem.formatItem(i);
	}
	
	@EventHandler
	public void onCraft(PrepareItemCraftEvent e) {
		if (e.getInventory() instanceof AnvilInventory || e.getInventory() instanceof SmithingInventory) return;
		if (e.getRecipe() == null) return;
		
		// All Bukkit recipe instances implement Keyed.
		NamespacedKey key = ((Keyed)e.getRecipe()).getKey();
		if (!key.getNamespace().equals(NamespacedKey.MINECRAFT)) {
			if (!e.getView().getPlayer().hasDiscoveredRecipe(key))
				e.getInventory().setResult(null);
			e.getView().getPlayer().sendActionBar(Component.text("\u00a7cYou have not unlocked this recipe."));
			return;
		}
		
		ItemStack i = e.getInventory().getResult();
		
		if (i != null) {
			// Prevent dyeing custom items.
			if (BeanItem.from(i) != null && i.getItemMeta() instanceof LeatherArmorMeta) {
				e.getInventory().setResult(null);
				return;
			}
			
			e.getInventory().setResult(BeanItem.formatItem(i));
		}
	}
	
	// TODO: Create a proper custom enchantment method.
	
	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		BeanItem bi = BeanItem.from(e.getItem());
		if (bi != null && bi instanceof BItemDurable) {
			BItemDurable tool = ((BItemDurable)bi);
			if (!tool.isEnchantable()) { e.setCancelled(true); return; }
			if (!tool.hasForbiddenEnchantments()) { return; }
			
			Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>(e.getEnchantsToAdd());
			
			enchants.forEach((enchant, level) -> {
				if (!tool.isEnchantAllowed(enchant)) {
					// Remove bad enchant
					e.getEnchantsToAdd().remove(enchant);
					
					List<Enchantment> possibilities = new ArrayList<Enchantment>(tool.getValidEnchantments());
					
					// Remove current enchants
					possibilities.removeAll(e.getEnchantsToAdd().keySet());
					
					// Remove custom enchantments with a minimum level requirement too low
					List<Enchantment> clone = new ArrayList<Enchantment>(possibilities);
					for (Enchantment possibility : clone)
						if (possibility instanceof BeanEnchantment)
							if (e.getExpLevelCost() < ((BeanEnchantment)enchant).getLevelRequirement())
								possibilities.remove(enchant);
						
					if (possibilities.isEmpty())
						return;
					
					int fromMax = enchant.getMaxLevel() - level;
					Enchantment replacement = possibilities.get(getPlugin().getRandom().nextInt(tool.getValidEnchantments().size()));
					level = replacement.getMaxLevel() - fromMax;
					if (level < 1) level = 1;
					
					e.getEnchantsToAdd().put(replacement, level);
				}
			});
		}
		
		Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> BeanItem.formatItem(e.getItem()));
	}
	
	@EventHandler
	public void preEnchant(PrepareItemEnchantEvent e) {
		BeanItem bi = BeanItem.from(e.getItem());
		if (bi != null && !bi.isEnchantable())
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		List<HumanEntity> list = e.getViewers();
		if (list.isEmpty())
			return;
		
		PlayerProfile pp = PlayerProfile.from((Player) list.get(0));
		if (pp.getBeanGui() != null) {
			pp.getBeanGui().onInventoryClosed(e);
			pp.closeBeanGui();
		}
		
		customRepairHold.remove(pp.getUniqueId());
	}
	
	// Store the custom repair remaining item so it can be put back after automatically taken by anvil logic.
	private HashMap<UUID, ItemStack> customRepairHold = new HashMap<UUID, ItemStack>();
	
	@EventHandler
	public void onAnvilPrepare(PrepareAnvilEvent e) {
		final ItemStack[] contents = e.getInventory().getContents();
		ItemStack result = e.getResult();
		final BeanItem biOne = BeanItem.from(contents[0]);
		final boolean isEnchantBook = contents[1] != null && contents[1].getType() == Material.ENCHANTED_BOOK;
		boolean customRepairFound = false;
		float newCost = e.getInventory().getRepairCost();
		
		// Check for custom repair materials, gotta check even if there's already a result since renaming exists..
		if (biOne != null && biOne instanceof BItemDurable) {
			BItemDurable bid = ((BItemDurable)biOne);
			if (!isEnchantBook) {
				// Sort out durability
				final float repairPerc = bid.getRepairPercentage(contents[1]);
				if (repairPerc > 0f) {
					final int stackSize = contents[1].getAmount();
					final float totalRepair = repairPerc * stackSize;
					
					result = contents[0].clone();
					customRepairFound = true;
					
					// Increase cost based on the amount of items needed to do the repair
					final int maxAmount =  (int) (((BeanItem.getMaxDurability(contents[0]) - BeanItem.getDurability(contents[0])) / repairPerc) + 1);
					final int amountUsed = Math.min(stackSize, maxAmount);
					newCost += amountUsed;
					
					// Store the resulting item in the second slot after repair
					ItemStack remaining = contents[1].clone();
					remaining.setAmount((int) (remaining.getAmount() - amountUsed));
					customRepairHold.put(e.getView().getPlayer().getUniqueId(), remaining);
					
					BeanItem.addDurability(result, (int) ((totalRepair/100F) * bid.getMaxDurability())); // Values over the max are sorted in #addDurability
				}
			}
		}
		
		// Forget the rest.
		if (result == null) return;
		
		// Prevent a result from appearing if a player is trying to combine different custom items or a custom item with a non custom item.
		// But still trying to allow every other typical result (eg. custom pickaxe + repair material)
		if (!customRepairFound && !isEnchantBook) { // Don't bother if it's an enchantment book.
			if (contents[1] != null) {
				BeanItem bi2 = BeanItem.from(contents[1]);
				if (!(biOne == null && bi2 == null)) {
					if (contents[0].getType().equals(contents[1].getType())) {
						e.setResult(null);
						return;
					}
				}
			}
		}
		
		BeanItem bi = BeanItem.from(result);
		if (bi != null && bi instanceof BItemDurable) {
			BItemDurable bid = ((BItemDurable)bi);
			// Prevent any forbidden enchantments, remove 1 RepairCost per enchantment removed if it's an item with durability.
			if (bid.hasForbiddenEnchantments()) {
				List<Enchantment> forbidden = bid.getForbiddenEnchantments();
				final int size = forbidden.size();
				for (int x = -1; ++x < size;) {
					if (!result.containsEnchantment(forbidden.get(x))) continue;
					result.removeEnchantment(forbidden.get(x));
					
					if (!(result.getItemMeta() instanceof Repairable)) continue;
					Repairable meta = (Repairable) result.getItemMeta();
					if (meta.hasRepairCost()) {
						meta.setRepairCost(meta.getRepairCost() - 1);
						result.setItemMeta((@Nullable ItemMeta) meta);
					}
				}
			}
		}
		
		// Set durability for other instances.
		if (!customRepairFound) {
			float duraPerc = ((float)result.getType().getMaxDurability() - (float)((Damageable)result.getItemMeta()).getDamage()) / (float)result.getType().getMaxDurability();
			BeanItem.setDurability(result, (int) (duraPerc * ((float)BeanItem.getMaxDurability(result))));
		}
		
		// Re-format the item to address new changes.
		BeanItem.formatItem(result);
		
		// Prevent a result from appearing, similarly to vanilla, if the result item is exactly the same.
		if ((contents[0] != null && contents[0].isSimilar(result))) {
			e.setResult(null);
			return;
		}
		
		// Update the result.
		e.setResult(result);
		
		// Increase the cost of forging based on rarity.
		switch (BeanItem.getItemRarity(result)) {
		case UNCOMMON: newCost *= 1.1; break;
		case RARE: newCost *= 1.2; break;
		case EPIC: newCost *= 1.35; break;
		case LEGENDARY: newCost *= 1.5; break;
		case MYTHIC: newCost *= 2; break;
		case IRIDESCENT: newCost *= 3; break;
		default: break;
		}
		
		// Reduce the cost of forging based on level.
		final SkillInfo skillInfo = PlayerProfile.from(e.getView().getPlayer()).getSkills().getSkillInfo(SkillType.REPAIR);
		if (skillInfo.getLevel() >= 10) {
			final AnvilInventory inv = e.getInventory();
			
			if (inv.getContents()[1] != null) {
				final int finalCost = (int) Math.max(1, newCost - Math.min(15, ((float)skillInfo.getLevel() / 80F)));
				
				Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> { inv.setMaximumRepairCost(100); inv.setRepairCost(finalCost); });
			}
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		// Cancel item drags if it drags over any part of the custom gui.
		PlayerProfile pp = PlayerProfile.from(e.getWhoClicked());
		if (pp.getBeanGui() != null) {
			for (int i : e.getRawSlots()) {
				if (i < pp.getBeanGui().getInventory().getSize()) {
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getSlotType() == SlotType.RESULT) {
			if (e.getInventory() instanceof AnvilInventory) {
				if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return; // prevents getting infinite repair xp by clicking empty slot
				AnvilInventory inv = (AnvilInventory) e.getInventory();
				
				Player p = (Player) e.getView().getPlayer();
				
				if (inv.getContents()[1] != null) {
					int cost = inv.getRepairCost();
					
					if (cost < 1) return;
					
					int playerXp = p.getLevel();
					if (playerXp >= cost)
						PlayerProfile.from(p).getSkills().addXp(SkillType.REPAIR, cost*200);
				}
				
				
				// If there was a custom repair, mimick default anvil functionality and place back the remaining quantity
				if (this.customRepairHold.containsKey(p.getUniqueId())) {
					Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> {
						inv.setSecondItem(this.customRepairHold.getOrDefault(p.getUniqueId(), new ItemStack(Material.AIR)));
						customRepairHold.remove(p.getUniqueId());
					});
				}
			}
			return;
		}
		
		if (e.getSlotType() != SlotType.CONTAINER && e.getSlotType() != SlotType.QUICKBAR) return;
		
		final ItemStack stack = e.getCurrentItem();
		final Player p = (Player) e.getView().getPlayer();
		final PlayerProfile pp = PlayerProfile.from(p);
		final BeanGui bui = pp.getBeanGui();
		
		if (stack != null) {
			// If supporter - Shulker Box open shortcut in Inventory
			if (e.isRightClick() && (e.getCursor() == null || e.getCursor().getType() == Material.AIR)) {
				if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
					if (stack.getType().name().endsWith("SHULKER_BOX")) {
						if (p.hasPermission(Permission.QUICK_SHULKER_BOX)) {
							if (bui == null || !(bui instanceof BeanGuiShulker)) {
								e.setCancelled(true); // Schedule the task so client side doesn't end up with a ghost item.
								Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> { new BeanGuiShulker(p, stack, e.getSlot()).openInventory(); });
							}
						}
						return;
					}
				}
			}
			
			// Main Menu Item
			if (stack.equals(BeanGui.menuItem)) {
				e.setCancelled(true);
				if (bui == null || !(bui instanceof BeanGuiMainMenu)) { // Schedule the task so client side doesn't end up with a ghost item.
					Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> { new BeanGuiMainMenu(p).openInventory(); });
					return;
				}
			}
		}
		
		if (bui != null && !bui.preInventoryClick(e))
			bui.onInventoryClicked(e);
	}
	
	@EventHandler
	public void onLootGeneration(LootGenerateEvent e) {
		for (ItemStack i : e.getLoot())
			BeanItem.formatItem(i);
	}
	
}
