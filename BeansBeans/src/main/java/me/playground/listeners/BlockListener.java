package me.playground.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.flags.Flags;
import me.playground.skills.Skill;
import net.kyori.adventure.text.format.TextColor;

public class BlockListener extends EventListener {
	
	public BlockListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!enactRegionPermission(getRegionAt(e.getBlock().getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "build")) return;
		
		String blockName = e.getBlock().getType().name();
		
		// Handle custom skulls.
		if (e.getItemInHand().getType() == Material.PLAYER_HEAD) {
			if (BeanItem.from(e.getItemInHand()) != null) {
				Skull skull = (Skull) e.getBlock().getState();
				blockName = e.getItemInHand().getItemMeta().getPersistentDataContainer().get(BeanItem.KEY_ID, PersistentDataType.STRING);
				skull.getPersistentDataContainer().set(BeanItem.KEY_ID, PersistentDataType.STRING, blockName);
				skull.update();
			}
		}
		
		// Handle the custom names on containers and make the illusion that nothing changed.
		else if (e.getBlock().getState() instanceof Container) {
			Container c = (Container) e.getBlock().getState();
			if (c.customName() != null) { // Remove colour
				c.customName(c.customName().color(TextColor.color(0x3F3F3F)));
				c.update();
			}
		}
		
		// Allow for signs to be edited.
		else if (e.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) e.getBlock().getState();
			sign.setEditable(true);
			sign.update();
		}
		
		// To prevent placing a crop and not being able to obtain experience from it after harvesting it later on the same reset cycle.
		else if (!(e.getBlock().getBlockData() instanceof Ageable)) {
			e.getBlock().setMetadata("placed", new FixedMetadataValue(getPlugin(), true));
		}
		
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		pp.getStats().addToStat(StatType.BLOCK_PLACE, blockName, 1);
		pp.getStats().addToStat(StatType.BLOCK_PLACE, "total", 1, true);
		
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		pp.getSkills().doSkillEvents(e, Skill.BUILDING);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("protected")) {
			e.setCancelled(true);
			return;
		}
		
		final Player p = e.getPlayer();
		String blockName = e.getBlock().getType().name();
		
		if (!enactRegionPermission(getRegionAt(e.getBlock().getLocation()), e, p, Flags.BUILD_ACCESS, "break")) return;
		
		final ItemStack hand = e.getPlayer().getEquipment().getItemInMainHand();
		if (hand != null && hand.getType() != Material.AIR) {
			final BeanItem custom = BeanItem.from(hand);
			if (custom != null)
				custom.onBlockMined(e);
		}
		
		// Handle custom skulls.
		if (p.getGameMode() != GameMode.CREATIVE && (e.getBlock().getType() == Material.PLAYER_HEAD || e.getBlock().getType() == Material.PLAYER_WALL_HEAD)) {
			String id = ((Skull)e.getBlock().getState()).getPersistentDataContainer().getOrDefault(BeanItem.KEY_ID, PersistentDataType.STRING, null);
			if (id != null) {
				e.setDropItems(false);
				blockName = id;
				e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), BeanItem.from(id).getItemStack());
			}
		}
		
		PlayerProfile pp = PlayerProfile.from(p);
		
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
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreakFinal(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("placed"))
			e.getBlock().removeMetadata("placed", getPlugin());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onExplosion(BlockExplodeEvent e) {
		if (!getRegionAt(e.getBlock().getLocation()).getEffectiveFlag(Flags.EXPLOSIONS))
			e.blockList().clear();
	}
	
	/**
	 * Blocks burning due to fire
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onBurn(BlockBurnEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSpread(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.SPREAD)
			e.setCancelled(true);
	}
	
	/**
	 * When an item is dropped from a block
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onItemDrop(BlockDropItemEvent e) {
		if (e.getItems().isEmpty()) return;
		if (e.getBlockState().getType() == Material.ANCIENT_DEBRIS) {
			if (!e.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
				// Make the non-silk drop from Ancient Debris into Netherite Scrap to support the anti Mining skill XP Server Restart Exploit.
				e.getItems().get(0).setItemStack(new ItemStack(Material.NETHERITE_SCRAP));
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
		}
	}
	
}
