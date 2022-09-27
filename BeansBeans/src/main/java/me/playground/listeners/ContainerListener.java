package me.playground.listeners;

import java.util.*;

import me.playground.enchants.BEnchantment;
import me.playground.items.*;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.items.tracking.ManifestationReason;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;

import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiLivingHopper;
import me.playground.gui.BeanGuiMainMenu;
import me.playground.gui.BeanGuiShulker;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;

public class ContainerListener extends EventListener {

	private final List<Player> slowdown = new ArrayList<>();

	public ContainerListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler
	public void onCraft(PrepareResultEvent e) {
		final Inventory inv = e.getInventory();
		if (e.getResult() == null) return;

		// Deny the cleansing of Enchantments that cannot be cleansed while also forcing the default enchantments of custom items to stay.
		if (inv instanceof GrindstoneInventory gInv) {
			ItemStack result = e.getResult();
			ItemStack item1 = gInv.getUpperItem() == null ? gInv.getLowerItem() : gInv.getUpperItem();
			ItemStack item2 = gInv.getLowerItem();
			BeanItem bi = BeanItem.from(result);

			// Grab all the combined Enchantments.
			Map<Enchantment, Integer> totalEnchants = new HashMap<>(item1.getEnchantments());
			if (item2 != null && item1 != item2) {
				item2.getEnchantments().forEach((enchantment, level) -> {
					if (totalEnchants.get(enchantment) >= level) return;
					totalEnchants.put(enchantment, level);
				});
			}

			result.addUnsafeEnchantments(totalEnchants);

			// Upgrade any Enchantments whose Default Enchantments levels exceed what's currently on the item (Although this is incredibly unlikely).
			// Also add any Default Enchantments that aren't on the item yet.
			if (bi != null && bi.hasDefaultEnchantments()) {
				bi.getDefaultEnchantments().forEach((enchantment, level) -> {
					if (result.getEnchantmentLevel(enchantment) < level)
						result.addUnsafeEnchantment(enchantment, level);
				});
			}

			// Remove any Cleanse-able Enchantments from the result.
			totalEnchants.forEach((enchantment, integer) -> {
				if (BEnchantment.from(enchantment).isCleansable())
					result.removeEnchantment(enchantment);
			});
		}

		// Update the formatting of any item that's being crafted, assuming it isn't null.
		ItemStack i = e.getResult();
		if (i == null) return;
		if (!(inv instanceof SmithingInventory)) {
			BeanItem.resetItemFormatting(i);
			return;
		}
		
		SmithingRecipe sr = ((SmithingRecipe)((SmithingInventory)inv).getRecipe());

		if (sr.willCopyNbt()) {
			// Dumb that I have to do this. willCopyNbt is necessary but removes nbt from the result before overriding, so we need to force a "merge" of nbt.
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
	public void preCraft(PrepareItemCraftEvent e) {
		if (e.getInventory() instanceof SmithingInventory) return;
		if (e.getRecipe() == null) return;
		
		// All Bukkit recipe instances implement Keyed.
		NamespacedKey key = ((Keyed)e.getRecipe()).getKey();
		if (!key.getNamespace().equals(NamespacedKey.MINECRAFT)) {
			if (!e.getView().getPlayer().hasDiscoveredRecipe(key))
				e.getInventory().setResult(null);
			e.getView().getPlayer().sendActionBar(Component.text("\u00a7cYou have not unlocked this recipe."));
			return;
		} else {
			// Prevent crafting vanilla items using custom items.
			int size = e.getInventory().getMatrix().length;
			for (int x = -1; ++x < size;)
				if (BeanItem.from(e.getInventory().getMatrix()[x]) != null) {
					e.getInventory().setResult(null);
				}
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

	/**
	 * For some stupid reason bukkit decided it'd be funny to just make this an Inventory Click Event and not actually check if this was a valid craft or not.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemCraft(CraftItemEvent e) {
		if (e.getInventory().getResult() == null) return;

		Inventory botInv = e.getView().getBottomInventory();
		ItemStack result = e.getInventory().getResult().clone();
		ItemStack cursor = e.getCursor(); // Never null

		// Left-clicking the slot.
		if (e.getAction().name().startsWith("PICKUP_")) {
			if (cursor.getType() == Material.AIR || cursor.isSimilar(result) && ((cursor.getAmount() + result.getAmount()) < cursor.getMaxStackSize()))
				doManifestationStuff(e.getRecipe(), 1, result);
			else {
				e.setCancelled(true);
			}
		}

		// Dropping items immediately out of the result slot. This can only be done if the cursor is AIR.
		else if (e.getAction() == InventoryAction.DROP_ONE_SLOT || e.getAction() == InventoryAction.DROP_ALL_SLOT) {
			if (cursor.getType() == Material.AIR) {
				doManifestationStuff(e.getRecipe(), 1, result);
			} else {
				e.setCancelled(true);
			}
		}

		// Any other action such as shift clicking, just calculate the inventory.
		else {
			int amountBefore = countItem(botInv, result) + (cursor.isSimilar(result) ? cursor.getAmount() : 0);

			slowdown.add((Player) e.getView().getPlayer()); // Prevent any other interactions by this player while this is calculating.
			getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
				int amountNow = countItem(botInv, result) + (cursor.isSimilar(result) ? cursor.getAmount() : 0);
				int crafts = (amountNow - amountBefore) / result.getAmount();
				doManifestationStuff(e.getRecipe(), crafts, result);
				slowdown.remove((Player) e.getView().getPlayer());
			});
		}
	}

	private void doManifestationStuff(Recipe recipe, int crafts, ItemStack result) {
		if (crafts < 0 || result == null) return;

		getPlugin().getItemTrackingManager().incrementManifestationCount(result, ManifestationReason.CRAFTING, (long) crafts * result.getAmount());

		if (recipe instanceof ShapedRecipe shapedRecipe) {
			shapedRecipe.getIngredientMap().forEach((str, item) -> {
				if (item == null) return;
				getPlugin().getItemTrackingManager().incrementDemanifestationCount(item, DemanifestationReason.CRAFTING, (long) crafts * item.getAmount());
			});
		} else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
			shapelessRecipe.getIngredientList().forEach((item) -> {
				if (item == null) return;
				getPlugin().getItemTrackingManager().incrementDemanifestationCount(item, DemanifestationReason.CRAFTING, (long) crafts * item.getAmount());
			});
		}
	}

	private int countItem(Inventory i, ItemStack itemStack) {
		int size = i.getStorageContents().length;
		int count = 0;
		ItemStack[] contents = i.getStorageContents();
		for (int x = -1; ++x < size;)
			if (itemStack.isSimilar(contents[x]))
				count += contents[x].getAmount();
		return count;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemFuel(FurnaceBurnEvent e) {
		getPlugin().getItemTrackingManager().incrementDemanifestationCount(e.getFuel(), DemanifestationReason.FUEL, e.getFuel().getAmount());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemSmelt(BlockCookEvent e) {
		getPlugin().getItemTrackingManager().incrementDemanifestationCount(e.getSource(), DemanifestationReason.COOKING, e.getSource().getAmount());
		getPlugin().getItemTrackingManager().incrementManifestationCount(e.getResult(), ManifestationReason.COOKING, e.getResult().getAmount());
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
			try {
				pp.getBeanGui().onInventoryClosed(e);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				pp.closeBeanGui();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryDrag(InventoryDragEvent e) {
		PlayerProfile pp = PlayerProfile.from(e.getWhoClicked());
		if (pp.getBeanGui() != null)
			pp.getBeanGui().onInventoryDrag(e);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryClickArmorCheck(InventoryClickEvent e) {
		// Prevent equipping a custom skull that's considered a block to your head.
		if (!(e.getClickedInventory() instanceof PlayerInventory pInv)) return;
		if (!(e.getView().getTopInventory() instanceof PlayerInventory)) return;

		boolean helmetSlot = e.getRawSlot() == 5;
		ItemStack itemToCheck = null;

		if (e.isShiftClick() && pInv.getHelmet() == null) { // Check for shift clicking into the helmet slot.
			itemToCheck = e.getCurrentItem();
		} else if (!e.isShiftClick() && helmetSlot) { // Check for clicking the helmet slot directly without shift clicking.
			if (e.getCursor().getAmount() > 1 && pInv.getHelmet() != null) return;
			itemToCheck = e.getCursor();
		} else if (helmetSlot && e.getHotbarButton() > -1) {
			itemToCheck = pInv.getItem(e.getHotbarButton());
		}

		if (itemToCheck != null) {
			BeanItem custom = BeanItem.from(itemToCheck);
			if (custom == null) return;
			if (custom instanceof BeanItemHeirloom)
				e.setCancelled(true);
			else if (custom instanceof BeanBlock bBlock && !bBlock.isWearable())
				e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getView().getPlayer();
		if (slowdown.contains(p)) {
			p.sendActionBar(Component.text("\u00a7cYour action was cancelled. Slow down."));
			e.setCancelled(true);
			return;
		}

		// TODO: Just make a custom UI for Grindstone

		// Prevent the ability to place custom items with only default or non-cleans-able enchantments into the Grindstone.
		if (e.getView().getTopInventory() instanceof GrindstoneInventory) {
			ItemStack cringe = e.getSlotType() != SlotType.CRAFTING && e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ? e.getCurrentItem() : e.getCursor();
			if (!cringe.getEnchantments().isEmpty()) {
				if (BeanItem.func(cringe, (custom) -> {
					if (!custom.hasDefaultEnchantments()) return false;
					// Cheap check since it's impossible to remove the Default Enchantments anyway...
					if (cringe.getEnchantments().size() > custom.getDefaultEnchantments().size()) return false;
					for (Enchantment ench : cringe.getEnchantments().keySet())
						if (!BEnchantment.from(ench).isCleansable()) return true;
					return true;
				})) {
					e.setCancelled(true);
					return;
				}
			}
		}
		
		if (e.getSlotType() != SlotType.CONTAINER && e.getSlotType() != SlotType.QUICKBAR) return;
		
		final ItemStack stack = e.getCurrentItem();
		final PlayerProfile pp = PlayerProfile.from(p);
		final BeanGui bui = pp.getBeanGui();
		
		if (stack != null) {
			// If supporter - Shulker Box open shortcut in Inventory
			if (e.isRightClick() && (e.getCursor() == null || e.getCursor().getType() == Material.AIR)) {
				if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
					if (stack.getType().name().endsWith("SHULKER_BOX")) {
						if (p.hasPermission(Permission.QUICK_SHULKER_BOX)) {
							e.setCancelled(true); // Schedule the task so client side doesn't end up with a ghost item.
							if (bui instanceof BeanGuiShulker && ((BeanGuiShulker)bui).getShulkerSlot() == e.getSlot()) return;
							Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> new BeanGuiShulker(p, stack, e.getSlot()).openInventory());
						}
						return;
					}
				}
			}
			
			// Main Menu Item
			if (stack.equals(BeanItem.PLAYER_MENU.getOriginalStack())) {
				e.setCancelled(true);
				if (!(bui instanceof BeanGuiMainMenu)) { // Schedule the task so client side doesn't end up with a ghost item.
					Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> new BeanGuiMainMenu(p).openInventory());
					return;
				}
			}
		}
		
		if (bui != null && !bui.preInventoryClick(e))
			bui.onInventoryClicked(e);
	}

	// TODO: in the future customise all loot generation. For now, just remove the enchants we don't want to see.
	@EventHandler
	public void onLootGeneration(LootGenerateEvent e) {
		for (ItemStack i : e.getLoot()) {

			// If mending, refine it instead :)
			if (i.containsEnchantment(Enchantment.MENDING)) {
				i.removeEnchantment(Enchantment.MENDING);
				BItemDurable.setRefinementTier(i, 1 + rand.nextInt(3), true);
			}

			// Remove cringe
			if (i.containsEnchantment(Enchantment.BINDING_CURSE))
				i.removeEnchantment(Enchantment.BINDING_CURSE);
			if (i.containsEnchantment(Enchantment.VANISHING_CURSE))
				i.removeEnchantment(Enchantment.VANISHING_CURSE);

			getPlugin().getEnchantmentManager().replaceEnchantments(i, true); // formats for us
		}
	}
	
	@EventHandler
	public void onHopperTransfer(InventoryMoveItemEvent e) {
		// Deny any transaction if the container has the noHopper tag.
		if (e.getDestination().getHolder() instanceof Metadatable && ((Metadatable)e.getDestination().getHolder()).hasMetadata("noHopper")) {
			e.setCancelled(true);
			return;
		}
		
		if (e.getDestination().getHolder() instanceof Hopper hopper) {
			BeanBlock custom = BeanBlock.from(hopper);
			if (custom instanceof BItemLivingHopper) {
				ItemStack[] whitelist = ((BItemLivingHopper)custom).getFilterContents(hopper);
				boolean allowed = true; // All allowed by default
				for (int x = -1; ++x < whitelist.length;) {
					if (whitelist[x] == null) continue;
					allowed = false; // If there's a non-null item in the filter, disallow all by default
					if (whitelist[x].getType() == e.getItem().getType()) {
						BeanItem itemCustom = BeanItem.from(whitelist[x]);
						
						if (itemCustom == null || BeanItem.is(e.getItem(), itemCustom)) {
							allowed = true; // Allow if match
							break;
						}
					}
				}
				
				if (allowed) {
					hopper.setMetadata("noHopper", new FixedMetadataValue(getPlugin(), true));
					Bukkit.getScheduler().runTask(getPlugin(), () ->  {
						BeanGuiLivingHopper.updateViewerInventory(hopper, null);
						hopper.removeMetadata("noHopper", getPlugin());
					});
				}
				else
					e.setCancelled(true);
			}
		}
		
		if (e.getDestination().getHolder() instanceof Chest chest) {
			BeanBlock custom = BeanBlock.from(chest);
			if (custom instanceof BItemBigChest customChest) {
				e.setCancelled(true);
				Inventory i = customChest.createInventorySnapshot(chest);
				Map<Integer, ItemStack> stacks = i.addItem(e.getItem());
				e.getSource().removeItem(e.getItem());
				if (!stacks.isEmpty())
					e.getSource().addItem(stacks.get(0));
				customChest.saveInventory(chest, i.getContents(), true);
				e.calledGetItem = false;
				e.calledSetItem = false;
			}
		}
		
		if (e.getSource().getHolder() instanceof Hopper hopper) {
			BeanBlock custom = BeanBlock.from(hopper);
			if (custom instanceof BItemLivingHopper) {
				hopper.setMetadata("noHopper", new FixedMetadataValue(getPlugin(), true));
				Bukkit.getScheduler().runTask(getPlugin(), () ->  {
					BeanGuiLivingHopper.updateViewerInventory(hopper, null);
					hopper.removeMetadata("noHopper", getPlugin());
				});
			}
		}
	}
	
}
