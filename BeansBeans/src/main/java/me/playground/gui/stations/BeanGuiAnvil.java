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
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New anvil. Combining books is disabled. Added refinement.
 */
public class BeanGuiAnvil extends BeanGui {

	protected static final ItemStack confNone = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confRed = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.empty());
	protected static final ItemStack confGreen = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.empty());

	protected static final ItemStack indicatorModify = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.text("Item to Modify", NamedTextColor.GRAY));
	protected static final ItemStack indicatorWait = newItem(new ItemStack(Material.WHITE_STAINED_GLASS_PANE), Component.text("Waiting for Combination", NamedTextColor.WHITE));
	protected static final ItemStack indicatorInvalid = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE), Component.text("Invalid Combination", NamedTextColor.RED));
	protected static final ItemStack indicatorRepair = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u2692 Repair \u2692", NamedTextColor.GREEN));
	protected static final ItemStack indicatorCombine = newItem(new ItemStack(Material.LIME_STAINED_GLASS_PANE), Component.text("\u2692 Combine \u2692", NamedTextColor.GREEN));
	protected static final ItemStack incidatorRepaired = newItem(new ItemStack(Material.GREEN_STAINED_GLASS_PANE), Component.text("Fully Repaired", NamedTextColor.DARK_GREEN));
	protected static final ItemStack indicatorRefine = newItem(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE), Component.text("\u2692 Refinement \u2692", NamedTextColor.AQUA));

	protected static final ItemStack indicatorInfuse = newItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), Component.text("Item to Consume", NamedTextColor.GRAY));

	protected static final ItemStack noResult = newItem(new ItemStack(Material.GRAY_DYE), Component.text("No Result", NamedTextColor.GRAY));

	protected static final ItemStack purchaseConfirm = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNmN2FmNTQ4ZGNhNmEyYTk0MmVkNzI2NDBkZDgwZTUwMGY4MzI5OGY4OWMzMWUzYWI0YTVmNmNlMjBlMmY0ZCJ9fX0="), Component.text("Confirm Forge"));
	protected static final ItemStack purchaseNone = newItem(Utils.getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM4YWIxNDU3NDdiNGJkMDljZTAzNTQzNTQ5NDhjZTY5ZmY2ZjQxZDllMDk4YzY4NDhiODBlMTg3ZTkxOSJ9fX0="), Component.text(""));

	private static final ItemStack combineBurdens = newItem(new ItemStack(Material.PINK_DYE), Component.text("Transfer Burdens", BeanColor.ENCHANT_BURDEN), Component.text("\u00a77When \u00a7aCombining\u00a77 equipment, attempt"), Component.text("\u00a77to transfer the burdens of"), Component.text("\u00a77the sacrificed item."), Component.empty(), Component.text("\u00a77Enabled: \u00a7aYes"));
	private static final ItemStack dontCombineBurdens = newItem(new ItemStack(Material.GRAY_DYE), Component.text("Transfer Burdens", BeanColor.ENCHANT_BURDEN), Component.text("\u00a77When \u00a7aCombining\u00a77 equipment, attempt"), Component.text("\u00a77to transfer the burdens of"), Component.text("\u00a77the sacrificed item."), Component.empty(), Component.text("\u00a77Enabled: \u00a7cNo"));

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

	@Nullable
	private final Block anvil;

	private boolean transferBurdens = true;

	// The confirmation button, basically.
	private final PurchaseOption[] purchase = {null, null};
	private boolean dualOption = false; // Exists since repairing using the same item has an option to refine too.

	private ItemStack upgradeItem;
	private ItemStack infuseItem;
	private final ItemStack[] result = new ItemStack[2];

	private int rightConsumeCount = 0;
	private final int infuseSlot = 34;
	private final int modifySlot = 28;

	private final int[] leftIndicators = {19, 10, 11, 12};
	private final int[] rightIndicators = {14, 15, 16, 25};

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
	public void onInventoryClosed(InventoryCloseEvent e) {
		ItemStack item1 = i.getItem(modifySlot);
		if (item1 != null)
			e.getPlayer().getInventory().addItem(item1).forEach((index, item) -> e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item));
		ItemStack item2 = i.getItem(infuseSlot);
		if (item2 != null)
			e.getPlayer().getInventory().addItem(item2).forEach((index, item) -> e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), item));
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		int slot = e.getRawSlot();

		// Pain pain pain
		if (e.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
			e.setCancelled(false);
			if (e.getCursor().isSimilar(result[0])) {
				i.setItem(13, null);
				calculateAnvil();
			} else if (e.getCursor().isSimilar(upgradeItem) || e.getCursor().isSimilar(infuseItem)) {
				calculateAnvil();
			}
			return;
		}

		if (slot > i.getSize()) {
			e.setCancelled(false);
			// Shift Click into the Anvil
			if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getCurrentItem() != null)
				calculateAnvil();
		} else {
			switch(slot) {
				case 21, 22, 23 -> {
					if (slot != 22 && !dualOption) return;
					int option = slot == 23 ? 1 : 0;

					if (purchase[option] != null && purchase[option].purchase(p, false)) {
						i.setItem(13, noResult);
						i.setItem(modifySlot, result[option]);
						i.getItem(infuseSlot).subtract(rightConsumeCount);
						p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.75F, 1.0F);

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
									p.getWorld().playEffect(anvil.getLocation(), Effect.STEP_SOUND, anvil.getType());
								}

								p.getWorld().playSound(anvil.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 0.75F, 1.0F);
								if (newState >= 3)
									close();
							}
						}
						calculateAnvil();
					} else {
						p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.4F, 0.8F);
					}
				}
				case modifySlot, infuseSlot -> { e.setCancelled(false); calculateAnvil(); }
