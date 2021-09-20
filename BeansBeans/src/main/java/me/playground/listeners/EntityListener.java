package me.playground.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Cat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.inventory.ItemStack;

import me.playground.items.BeanItem;
import me.playground.loot.LootTable;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class EntityListener extends EventListener {
	
	private final ArrayList<ArmorStand> damageIndicators = new ArrayList<ArmorStand>(); // unsure if needed.
	
	public EntityListener(Main plugin) {
		super(plugin);
	}
	
	private void spawnDamageIndicator(Location loc, int dmg, int col, int ticks) {
		final ArmorStand di = (ArmorStand) loc.getWorld().spawnEntity(new Location(loc.getWorld(), 0, -10, 0), EntityType.ARMOR_STAND);
		di.setInvisible(true);
		di.setMarker(true);
		di.customName(Component.text(dmg).color(TextColor.color(col)));
		di.setCustomNameVisible(true);
		di.teleport(loc);
		
		damageIndicators.add(di);
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				damageIndicators.remove(di);
				di.remove();
			}
		}, ticks);
	}
	
	public void clearDamageIndicators() {
		for (ArmorStand di : damageIndicators)
			di.remove();
		damageIndicators.clear();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onStupidDamage(EntityDamageByEntityEvent e) {
		// stupid entities
		if (isDumbEntity(e.getEntityType())) {
			final Region r = getRegionAt(e.getEntity().getLocation());
			if (e.getDamager() instanceof Player) {
				final Player p = (Player)e.getDamager();
				boolean cancel = r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember((Player)e.getDamager()));
				if (cancel) {
					p.sendActionBar(Component.text("\u00a7cYou don't have permission to break here."));
					e.setCancelled(true);
				}
			} else if (e.getDamager() instanceof Projectile) {
				if (((Projectile)e.getDamager()).getShooter() instanceof Player) {
					final Player p = ((Player)((Projectile)e.getDamager()).getShooter());
					boolean cancel = r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(p));
					if (cancel) {
						p.sendActionBar(Component.text("\u00a7cYou don't have permission to break here."));
						e.setCancelled(true);
					}
				} else {
					e.setCancelled(true);
				}
			} else if (!r.isWorldRegion()) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		final Location loc1 = e.getDamager().getLocation();
		final Location loc2 = e.getEntity().getLocation();
		final Region r1 = getRegionAt(loc1);
		final Region r2 = getRegionAt(loc2);
		
		if (e.getEntity() instanceof Player)
			PlayerProfile.from((Player)e.getEntity()).getHeirlooms().doDamageTakenByEntityEvent(e);
		if (e.getDamager() instanceof Player)
			PlayerProfile.from((Player)e.getDamager()).getHeirlooms().doMeleeDamageEvent(e);
		
		// Against a Player
		if (e.getEntity() instanceof Player && (e.getDamager() instanceof LivingEntity || e.getDamager() instanceof Projectile)) {
			boolean fromPlayer = e.getDamager() instanceof Player;
			// Projectile from Player
			if ((e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player))
				fromPlayer = true;
			
			if (fromPlayer) {
				if (!(r1.getEffectiveFlag(Flags.PVP) || r2.getEffectiveFlag(Flags.PVP))) // XXX: PVP
					e.setCancelled(true);
			} else {
				e.setDamage(e.getDamage() * r2.getEffectiveFlag(Flags.MOB_DAMAGE_FROM));
				if (e.getDamage() <= 0)
					e.setCancelled(true);
			}
		// Against regular entities
		} else if (e.getEntity() instanceof LivingEntity) {
			boolean bother = true;
			
			// Check for Animal Protection
			if (e.getEntity() instanceof Animals && r2.getEffectiveFlag(Flags.PROTECT_ANIMALS)) {
				Player p = (e.getDamager() instanceof Player) ? (Player)e.getDamager() : null;
				if ((e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player))
					p = (Player) ((Projectile)e.getDamager()).getShooter();
				if (p == null || (p != null && !(r2.getMember(p).higherThan(MemberLevel.VISITOR)))) {
					e.setDamage(0);
					bother = false;
				}
			}
			
			if (bother) {
				e.setDamage(e.getDamage() * r2.getEffectiveFlag(Flags.MOB_DAMAGE_TO));
				if (e.getDamage() > 0) {
					// Prevent tameable entities from dying to anyone except the Owner
					if (e.getEntity() instanceof Cat || e.getEntity() instanceof Wolf || e.getEntity() instanceof Parrot || e.getEntity() instanceof ChestedHorse) {
						Tameable tamed = (Tameable) e.getEntity();
						if (tamed.isTamed()) // TODO: pvp region check
							if (tamed.getOwner() != null && tamed.getOwner().getUniqueId() != e.getDamager().getUniqueId())
								e.setDamage(0);
					}
				}
			}
			if (e.getDamage() <= 0)
				e.setCancelled(true);
			else if (!isDumbEntity(e.getEntityType()))
				spawnDamageIndicator(loc2.add(-0.5 + getPlugin().getRandom().nextDouble(), e.getEntity().getHeight()-(getPlugin().getRandom().nextDouble()/2), -0.5 + getPlugin().getRandom().nextDouble()), (int)e.getDamage(), 0xff8899, 14);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityGrief(EntityChangeBlockEvent e) {
		// Prevent enderman griefing
		if (e.getEntityType().equals(EntityType.ENDERMAN))
			e.setCancelled(true);
		
		// Prevent boat griefing in non-world regions
		if (e.getEntity().getType() == EntityType.BOAT)
			if (!getRegionAt(e.getBlock().getLocation()).isWorldRegion())
				e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onFrameAttack(HangingBreakByEntityEvent e) {
		// Protect Item Frames from being destroyed by players
		final Region r = getRegionAt(e.getEntity().getLocation());
		if (e.getRemover() instanceof Player) {
			final Player p = (Player)e.getRemover();
			boolean cancel = r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember((Player)e.getRemover()));
			if (cancel) {
				p.sendActionBar(Component.text("\u00a7cYou don't have permission to break here."));
				e.setCancelled(true);
				return;
			}
		} else if (e.getRemover() instanceof Projectile && ((Projectile)e.getRemover()).getShooter() instanceof Player) {
			final Player p = ((Player)((Projectile)e.getRemover()).getShooter());
			boolean cancel = r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(p));
			if (cancel) {
				p.sendActionBar(Component.text("\u00a7cYou don't have permission to break here."));
				e.setCancelled(true);
				return;
			}
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
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(EntityDamageEvent e) {
		// Protect Dropped Items from being blown up
		if (e.getCause() == DamageCause.BLOCK_EXPLOSION || e.getCause() == DamageCause.ENTITY_EXPLOSION)
			if (e.getEntityType() == EntityType.DROPPED_ITEM)
				e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDeath(EntityDeathEvent e) {
		final LootTable lootTable = getPlugin().lootManager().getLootTable(e.getEntityType());
		boolean chargedKill = false, skeletonKill = false, nerfDrops = true, isMonster = e.getEntity() instanceof Monster;
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
				}
			}
		}
		
		if (e.getEntity().getKiller() != null) {
			nerfDrops = false;
			// If the player kill designation hasn't timed out, check the local area if it's a grinder.
			p = e.getEntity().getKiller();
			
			if (isMonster)
				if (e.getEntity().fromMobSpawner() || e.getEntity().getNearbyEntities(2, 7, 2).size() > 6)
					nerfDrops = true;
		} else if (chargedKill || skeletonKill) {
			nerfDrops = false;
			// If the player kill designation timed out, just check for the entities' current player target, which 99.999% of the time they will have one when attempting these special drops.
			if (isMonster)
				if (((Monster)e.getEntity()).getTarget() instanceof Player)
					p = (Player) ((Monster)e.getEntity()).getTarget();
		}
		
		if (nerfDrops) {
			// Slight nerf to grinders that require no player interaction
			// 50% chance to drop nothing, just drop vanilla stuff otherwise, nerf XP by 50% always.
			if (getPlugin().getRandom().nextInt(2) == 0)
				e.getDrops().clear();
			e.setDroppedExp((int) ((float)e.getDroppedExp()/2F));
		} else if (p != null) {
			// Do the magic of the custom stuff for this server.
			// Add to the player's statistics if it's a non grinded mob, add coin drops, apply the multipliers and drop the custom loot.
			final Region region = getRegionAt(e.getEntity().getLocation());
			PlayerProfile pp = PlayerProfile.from(p);
			
			// Stats
			pp.getStats().addToStat(StatType.KILLS, e.getEntityType().name(), 1);
			pp.getStats().addToStat(StatType.KILLS, "total", 1);
			if (skeletonKill) 
				pp.getStats().addToStat(StatType.KILLS, "withSkeletonShot", 1);
			if (chargedKill) 
				pp.getStats().addToStat(StatType.KILLS, "withChargedCreeper", 1);
			
			// Coin Drops
			if (isMonster) {
				int hp = (int) e.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
				if (!(hp/2 < 1))
					pp.addToBalance((long) (2 + getPlugin().getRandom().nextInt(hp/2) * region.getEffectiveFlag(Flags.MOB_DROP_COIN)));
			}
			
			// EXP Multiplier
			if (e.getDroppedExp() > 0) // XXX: MOB_DROP_EXP_MULTIPLIER
				e.setDroppedExp((int) ((float)e.getDroppedExp() * region.getEffectiveFlag(Flags.MOB_DROP_EXP)));
			
			// Custom Loot
			if (lootTable != null) {
				e.getDrops().clear();
				e.getDrops().addAll(lootTable.getRewardsFromSystem2(p, p.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS), pp.getLuck(), skeletonKill, chargedKill));
			}
		}
		// Vanilla Drops otherwise.
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onExplosion(EntityExplodeEvent e) {
		boolean cancelBlockDamage = false;
		final Region r = getRegionAt(e.getLocation());
		
		switch(e.getEntityType()) {
		case PRIMED_TNT: case MINECART_TNT:
			cancelBlockDamage = !r.getEffectiveFlag(Flags.TNT);
			break;
		default:
			cancelBlockDamage = !r.getEffectiveFlag(Flags.EXPLOSIONS);
			break;
		}
		
		if (cancelBlockDamage)
			e.blockList().clear();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemSpawn(ItemSpawnEvent e) {
		ItemStack item = e.getEntity().getItemStack();
		e.getEntity().setItemStack(BeanItem.formatItem(item));
	}
	
	@EventHandler
	public void onEntityItemPickup(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player) {
			PlayerProfile pp = PlayerProfile.from(((Player)e.getEntity()));
			if (!pp.canPickupItem(e.getItem()))
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onEntitySpawn(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.NATURAL) {
			final Region r = getRegionAt(e.getLocation());
			if (e.getEntity() instanceof Monster)
				e.setCancelled(!r.getEffectiveFlag(Flags.MOB_HOSTILE_SPAWNS));
			else
				e.setCancelled(!r.getEffectiveFlag(Flags.MOB_PASSIVE_SPAWNS));
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
