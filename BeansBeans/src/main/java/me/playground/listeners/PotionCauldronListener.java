package me.playground.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent.ChangeReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.bukkit.util.EulerAngle;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import net.kyori.adventure.text.Component;

/**
 * This class handles the potion cauldron mechanics and events that could break it.
 * @author Mogubean
 */
public class PotionCauldronListener extends EventListener {

	private final NamespacedKey potKey;
	private final NamespacedKey potInfoKey;
	
	public PotionCauldronListener(Main plugin) {
		super(plugin);
		potKey = new NamespacedKey(plugin, "cauldronPot");
		potInfoKey = new NamespacedKey(plugin, "cauldronPotInfo");
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getPlayer().isSneaking()) return;
		if (e.getHand() != EquipmentSlot.HAND) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getClickedBlock().getType() != Material.CAULDRON) return;
		final Material itemType = e.getItem() == null ? null : e.getItem().getType();
		
		if (!(itemType == null || itemType.name().endsWith("POTION") || itemType == Material.GLASS_BOTTLE || itemType == Material.ARROW || itemType.name().endsWith("BUCKET"))) return;
		final Player p = e.getPlayer();
		final Block b = e.getClickedBlock();
		final Region r = getRegionAt(e.getClickedBlock().getLocation());
		
		// If no build access, check container access
		if (!(itemType == null || checkRegionPermission(r, e, p, Flags.BUILD_ACCESS) || enactRegionPermission(r, e, p, Flags.CONTAINER_ACCESS, "use cauldrons"))) return;
		
		final Location loc = e.getClickedBlock().getLocation().add(0.5, -0.3, 0.5);
		final World world = e.getClickedBlock().getWorld();
		
		Collection<ArmorStand> stands = loc.getNearbyEntitiesByType(ArmorStand.class, 0.8D);
		ArmorStand stand = null;
		
		for (ArmorStand s : stands) {
			if (s.getPersistentDataContainer().isEmpty()) continue;
			if (!s.getPersistentDataContainer().has(potKey)) continue;
			stand = s;
			break;
		}
		
		/**
		 * Handle right clicking with nothing, take a little taste.
		 */
		if (itemType == null && stand != null) {
			PlayerProfile pp = PlayerProfile.from(p);
			if (pp.onCdElseAdd("potDrink", 200, true)) return;
			pp.getStats().addToStat(StatType.GENERIC, "cauldronSips", 1);
			PersistentDataContainer pdc = stand.getPersistentDataContainer();
			PotionType cpType = PotionType.valueOf(pdc.get(potKey, PersistentDataType.STRING));
			byte[] potInfo = pdc.get(potInfoKey, PersistentDataType.BYTE_ARRAY);
			if (cpType.isInstant() || cpType == PotionType.LUCK) return; // Can't taste luck or instant potions
			p.addPotionEffect(new PotionEffect(cpType.getEffectType(), 60, potInfo[1]));
			doCauldronEffectLocal(p, b, stand, cpType);
		/**
		 * Handle potions being put into the cauldron
		 */
		} else if (itemType.name().endsWith("POTION")) {
			PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
			PotionType type = meta.getBasePotionData().getType();
			
			// Prevent dumb
			if (type == PotionType.AWKWARD || type == PotionType.MUNDANE || type == PotionType.THICK)
				return;
			
			// Prevent water being dumb
			if (type == PotionType.WATER && stand != null) {
				e.setCancelled(true); 
				return;
			}
			
			// Disallow custom potions for now
			if (meta.hasCustomEffects()) return;
			
			/**
			 * If there is no armour stand, create one from the potion being put into it.
			 */
			if (stand == null) {
				stand = (ArmorStand) world.spawnEntity(loc.subtract(0, 50.6, 0), EntityType.ARMOR_STAND);
				stand.setVisible(false);
				stand.setInvulnerable(true);
				stand.setGravity(false);
				stand.setBasePlate(false);
				stand.setCanTick(false);
				stand.setCollidable(false);
				stand.setCanMove(false);
				stand.setMarker(true);
				stand.addDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
				stand.setHeadPose(new EulerAngle(3.14, 0, 0));
				stand.getPersistentDataContainer().set(potKey, PersistentDataType.STRING, type.name());
				stand.getPersistentDataContainer().set(potInfoKey, PersistentDataType.BYTE_ARRAY, new byte[] {
						(byte) (meta.getBasePotionData().isUpgraded() ? 1 : 0), 
						(byte) (meta.getBasePotionData().isExtended() ? 1 : 0),
						(byte) 1});
				stand.getEquipment().setHelmet(new ItemStack(potTypeToGlass(type)));
				stand.teleport(loc.add(0, 50, 0));
				
				if (p.getGameMode() != GameMode.CREATIVE) {
					if (e.getItem().subtract(1).getAmount() < 1) {
						p.getInventory().setItem(EquipmentSlot.HAND, new ItemStack(Material.GLASS_BOTTLE));
					} else {
						HashMap<Integer, ItemStack> stacks = p.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
						stacks.forEach((idx, item) -> {world.dropItem(loc, item);});
					}
				}
				doFinalEffect(p, b, stand, type, 1);
			/**
			 * Handle placing more potions into the cauldron.
			 * Trying to place a different type of potion (including same types but upgraded when the original wasn't) in won't work.
			 */
			} else {
				PersistentDataContainer pdc = stand.getPersistentDataContainer();
				PotionType cpType = PotionType.valueOf(pdc.get(potKey, PersistentDataType.STRING));
				byte[] potInfo = pdc.get(potInfoKey, PersistentDataType.BYTE_ARRAY);
				
				if (potInfo[2] >= 8) {
					p.sendActionBar(Component.text("This cauldron cannot hold anymore potions."));
					return;
				}
				
				if (type != cpType || potInfo[1] != (meta.getBasePotionData().isExtended() ? 1 : 0) || potInfo[0] != (meta.getBasePotionData().isUpgraded() ? 1 : 0)) {
					p.sendActionBar(Component.text("You cannot mix different types of potions."));
					return;
				}
				
				if (p.getGameMode() != GameMode.CREATIVE) {
					if (e.getItem().subtract(1).getAmount() < 1) {
						p.getInventory().setItem(EquipmentSlot.HAND, new ItemStack(Material.GLASS_BOTTLE));
					} else {
						HashMap<Integer, ItemStack> stacks = p.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
						stacks.forEach((idx, item) -> {world.dropItem(loc, item);});
					}
				}
				
				potInfo[2]++;
				pdc.set(potInfoKey, PersistentDataType.BYTE_ARRAY, potInfo);
				doFinalEffect(p, b, stand, type, potInfo[2]);
			}
		/**
		 * Handle obtaining the potion from the cauldron using a glass bottle.
		 */
		} else if (itemType == Material.GLASS_BOTTLE) {
			if (stand == null) return;
			PersistentDataContainer pdc = stand.getPersistentDataContainer();
			byte[] potInfo = pdc.get(potInfoKey, PersistentDataType.BYTE_ARRAY);
			
			if (potInfo[2] < 1) { 
				stand.remove(); 
				return; 
			}
			
			if (potInfo[2] >= 1) {
				ItemStack potion = new ItemStack(Material.POTION);
				PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
				PotionType type = PotionType.valueOf(pdc.get(potKey, PersistentDataType.STRING));
				pMeta.setBasePotionData(new PotionData(type, potInfo[1] == 1, potInfo[0] == 1));
				potion.setItemMeta(pMeta);
				
				e.getItem().subtract(1);
				HashMap<Integer, ItemStack> stacks = p.getInventory().addItem(potion);
				stacks.forEach((idx, item) -> {world.dropItem(loc, potion);});
				
				potInfo[2]--;
				pdc.set(potInfoKey, PersistentDataType.BYTE_ARRAY, potInfo);
				doFinalEffect(p, b, stand, type, potInfo[2]);
			}
		/**
		 * Cancel bucket crap when there's a potion already in the "cauldron"
		 */
		} else if (itemType.name().endsWith("BUCKET")) {
			if (stand != null)
				e.setCancelled(true);
		/**
		 * Tipped Arrow magic
		 */
		} else if (itemType == Material.ARROW) {
			if (stand == null) return;
			PersistentDataContainer pdc = stand.getPersistentDataContainer();
			byte[] potInfo = pdc.get(potInfoKey, PersistentDataType.BYTE_ARRAY);
			
			if (potInfo[2] < 1) { 
				stand.remove(); 
				return;
			}
			int count = e.getItem().getAmount();
			count = count > 8 ? 8 : count;
			
			ItemStack arrow = new ItemStack(Material.TIPPED_ARROW, count);
			PotionMeta pMeta = (PotionMeta) arrow.getItemMeta();
			PotionType type = PotionType.valueOf(pdc.get(potKey, PersistentDataType.STRING));
			pMeta.setBasePotionData(new PotionData(type, potInfo[1] == 1, potInfo[0] == 1));
			arrow.setItemMeta(pMeta);
			
			e.getItem().subtract(count);
			HashMap<Integer, ItemStack> stacks = p.getInventory().addItem(arrow);
			stacks.forEach((idx, item) -> {world.dropItem(loc, arrow);});
			
			potInfo[2]--;
			pdc.set(potInfoKey, PersistentDataType.BYTE_ARRAY, potInfo);
			doFinalEffect(p, b, stand, type, potInfo[2]);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCauldronBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.CAULDRON) {
			Collection<ArmorStand> stands = e.getBlock().getLocation().add(0.5, -0.3, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.8D);
			for (ArmorStand s : stands) {
				PersistentDataContainer pdc = s.getPersistentDataContainer();
				if (pdc.isEmpty()) continue;
				if (!pdc.has(potKey)) continue;
				byte[] potInfo = pdc.get(potInfoKey, PersistentDataType.BYTE_ARRAY);
				AreaEffectCloud cloud = (AreaEffectCloud) s.getWorld().spawnEntity(e.getBlock().getLocation().add(0.5, 0.1, 0.5), EntityType.AREA_EFFECT_CLOUD);
				cloud.getWorld().playSound(cloud.getLocation(), Sound.ITEM_BUCKET_EMPTY, 0.4F, 1.0F);
				cloud.setBasePotionData(new PotionData(PotionType.valueOf(pdc.get(potKey, PersistentDataType.STRING)), potInfo[1] == 1, potInfo[0] == 1));
				cloud.setDuration(75);
				cloud.setRadius(0.8F);
				s.remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent e) {
		List<Block> blocks = e.getBlocks();
		int size = blocks.size();
		for (int x = -1; ++x < size;) {
			Block b = blocks.get(x);
			if (b.getType() != Material.CAULDRON) continue;
			doPistonMagic(b.getLocation(), e.getDirection());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent e) {
		List<Block> blocks = e.getBlocks();
		int size = blocks.size();
		for (int x = -1; ++x < size;) {
			Block b = blocks.get(x);
			if (b.getType() != Material.CAULDRON) continue;
			doPistonMagic(b.getLocation(), e.getDirection());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onLevelChange(CauldronLevelChangeEvent e) {
		if (e.getReason() != ChangeReason.NATURAL_FILL) return;
		Collection<ArmorStand> stands = e.getBlock().getLocation().add(0.5, -0.3, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.8D);
		for (ArmorStand s : stands) {
			PersistentDataContainer pdc = s.getPersistentDataContainer();
			if (pdc.isEmpty()) continue;
			if (!pdc.has(potKey)) continue;
			e.setCancelled(true);
			return;
		}
	}
	
	private final void doPistonMagic(Location l, BlockFace face) {
		Collection<ArmorStand> stands = l.add(0.5, -0.3, 0.5).getNearbyEntitiesByType(ArmorStand.class, 0.8D);
		for (ArmorStand s : stands) {
			PersistentDataContainer pdc = s.getPersistentDataContainer();
			if (pdc.isEmpty()) continue;
			if (!pdc.has(potKey)) continue;	
			Location newLoc = s.getLocation().add(face.getModX(), face.getModY(), face.getModZ());
			s.teleport(newLoc, TeleportCause.PLUGIN);
			return;
		}
	}
	
	private final void doFinalEffect(Player p, Block b, ArmorStand stand, PotionType type, int newSize) {
		doArmSwing(p);
		Color col = type.getEffectType().getColor();
		Location l = b.getLocation().add(0.5, -0.3, 0.5);
		stand.getWorld().playSound(l, Sound.ITEM_BOTTLE_FILL, 0.7F, 1.0F);
		stand.teleport(l.subtract(0, 0.6 - (newSize * 0.04), 0), TeleportCause.PLUGIN);
		for (int x = -1; ++x < 4;)
			stand.getWorld().spawnParticle(Particle.SPELL_MOB, b.getLocation().toCenterLocation(), 0, (double)col.getRed()/255.0, (double)col.getGreen()/255.0, (double)col.getBlue()/255.0);
		stand.getWorld().spawnParticle(Particle.BLOCK_DUST, b.getLocation().toCenterLocation(), 4, potTypeToGlass(type).createBlockData());
		if (newSize <= 0)
			stand.remove();
	}
	
	private final void doCauldronEffectLocal(Player p, Block b, ArmorStand stand, PotionType type) {
		doArmSwing(p);
		p.playSound(b.getLocation().add(0.5, -0.3, 0.5), Sound.ITEM_BOTTLE_FILL, 0.5F, 0.8F);
		Color col = type.getEffectType().getColor();
		for (int x = -1; ++x < 3;)
			p.spawnParticle(Particle.SPELL_MOB, b.getLocation().toCenterLocation(), 0, (double)col.getRed()/255.0, (double)col.getGreen()/255.0, (double)col.getBlue()/255.0);
		p.spawnParticle(Particle.BLOCK_DUST, b.getLocation().toCenterLocation(), 3, potTypeToGlass(type).createBlockData());
	}
	
	private Material potTypeToGlass(PotionType type) {
		switch(type) {
		case FIRE_RESISTANCE: return Material.ORANGE_STAINED_GLASS;
		case INSTANT_DAMAGE: return Material.BLACK_STAINED_GLASS;
		case INSTANT_HEAL: return Material.RED_STAINED_GLASS;
		case INVISIBILITY: return Material.LIGHT_GRAY_STAINED_GLASS;
		case JUMP: return Material.LIME_STAINED_GLASS;
		case LUCK: return Material.GREEN_STAINED_GLASS;
		case NIGHT_VISION: return Material.BLUE_STAINED_GLASS;
		case POISON: return Material.GREEN_STAINED_GLASS;
		case REGEN: return Material.PINK_STAINED_GLASS;
		case SLOWNESS: return Material.GRAY_STAINED_GLASS;
		case SLOW_FALLING: return Material.WHITE_STAINED_GLASS;
		case SPEED: return Material.LIGHT_BLUE_STAINED_GLASS;
		case STRENGTH: return Material.RED_STAINED_GLASS;
		case TURTLE_MASTER: return Material.PURPLE_STAINED_GLASS;
		case UNCRAFTABLE: return Material.MAGENTA_STAINED_GLASS;
		case WATER_BREATHING: return Material.BLUE_STAINED_GLASS;
		case WEAKNESS: return Material.GRAY_STAINED_GLASS;
		default: return Material.BLUE_STAINED_GLASS;
		}
	}
	
}
