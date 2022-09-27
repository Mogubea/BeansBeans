package me.playground.gui.stations;

import me.playground.enchants.BEnchantment;
import me.playground.gui.BeanGui;
import me.playground.items.BItemDurable;
import me.playground.items.BeanItem;
import me.playground.items.lore.Lore;
import me.playground.menushop.PurchaseOption;
import me.playground.skills.Skill;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Replacement of the vanilla Anvil
 */
public class BeanGuiAnvil extends BeanGui {

	protected static final ItemStack confNone = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confRed = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confGreen = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.empty());

	protected static final ItemStack indicatorModify = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.text("Item to Modify", NamedTextColor.GRAY));
	protected static final ItemStack indicatorWait = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.text("Waiting for Combination", NamedTextColor.WHITE));
	protected static final ItemStack indicatorInvalid = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.text("Invalid Combination", NamedTextColor.RED));
	protected static final ItemStack indicatorEnchant = newItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), Component.text("\u269D Enchant \u269D", BeanColor.ENCHANT));
	protected static final ItemStack indicatorRepair = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u2692 Repair \u2692", NamedTextColor.GREEN));
	protected static final ItemStack indicatorCombine = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u2692 Combine \u2692", NamedTextColor.GREEN));
	protected static final ItemStack indicatorRepaired = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.text("Fully Repaired", NamedTextColor.WHITE));
	protected static final ItemStack indicatorRefine = newItem(new ItemStack(Material.PINK_STAINED_GLASS_PANE), Component.text("\u2692 Refine \u2692", NamedTextColor.LIGHT_PURPLE));

	protected static final ItemStack indicatorInfuse = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.text("Item to Consume", NamedTextColor.GRAY));

	protected static final ItemStack noResult = newItem(new ItemStack(Material.GRAY_DYE), Component.text("No Result", NamedTextColor.GRAY));

	protected static final ItemStack purchaseConfirm = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNmN2FmNTQ4ZGNhNmEyYTk0MmVkNzI2NDBkZDgwZTUwMGY4MzI5OGY4OWMzMWUzYWI0YTVmNmNlMjBlMmY0ZCJ9fX0="), Component.text("Confirm Forge"));
	protected static final ItemStack purchaseNone = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM4YWIxNDU3NDdiNGJkMDljZTAzNTQzNTQ5NDhjZTY5ZmY2ZjQxZDllMDk4YzY4NDhiODBlMTg3ZTkxOSJ9fX0="), Component.text(""));

	protected static final ItemStack anvilOne = newItem(new ItemStack(Material.ANVIL),
			Component.text("Item to Modify", NamedTextColor.WHITE),
			Component.text("\u00a77Place the item you wish to"),
			Component.text("\u00a77repair, rename or refine"),
			Component.text("\u00a77into the slot above."));

	protected static final ItemStack anvilTwo = newItem(new ItemStack(Material.ANVIL),
			Component.text("Item to Consume", NamedTextColor.WHITE),
			Component.text("\u00a77Place the item you wish to"),
			Component.text("\u00a77destroy and infuse into the"),
			Component.text("\u00a77item on the left into the"),
			Component.text("\u00a77slot above."));

	private static final List<Material> anvilStates = Arrays.asList(Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.AIR);

	private static final ItemStack awaitInput = newItem(purchaseNone, Component.text("Awaiting Input", NamedTextColor.DARK_GRAY), "Placing items into the Modification and Infusion slots below will preview the modified item in the slot above.");
	private static final ItemStack invalidInput = newItem(purchaseNone, Component.text("Invalid Combination", NamedTextColor.DARK_GRAY), "The current combination of items do not result in anything.");
	private static final ItemStack repairedInput = newItem(purchaseNone, Component.text("Already Repaired", NamedTextColor.DARK_GRAY), "The item you wish to repair is already at maximum durability.");

	private static final PurchaseOption renamingPurchase = new PurchaseOption(Material.SPRUCE_SIGN, Component.text("Rename Item", NamedTextColor.WHITE),
			Lore.getBuilder("Modify the display name of the items in the adjacent slot.").dontFormatColours().build());
	private static final List<TextComponent> renamedLore = Lore.fastBuild(true, 34, "Right-Click to remove the existing rename for &aFree&r.");

	static {
		renamingPurchase.setPurchaseWord("rename");
		renamingPurchase.addExperienceCost(1);
		renamingPurchase.setSubtext("Forging");
	}

	private final @Nullable Block anvil;
	private final int infuseSlot = 34;
	private final int modifySlot = 28;

	// The confirmation button, basically.
	private final PurchaseOption[] purchase = {null, null};
	private boolean dualOption = false; // Exists since repairing using the same item has an option to refine too.

	private ItemStack upgradeItem;
	private ItemStack infuseItem;
	private final ItemStack[] result = new ItemStack[2];

	private int skillXpToGive = 0;
	private int rightConsumeCount = 0;
	private boolean isRefining;

	private final int[] leftIndicators = {19, 10, 11, 12};
	private final int[] rightIndicators = {14, 15, 16, 25};

	private boolean trueClosure = true;

	public BeanGuiAnvil(@NotNull Player p, @Nullable Block anvil) {
		super(p);
		setName("Anvil");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,bBlank,
				bBlank, indicatorModify, indicatorModify, indicatorModify,noResult, indicatorInfuse, indicatorInfuse, indicatorInfuse,bBlank,
				bBlank, indicatorModify,bBlank,bBlank,awaitInput,bBlank,bBlank, indicatorInfuse,bBlank,
				bBlank,null,bBlank,bBlank,bBlank,bBlank,bBlank,null,bBlank,
				bBlank,anvilOne,bBlank,bBlank,bBlank,bBlank,bBlank,anvilTwo,bBlank,
				confNone,confNone,confNone,confNone,closeUI,confNone,confNone,confNone,confNone,
		};
		this.interactCooldown = 0;
		this.anvil = anvil;
	}

	@Override
	public void onInventoryOpened() {
		// For when renaming
		if (!trueClosure) {
			i.setItem(modifySlot, upgradeItem);
			i.setItem(infuseSlot, infuseItem);
			calculateAnvil();
			this.trueClosure = true;
		}
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		if (!trueClosure) return;

		ItemStack item1 = i.getItem(modifySlot);
		if (item1 != null)
			p.getInventory().addItem(item1).forEach((index, item) -> e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item));
		ItemStack item2 = i.getItem(infuseSlot);
		if (item2 != null)
			p.getInventory().addItem(item2).forEach((index, item) -> e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item));
		p.updateInventory();
		p.saveData();
	}

	@Override
	public void onInventoryDrag(InventoryDragEvent e) {
		if (e.getRawSlots().contains(modifySlot) || e.getRawSlots().contains(infuseSlot)) {
			calculateAnvil();
			p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
		}
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();

		// Pain pain pain
		if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
			e.setCancelled(false);
			if (e.getCursor() != null && e.getCursor().isSimilar(result[0])) { // getCursor cannot be null, but it shuts up the IDE.
				i.setItem(13, null);
				calculateAnvil();
				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
			} else if (e.getCursor().isSimilar(upgradeItem) || e.getCursor().isSimilar(infuseItem)) {
				calculateAnvil();
				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
			}
			return;
		}

		if (slot > i.getSize()) {
			e.setCancelled(false);
			// Shift Click into the Anvil
			if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem() != null) {
				calculateAnvil();
				p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
			}
		} else {
			switch(slot) {
				case 21, 22, 23 -> {
					if (slot != 22 && !dualOption) return;
					int option = slot == 23 ? 1 : 0;

					if (purchase[option] != null && purchase[option].purchase(p, false)) {
						boolean isEnchant = upgradeItem.getType() != Material.ENCHANTED_BOOK && infuseItem.getType() == Material.ENCHANTED_BOOK;

						i.setItem(13, noResult);
						i.setItem(modifySlot, result[option]);
						i.getItem(infuseSlot).subtract(rightConsumeCount);

						if (option == 0 && isRefining) { // Refining
							p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.2F);
							p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getEyeLocation(), 16, 1, 1, 1);
						}

						if (p.getGameMode() != GameMode.CREATIVE) // XP when not in Creative Mode
							pp.getSkills().addExperience(isEnchant ? Skill.ENCHANTING : Skill.FORGING, 47 * purchase[option].getExperienceCost() + (purchase[option].getCoinCost()/20));

						// Degrade anvil
						if (anvil != null) {
							int chance = 10;
							if (getPlugin().getRandom().nextInt(100) < chance) {
								int newState = anvilStates.indexOf(anvil.getType()) + 1;
								Directional directional = (Directional) anvil.getBlockData();
								BlockFace facing = directional.getFacing();

								anvil.setType(anvilStates.get(newState));
								if (anvil.getType() != Material.AIR) {
									directional = (Directional) anvil.getBlockData();
									directional.setFacing(facing);
									anvil.setBlockData(directional);
									p.getWorld().spawnParticle(Particle.CRIT_MAGIC, anvil.getLocation().add(0.5, 0.5, 0.5), 8, 0.25, 0.25, 0.25);
									p.getWorld().playEffect(anvil.getLocation(), Effect.STEP_SOUND, anvil.getType());
								}

								p.getWorld().playSound(anvil.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.75F, 1.0F);
								if (newState >= 3)
									close();
							} else {
								p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.75F, 1.0F);
							}
						} else {
							p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.75F, 1.0F);
						}
						calculateAnvil();
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
					}
				}
				case modifySlot, infuseSlot -> {
					p.playSound(p.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 0.2F, 0.8F);
					e.setCancelled(false);
					calculateAnvil();
				}
				case (modifySlot-1), (infuseSlot+1) -> {
					boolean left = slot == (modifySlot-1);
					ItemStack item = i.getItem(left ? modifySlot : infuseSlot);
					if (item == null) break;
					boolean renamed = BeanItem.hasBeenRenamed(item);
					if (e.isRightClick() && renamed) {
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 0.3F, 1.0F);
						item.editMeta(meta -> meta.displayName(null));
						BeanItem.formatItem(item);
						doRenameItems();
					} else if (e.isLeftClick()) {
						if (renamingPurchase.canPurchase(p)) {
							this.trueClosure = false;
							getPlugin().getSignMenuFactory().requestSignResponse(p, Material.BIRCH_WALL_SIGN, (strings -> {
								if (strings[0] == null || strings[0].isEmpty())
									throw new RuntimeException("Item names cannot be empty.");

								if (renamingPurchase.purchase(p)) {
									String newName = strings[0] + (strings[1] != null ? strings[1] : "");
									item.editMeta(meta -> meta.displayName(Component.text(newName)));
									BeanItem.formatItem(item);
									doRenameItems();
									if (p.getGameMode() == GameMode.SURVIVAL)
										pp.getSkills().addExperience(Skill.FORGING, 29);
									p.playSound(p.getLocation(), Sound.ITEM_BOOK_PUT, 0.2F, 1.0F);
								} else {
									throw new RuntimeException("There was an error renaming your item.");
								}
							}), true, "New Item Name");
						}
					}
				}
				case 49 -> p.closeInventory();
				default -> {  }
			}
		}
	}

	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
		if (e.getAction() == InventoryAction.NOTHING) return true;

		e.setCancelled(true);
		return ((!(e.getRawSlot() > i.getSize()) || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) && pp.onCdElseAdd("guiClick", 200, true));
	}

	@Override
	protected void playOpenSound() {
		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.12F, 0.8F);
	}

	/**
	 * Calculate the results, costs etc.
	 */
	private void calculateAnvil() {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
			p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_FALL, 0.12F, 1.4F);
			boolean isRepairing = false;
			boolean isBookEnchanting = false;
			boolean checkForEnchants = true;
			boolean areSimilar = false;

			List<List<TextComponent>> enchantWarning = Arrays.asList(null, null);

			double[] newCost = {0, 0};
			int[] coinCost = {0, 0};

			ItemStack left = indicatorModify;
			ItemStack right = indicatorInfuse;
			ItemStack bottom = confNone;
			purchase[0] = null;
			purchase[1] = null;
			result[0] = null;
			result[1] = null;
			upgradeItem = i.getItem(modifySlot);
			infuseItem = i.getItem(infuseSlot);
			rightConsumeCount = 0;
			dualOption = false;
			isRefining = false;

			ItemStack resultItem = upgradeItem == null ? noResult : upgradeItem.clone();
			ItemStack resultItem2 = resultItem.clone();

			float repairVal = 0;

			if (upgradeItem != null && infuseItem != null) {
				boolean upgradeBook = upgradeItem.getType() == Material.ENCHANTED_BOOK;
				boolean infuseBook = infuseItem.getType() == Material.ENCHANTED_BOOK;

				int enchantIrreparable = Math.min(3, upgradeItem.getItemMeta().getEnchantLevel(BEnchantment.BURDEN_IRREPARABLE));
				areSimilar = areSimilar();

				BItemDurable custom = BeanItem.from(upgradeItem, BItemDurable.class);

				// If the two items are of the same type.
				if (areSimilar) {
					if (upgradeBook) { // Book Combining
						purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7bCombine Books"), Lore.getBuilder("Combine the two Enchanted Books for 25% of any enchantment costs. Enchantments of the same level will upgrade for a cost.").build());
						purchase[0].addSkillRequirement(Skill.ENCHANTING, 1);
						left = indicatorCombine;
						right = indicatorCombine;
						rightConsumeCount = 1;
					} else if (BeanItem.getDurability(upgradeItem) > 0) { // Repair and Refinement
						rightConsumeCount = 1;

						// Refine if possible
						if (BItemDurable.canBeRefined(upgradeItem) && pp.getSkillLevel(Skill.FORGING) >= 4) {
							int newRefine = Math.max(BItemDurable.getRefinementTier(upgradeItem), BItemDurable.getRefinementTier(infuseItem)) + 1;
							if (newRefine > 15) {
								// TODO: Do something if over refinement 15, for now, do nothing. Very little players will get to this point any time soon (written late 2022).
							} else {
								isRefining = true;
								left = indicatorRefine;
								right = indicatorRefine;

								// Upgrade the preview item's refinement tier
								BItemDurable.setRefinementTier(resultItem, newRefine, true);

								newCost[0] += 10 + (2 * newRefine * newRefine);
								coinCost[0] = 5000 * newRefine * newRefine;

								StringBuilder statChanges = new StringBuilder();
								List<Attribute> refinedAttributes = Arrays.asList(Attribute.GENERIC_ATTACK_DAMAGE, Attribute.GENERIC_ATTACK_SPEED, Attribute.GENERIC_MAX_HEALTH, Attribute.GENERIC_ARMOR);

								// Stats
								refinedAttributes.forEach(attribute -> {
									double val = BeanItem.getAttributeValue(upgradeItem, attribute);
									if (val <= 0) return;
									double newVal = BeanItem.getAttributeValue(resultItem, attribute);
									if (val == newVal) return;
									statChanges.append("\n&8 • &7").append(BeanItem.getAttributeString(attribute)).append(": &f&m").append(dec.format(val)).append("&8&l \u2192 &a").append(dec.format(newVal));
								});

								// Durability
								int oldDura = BeanItem.getMaxDurability(upgradeItem);
								int newDura = BeanItem.getMaxDurability(resultItem);

								if (oldDura != newDura)
									statChanges.append("\n&8 • &7Durability: &f&m").append(df.format(oldDura)).append("&8&l \u2192 &a").append(df.format(newDura));

								// Runic Capacity
								int oldRunic = BeanItem.getBaseRunicCapacity(upgradeItem);
								int newRunic = BeanItem.getBaseRunicCapacity(resultItem);

								if (oldRunic != newRunic)
									statChanges.append("\n&8 • &7Runic Capacity: &f&m").append(df.format(oldRunic)).append("&8&l \u2192 &f&").append(BeanColor.ENCHANT.asHexString()).append(df.format(newRunic)).append(" \u269D");

								purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7dCombine and Refine"),
										Lore.getBuilder("Combine the two items provided whilst also &drefining &rthe item to &" + BeanItem.getItemRarity(resultItem).getRefinementColour().asHexString() +
												"Tier " + Utils.toRoman(BItemDurable.getRefinementTier(resultItem)) + "&7, permanently increasing its stats.\n" + statChanges).setLineLimit(36).build());
								purchase[0].addSkillRequirement(Skill.FORGING, 4 + newRefine);
							}
						}

						// Check Repair possibilities
						repairVal = 6.75f + ((float) BeanItem.getDurability(infuseItem) / (float) BeanItem.getMaxDurability(infuseItem)) * 100f;
					} else {
						left = name == null ? indicatorInvalid : indicatorRepair;
						right = indicatorInvalid;
					}
				} else if (infuseBook) { // Check for one infusement book
					rightConsumeCount = 1;
					isBookEnchanting = true;

					purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("Enchant", BeanColor.ENCHANT),
							Lore.getBuilder("Infuse the item with the enchantments and burdens of the &"+BeanItem.getItemRarity(infuseItem).getColour().asHexString()+"&r provided. Enchantments of the same level will upgrade for a cost.").build());
					purchase[0].addSkillRequirement(Skill.ENCHANTING, 1);
					left = indicatorEnchant;
					right = indicatorEnchant;
				} else { // Check for Repair
					repairVal = custom != null ? custom.getRepairPercentage(infuseItem) : getRepairValueFor(upgradeItem, infuseItem);
					checkForEnchants = false;
				}

				// Do Enchantment stuff
				if (checkForEnchants) {
					int[] remainingRunicCapacity = {BeanItem.getRunicCapacity(resultItem), BeanItem.getRunicCapacity(resultItem2)};

					// List of Enchantments that will end up on the resulting items.
					List<Map<Enchantment, Integer>> totalEnchants = new ArrayList<>();
					totalEnchants.add(new HashMap<>(upgradeBook ? ((EnchantmentStorageMeta)upgradeItem.getItemMeta()).getStoredEnchants() : upgradeItem.getEnchantments()));
					if (isRefining) totalEnchants.add(new HashMap<>(upgradeItem.getEnchantments()));

					Map<Enchantment, Integer> enchantsToInfuse = infuseBook ? ((EnchantmentStorageMeta)infuseItem.getItemMeta()).getStoredEnchants() : infuseItem.getEnchantments();

					// Check for new Burdens that may increase the runic capacity to allow for more enchantments and combinations of enchantments.
					for (Map.Entry<Enchantment, Integer> entry : enchantsToInfuse.entrySet()) {
						final BEnchantment bEnchantment = BEnchantment.from(entry.getKey());
						if (!bEnchantment.isCursed()) continue;

						final int curLevel = totalEnchants.get(0).getOrDefault(entry.getKey(), 0);
						final int newLevel = entry.getValue();
						if (curLevel >= newLevel) continue; // Upgrade Item is already better or equal to the Infusing Item. !!! Burdens cannot be combined !!!
						if (newLevel < 1) continue; // Just in-case

						if (bEnchantment.conflictsWith(upgradeItem)) continue; // Infusing Enchantment conflicts with Upgrade Item

						if (infuseBook) {
							newCost[0] += bEnchantment.getExperienceCost(newLevel) * (upgradeBook ? 0.25 : 1);
							newCost[1] += bEnchantment.getExperienceCost(newLevel) * (upgradeBook ? 0.25 : 1);
						}

						// Update both items enchant lists
						for (int x = -1; ++x < totalEnchants.size();) {
							remainingRunicCapacity[x] -= (bEnchantment.getRunicValue(newLevel) - bEnchantment.getRunicValue(curLevel));
							totalEnchants.get(x).put(entry.getKey(), newLevel);
						}
					}

					// Attempt to combine Enchantments or add new Enchantments, assuming there is Runic room available. If not, warn the user about it.
					for (Map.Entry<Enchantment, Integer> entry : enchantsToInfuse.entrySet()) {
						// Break out if the remaining capacities are 0 and a warning has been issued
						if (remainingRunicCapacity[0] <= 0 && (!isRefining || remainingRunicCapacity[1] <= 0) && enchantWarning.get(0) != null) break;

						final BEnchantment bEnchantment = BEnchantment.from(entry.getKey());
						if (bEnchantment.isCursed()) continue; // Burdens cannot be combined, no use checking here
						if (bEnchantment.conflictsWith(upgradeItem)) continue; // Infusing Enchantment conflicts with Upgrade Item

						final int curLevel = totalEnchants.get(0).getOrDefault(entry.getKey(), 0);
						int newLevel = entry.getValue();

						if (newLevel < 1) continue; // If below 0, skip, shouldn't exist anyway
						if (newLevel < curLevel) continue; // If below the current level, skip
						if (curLevel == newLevel && curLevel < bEnchantment.getMaxLevel()) newLevel++; // If same level, combine

						int runicCost = bEnchantment.getRunicValue(newLevel) - bEnchantment.getRunicValue(curLevel);

						// Loop through the 2 (or just 1) results.
						for (int x = -1; ++x < totalEnchants.size();) {
							if (remainingRunicCapacity[x] - runicCost < 0) {

								// Attempt to downgrade the enchantment to fit it onto the item.
								if (remainingRunicCapacity[x] > 0) {
									for (int lvl = newLevel; --lvl > Math.max(0, curLevel - 1);) {
										int newRunicCost = bEnchantment.getRunicValue(lvl);
										if (remainingRunicCapacity[x] - newRunicCost >= 0) {
											remainingRunicCapacity[x] -= newRunicCost;
											totalEnchants.get(x).put(entry.getKey(), lvl);
											if (infuseBook) newCost[x] += bEnchantment.getExperienceCost(lvl) * (upgradeBook ? 0.25 : 1);
											break;
										}
									}
								}

								// Warn the user about the inability to merge all the Enchantments due to a lack of Runic Capacity.
								enchantWarning.set(x, Lore.getBuilder("&c\u26a0 Some Enchantments can't be transferred due to the lack of available &" + BeanColor.ENCHANT.asHexString() + "Runic Capacity&c.").build().getLore());
							} else {
								remainingRunicCapacity[x] -= runicCost;
								totalEnchants.get(x).put(entry.getKey(), newLevel); // Change to new level

								if (infuseBook) newCost[x] += bEnchantment.getExperienceCost(newLevel) * (upgradeBook ? 0.25 : 1);
							}
						}
					}

					if (upgradeBook) { // Books need to have it added to their storage meta
						for (Map.Entry<Enchantment, Integer> enchant : totalEnchants.get(0).entrySet())
							resultItem.editMeta(meta -> ((EnchantmentStorageMeta)meta).addStoredEnchant(enchant.getKey(), enchant.getValue(), true));
					} else { // Regular enchantment adding
						resultItem.addUnsafeEnchantments(totalEnchants.get(0));
						if (isRefining) resultItem2.addUnsafeEnchantments(totalEnchants.get(1));
					}
				}

				// Do Repair
				if (repairVal > 0) {
					isRepairing = true;
					repairVal *= (1 - (0.25 * enchantIrreparable));
					double repairCost = 0;
					int repairCoinCost = 0;

					// Check the lowest durability so people don't try to avoid costs by going inverse, assuming the items are the same.
					ItemStack toRepair = upgradeItem;
					if (areSimilar && BeanItem.getDurability(infuseItem) < BeanItem.getDurability(upgradeItem))
						toRepair = infuseItem;

					int curDuraInt = BeanItem.getDurability(toRepair);
					int maxDuraInt = BeanItem.getMaxDurability(upgradeItem);
					float currentDurability = ((float)curDuraInt / (float)maxDuraInt) * 100f;
					float newDurability = currentDurability;

					// Do calculations for when the left item actually needs to be repaired.
					if (currentDurability < 100f) {
						int consumeCount = 0;

						while (newDurability < 100f && consumeCount < infuseItem.getAmount()) {
							consumeCount++;
							newDurability += repairVal;
						}

						if (!areSimilar)
							this.rightConsumeCount += consumeCount;

						// 100% cap
						if (newDurability >= 100f)
							newDurability = 100f;

						// Increment the XP cost of the repair based on % repaired
						repairCost = ((newDurability - currentDurability) / 30) * (1 + (0.25 * enchantIrreparable));

						// Increment the Coin cost of the repair based on % and durability repaired
						int newDuraInt = (int) ((float) maxDuraInt * (newDurability / 100f));
						repairCoinCost = (int) (((repairCost * 10) + ((newDuraInt - curDuraInt) * 0.5)) * (1 + (0.25 * enchantIrreparable)));

						BeanItem.setDurability(resultItem, (int) (BeanItem.getMaxDurability(resultItem) * (newDurability / 100f)));
						BeanItem.setDurability(resultItem2, (int) (BeanItem.getMaxDurability(resultItem2) * (newDurability/100f)));

						// Further, increment the costs based on the resulting runic and refinement levels.
						newCost[0] += repairCost * (1 + (BeanItem.getRunicExpenses(resultItem) * 0.1));
						coinCost[0] += repairCoinCost * (1 + (BItemDurable.getRefinementTier(resultItem) * 0.08));
					}

					if (!isRefining) {
						if (areSimilar) {
							purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7aCombine and Repair"),
									Lore.getBuilder("Combine the two items provided, repairing and attempting to merge enchantments, burdens, refinement tier and more.").dontFormatColours().build());
							left = indicatorCombine;
							right = indicatorCombine;
						} else {
							purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7aRepair"),
									Lore.getBuilder("Repair your item by infusing &f" + rightConsumeCount + "&7 of the items provided on the right.").build());
							left = indicatorRepair;
							right = indicatorRepair;
						}
					} else {
						int infuseRefineTier = BItemDurable.getRefinementTier(infuseItem);
						int modifyRefuseTier = BItemDurable.getRefinementTier(upgradeItem);

						// Check if there's actually any changes warranting a dual option
						boolean needDualOption = true;

						if (upgradeItem.isSimilar(resultItem2))
							needDualOption = false;
						else if (infuseRefineTier == modifyRefuseTier)
							if (infuseItem.getEnchantments().isEmpty() && upgradeItem.getEnchantments().isEmpty())
								if (BeanItem.getDurability(upgradeItem) >= BeanItem.getMaxDurability(upgradeItem))
									needDualOption = false;

						if (needDualOption) {
							right = indicatorCombine;
							dualOption = true;

							// If the sacrificing item has a larger refinement level, set that as the refinement level.
							if (infuseRefineTier > modifyRefuseTier)
								BItemDurable.setRefinementTier(resultItem2, infuseRefineTier, true);

							// Set the costs and increment the costs based on the runic and refinement levels.
							newCost[1] += repairCost * (1 + (BeanItem.getRunicExpenses(resultItem2) * 0.1));
							coinCost[1] += repairCoinCost * (1 + (BItemDurable.getRefinementTier(resultItem2) * 0.08));

							purchase[1] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7aCombine and Repair"),
									Lore.getBuilder("Combine the two items provided, repairing and attempting to merge enchantments, burdens, refinement tier and more.").dontFormatColours().build());
						}
					}
				}
			}

			// Update Displayed Items
			if (!resultItem.equals(noResult)) {
				this.result[0] = BeanItem.formatItem(resultItem);
				this.result[1] = dualOption ? BeanItem.formatItem(resultItem2) : null;
			}

			ItemStack purchaseButton = awaitInput;

			if (newCost[0] <= 0 || rightConsumeCount < 1) {
				if (upgradeItem == null && infuseItem == null) {
					purchase[0] = null;
				} else {
					if (infuseItem == null) {
						left = indicatorWait;
					} else if (upgradeItem == null) {
						right = indicatorWait;
					} else {
						purchase[0] = null;
						if (isRepairing) {
							left = indicatorRepaired;
							right = indicatorRepaired;
							purchaseButton = repairedInput;
						} else {
							left = indicatorInvalid;
							right = indicatorInvalid;
							purchaseButton = invalidInput;
						}

						bottom = confRed;
					}
				}
			} else {
				// Further, increment the price based on the rarity level of the final result.
				for (int x = -1; ++x < (dualOption ? 2 : 1);) {
					switch (BeanItem.getItemRarity(result[x])) {
						case TRASH -> { newCost[x] *= 0.5; coinCost[x] *= 0.5; }
						case UNCOMMON -> { newCost[x] *= 1.1; coinCost[x] *= 1.1; }
						case RARE -> { newCost[x] *= 1.25; coinCost[x] *= 1.25; }
						case EPIC -> { newCost[x] *= 1.5; coinCost[x] *= 1.5; }
						case LEGENDARY, EVENT, SPECIAL -> { newCost[x] *= 1.75; coinCost[x] *= 1.75; }
						case MYTHIC, ASTRAL -> { newCost[x] *= 2; coinCost[x] *= 2; }
						case IRIDESCENT -> { newCost[x] *= 3; coinCost[x] *= 3; }
					}

					// Finalise the purchase
					purchase[x].addExperienceCost(Math.max(1, (int)newCost[x]));
					purchase[x].addCoinCost(coinCost[x]);
					purchase[x].setSubtext(isBookEnchanting ? "Enchanting" : "Forging");
					purchase[x].setPurchaseWord(isBookEnchanting ? "enchant" : "forge");
				}

				boolean canOne = purchase[0].canPurchase(p);
				bottom = (canOne || dualOption && purchase[1].canPurchase(p)) ? confGreen : confRed;
			}

			// Set indicator slots
			for (int x = -1; ++x < leftIndicators.length;)
				i.setItem(leftIndicators[x], left);
			for (int x = -1; ++x < rightIndicators.length;)
				i.setItem(rightIndicators[x], right);
			for (int x = -1; ++x < 9;) {
				if (x == 4) continue;
				i.setItem(45 + x, bottom);
			}

			i.setItem(21, bBlank);
			i.setItem(23, bBlank);

			if (dualOption) {
				purchaseButton = bBlank;
				i.setItem(21, purchase[0].getDisplayItem(p, enchantWarning.get(0)));
				i.setItem(23, purchase[1].getDisplayItem(p, enchantWarning.get(1)));
				i.setItem(12, result[0]);
				i.setItem(13, bBlank);
				i.setItem(14, result[1]);
			} else if (purchase[0] != null) {
				purchaseButton = purchase[0].getDisplayItem(p, enchantWarning.get(0));
				i.setItem(13, result[0]);
			} else {
				i.setItem(13, noResult);
			}

			i.setItem(22, purchaseButton);

			doRenameItems();
		});
	}

	private void doRenameItems() {
		boolean[] renamed = {BeanItem.hasBeenRenamed(upgradeItem), BeanItem.hasBeenRenamed(infuseItem)};

		// Rename items
		i.setItem(modifySlot - 1, upgradeItem != null ? (renamingPurchase.getDisplayItem(p, renamed[0] ? renamedLore : null)) : bBlank);
		i.setItem(infuseSlot + 1, infuseItem != null ? (renamingPurchase.getDisplayItem(p, renamed[1] ? renamedLore : null)) : bBlank);
		if (renamed[0]) i.getItem(modifySlot - 1).addUnsafeEnchantment(BEnchantment.FAKE_GLOW, 1);
		if (renamed[1]) i.getItem(infuseSlot + 1).addUnsafeEnchantment(BEnchantment.FAKE_GLOW, 1);
	}

	private boolean areSimilar() {
		if (upgradeItem == null || infuseItem == null) return false;

		BeanItem item = BeanItem.from(upgradeItem);
		if (item != null) return BeanItem.is(infuseItem, item);

		return upgradeItem.getType().equals(infuseItem.getType());
	}

	// TODO: Create a class to obtain the repairable items for each item and their respective repair values

	/**
	 * Basically here to give Netherite equipment a helping hand when it comes to repairing.
	 */
	private int getRepairValueFor(@NotNull ItemStack tool, @NotNull ItemStack item) {
		if (tool.getType().name().startsWith("NETHERITE_")) {
			switch (item.getType()) {
				case NETHERITE_INGOT -> { return 100; }
				case NETHERITE_SCRAP -> { return 45; }
			}
		}

		return tool.isRepairableBy(item) ? 20 : 0;
	}

}
