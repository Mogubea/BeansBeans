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

		// Check blocks that go up and up
		if (heightCheck > 0) {
			Location loc = b.getLocation().clone();
			int height = 0;
			int sugar = b.getLocation().subtract(0, 1, 0).getBlock().getType() != b.getType() || b.hasMetadata("placed") ? 0 : 1;

			while(height < heightCheck) {
				Block block = loc.add(0, 1, 0).getBlock();
				if (block.getType() != b.getType()) break;
				if (!block.hasMetadata("placed")) sugar++;
				height++;
			}
			
			skillXP *= sugar;
			if (skillXP <= 0) return false;
		} else {
			if (b.hasMetadata("placed")) return false;

			// Check age if age-able, excluding Sugar Cane since they age weirdly.
			if (b.getBlockData() instanceof Ageable crop)
				if (crop.getAge() < crop.getMaximumAge()) return false;
		}

		s.addExperience(this, skillXP);
		return true;
	}
	
	private int getExperienceValue(Block b) {
		final Material material = b.getType();

		return switch (material) {
			case BAMBOO -> 1;
			case MOSS_CARPET, MOSS_BLOCK -> 3;
			case CHORUS_PLANT -> 4;
			case AZALEA, FLOWERING_AZALEA, SUGAR_CANE -> 5;
			case GLOW_BERRIES -> 8;
			case SWEET_BERRY_BUSH -> 7;
			case CARROTS, POTATOES, NETHER_WART -> 9;
			case COCOA, WHEAT -> 10;
			case BEETROOTS -> 11;
			case CACTUS -> 15;
			case MELON, PUMPKIN -> 20;
			case CHORUS_FLOWER -> 22;
			case BEEHIVE, BEE_NEST -> 100;
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
