package me.playground.listeners;

import java.util.List;

import me.playground.items.tracking.DemanifestationReason;
import me.playground.items.tracking.ManifestationReason;
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.BeanColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.event.player.PlayerTradeEvent;
import me.playground.items.BeanItem;
import me.playground.items.ItemRarity;
import me.playground.loot.LootRetriever;
import me.playground.loot.LootTable;
import me.playground.loot.RetrieveMethod;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.skills.Skill;
import net.kyori.adventure.text.Component;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class EntityListener extends EventListener {

	private final NamespacedKey KEY_ENTITY_LEVEL;
	private final NamespacedKey KEY_PIGLIN_BARTERER;
	
	public EntityListener(Main plugin) {
		super(plugin);

		KEY_ENTITY_LEVEL = plugin.getKey("ENTITY_LEVEL");
		KEY_PIGLIN_BARTERER = plugin.getKey("PIGLIN_BARTERER");
		spawnerCountKey = plugin.getKey("spawnerspawns");
	}
	
	private void spawnDamageIndicator(Location loc, int dmg) {
		final ArmorStand di = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND, SpawnReason.CUSTOM, (armorStand) -> {
			ArmorStand stand = (ArmorStand) armorStand;
			stand.setInvisible(true);
			stand.setInvulnerable(true);
			stand.setMarker(true);
			stand.setCollidable(false);
			stand.customName(Component.text(dmg, BeanColor.BAN));
			stand.setCustomNameVisible(true);
			stand.setPersistent(false); // Removes on restart
			stand.setGravity(true);
		});

		Bukkit.getScheduler().runTaskLater(getPlugin(), di::remove, 18);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onStupidDamage(EntityDamageByEntityEvent e) {
		// I don't care if a player is in creative mode, just don't hurt the damn entity ever.
		// If it's Invulnerable, it is likely that for a good reason.
		if (e.getEntity().isInvulnerable()) {
			e.setCancelled(true);
			return;
		}

		// stupid entities
		if (isDumbEntity(e.getEntityType())) {
			final Region r = getRegionAt(e.getEntity().getLocation());
			if (e.getDamager() instanceof Player) {
				enactRegionPermission(r, e, (Player)e.getDamager(), Flags.BUILD_ACCESS, "break");
			} else if (e.getDamager() instanceof Projectile) {
				if (((Projectile) e.getDamager()).getShooter() instanceof Player p) {
					enactRegionPermission(r, e, p, Flags.BUILD_ACCESS, "break");
				} else {
					e.setCancelled(true);
				}
			} else if (!r.isWorldRegion()) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityDamageRegionChecks(EntityDamageByEntityEvent e) {
		final Location loc1 = e.getDamager().getLocation();
		final Location loc2 = e.getEntity().getLocation();
		final Region regionAttacker = getRegionAt(loc1);
		final Region regionDefender = getRegionAt(loc2);

		Player attackingPlayer = null;

		if (e.getEntity() instanceof Player p)
			PlayerProfile.from(p).getHeirlooms().doDamageTakenByEntityEvent(e);
		if (e.getDamager() instanceof Player p)
			PlayerProfile.from(p).getHeirlooms().doMeleeDamageEvent(e);

		// Against a Player
		if (e.getEntity() instanceof Player && (e.getDamager() instanceof LivingEntity || e.getDamager() instanceof Projectile || e.getDamager() instanceof Tameable)) {

			if ((e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player p)) // Projectile fired from the player
				attackingPlayer = p;
			else if ((e.getDamager() instanceof Tameable tameable) && tameable.isTamed()) { // Tamed entity attack
				attackingPlayer = (Player) tameable.getOwner();

				// Check for animal protection with tame-ables, if they are protected from being attacked here, don't let them attack.
				if (regionAttacker.getEffectiveFlag(Flags.PROTECT_ANIMALS)) {
					e.setCancelled(true);
					return;
				}
			}

			if (attackingPlayer != null) {
				if (!(regionAttacker.getEffectiveFlag(Flags.PVP) && regionDefender.getEffectiveFlag(Flags.PVP)))
					e.setCancelled(true);
			} else {
				e.setDamage(e.getDamage() * regionDefender.getEffectiveFlag(Flags.MOB_DAMAGE_FROM));
			}
			// Against regular entities
		} else if (e.getEntity() instanceof LivingEntity) {
			attackingPlayer = (e.getDamager() instanceof Player) ? (Player)e.getDamager() : null;
			if ((e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player))
				attackingPlayer = (Player) ((Projectile)e.getDamager()).getShooter();

			// Check for Animal/Villager Protection
			if ((e.getEntity() instanceof Villager || e.getEntity() instanceof Animals || (e.getEntity() instanceof Fish fish && fish.isFromBucket())) && regionDefender.getEffectiveFlag(Flags.PROTECT_ANIMALS))
				if (attackingPlayer == null || !regionDefender.getMember(attackingPlayer).higherThan(MemberLevel.VISITOR))
					e.setDamage(0);

			// Check specifically for Villager Protection against Players only
			if (e.getEntity() instanceof Villager && attackingPlayer != null && !checkRegionPermission(regionDefender, e, attackingPlayer, Flags.VILLAGER_ACCESS))
				e.setDamage(0);

			if (e.getDamage() > 0) {
				e.setDamage(e.getDamage() * regionDefender.getEffectiveFlag(Flags.MOB_DAMAGE_TO));
				if (e.getDamage() > 0) {
					if (e.getEntity() instanceof Cat || e.getEntity() instanceof Wolf || e.getEntity() instanceof Parrot || e.getEntity() instanceof AbstractHorse) {
						Tameable tamed = (Tameable) e.getEntity();

						if (tamed.isTamed()) {
							// Allow mobs to kill pets, disallow players to kill pets
							if (attackingPlayer == null || regionAttacker.getEffectiveFlag(Flags.PVP) && regionDefender.getEffectiveFlag(Flags.PVP))
								return;

							// Otherwise, if not the owner, set the damage to 0.
							if (tamed.getOwner() != null && tamed.getOwner().getUniqueId() != e.getDamager().getUniqueId())
								e.setDamage(0);
						}
					}
				}
			}
		}

		if (e.getDamage() <= 0) {
			e.setCancelled(true);
		} else if (attackingPlayer != null) { // Remove invulnerability from teleporting if they're deciding to attack things already.
			PlayerProfile.from(attackingPlayer).clearCooldown("teleportInvulnerability");
			attackingPlayer.sendActionBar(Component.text("Your teleport protection has faded."));
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamageFinal(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player p)) return;
		if (!isDumbEntity(e.getEntityType())) {
			PlayerProfile.from(p).getSkills().doSkillEvents(e, Skill.COMBAT);
			spawnDamageIndicator(e.getEntity().getLocation().add(-0.5 + getPlugin().getRandom().nextDouble(), (e.getEntity().getHeight()/2) + ((e.getEntity().getHeight()/4) * (rand.nextDouble(2) - 1)), -0.5 + getPlugin().getRandom().nextDouble()), (int)e.getFinalDamage());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityGrief(EntityChangeBlockEvent e) {
		// Prevent enderman griefing
		if (e.getEntityType().equals(EntityType.ENDERMAN))
			e.setCancelled(true);
		
		// Prevent boat griefing in non-world regions
		else if (e.getEntity().getType() == EntityType.BOAT)
			e.setCancelled(!getRegionAt(e.getBlock().getLocation()).isWorldRegion());
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSnowman(EntityBlockFormEvent e) {
		// Prevent mobs leaving ice and snow trails
		if (!(e.getEntity() instanceof Player))
			e.setCancelled(!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.ENTITY_TRAILS));
		else {
			final Region r = getRegionAt(e.getBlock().getLocation());
			e.setCancelled(!checkRegionPermission(r, e, (Player)e.getEntity(), Flags.BUILD_ACCESS));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFrameAttack(HangingBreakByEntityEvent e) {
		// Protect Item Frames from being destroyed by players
		final Region r = getRegionAt(e.getEntity().getLocation());
		if (e.getRemover() instanceof Player) {
			enactRegionPermission(r, e, (Player)e.getRemover(), Flags.BUILD_ACCESS, "break");
		} else if (e.getRemover() instanceof Projectile && ((Projectile)e.getRemover()).getShooter() instanceof Player) {
			enactRegionPermission(r, e, ((Player)((Projectile)e.getRemover()).getShooter()), Flags.BUILD_ACCESS, "break");
		} else if (!r.isWorldRegion()) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(HangingBreakEvent e) {
		// Protect Item Frames from being blown up
		if (e.getCause() == RemoveCause.EXPLOSION)
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player p) {
			PlayerProfile pp = PlayerProfile.from(p);
			// Teleport Invulnerability check
			if (pp.onCooldown("teleportInvulnerability")) {
//				p.getWorld().spawnParticle(Particle.SONIC_BOOM, p.getEyeLocation(), 1, 0, 0, 0, 0.1);
				e.setCancelled(true);
			}
			return;
		}

		if (e.getEntity() instanceof Item item) {
			// Protect Dropped Items from being blown up
			if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION) {
				e.setCancelled(true);
				return;
			}

			// Log the destruction of the item
			getPlugin().getItemTrackingManager().incrementDemanifestationCount(item.getItemStack(), DemanifestationReason.DESTROYED, item.getItemStack().getAmount());
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player p)) return;
		if (e.getCause() != DamageCause.FALL) return;

		Skill.ACROBATICS.performSkillEvent(PlayerProfile.from(p).getSkills(), e);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDeath(EntityDeathEvent e) {
		// Don't interfere with Armour Stands
		if (e.getEntity() instanceof ArmorStand) {
			if (((ArmorStand)e.getEntity()).isMarker())
				e.setCancelled(true);
			return;
		}
		
		// Baby Animals don't drop anything.
		if (e.getEntity() instanceof Animals && !((Animals)e.getEntity()).isAdult()) return;
		
		final LootTable lootTable = getPlugin().lootManager().getLootTable(e.getEntityType());
		boolean chargedKill = false, skeletonKill = false, petKill = false, nerfDrops = true, isMonster = e.getEntity() instanceof Monster, isNatural = true;
		Player p = null;
		if (e.getEntity().getLastDamageCause() != null) {
			if ((e.getEntity().getLastDamageCause()) instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
				if (ev.getDamager() instanceof Skeleton) {
					skeletonKill = true;
				} else if (ev.getDamager() instanceof Projectile) {
					if (((Projectile)ev.getDamager()).getShooter() != null && ((Projectile)ev.getDamager()).getShooter() instanceof Skeleton)
						skeletonKill = true;
				} else if (ev.getDamager() instanceof Creeper && ((Creeper)ev.getDamager()).isPowered()) {
					chargedKill = true;
				} else if (ev.getDamager() instanceof Tameable tameable && tameable.isTamed()) {
					petKill = true;
				}
			}
		}
		
		if (e.getEntity().getKiller() != null) {
			nerfDrops = petKill;
			// If the player kill designation hasn't timed out, check the local area if it's a grinder.
			p = e.getEntity().getKiller();
		} else if (chargedKill || skeletonKill) {
			nerfDrops = false;
			// If the player kill designation timed out, just check for the entities' current player target, which 99.999% of the time they will have one when attempting these special drops.
			if (isMonster)
				if (((Monster)e.getEntity()).getTarget() instanceof Player)
					p = (Player) ((Monster)e.getEntity()).getTarget();
		}

		// TODO: Have a better check for grinder entities than just checking nearby entity count
		// Nerf entities from spawners or in grinders ALWAYS.
		if (e.getEntity().fromMobSpawner() || e.getEntity().getWorld().getNearbyEntitiesByType(e.getEntityType().getEntityClass(), e.getEntity().getLocation(), 3).size() > 3) {
			isNatural = false;
			nerfDrops = true;
		}

		PlayerProfile pp = p != null ? PlayerProfile.from(p) : null;
		int looting = p != null ? p.getInventory().getItemInMainHand().getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS) : nerfDrops ? -1 : 0;
		float luck = p != null ? pp.getLuck() : nerfDrops ? -10 : 0;

		if (p != null) {
			// Do the magic of the custom stuff for this server.
			// Add to the player's statistics if it's a non-grinder mob, add coin drops, apply the multipliers and drop the custom loot.
			final Region region = getRegionAt(e.getEntity().getLocation());
			
			// Stats
			pp.getStats().addToStat(StatType.KILLS, e.getEntityType().name(), 1);
			pp.getStats().addToStat(StatType.KILLS, "total", 1, false);
			if (skeletonKill) 
				pp.getStats().addToStat(StatType.KILLS, "withSkeletonShot", 1, false);
			if (chargedKill) 
				pp.getStats().addToStat(StatType.KILLS, "withChargedCreeper", 1, false);
			if (petKill)
				pp.getStats().addToStat(StatType.KILLS, "withVanillaPet", 1, false);
			
			// Coin Drops
			/*if (isMonster) {
				int hp = (int) e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if (!(hp/2 < 1))
					pp.addToBalance((long) (2 + getPlugin().getRandom().nextInt(hp/2) * region.getEffectiveFlag(Flags.MOB_DROP_COIN)));
			}*/

			if (e.getDroppedExp() > 0) {
				// EXP Level Multiplier
				PersistentDataContainer container = e.getEntity().getPersistentDataContainer();
				int mobLevel = container.getOrDefault(KEY_ENTITY_LEVEL, PersistentDataType.SHORT, (short)1);
				e.setDroppedExp((int) ((float)e.getDroppedExp() * (1 + (mobLevel/10)))); // 10% bonus xp base for level 1 mobs

				// EXP Region Multiplier
				e.setDroppedExp((int) ((float)e.getDroppedExp() * region.getEffectiveFlag(Flags.MOB_DROP_EXP)));
			}
		}

		// Remove 50% xp
		if (nerfDrops) e.setDroppedExp((int) ((float)e.getDroppedExp()/2F));

		// Custom Loot
		if (lootTable != null) {
			e.getDrops().clear();

			// 50% chance to not drop anything
			if (nerfDrops && rand.nextBoolean()) return;

			e.getDrops().addAll(LootRetriever.from(lootTable, RetrieveMethod.INDIVIDUAL_CHANCE, p)
					.looting(looting)
					.biome(e.getEntity().getLocation().getBlock().getBiome())
					.burn(e.getEntity().getFireTicks() > 0)
					.skeleton(skeletonKill)
					.entity(e.getEntity())
					.creeper(chargedKill)
					.luck(luck)
					.natural(isNatural)
					.getLoot());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onExplosionPrime(ExplosionPrimeEvent e) {
		switch(e.getEntityType()) {
			case WITHER_SKULL, WITHER -> {
				if (!getRegionAt(e.getEntity().getLocation()).getEffectiveFlag(Flags.ENTITY_EXPLOSIONS))
					e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onExplosion(EntityExplodeEvent e) {
		boolean cancelBlockDamage;
		final Region r = getRegionAt(e.getLocation());

		cancelBlockDamage = switch (e.getEntityType()) {
			case PRIMED_TNT, MINECART_TNT -> !r.getEffectiveFlag(Flags.BLOCK_EXPLOSIONS);
			default -> !r.getEffectiveFlag(Flags.ENTITY_EXPLOSIONS);
		};
		
		if (cancelBlockDamage)
			e.blockList().clear();

		// Poison Cloud from Creepers if block damage is cancelled.
		if (cancelBlockDamage && e.getEntityType() == EntityType.CREEPER) {
			int level = e.getEntity().getPersistentDataContainer().getOrDefault(KEY_ENTITY_LEVEL, PersistentDataType.SHORT, (short)1);
			e.getLocation().getWorld().playSound(e.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.4F, 0.45F);
			e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.AREA_EFFECT_CLOUD, SpawnReason.CUSTOM, (entity) -> {
				AreaEffectCloud cloud = (AreaEffectCloud) entity;
				cloud.setBasePotionData(new PotionData(PotionType.POISON));
				cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 130 + level * 20, 2 + (level/5)), true);
				cloud.setDuration(100);
				cloud.setRadius(2F);
				cloud.setWaitTime(15);
				cloud.setRadiusPerTick(0.04F);
			});
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityTransform(EntityTransformEvent e) {
		if (e.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING)
			getRegionAt(e.getEntity().getLocation()).getEffectiveFlag(Flags.PROTECT_ANIMALS);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemSpawn(ItemSpawnEvent e) {
		ItemStack item = e.getEntity().getItemStack();
		e.getEntity().setItemStack(BeanItem.formatItem(item));
	}
	
	@EventHandler()
	public void onEntityItemPickup(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player) {
			PlayerProfile pp = PlayerProfile.from(((Player)e.getEntity()));
			if (!pp.canPickupItem(e.getItem()))
				e.setCancelled(true);
			else if (pp.getBeanGui() != null) 
				pp.getBeanGui().onItemPickup(e);
		} else if (e.getEntity() instanceof Monster) {
			if (e.getEntity() instanceof Piglin piglin) { // Track piglin barterer
				if (e.getItem().getThrower() != null) {
					ProfileStore ps = ProfileStore.from(e.getItem().getThrower(), true);
					if (ps == null) return;

					piglin.getPersistentDataContainer().set(KEY_PIGLIN_BARTERER, PersistentDataType.INTEGER, ps.getId());
				}
			} else { // Cancel hostile item pickups for now.
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDeath(ItemDespawnEvent e) {
		getPlugin().getItemTrackingManager().incrementDemanifestationCount(e.getEntity().getItemStack(), DemanifestationReason.DESPAWNED, e.getEntity().getItemStack().getAmount());
	}

	@EventHandler
	public void onPiglinBarter(PiglinBarterEvent e) {
		if (e.getEntity().getPersistentDataContainer().has(KEY_PIGLIN_BARTERER)) {
			int id = e.getEntity().getPersistentDataContainer().getOrDefault(KEY_PIGLIN_BARTERER, PersistentDataType.INTEGER, 0);
			PlayerProfile pp = PlayerProfile.fromIfExists(id);
			if (pp != null) { // TODO: Temporary value
				pp.getSkills().addExperience(Skill.TRADING, 20);
				pp.getStats().addToStat(StatType.TRADING, "piglinBarters", 1);
			}

			e.getEntity().getPersistentDataContainer().remove(KEY_PIGLIN_BARTERER);
		}

		getPlugin().getItemTrackingManager().incrementDemanifestationCount(e.getInput(), DemanifestationReason.BARTERING, e.getInput().getAmount());

		for (ItemStack item : e.getOutcome())
			getPlugin().getItemTrackingManager().incrementManifestationCount(item, ManifestationReason.BARTERING, item.getAmount());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onVillagerTrade(PlayerTradeEvent e) {
		// TODO: Temporary value
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		pp.getSkills().addExperience(Skill.TRADING, 20);
		pp.getStats().addToStat(StatType.TRADING, "villagerTrades", 1);

		getPlugin().getItemTrackingManager().incrementManifestationCount(e.getTrade().getResult(), ManifestationReason.TRADING, e.getTrade().getResult().getAmount());

		for (ItemStack cost : e.getTrade().getIngredients()) {
			if (cost == null || cost.getType() == Material.AIR) continue;
			getPlugin().getItemTrackingManager().incrementDemanifestationCount(cost, DemanifestationReason.TRADING, cost.getAmount());
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onVillagerAcquireTrade(VillagerAcquireTradeEvent e) {
		MerchantRecipe recipe = e.getRecipe();
		List<ItemStack> ingredients = recipe.getIngredients();

		// Nerf positive and negative effects to villager prices
		recipe.setPriceMultiplier(recipe.getPriceMultiplier() / 3);
		
		// Make enchants rarer
		if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
			getPlugin().getEnchantmentManager().replaceEnchantments(recipe.getResult(), true); // TODO: TEMPORARY
			ItemRarity rarity = BeanItem.getItemRarity(recipe.getResult());
			int max = 5;

			// Prevent any epic
			if (rarity.is(ItemRarity.EPIC)) {
				e.setCancelled(true);
				return;
			}

			switch (rarity) {
//			case LEGENDARY: max = 1; ingredients.set(0, new ItemStack(Material.EMERALD_BLOCK, 55 + getPlugin().getRandom().nextInt(5))); ingredients.set(1, new ItemStack(Material.NETHER_STAR, 3)); break;
//			case EPIC: max = 1; ingredients.set(0, new ItemStack(Material.EMERALD_BLOCK, 23 + getPlugin().getRandom().nextInt(8))); break;
				case RARE -> {
					max = 3;
					ingredients.set(0, new ItemStack(Material.EMERALD_BLOCK, 24 + getPlugin().getRandom().nextInt(8)));
				}
				case UNCOMMON -> ingredients.set(0, new ItemStack(Material.EMERALD, 54 + getPlugin().getRandom().nextInt(10)));
				default -> ingredients.set(0, new ItemStack(Material.EMERALD, 40 + getPlugin().getRandom().nextInt(7)));
			}
			recipe.setPriceMultiplier(0.01F); // Even harder nerf, this is mostly for the positive discounts which are INSANE in Vanilla.
			recipe.setMaxUses(max);
		}
		
		final int iSize = ingredients.size();
		for (int y = -1; ++y < iSize;)
			if (ingredients.get(y).getType() != Material.AIR)
				BeanItem.formatItem(ingredients.get(y));
		
		MerchantRecipe newRecipe = new MerchantRecipe(BeanItem.formatItem(recipe.getResult()), recipe.getUses(), 
				recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.shouldIgnoreDiscounts());
		newRecipe.setIngredients(ingredients);
		
		e.setRecipe(newRecipe);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onVillagerCareerChange(VillagerCareerChangeEvent e) {
		e.getEntity().setVillagerExperience(2);
	}

	/*@EventHandler(priority = EventPriority.LOW)
	public void onVillagerChangeCareer(VillagerCareerChangeEvent e) {
		List<MerchantRecipe> recipes = e.getEntity().getRecipes();
		final int size = recipes.size();
		
		for (int x = -1; ++x < size;) {
			MerchantRecipe recipe = recipes.get(x);
			// Make enchants rarer
			if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
				recipe.setMaxUses(recipe.getMaxUses() / 2);
				if (((EnchantmentStorageMeta)recipe.getResult().getItemMeta()).hasEnchant(Enchantment.MENDING))
					recipe.setMaxUses(2);
			}
			
			BeanItem.formatItem(recipe.getResult()); // This should work.
			List<ItemStack> ingredients = recipe.getIngredients();
			final int iSize = ingredients.size();
			for (int y = -1; ++y < iSize;)
				if (ingredients.get(y).getType() != Material.AIR)
					BeanItem.formatItem(ingredients.get(y));
			recipe.setIngredients(ingredients); // Need to do this since getIngredients clones by default.
		}
	}*/
	
	private final NamespacedKey spawnerCountKey;
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onSpawnerSpawn(SpawnerSpawnEvent e) {
		// Cancel spawner spawns if the spawner is powered.
		if (e.getSpawner().getBlock().isBlockPowered() || !(getRegionAt(e.getSpawner().getLocation()).getEffectiveFlag(Flags.MOB_SPAWNERS))) {
			e.setCancelled(true);
			return;
		}

		int count = e.getSpawner().getPersistentDataContainer().getOrDefault(spawnerCountKey, PersistentDataType.INTEGER, 0);
		e.getSpawner().getPersistentDataContainer().set(spawnerCountKey, PersistentDataType.INTEGER, count + 1);
		e.getSpawner().update();
		e.getEntity().setPortalCooldown(Integer.MAX_VALUE); // Good enough
	}

	//final int chunkEntityLimit = 300;
	//TODO: determine practicality
	@EventHandler(priority = EventPriority.LOWEST)
	public void preEntitySpawn(CreatureSpawnEvent e) {
		// Cancel living entity spawning if the chunk limit is hit.
		//int amountCurrently = e.getEntity().getChunk().getEntities().length;
		//if (amountCurrently >= chunkEntityLimit)
		//	e.setCancelled(true);

		// Cancel Pig Zombies spawning in portals
		if (e.getSpawnReason() == SpawnReason.NETHER_PORTAL)
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntitySpawn(CreatureSpawnEvent e) {
		// Disable ticking for player placed armor stands
		if (e.getEntity().getType() == EntityType.ARMOR_STAND && e.getSpawnReason() == SpawnReason.DEFAULT) {
			ArmorStand stand = (ArmorStand) e.getEntity();
			stand.setCanTick(false);
//			stand.setArms(true);
			return;
		}

		// Check region flags for natural mobs
		if (e.getSpawnReason() == SpawnReason.NATURAL) {
			final Region r = getRegionAt(e.getLocation());
			if (e.getEntity() instanceof Monster)
				e.setCancelled(!r.getEffectiveFlag(Flags.MOB_HOSTILE_SPAWNS));
			else
				e.setCancelled(!r.getEffectiveFlag(Flags.MOB_PASSIVE_SPAWNS));
		}
	}

	// TODO: make this more elaborate, this is merely a test
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntitySpawn2(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL) {
			LivingEntity monster = e.getEntity();
			short level = (short) (1 + rand.nextInt(5));

			switch (e.getEntityType()) {
				case SHULKER -> {
				}
				// Remove armour equipment for now
				case SKELETON, ZOMBIE -> {
					ItemStack hand = monster.getEquipment().getItemInMainHand();
					monster.getEquipment().clear();

					monster.getEquipment().setItemInMainHand(hand);
				}
				// Increase the movement speed of Spiders and Creepers
				case SPIDER, CAVE_SPIDER, CREEPER -> {
					if (level > 1) {
						AttributeInstance attribute = monster.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
						attribute.addModifier(new AttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED.translationKey(), (level-1) * 0.002, Operation.ADD_NUMBER));
					}
				}
				case WITHER_SKELETON, SILVERFISH -> { }
				case WITHER -> {
					AttributeInstance attribute = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					attribute.addModifier(new AttributeModifier(Attribute.GENERIC_MAX_HEALTH.translationKey(), 1, Operation.MULTIPLY_SCALAR_1));
					monster.setHealth(attribute.getValue());
					return;
				}
				default -> { return; }
			}

			monster.getPersistentDataContainer().set(KEY_ENTITY_LEVEL, PersistentDataType.SHORT, level);

			// Increase the HP of mobs above level 1
			if (level > 1) {
				AttributeInstance attribute = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
				attribute.addModifier(new AttributeModifier(Attribute.GENERIC_MAX_HEALTH.translationKey(), (level-1) * 0.1, Operation.MULTIPLY_SCALAR_1));
				monster.setHealth(attribute.getValue());
			}

			// Increase the armour of mobs above level 3
			if (level > 3) {
				AttributeInstance attribute = monster.getAttribute(Attribute.GENERIC_ARMOR);
				if (attribute == null)
					monster.registerAttribute(Attribute.GENERIC_ARMOR);
				attribute.addModifier(new AttributeModifier(Attribute.GENERIC_ARMOR.translationKey(), 3 + rand.nextInt(level * 2), Operation.ADD_NUMBER));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onEntityTrample(EntityInteractEvent e) {
		if (e.getBlock().getType() == Material.FARMLAND)
			e.setCancelled(true);
	}
	
	private boolean isDumbEntity(EntityType e) {
		return e == EntityType.PAINTING || e == EntityType.ARMOR_STAND || e == EntityType.GLOW_ITEM_FRAME || e == EntityType.ITEM_FRAME;
	}
	
}
