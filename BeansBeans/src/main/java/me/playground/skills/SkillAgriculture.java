package me.playground.skills;

import java.util.List;

import me.playground.items.lore.Lore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

import me.playground.listeners.events.PlayerRightClickHarvestEvent;
import net.kyori.adventure.text.Component;

public class SkillAgriculture extends Skill {
	
	protected SkillAgriculture() {
		super("Agriculture", 0xffcc66, BarColor.YELLOW, '6', "\ud83d\udd31", Material.GOLDEN_HOE, Material.YELLOW_DYE,
				"Earn Agriculture XP by cultivating crops and tending to animals!");
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		Block b = null;
		if (e instanceof BlockBreakEvent)
			b = ((BlockBreakEvent)e).getBlock();
		else if (e instanceof PlayerRightClickHarvestEvent)
			b = ((PlayerRightClickHarvestEvent)e).getClickedBlock();
		else if (e instanceof PlayerHarvestBlockEvent)
			b = ((PlayerHarvestBlockEvent)e).getHarvestedBlock();
		
		if (b == null) return false;
		
		int skillXP = getExperienceValue(b);
		if (skillXP < 1) return false;
		
		if (b.getType() == Material.SUGAR_CANE) {
			Location loc = b.getLocation().clone();
			int height = 0;
			int sugar = 0;
			
			if (b.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.SUGAR_CANE) sugar--;
			while(height < 3 && loc.subtract(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) height++;
			if (!(height > 2)) height = 0;
			while(height < 3 && loc.add(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) { height++; sugar++; }
			
			skillXP *= sugar;
			if (skillXP <= 0) return false;
		}
		
		if (b.getType() != Material.SUGAR_CANE && b.getBlockData() instanceof Ageable) {
			Ageable crop = (Ageable) b.getBlockData();
			if (crop.getAge() < crop.getMaximumAge()) return false;
		}
		
		s.addExperience(this, skillXP);
		return true;
	}
	
	private int getExperienceValue(Block b) {
		final Material material = b.getType();
		
		switch(material) {
		case MOSS_CARPET: return 4;
		case MOSS_BLOCK: return 5;
		case AZALEA: return 5;
		case FLOWERING_AZALEA: return 6;
		case BAMBOO: return 7;
		case CHORUS_PLANT: return 8;
		case SUGAR_CANE: return 10;
		case SWEET_BERRY_BUSH: return 13;
		case COCOA: return 15;
		case WHEAT: return 18;
		case CARROTS: return 18;
		case POTATOES: return 18;
		case BEETROOTS: return 23;
		case NETHER_WART: return 23;
		case CACTUS: return 27;
		case MELON: return 33;
		case PUMPKIN: return 33;
		case CHORUS_FLOWER: return 35;
		case BEEHIVE: return 130;
		case BEE_NEST: return 130;
		default: 
			return 0;
		}
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}
	
}
