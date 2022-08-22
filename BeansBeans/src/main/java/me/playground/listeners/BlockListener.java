package me.playground.listeners;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.playground.gui.stations.BeanGuiEnchantingTable;
import me.playground.items.BeanBlock;
import me.playground.items.BeanItem;
import me.playground.items.tracking.ManifestationReason;
import me.playground.listeners.events.CustomBlockBreakEvent;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.skills.Skill;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class BlockListener extends EventListener {
	
	public BlockListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent e) {
		Region region = getRegionAt(e.getBlock().getLocation());
		Block block = e.getBlock();

		if (!enactRegionPermission(region, e, e.getPlayer(), Flags.BUILD_ACCESS, "build")) return;
		
		String blockName = block.getType().name();
		BeanItem custom = BeanItem.from(e.getItemInHand());
		
		// Custom Blocks
		if (custom instanceof BeanBlock) {
			if ((blockName = ((BeanBlock)custom).preBlockPlace(e)) == null) {
				e.setCancelled(true);
				return;
			}

			// Don't continue if the event has been cancelled.
			if (e.isCancelled()) return;
		}
		
		// Handle the custom names on containers and make the illusion that nothing changed.
		else if (block.getState() instanceof Container container) {

			// Disallow connection to custom Chests.
			if (container instanceof Chest) {
				if (container.getBlockData() instanceof org.bukkit.block.data.type.Chest chestData) {
					// Check if the current chest we're placing is in the process of turning into a double chest..
					if (chestData.getType() != Type.SINGLE) {
						// Grab the facing direction to determine the order of faces to check.
						// This can save time if the first connection is immediately a valid one.
						boolean right = chestData.getType() == Type.RIGHT;
						
						// Check whether we need to check the Z or the X axis for adjacent chests.
						Vector[] adjacentChecks = new Vector[2];
						if (chestData.getFacing().getModX() != 0) { // Z
							adjacentChecks[right ? 1 : 0] = new Vector(0, 0, chestData.getFacing().getModX());
							adjacentChecks[right ? 0 : 1] = new Vector(0, 0, -chestData.getFacing().getModX());
						} else { // X
							adjacentChecks[right ? 0 : 1] = new Vector(chestData.getFacing().getModZ(), 0, 0);
							adjacentChecks[right ? 1 : 0] = new Vector(-chestData.getFacing().getModZ(), 0, 0);
						}
						
						boolean firstAttempt = true;
						for (Vector vectorCheck : adjacentChecks) {
							Block b = block.getLocation().add(vectorCheck).getBlock();
							if (!(b.getState() instanceof Chest chest2)) {
								if (firstAttempt) continue;
								chestData.setType(Type.SINGLE); // No second chest to check for and the first check failed, set to single.
							} else {
								org.bukkit.block.data.type.Chest chestData2 = (org.bukkit.block.data.type.Chest) chest2.getBlockData();
								if (chestData.getFacing().equals(chestData2.getFacing())) {
									
									// If the checked chest is already a double or it's a custom, fail and flip the check to the other side.
									if (chestData2.getType() != Type.SINGLE || BeanBlock.from(b) != null) {
										// Reset its type
										chestData2.setType(chestData2.getType());
										chest2.setBlockData(chestData2);
										chest2.update();
										if (firstAttempt) {
											chestData.setType(right ? Type.LEFT : Type.RIGHT);
										} else {
											chestData.setType(Type.SINGLE);
										}
									// Valid connection, break free.
									} else {
										break;
									}
								}
								firstAttempt = false;
							}
						}
						container.setBlockData(chestData);
						container.update();
					}
				}
			}

			Component name = container.customName();
			if (name != null) { // Remove colour
				container.customName(name.color(null));
				container.update();
			}
		}
		
		// Allow for signs to be edited.
		else if (block.getState() instanceof Sign sign) {
			sign.setEditable(true);
			sign.update();
		}
		
		// Enchant Table default container values.
		else if (block.getState() instanceof EnchantingTable table) {
			PersistentDataContainer pdc = table.getPersistentDataContainer();
			
			pdc.set(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)0);
			pdc.set(BeanGuiEnchantingTable.KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
			table.update();
		}
		
		// Block placing, ignore crops to allow xp
		if (!(block.getBlockData() instanceof Ageable))
			block.setMetadata("placed", new FixedMetadataValue(getPlugin(), true));
		
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		pp.getStats().addToStat(StatType.BLOCK_PLACE, blockName, 1);
		pp.getStats().addToStat(StatType.BLOCK_PLACE, "total", 1, true);
		
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		pp.getSkills().doSkillEvents(e, Skill.BUILDING);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		Region region = getRegionAt(block.getLocation());
		Player p = e.getPlayer();

		boolean canBuild = checkRegionPermission(region, e, p, Flags.BUILD_ACCESS);

		// Crop Access + Crop Replenish combo allows for destroying crops inside the region.
		if (isHarvestableCrop(block.getType()) && region.getEffectiveFlag(Flags.CROP_REPLENISH))
			canBuild = checkRegionPermission(region, e, p, Flags.CROP_ACCESS);

		if (!enactRegionPermission(canBuild, e, p, "build")) return;
		
		CustomBlockBreakEvent ce = e instanceof CustomBlockBreakEvent ? ((CustomBlockBreakEvent)e) : null;
		PlayerProfile pp = ce != null ? ce.getProfile() : PlayerProfile.from(p);
		
		// Activate Custom Item Events
		if (ce == null || ce.isActivatingCustomItems())
			BeanItem.from(p.getEquipment().getItemInMainHand(), (custom) -> custom.onBlockMined(e));
		
		String blockName = e.getBlock().getType().name();
		
		// Check Custom Block Ids
		if (ce == null || ce.isCheckingCustomBlocks()) {
			BeanBlock customBlock = BeanBlock.from(e.getBlock(), (custom) -> custom.onBlockBreak(e));
			if (customBlock != null)
				blockName = customBlock.getIdentifier();
		}

		// Stop placing and breaking a block for xp.
		if (e.getBlock().hasMetadata("placed")) {
			if (p.getGameMode() == GameMode.CREATIVE) return;
			pp.getSkills().doSkillEvents(e, Skill.MINING);
		} else {
			pp.getStats().addToStat(StatType.BLOCK_BREAK, blockName, 1);
			pp.getStats().addToStat(StatType.BLOCK_BREAK, "total", 1, true);
			if (p.getGameMode() == GameMode.CREATIVE) return;
			pp.getSkills().doSkillEvents(e, Skill.MINING, Skill.FORAGING, Skill.AGRICULTURE);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreakFinal(BlockBreakEvent e) {
		e.getBlock().removeMetadata("noHopper", getPlugin());
		e.getBlock().removeMetadata("placed", getPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onExplosion(BlockExplodeEvent e) {
		if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.EXPLOSIONS))
			e.blockList().clear();
	}

	/**
	 * When a block like water, lava or pistons break a crop we will nerf the drops to balance automation a bit
	 */
	@EventHandler
	public void onBlockHarvestBlock(BlockBreakBlockEvent e) {
		Block b = e.getBlock();
		boolean track = false;

		if (b.getState() instanceof Ageable ageable) {
			boolean harvestAge = ageable.getAge() >= ageable.getMaximumAge();

			// 80% chance for poor quality and less seed drops.
			switch(b.getType()) {
				case NETHER_WART -> {
					if (harvestAge) {
						track = true;
						if (rand.nextInt(100) > 19) {
							e.getDrops().clear();
							e.getDrops().add(new ItemStack(Material.NETHER_WART, 2));
						}
					}
				}
				case WHEAT -> {
					if (harvestAge) {
						track = true;
						if (rand.nextInt(100) > 19) {
							e.getDrops().clear();
							e.getDrops().add(BeanItem.POOR_QUALITY_WHEAT.getItemStack());
							e.getDrops().add(new ItemStack(Material.WHEAT_SEEDS, 1));
						}
					}
				}
				case BEETROOTS -> {
					if (harvestAge) {
						track = true;
						if (rand.nextInt(100) > 19) {
							e.getDrops().clear();
							e.getDrops().add(BeanItem.POOR_QUALITY_BEETROOT.getItemStack());
							e.getDrops().add(new ItemStack(Material.BEETROOT_SEEDS, 1));
						}
					}
				}
				case POTATO -> {
					if (harvestAge) {
						track = true;
						if (rand.nextInt(100) > 19) {
							e.getDrops().clear();
							ItemStack poorPotato = BeanItem.POOR_QUALITY_POTATO.getItemStack();
							poorPotato.setAmount(1 + rand.nextInt(4));
							e.getDrops().add(poorPotato);
						}
					}
				}
				case CARROTS -> {
					if (harvestAge) {
						track = true;
						if (rand.nextInt(100) > 19) {
							e.getDrops().clear();
							ItemStack poorCarrot = BeanItem.POOR_QUALITY_CARROT.getItemStack();
							poorCarrot.setAmount(1 + rand.nextInt(4));
							e.getDrops().add(poorCarrot);
						}
					}
				}
			}
		}
		if (b.getType() == Material.MELON) {
			if (!b.hasMetadata("placed")) {
				track = true;
				e.getDrops().clear();
				if (rand.nextInt(8) > 0) // 12.5% chance for no melon
					e.getDrops().add(new ItemStack(Material.MELON_SLICE, 1 + rand.nextInt(2)));
			}
		} else if (b.getType() == Material.PUMPKIN) {
			if (!b.hasMetadata("placed")) {
				track = true;
				e.getDrops().clear();
				if (rand.nextInt(10) > 0) // 10% chance for no pumpkin seeds
					e.getDrops().add(new ItemStack(Material.PUMPKIN_SEEDS, 1));
				if (rand.nextInt(10) == 9) // 10% chance for pumpkin block
					e.getDrops().add(new ItemStack(Material.PUMPKIN));
			}

		}

		b.removeMetadata("placed", getPlugin());

		if (track)
			for (ItemStack item : e.getDrops())
				if (item != null)
					getPlugin().getItemTrackingManager().incrementManifestationCount(item, ManifestationReason.AUTO_HARVEST, item.getAmount());
	}

	/**
	 * Blocks burning due to fire
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBurn(BlockBurnEvent e) {
		e.setCancelled(true);
	}

	/**
	 * Blocks igniting on fire
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onSpread(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.SPREAD || e.getCause() == IgniteCause.LIGHTNING)
			e.setCancelled(true);
	}

	/**
	 * Armour being dispensed by a dispenser, attempting to auto equip
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onDispenseArmour(BlockDispenseArmorEvent e) {
		// Check for custom items that shouldn't be equipped.
		BeanBlock bBlock = BeanBlock.from(e.getItem(), BeanBlock.class);
		if (bBlock == null) return;

		e.setCancelled(!bBlock.isWearable());
	}
	
	/**
	 * When an item is dropped from a block
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onItemDrop(BlockDropItemEvent e) {
		if (e.getItems().isEmpty()) return;
		final BlockState state = e.getBlockState();
		
		// If custom block, add to the drops, ignore following checks.
		boolean isCustom = BeanBlock.from(state, (custom) -> {
			e.getItems().get(0).setItemStack(custom.getItemStack());
			custom.onBlockDropItems(e);
		}) == null;

		if (!isCustom) {
			if (state.getType() == Material.ANCIENT_DEBRIS) {
				if (!e.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
					// Make the non-silk drop from Ancient Debris into Netherite Scrap to support the anti Mining skill XP Server Restart Exploit.
					e.getItems().get(0).setItemStack(new ItemStack(Material.NETHERITE_SCRAP));
			}

			// Drop lapis from the storage compartment in enchanting tables.
			if (state instanceof EnchantingTable) {
				int count = ((EnchantingTable)e.getBlockState()).getPersistentDataContainer().getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)0);

				if (count > 0) {
					int blocks = count / 9;
					int rem = count % 9;
					int loops = (blocks / 64) + 1;

					for (int x = -1; ++x < loops;) {
						int amt = Math.min(64, blocks);
						blocks -= amt;
						e.getItems().add(e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.LAPIS_BLOCK, amt)));
					}

					if (rem > 0)
						e.getItems().add(e.getBlock().getWorld().dropItem(e.getBlock().getLocation().add(0.5, 0, 0.5), new ItemStack(Material.LAPIS_LAZULI, rem)));
				}
			}
		}
		
		PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(e, Skill.MINING, Skill.FORAGING, Skill.AGRICULTURE);
	}
	
	/**
	 * Effects blocks such as Vines and Mushrooms
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockSpread(BlockSpreadEvent e) {
		if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.BLOCK_SPREAD))
			e.setCancelled(true);
	}
	
	/**
	 * Effects blocks such as Wheat, Sugar Cane, Bamboo, Cactus, Watermelon, Pumpkins and Eggs.
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockGrow(BlockGrowEvent e) {
		if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.BLOCK_SPREAD))
			e.setCancelled(true);
	}
	
	/**
	 * Effects the formation of things like Obsidian, Ice and Snow Layers.
	 * We're only checking for snow and ice here.
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockForm(BlockFormEvent e) {
		if (e.getNewState().getType() == Material.SNOW) {
			if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.SNOW_FORMATION))
				e.setCancelled(true);
		} else if (e.getNewState().getType() == Material.ICE) {
			if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.ICE_FORMATION))
				e.setCancelled(true);
		} else if (e.getNewState().getType() == Material.STONE || e.getNewState().getType() == Material.COBBLESTONE) {
			if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.STONE_FORMATION))
				e.setCancelled(true);
		} else if (e.getNewState().getType() == Material.OBSIDIAN) {
			if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.OBSIDIAN_FORMATION))
				e.setCancelled(true);
		}
	}

	private boolean isHarvestableCrop(Material m) {
		return switch (m) {
			case WHEAT, COCOA, CARROTS, POTATOES, NETHER_WART/*, CACTUS*/, MELON, PUMPKIN, BEETROOTS/*, SUGAR_CANE*/ -> true;
			default -> false;
		};
	}

}
