package me.playground.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
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
import me.playground.playerprofile.skills.BxpValues;
import me.playground.playerprofile.skills.Skill;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillType;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.flags.Flags;
import me.playground.utils.MaterialHelper;
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
		pp.pokeAFK(); // Poke regardless of gamemode
		
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		pp.getStats().addToStat(StatType.BLOCK_PLACE, blockName, 1);
		pp.getStats().addToStat(StatType.BLOCK_PLACE, "total", 1);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("protected")) {
			e.setCancelled(true);
			return;
		}
		
		final Player p = e.getPlayer();
		String blockName = e.getBlock().getType().name();
		
		if (!enactRegionPermission(getRegionAt(e.getBlock().getLocation()), e, p, Flags.BUILD_ACCESS, "break"));
		
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
		
		// Stop placing and breaking a block for xp.
		if (e.getBlock().hasMetadata("placed")) {
			e.getBlock().removeMetadata("placed", getPlugin());
			return;
		}
		
		PlayerProfile pp = PlayerProfile.from(p);
		pp.pokeAFK(); // Poke regardless of gamemode
		
		if (p.getGameMode() == GameMode.CREATIVE) return;
		
		pp.getStats().addToStat(StatType.BLOCK_BREAK, blockName, 1);
		pp.getStats().addToStat(StatType.BLOCK_BREAK, "total", 1);
		
		final SkillData sd = pp.getSkills();
		
		if (Skill.MINING.doSkillEvent(sd, e)) 
			return;
		
		Material m = e.getBlock().getType();
		int xpInt = 0;
		
		if (MaterialHelper.isLog(m, false)) {
			sd.addXp(SkillType.LOGCUTTING, 40);
		} else if ((xpInt = BxpValues.getDiggingValue(m)) > 0) {
			sd.addXp(SkillType.EXCAVATION, xpInt);
		} else if (m == Material.MELON || m == Material.PUMPKIN || m == Material.SUGAR_CANE || m == Material.BAMBOO) {
			sd.addXp(SkillType.AGRICULTURE, BxpValues.getFarmingValue(m));
		} else if ((e.getBlock().getBlockData() instanceof Ageable)) {
			Ageable aged = (Ageable)e.getBlock().getBlockData();
			if (aged.getAge() >= aged.getMaximumAge())
				sd.addXp(SkillType.AGRICULTURE, BxpValues.getFarmingValue(m));
		}
		
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
