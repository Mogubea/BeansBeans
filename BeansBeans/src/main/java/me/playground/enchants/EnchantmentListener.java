package me.playground.enchants;

import me.playground.items.BeanItem;
import me.playground.playerprofile.PlayerProfile;
import me.playground.regions.flags.Flags;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustTransition;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.listeners.BlockDestructionSequence;
import me.playground.listeners.EventListener;
import me.playground.listeners.events.CustomBlockBreakEvent;
import me.playground.main.Main;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class EnchantmentListener extends EventListener {
	
	public EnchantmentListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onItemDamage(PlayerItemDamageEvent e) {
		if (e.getItem().getItemMeta() == null) return; // Weird undefined nullability

		// New and nerfed Unbreaking
		int level = e.getItem().getItemMeta().getEnchantLevel(BEnchantment.UNBREAKING);
		if (level > 0) {
			double odds = ((level+1) * 12.5);
			if ((rand.nextDouble() * 100) < odds)
				e.setCancelled(true);
			return;
		}

		// 20% per level to take 1 - 1+level additional damage.
		int fragile = e.getItem().getItemMeta().getEnchantLevel(BEnchantment.BURDEN_FRAGILE);
		if (fragile > 0)
			if (rand.nextInt(100) < (fragile * 20))
				e.setDamage(e.getDamage() + 1 + rand.nextInt(fragile + 1));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player p)) return;
		if (!(e.getEntity() instanceof LivingEntity ent)) return;

		ItemStack item = p.getEquipment().getItemInMainHand();
		if (item.getItemMeta() == null) return; // Weird undefined nullability

		if ((ent.getCategory() == EntityCategory.ARTHROPOD && item.getItemMeta().hasEnchant(BEnchantment.BURDEN_ARACHNOPHOBIC)) ||
				(ent.getCategory() == EntityCategory.UNDEAD && item.getItemMeta().hasEnchant(BEnchantment.BURDEN_NECROPHOBIC)) ||
				(ent instanceof Animals && item.getItemMeta().hasEnchant(BEnchantment.BURDEN_ZOOPHOBIC))) {
			p.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, e.getEntity().getLocation().add(0, e.getEntity().getHeight()/2, 0), 4, 0.3, e.getEntity().getHeight()/2, 0.3, 0.02);
			e.setCancelled(true);
			return;
		}

		int investmentEnchant = item.getItemMeta().getEnchantLevel(BEnchantment.PAY_TO_WIN);
		if (investmentEnchant > 0) {
			PlayerProfile pp = PlayerProfile.from(p);
			int coinDeduct = ((2^investmentEnchant) + (investmentEnchant-1)) * 10; // 20, 50, 100

			if (pp.getBalance() < coinDeduct) return;
			double damage = (2^investmentEnchant) + (investmentEnchant-1); // 2, 5, 10

			// Deal 25% to sweeped entities
			if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
				e.setDamage(e.getDamage() + (damage / 4));
			} else {
				pp.addToBalance(-coinDeduct);
				e.setDamage(e.getDamage() + damage);
			}
		}

		// Enervating Burden
		// Each offensive swing has a 2.5% chance to drain the player of some saturation, however, there is an 8-second cooldown between drains.
		if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK && item.getItemMeta().hasEnchant(BEnchantment.BURDEN_ENERVATING)) {
			PlayerProfile pp = PlayerProfile.from(p);
			if (!pp.onCooldown("enervating_drain")) {
				int random = rand.nextInt(40);
				if (random == 0) {
					pp.addCooldown("enervating_drain", 1000 * 8, true);
					if (p.getSaturation() > 0)
						p.setSaturation(p.getSaturation() - 0.1F);
					else if (p.getFoodLevel() > 0)
						p.setFoodLevel(p.getFoodLevel() - 1);
				}
			}
		}

	}

	private final AttributeModifier MODIFIER_SWIFT_SPRINT = new AttributeModifier(UUID.fromString("c6a436f8-9b99-49a3-a31f-a3a1bdce9428"), Attribute.GENERIC_MOVEMENT_SPEED.translationKey(), 0.03, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

	@EventHandler(ignoreCancelled = true)
	public void onPlayerSprintToggle(PlayerToggleSprintEvent e) {
		Player p = e.getPlayer();
		ItemStack leggings = p.getEquipment().getLeggings();
		int lv = leggings == null ? 0 : leggings.getItemMeta().getEnchantLevel(BEnchantment.SWIFT_SPRINT);
		AttributeInstance attributeInstance = p.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (attributeInstance == null) return; // Don't think this is possible.

		if (e.isSprinting() && lv > 0) {
			AttributeModifier modifier = new AttributeModifier(MODIFIER_SWIFT_SPRINT.getUniqueId(), MODIFIER_SWIFT_SPRINT.getName(), MODIFIER_SWIFT_SPRINT.getAmount() * lv, MODIFIER_SWIFT_SPRINT.getOperation());
			if (!attributeInstance.getModifiers().contains(modifier)) // Because the server would rather complain than just... Idk... DEAL WITH IT?
				attributeInstance.addModifier(modifier);
		} else {
			attributeInstance.removeModifier(MODIFIER_SWIFT_SPRINT);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFishMob(PlayerFishEvent e) {
		if (e.getState() != State.CAUGHT_ENTITY || e.getCaught() == null) return;
		
		Player p = e.getPlayer();
		ItemStack rod = p.getEquipment().getItemInMainHand();
		if (rod.getType() != Material.FISHING_ROD)
			rod = p.getEquipment().getItemInOffHand();
		
		if (!rod.getItemMeta().hasEnchant(BEnchantment.SCORCHING)) return;
		
		if (e.getCaught().getFireTicks() < 80)
			e.getCaught().setFireTicks(80);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onItemDrop(BlockDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getEquipment().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();

		if (!e.getBlockState().getBlock().isPreferredTool(item)) return;
		if (meta == null) return;
		
		if (meta.hasEnchant(BEnchantment.SMELTING_EDGE)) {
			for (Item i : e.getItems()) {
				ItemStack is = i.getItemStack();
				if (!getPlugin().recipeManager().hasCookedVersion(is.getType())) continue;
				is.setType(getPlugin().recipeManager().getCookedVersion(is.getType()));
				p.getWorld().spawnParticle(Particle.FLAME, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 3, 0.25, 0.25, 0.25, 0.02);
				p.getWorld().playSound(e.getBlock().getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.05F, 1F);
			}
		}
	}

	/**
	 * {@link BEnchantment#PRESERVATION}
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreakFirst(BlockBreakEvent e) {
		if (!(e.getBlock().getBlockData() instanceof Ageable)) return; // Prevent non age-ables

		ItemMeta meta = e.getPlayer().getInventory().getItemInMainHand().getItemMeta();
		if (meta == null || !meta.hasEnchant(BEnchantment.PRESERVATION)) return;

		Material m = e.getBlock().getType();
		
		if (m == Material.BAMBOO || m == Material.SUGAR_CANE) return; // Ignore these
		
		// Preserve stems
		if (!(m == Material.PUMPKIN_STEM || m == Material.ATTACHED_PUMPKIN_STEM || m == Material.ATTACHED_MELON_STEM || m == Material.MELON_STEM)) {
			Ageable crop = (Ageable) e.getBlock().getBlockData();
			if (crop.getAge() >= crop.getMaximumAge()) return;
		}
		
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		ItemStack item = p.getEquipment().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();

		if (meta == null) return;

		if (isPreferredTool(item, b)) {
			// Experienced Enchantment
			if (meta.hasEnchant(BEnchantment.EXPERIENCED)) {
				double val = rand.nextDouble()*100;
				if (val < (meta.getEnchantLevel(BEnchantment.EXPERIENCED) * 0.01)) {
					e.setExpToDrop((int) (e.getExpToDrop() + 50 * b.getType().getHardness()));
					p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 2, 0.25, 0.25, 0.25, 0.03);
				} else if (val < (item.getItemMeta().getEnchantLevel(BEnchantment.EXPERIENCED) * 6)) {
					e.setExpToDrop(e.getExpToDrop() + 1);
					p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 2, 0.15, 0.15, 0.15, 0.03);
				}
			}

			// Invigorating Enchantment
			// Axes typically do not have any reliable source of massive instant-breaking, so an axe's chance of activating Rejuvenating is higher.
			if (meta.hasEnchant(BEnchantment.REJUVENATING)) {
				int isAxe = BEnchantmentTarget.AXE.includes(item) ? 1 : 0;
				int random = rand.nextInt(400);
				if (random <= isAxe) { // 1 in 400 (1 in 200 for axes).
					short oldValue = meta.getPersistentDataContainer().getOrDefault(BEnchantment.KEY_REJUVENATION, PersistentDataType.SHORT, (short)0);
					if (oldValue < 500) {
						item.editMeta(mete -> mete.getPersistentDataContainer().set(BEnchantment.KEY_REJUVENATION, PersistentDataType.SHORT, (short)(oldValue + 1)));
						BeanItem.recalculateMaxDurability(item);
						BeanItem.formatItem(item);
					} else {
						BeanItem.addDurability(item, 10 + rand.nextInt(21));
					}
					p.sendActionBar(Component.text("Your ").append(Component.translatable(item.translationKey()).append(Component.text(" feels rejuvenated."))).color(BeanColor.ENCHANT_STACK));
					p.spawnParticle(Particle.VILLAGER_HAPPY, p.getEyeLocation().add(0.5, -0.4, 0.5), 8, 0.4, 0,4, 0.4);
					p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.43F, 1.65F);
				}
			}
		}

		// Enervating Burden
		// Each block broken has a 2.5% chance to drain the player of some saturation, however, there is an 8-second cooldown between drains.
		if (item.getItemMeta().hasEnchant(BEnchantment.BURDEN_ENERVATING)) {
			PlayerProfile pp = PlayerProfile.from(p);
			if (!pp.onCooldown("enervating_drain")) {
				int random = rand.nextInt(40);
				if (random == 0) {
					pp.addCooldown("enervating_drain", 1000 * 8, true);
					if (p.getSaturation() > 0)
						p.setSaturation(p.getSaturation() - 0.1F);
					else if (p.getFoodLevel() > 0)
						p.setFoodLevel(p.getFoodLevel() - 1);
				}
			}
		}
		
		if (!(e.getBlock().getBlockData() instanceof Ageable)) return; // Prevent non age-ables
		if (e instanceof CustomBlockBreakEvent custom && custom.getEnchantmentCause() == BEnchantment.REAPING) return; // Prevent infinite loop
		
		if (item.getItemMeta().hasEnchant(BEnchantment.REAPING)) {
			int power = item.getItemMeta().getEnchantLevel(BEnchantment.REAPING) + 1;
			float dir = p.getLocation().getYaw();
			
			boolean facingX = (dir >= 45 && dir <= 135) || (dir <= -45 && dir >= -135);
			
			for (int x = -power; ++x < power;) {
				if (x == 0) continue; // Ignore the original block
				
				Block bb = e.getBlock().getRelative(!facingX ? x : 0, 0, facingX ? x : 0);
				if (bb.getType() != b.getType()) continue; // Can only reap the same kinds of crop
				if (new BlockDestructionSequence(e.getPlayer(), bb, BEnchantment.REAPING, false, false).setItemDamage(0).fireSequence()) {
					b.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, bb.getLocation().add(0.5, 0.6, 0.5), 4, 0.25, 0.2, 0.25, new DustTransition(Color.BLACK, Color.BLUE, 1.0F));
					b.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, bb.getLocation().add(0.5, 0.5, 0.5), 2, 0.15, 0.15, 0.15, 0.03);
				}
			}
			
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemDropLast(BlockDropItemEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getEquipment().getItemInMainHand();
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		BlockState state = e.getBlockState();
		Material type = state.getType();
		if (type == Material.WHEAT || type == Material.CARROTS || type == Material.POTATOES || type == Material.BEETROOTS || type == Material.NETHER_WART || type == Material.COCOA) {
			if (meta.hasEnchant(BEnchantment.REPLENISH) || getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.CROP_REPLENISH)) {
				int size = e.getItems().size();
				for (int x = size; --x >= 0;) {
					ItemStack i = e.getItems().get(x).getItemStack();
					Material iType = i.getType();
					if (iType == Material.WHEAT_SEEDS || iType == Material.CARROT || iType == Material.POTATO || iType == Material.BEETROOT_SEEDS || iType == Material.NETHER_WART || iType == Material.COCOA_BEANS) {
						i.setAmount(i.getAmount() - 1);
						if (i.getAmount() < 1) {
							e.getItems().get(x).remove();
							e.getItems().remove(x);
						}
						p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 2, 0.15, 0.15, 0.15, 0.03);
						break;
					}
				}
				
				// Delay the replacement to divide the maximum amount of spammed requests from this enchantment.
				getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
					Ageable ageable = (Ageable) state.getBlockData();
					ageable.setAge(0);
					state.getBlock().getLocation().getBlock().setBlockData(ageable);
				}, 5L);
			}
		}
		
		if (meta.hasEnchant(BEnchantment.TELEKINESIS)) {
			int size = e.getItems().size();
			for (int x = size; --x >= 0;) { // Reverse iteration to make sure we get all the items without causing out of bound issues.
				ItemStack overflow = p.getInventory().addItem(e.getItems().get(x).getItemStack()).getOrDefault(0, null);
				if (overflow == null) {
					e.getItems().get(x).remove();
					e.getItems().remove(x);
				} else
					e.getItems().get(x).setItemStack(overflow);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemDropLast(EntityDeathEvent e) {
		Player p = e.getEntity().getKiller();
		if (p == null) return;

		ItemMeta meta = p.getEquipment().getItemInMainHand().getItemMeta();
		if (meta == null) return;
		
		if (meta.hasEnchant(BEnchantment.TELEKINESIS)) {
			int size = e.getDrops().size();
			for (int x = size; --x >= 0;) { // Reverse iteration to make sure we get all the items without causing out of bound issues.
				ItemStack overflow = p.getInventory().addItem(e.getDrops().get(x)).getOrDefault(0, null);
				if (overflow == null)
					e.getDrops().remove(x);
				else
					e.getDrops().set(x, overflow);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerRiptide(PlayerRiptideEvent e) {
		BeanItem.reduceItemDurabilityBy(e.getItem(), e.getItem().getItemMeta().getEnchantLevel(Enchantment.RIPTIDE) * 2);
	}

	private boolean isPreferredTool(ItemStack item, Block b) {
		return b.isPreferredTool(item) || (item.getType().name().endsWith("_HOE") && ((b.getBlockData() instanceof Ageable ageable && ageable.getAge() >= ageable.getMaximumAge()) || b.getType().name().endsWith("_LEAVES")));
	}

}
