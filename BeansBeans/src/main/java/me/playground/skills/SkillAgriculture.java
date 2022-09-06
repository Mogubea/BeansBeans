package me.playground.skills;

import java.util.List;

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

		final int heightCheck = getHeightCheck(b.getType());

		if (heightCheck > 0) {
			Location loc = b.getLocation().clone();
			int height = 0;
			int sugar = 0;
			
			if (b.getLocation().subtract(0, 1, 0).getBlock().getType() != b.getType()) sugar--;
			while(height < heightCheck && loc.add(0, 1, 0).getBlock().getType() == b.getType()) { height++; sugar++; }
			
			skillXP *= sugar;
			if (skillXP <= 0) return false;
		}
		
		if (b.getType() != Material.SUGAR_CANE && b.getBlockData() instanceof Ageable crop) {
			if (crop.getAge() < crop.getMaximumAge()) return false;
		}
		
		s.addExperience(this, skillXP);
		return true;
	}
	
	private int getExperienceValue(Block b) {
		final Material material = b.getType();

		return switch (material) {
			case MOSS_CARPET -> 4;
			case MOSS_BLOCK, BAMBOO -> 5;
			case CHORUS_PLANT -> 7;
			case AZALEA, FLOWERING_AZALEA -> 8;
			case SUGAR_CANE, GLOW_BERRIES -> 10;
			case SWEET_BERRY_BUSH -> 13;
			case COCOA -> 15;
			case CARROTS, POTATOES -> 16;
			case WHEAT -> 18;
			case NETHER_WART -> 21;
			case BEETROOTS -> 23;
			case CACTUS -> 27;
			case MELON, PUMPKIN -> 33;
			case CHORUS_FLOWER -> 35;
			case BEEHIVE, BEE_NEST -> 130;
			default -> 0;
		};
	}

	private int getHeightCheck(Material material) {
		return switch(material) {
			case BAMBOO -> 16;
			case SUGAR_CANE -> 4;
			case CACTUS -> 3;
			default -> 0;
		};
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}
	
}
