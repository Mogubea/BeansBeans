package me.playground.items.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;

public class BeanEnchantmentListener implements Listener {
	
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
	
	public static List<Component> getEnchantLore(Enchantment ench, int level) {
		if (ench.equals(BeanEnchantment.MOLTEN_TOUCH))
			return Arrays.asList(Component.text("\u00a77 Automatically \u00a76smelt\u00a77 blocks at"), Component.text("\u00a77 the cost of \u00a7c2\u00a77 durability"));
		if (ench.equals(Enchantment.ARROW_DAMAGE))
			return Arrays.asList(Component.text("\u00a77 Increases arrow damage by \u00a7c"+(25+(25*level))+"%"));
		if (ench.equals(Enchantment.ARROW_FIRE))
			return Arrays.asList(Component.text("\u00a77 Arrows set enemies on fire"), Component.text("\u00a77 for \u00a7c5 \u00a77seconds"));
		if (ench.equals(Enchantment.ARROW_INFINITE))
			return Arrays.asList(Component.text("\u00a77 Regular arrows are no longer consumed"));
		if (ench.equals(Enchantment.ARROW_KNOCKBACK))
			return Arrays.asList(Component.text("\u00a77 Arrows knock enemies back an extra"), Component.text("\u00a7c " + (3*level) + "\u00a77 blocks"));
		if (ench.equals(Enchantment.BINDING_CURSE))
			return Arrays.asList(Component.text("\u00a77 Can't be removed once worn"));
		if (ench.equals(Enchantment.CHANNELING))
			return Arrays.asList(Component.text("\u00a77 During storms, enemies hit will"), Component.text("\u00a77 struck by lightning"));
		if (ench.equals(Enchantment.DEPTH_STRIDER))
			return Arrays.asList(Component.text("\u00a77 Reduces the amount water slows"), Component.text("\u00a77 movement speed by \u00a7a" + (level>=3 ? 100 : 33*level) + "%"));
		if (ench.equals(Enchantment.DIG_SPEED))
			return Arrays.asList(Component.text("\u00a77 Increases mining speed"));
		if (ench.equals(Enchantment.DAMAGE_ALL))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage by \u00a7c"+(0.5+(0.5*level))));
		if (ench.equals(Enchantment.DAMAGE_ARTHROPODS))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a74 arthropods\u00a77 by \u00a7c"+(2.5*level)));
		if (ench.equals(Enchantment.DAMAGE_UNDEAD))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a77 the \u00a74undead\u00a77 by \u00a7c"+(2.5*level)));
		if (ench.equals(Enchantment.DURABILITY))
			return Arrays.asList(Component.text("\u00a77 Has a chance to not lose durability"));
		if (ench.equals(Enchantment.FIRE_ASPECT))
			return Arrays.asList(Component.text("\u00a77 Attacks set enemies on fire for"), Component.text("\u00a7c " + (4*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.FROST_WALKER))
			return Arrays.asList(Component.text("\u00a77 Walking over water creates an icy"), Component.text("\u00a77 path to walk on"));
		if (ench.equals(Enchantment.IMPALING))
			return Arrays.asList(Component.text("\u00a77 Increases attack damage against"), Component.text("\u00a74 aquatic mobs\u00a77 by \u00a7c" + (2.5*level)));
		if (ench.equals(Enchantment.KNOCKBACK))
			return Arrays.asList(Component.text("\u00a77 Attacks knock enemies back an"), Component.text("\u00a77 extra \u00a7c" + (3*level) + "\u00a77 blocks"));
		if (ench.equals(Enchantment.LOOT_BONUS_MOBS))
			return Arrays.asList(Component.text("\u00a77 All mobs potentially drop an extra"), Component.text("\u00a7a " + (1*level) + "\u00a77 " + (level>1?"items":"item") + " where applicable."));
		if (ench.equals(Enchantment.LOOT_BONUS_BLOCKS))
			return Arrays.asList(Component.text("\u00a77 Mining ores potentially multiplies"), Component.text("\u00a77 drops by up to \u00a7a" + (1+(1*level)) + "x"));
		if (ench.equals(Enchantment.LOYALTY))
			return Arrays.asList(Component.text("\u00a77 Will return after being thrown once"), Component.text("\u00a77 a short amount of time has passed"));
		if (ench.equals(Enchantment.LUCK))
			return Arrays.asList(Component.text("\u00a77 Increases the chance of finding"), Component.text("\u00a77 treasure by \u00a7a" + (2*level) + "%"));
		if (ench.equals(Enchantment.LURE))
			return Arrays.asList(Component.text("\u00a77 Decreases the wait time for catching"), Component.text("\u00a77 something by \u00a7a" + (5*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.MENDING))
			return Arrays.asList(Component.text("\u00a77 While in use, collecting experience"), Component.text("\u00a77 will slowly repair this item"));
		if (ench.equals(Enchantment.MULTISHOT))
			return Arrays.asList(Component.text("\u00a77 Fires an additional \u00a7a2 \u00a77projectiles"), Component.text("\u00a77 while only consuming one"));
		if (ench.equals(Enchantment.OXYGEN))
			return Arrays.asList(Component.text("\u00a77 Increases underwater breathing time"), Component.text("\u00a77 by \u00a7a" + (15*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL))
			return Arrays.asList(Component.text("\u00a77 Reduces all damage taken"));
		if (ench.equals(Enchantment.PROTECTION_EXPLOSIONS))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by explosions"));
		if (ench.equals(Enchantment.PROTECTION_FIRE))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by fire"));
		if (ench.equals(Enchantment.PROTECTION_PROJECTILE))
			return Arrays.asList(Component.text("\u00a77 Reduces damage taken by projectiles"));
		if (ench.equals(Enchantment.PROTECTION_FALL))
			return Arrays.asList(Component.text("\u00a77 Reduces fall damage taken by \u00a7a"+Math.min(80, (12*level))+"%"));
		if (ench.equals(Enchantment.PIERCING))
			return Arrays.asList(Component.text("\u00a77 Arrows can pierce up to \u00a7c"+level+"\u00a77 mobs"));
		if (ench.equals(Enchantment.QUICK_CHARGE))
			return Arrays.asList(Component.text("\u00a77 Decreases loading time by \u00a7a" + (0.25*level) + "\u00a77 seconds"));
		if (ench.equals(Enchantment.RIPTIDE))
			return Arrays.asList(Component.text("\u00a77 Launches the player when thrown, but"), Component.text("\u00a77 can only be thrown during rainy"), Component.text("\u00a77 weather and underwater"));
		if (ench.equals(Enchantment.SILK_TOUCH))
			return Arrays.asList(Component.text("\u00a77 Mining a block will typically drop itself"));
		if (ench.equals(Enchantment.SOUL_SPEED))
			return Arrays.asList(Component.text("\u00a77 At the cost of durability, increases"), Component.text("\u00a77 movespeed when on soulsand by \u00a7a" + (30+(10.5*level) + "%")));
		if (ench.equals(Enchantment.SWEEPING_EDGE))
			return Arrays.asList(Component.text("\u00a77 Increases sweeping damage by \u00a7c" + (37.5+(12.5*level))+"%"), Component.text("\u00a77 of this swords attack damage"));
		if (ench.equals(Enchantment.THORNS))
			return Arrays.asList(Component.text("\u00a77 Taking damage has a \u00a7c" + (15*level) + "%\u00a77 chance to"), Component.text("\u00a77 reflect \u00a7c1 - 4\u00a77 damage back to the attacker"));
		if (ench.equals(Enchantment.WATER_WORKER))
			return Arrays.asList(Component.text("\u00a77 Ignore underwater mining penalties"));
		
		return Arrays.asList(Component.text("\u00a77 No info yet."));
	}
	
}
