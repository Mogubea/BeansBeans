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
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
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
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.utils.MaterialHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BlockListener extends EventListener {
	
	public BlockListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent e) {
		final Region r = getRegionAt(e.getBlock().getLocation());
		
		if (r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(e.getPlayer()))) { // XXX: BUILDING
			e.setCancelled(true);
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to build here."));
			return;
		}
		
		// Handle custom skulls.
		if (e.getItemInHand().getType() == Material.PLAYER_HEAD) {
			if (BeanItem.from(e.getItemInHand()) != null) {
				Skull skull = (Skull) e.getBlock().getState();
				skull.getPersistentDataContainer().set(BeanItem.KEY_ID, PersistentDataType.STRING, e.getItemInHand().getItemMeta().getPersistentDataContainer().get(BeanItem.KEY_ID, PersistentDataType.STRING));
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
		else if (!(e.getBlock().getBlockData() instanceof Ageable))
			e.getBlock().setMetadata("placed", new FixedMetadataValue(getPlugin(), true));
		
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("protected")) {
			e.setCancelled(true);
			return;
		}
		
		final Region r = getRegionAt(e.getBlock().getLocation());
		final Player p = e.getPlayer();
		
		if (r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(p))) { // XXX: BREAKING
			e.setCancelled(true);
			p.sendActionBar(Component.text("\u00a7cYou don't have permission to break here."));
			return;
		}
		
		final ItemStack hand = e.getPlayer().getEquipment().getItemInMainHand();
		if (hand != null && hand.getType() != Material.AIR) {
			final BeanItem custom = BeanItem.from(hand);
			if (custom != null)
				custom.onBlockMined(e);
		}
		
		// Handle custom skulls.
		if (e.isDropItems() && (e.getBlock().getType() == Material.PLAYER_HEAD || e.getBlock().getType() == Material.PLAYER_WALL_HEAD)) {
			String id = ((Skull)e.getBlock().getState()).getPersistentDataContainer().getOrDefault(BeanItem.KEY_ID, PersistentDataType.STRING, null);
			if (id != null) {
				e.setDropItems(false);
				e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), BeanItem.from(id).getItemStack());
			}
		}
		
		// Remove placed metadata for this boot cycle.
		if (e.getBlock().hasMetadata("placed")) {
			e.getBlock().removeMetadata("placed", getPlugin());
			return;
		}
		
		if (p.getGameMode() != GameMode.SURVIVAL)
			return;
		
		final SkillData sd = PlayerProfile.from(p).getSkills();
		
		if (Skill.MINING.doSkillEvent(sd, e)) 
			return;
		
		Material m = e.getBlock().getType();
		int xpInt = 0;
		
		if (MaterialHelper.isLog(m, false)) {
			sd.addXp(SkillType.LOGCUTTING, 37);
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
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBurn(BlockBurnEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onSpread(BlockIgniteEvent e) {
		if (e.getCause() == IgniteCause.SPREAD)
			e.setCancelled(true);
	}
	
}
