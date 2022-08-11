package me.playground.enchants;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.listeners.EventListener;
import me.playground.main.Main;

public class BeanEnchantmentListener extends EventListener {
	
	public BeanEnchantmentListener(Main plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onFishMob(PlayerFishEvent e) {
		if (e.getState() != State.CAUGHT_ENTITY) return;
		
		Player p = e.getPlayer();
		ItemStack rod = p.getEquipment().getItemInMainHand();
		if (rod.getType() != Material.FISHING_ROD)
			rod = p.getEquipment().getItemInOffHand();
		
		if (!rod.containsEnchantment(BeanEnchantment.SEARING)) return;
		
		if (e.getCaught().getFireTicks() < 60)
			e.getCaught().setFireTicks(60);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		ItemStack item = p.getEquipment().getItemInMainHand();
		
		if (item != null) {
			if (item.containsEnchantment(BeanEnchantment.MOLTEN_TOUCH)) {
				if (e.isDropItems()) {
					final ItemMeta meta = item.getItemMeta();
					final Damageable tool;
					if (meta instanceof Damageable) {
						tool = (Damageable) meta;
						if (tool.getDamage() >= item.getType().getMaxDurability())
							return;
						
						int toDamage = 2;
						if (tool.getDamage()+toDamage >= item.getType().getMaxDurability())
							tool.setDamage(item.getType().getMaxDurability());
						else
							tool.setDamage(tool.getDamage()+toDamage);
						item.setItemMeta(meta);
					}
					
					//int fortuneLevel = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
					
					ItemStack newDrop = null;
					
					switch (e.getBlock().getType()) {
					case SANDSTONE:
						newDrop = new ItemStack(Material.SMOOTH_SANDSTONE, 1);
						break;
					case RED_SANDSTONE:
						newDrop = new ItemStack(Material.SMOOTH_RED_SANDSTONE, 1);
						break;
					case NETHERRACK:
						newDrop = new ItemStack(Material.NETHER_BRICK, 1);
						break;
					case STONE_BRICKS:
						newDrop = new ItemStack(Material.CRACKED_STONE_BRICKS, 1);
						break;
					case NETHER_BRICKS:
						newDrop = new ItemStack(Material.CRACKED_NETHER_BRICKS, 1);
						break;
					case CACTUS:
						newDrop = new ItemStack(Material.GREEN_DYE, 1);
						break;
					case QUARTZ_BLOCK:
						newDrop = new ItemStack(Material.SMOOTH_QUARTZ, 1);
						break;
					case SEA_PICKLE:
						newDrop = new ItemStack(Material.LIME_DYE, 1);
						break;
					case CLAY:
						newDrop = new ItemStack(Material.BRICK, 4);
						break;
					case WET_SPONGE:
						newDrop = new ItemStack(Material.SPONGE, 1);
						break;
					case STONE:
						newDrop = new ItemStack(Material.SMOOTH_STONE, 1);
						break;
					case COBBLESTONE:
						newDrop = new ItemStack(Material.STONE, 1);
						break;
					case IRON_ORE:
						newDrop = new ItemStack(Material.IRON_INGOT, 1);
						e.setExpToDrop(1);
						break;
					case GOLD_ORE:
						newDrop = new ItemStack(Material.GOLD_INGOT, 1);
						e.setExpToDrop(1);
						break;
					case ANCIENT_DEBRIS:
						newDrop = new ItemStack(Material.NETHERITE_SCRAP, 1);
						e.setExpToDrop(10);
						break;
					case OAK_LOG: case SPRUCE_LOG: case BIRCH_LOG: case JUNGLE_LOG: case ACACIA_LOG: case DARK_OAK_LOG: case CRIMSON_STEM: case WARPED_STEM:
					case STRIPPED_OAK_LOG: case STRIPPED_SPRUCE_LOG: case STRIPPED_BIRCH_LOG: case STRIPPED_JUNGLE_LOG: case STRIPPED_ACACIA_LOG: case STRIPPED_DARK_OAK_LOG:
					case STRIPPED_CRIMSON_STEM: case STRIPPED_WARPED_STEM:
						newDrop = new ItemStack(Material.CHARCOAL, 1);
						e.setExpToDrop(1);
						break;
					case SAND: case RED_SAND:
						newDrop = new ItemStack(Material.GLASS, 1);
						e.setExpToDrop(1);
						break;
					default:
						return;
					}
					
					e.setDropItems(false);
					e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), newDrop);
					e.getBlock().getWorld().spawnParticle(Particle.FLAME, e.getBlock().getLocation().add(0.5, 0.5, 0.5), 4);
				}
			}
		}
	}
	
}