//				case 46 -> i.setItem(46, (transferBurdens = !transferBurdens) ? combineBurdens : dontCombineBurdens);
				case 49 -> p.closeInventory();
				default -> {  }
			}
		}
	}

	@Override
	public boolean preInventoryClick(InventoryClickEvent e) {
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
			boolean isRefining = false;
			boolean isRepairing = false;
			List<TextComponent> enchantWarning = null; // Warn about some enchantments being unable to transfer

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

			ItemStack resultItem = upgradeItem == null ? noResult : upgradeItem.clone();
			ItemStack preRefineItem = resultItem.clone();

			float repairVal = 0;

			if (upgradeItem != null && infuseItem != null) {
				int enchantIrreparable = Math.min(3, upgradeItem.getItemMeta().getEnchantLevel(BEnchantment.BURDEN_IRREPARABLE));

				BItemDurable custom = BeanItem.from(upgradeItem, BItemDurable.class);

				// If the items are the same
				if (areSimilar()) {
					if (BeanItem.getDurability(upgradeItem) > 0) { // Repair and Refinement
						rightConsumeCount = 1;

						// Upgrade Item's current runic capacity
						int runicCapacity = BeanItem.getRunicCapacity(upgradeItem);

						// List of Enchantments which will end up on the previewed items.
						Map<Enchantment, Integer> totalEnchants = new HashMap<>(upgradeItem.getEnchantments());

						if (transferBurdens) {
							// Check for new Burdens that may increase the runic capacity to allow for more enchantments and combinations of enchantments.
							for (Map.Entry<Enchantment, Integer> entry : infuseItem.getEnchantments().entrySet()) {
								final BEnchantment bEnchantment = BEnchantment.from(entry.getKey());
								if (!bEnchantment.isCursed()) continue;

								final int curLevel = totalEnchants.getOrDefault(entry.getKey(), 0);
								final int newLevel = entry.getValue();
								if (curLevel >= newLevel) continue; // Upgrade Item is already better or equal to the Infusing Item. !!! Burdens cannot be combined !!!
								if (newLevel < 1) continue; // Just in-case

								if (bEnchantment.conflictsWith(upgradeItem)) continue; // Infusing Enchantment conflicts with Upgrade Item

								runicCapacity += (bEnchantment.getRunicValue(newLevel) - bEnchantment.getRunicValue(curLevel));
								totalEnchants.put(entry.getKey(), newLevel); // Change level of burden
							}
						}

						// Attempt to combine Enchantments or add new Enchantments, assuming there is Runic room available. If not, warn the user about it.
						for (Map.Entry<Enchantment, Integer> entry : infuseItem.getEnchantments().entrySet()) {
							final BEnchantment bEnchantment = BEnchantment.from(entry.getKey());
							if (bEnchantment.isCursed()) continue; // Burdens cannot be combined, no use checking here
							if (bEnchantment.conflictsWith(upgradeItem)) continue; // Infusing Enchantment conflicts with Upgrade Item

							final int curLevel = totalEnchants.getOrDefault(entry.getKey(), 0);
							final int newLevel = entry.getValue();

							if (curLevel > newLevel) continue; // Upgrade Item is already better than Infusing Item
							if (newLevel < 1) continue; // Just in-case

							int changedLevel = newLevel;

							// If same level, try to combine them for a +1 if below default max level. Otherwise, just try to override the old level.
							if (curLevel == newLevel) {
								if (curLevel >= bEnchantment.getMaxLevel()) continue;
								// TODO: add costs for combining enchantments
								changedLevel++; // +1
							}

							int runicCost = bEnchantment.getRunicValue(changedLevel) - bEnchantment.getRunicValue(curLevel);
							if ((runicCapacity - runicCost) < 0) {
								enchantWarning = Lore.getBuilder("&c\u26a0 Some Enchantments can't be transferred due to the lack of available &" + BeanColor.ENCHANT.asHexString() + " Runic Capacity&c.").build().getLore();
								continue;
							}

							totalEnchants.put(entry.getKey(), changedLevel); // Change to new level
						}

						resultItem.addUnsafeEnchantments(totalEnchants);
						preRefineItem.addUnsafeEnchantments(totalEnchants);

						// Refine if possible
						if (BItemDurable.canBeRefined(upgradeItem) && pp.getSkillLevel(Skill.FORGING) >= 4) {
							int newRefine = Math.max(BItemDurable.getRefinementTier(upgradeItem), BItemDurable.getRefinementTier(infuseItem)) + 1;
							if (newRefine > 15) {
								// TODO: Do something if over refinement 15, for now, do nothing. Very little players will get to this point any time soon (written mid 2022).
							} else {
								isRefining = true;
								left = indicatorRefine;
								right = indicatorRefine;

								// Upgrade the preview item's refinement tier
								BItemDurable.setRefinementTier(resultItem, newRefine, true);

								newCost[0] += 10 + (2 * newRefine * newRefine);
								coinCost[0] = 5000 * newRefine * newRefine;

								purchase[0] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7bCombine and Refine"),
										Lore.getBuilder("Combine the two items provided whilst also refining the item to &" + BeanItem.getItemRarity(resultItem).getRefinementColour().asHexString() +
												"Tier " + Utils.toRoman(BItemDurable.getRefinementTier(resultItem)) + "&7, permanently increasing its stats.").build());
								purchase[0].addSkillRequirement(Skill.FORGING, 4 + newRefine);
							}
						}

						// Check Repair possibilities
						repairVal = 6.75f + ((float)BeanItem.getDurability(infuseItem) / (float)BeanItem.getMaxDurability(infuseItem)) * 100f;
					} else {
						left = name == null ? indicatorInvalid : indicatorRepair;
						right = indicatorInvalid;
					}
				} else if (infuseItem.getType() == Material.ENCHANTED_BOOK) { // Check for Enchanting Book
					EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) infuseItem.getItemMeta();

				} else { // Check for Repair
					repairVal = custom != null ? custom.getRepairPercentage(infuseItem) : upgradeItem.isRepairableBy(infuseItem) ? 20 : 0;
				}

				// Do Repair
				if (repairVal > 0) {
					isRepairing = true;
					repairVal *= (1 - (0.25 * enchantIrreparable));
					double repairCost = 0;
					int repairCoinCost = 0;

					int curDuraInt = BeanItem.getDurability(upgradeItem);
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

						if (!areSimilar())
							this.rightConsumeCount += consumeCount;

						// 100% cap
						if (newDurability >= 100f)
							newDurability = 100f;

						// Increment the XP cost of the repair based on % repaired
						repairCost = ((newDurability - currentDurability) / 22) * (1 + (0.25 * enchantIrreparable));

						// Increment the Coin cost of the repair based on % and durability repaired
						int newDuraInt = (int) ((float) maxDuraInt * (newDurability / 100f));
						repairCoinCost = (int) (((repairCost * 10) + ((newDuraInt - curDuraInt) * 2)) * (1 + (0.25 * enchantIrreparable)));

						// Further, increment the XP cost based on the runic level and refinement level.
						repairCost *= 1 + (BeanItem.getRunicExpenses(upgradeItem) * 0.1);
						repairCost *= 1 + (BItemDurable.getRefinementTier(upgradeItem) * 0.08);

						BeanItem.setDurability(resultItem, (int) (BeanItem.getMaxDurability(resultItem) * (newDurability / 100f)));
						BeanItem.setDurability(preRefineItem, (int) (BeanItem.getMaxDurability(preRefineItem) * (newDurability/100f)));

						newCost[0] += repairCost;
						coinCost[0] += repairCoinCost;
					}

					if (!isRefining) {
						if (areSimilar()) {
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

						if (upgradeItem.isSimilar(preRefineItem))
							needDualOption = false;
						else if (infuseRefineTier == modifyRefuseTier)
							if (infuseItem.getEnchantments().isEmpty() && upgradeItem.getEnchantments().isEmpty())
								if (BeanItem.getDurability(upgradeItem) >= BeanItem.getMaxDurability(upgradeItem))
									needDualOption = false;

						// If the sacrificing item has a larger refinement level, set that as the refinement level.
						if (infuseRefineTier > modifyRefuseTier)
							BItemDurable.setRefinementTier(resultItem, infuseRefineTier, true);

						if (needDualOption) {
							right = indicatorCombine;
							dualOption = true;

							// If the sacrificing item has a larger refinement level, set that as the refinement level.
							if (infuseRefineTier > modifyRefuseTier)
								BItemDurable.setRefinementTier(preRefineItem, infuseRefineTier, true);

							newCost[1] = repairCost;
							coinCost[1] = repairCoinCost;

							purchase[1] = new PurchaseOption(purchaseConfirm, Component.text("\u00a7aCombine and Repair"),
									Lore.getBuilder("Combine the two items provided, repairing and attempting to merge enchantments, burdens, refinement tier and more.").dontFormatColours().build());
						}
					}
				}
			}

			// Update Displayed Item
			if (!resultItem.equals(noResult)) {
				this.result[0] = BeanItem.formatItem(resultItem);
				this.result[1] = dualOption ? BeanItem.formatItem(preRefineItem) : null;
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
							left = incidatorRepaired;
							right = incidatorRepaired;
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
				// Further, increment the price based on the rarity level.
				for (int x = -1; ++x < (dualOption ? 2 : 1);) {
					switch (BeanItem.getItemRarity(upgradeItem)) {
						case TRASH -> { newCost[x] *= 0.5; coinCost[x] *= 0.5; }
						case UNCOMMON -> { newCost[x] *= 1.1; coinCost[x] *= 1.1; }
						case RARE -> { newCost[x] *= 1.25; coinCost[x] *= 1.25; }
						case EPIC -> { newCost[x] *= 1.5; coinCost[x] *= 1.5; }
						case LEGENDARY, EVENT, SPECIAL -> { newCost[x] *= 2; coinCost[x] *= 2; }
						case MYTHIC, ASTRAL -> { newCost[x] *= 3; coinCost[x] *= 3; }
						case IRIDESCENT -> { newCost[x] *= 4; coinCost[x] *= 4; }
					}

					// Finalise the purchase
					purchase[x].addExperienceCost(Math.max(1, (int)newCost[x]));
					purchase[x].addCoinCost(coinCost[x]);
					purchase[x].setPurchaseWord("forge");
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
				i.setItem(21, purchase[0].getDisplayItem(p, enchantWarning));
				i.setItem(23, purchase[1].getDisplayItem(p, enchantWarning));
				i.setItem(12, result[0]);
				i.setItem(13, bBlank);
				i.setItem(14, result[1]);
			} else if (purchase[0] != null) {
				purchaseButton = purchase[0].getDisplayItem(p, enchantWarning);
				i.setItem(13, result[0]);
			} else {
				i.setItem(13, noResult);
			}

			i.setItem(22, purchaseButton);
		});
	}

	private boolean areSimilar() {
		if (upgradeItem == null || infuseItem == null) return false;

		BeanItem item = BeanItem.from(upgradeItem);
		if (item != null) return BeanItem.is(infuseItem, item);

		return upgradeItem.getType().equals(infuseItem.getType());
	}

}
