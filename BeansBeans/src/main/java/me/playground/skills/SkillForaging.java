package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

import net.kyori.adventure.text.Component;

public class SkillForaging extends Skill {
	
	protected SkillForaging() {
		super("Foraging", 0x36e579, BarColor.GREEN, 'a', "\ud83e\ude93", Material.DARK_OAK_SAPLING, Material.LIME_DYE,
				"Earn Foraging XP by chopping down forests and harvesting flowers!");
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		if (!(e instanceof BlockBreakEvent)) return false;
		final BlockBreakEvent event = (BlockBreakEvent) e;
		Block b = event.getBlock();
		
		int skillXP = getExperienceValue(b);
		
		if (skillXP < 1) return false;
		
		s.addExperience(this, skillXP);
		return true;
	}
	
	private int getExperienceValue(Block b) {
		final Material material = b.getType();
		
		switch(material) {
		case WARPED_WART_BLOCK: case NETHER_WART_BLOCK: case WARPED_ROOTS: case CRIMSON_ROOTS: return 3;
		
		case MUSHROOM_STEM: case RED_MUSHROOM_BLOCK: case BROWN_MUSHROOM_BLOCK: return 4;
		case VINE: case GLOW_LICHEN: case WEEPING_VINES: return 4;
		
		case TUBE_CORAL: case TUBE_CORAL_BLOCK: case TUBE_CORAL_FAN: case TUBE_CORAL_WALL_FAN:
		case BRAIN_CORAL: case BRAIN_CORAL_BLOCK: case BRAIN_CORAL_FAN: case BRAIN_CORAL_WALL_FAN:
		case BUBBLE_CORAL: case BUBBLE_CORAL_BLOCK: case BUBBLE_CORAL_FAN: case BUBBLE_CORAL_WALL_FAN:
		case FIRE_CORAL: case FIRE_CORAL_BLOCK: case FIRE_CORAL_FAN: case FIRE_CORAL_WALL_FAN:
		case HORN_CORAL: case HORN_CORAL_BLOCK: case HORN_CORAL_FAN: case HORN_CORAL_WALL_FAN:
		case LILY_PAD:
			return 5;
			
		case POPPY: case DANDELION: case ORANGE_TULIP: case WHITE_TULIP: case PINK_TULIP: case RED_TULIP: case CORNFLOWER:
		case BLUE_ORCHID: case ALLIUM: case AZURE_BLUET: case OXEYE_DAISY: return 6;
		
		case BROWN_MUSHROOM: case RED_MUSHROOM: return 8;
		case WARPED_FUNGUS: case CRIMSON_FUNGUS: return 10;
		
		case WARPED_HYPHAE: case CRIMSON_HYPHAE: case SHROOMLIGHT:
		case DARK_OAK_WOOD: case JUNGLE_WOOD: case SPRUCE_WOOD: case BIRCH_WOOD: case OAK_WOOD: case ACACIA_WOOD:
		case STRIPPED_DARK_OAK_WOOD: case STRIPPED_JUNGLE_WOOD: case STRIPPED_SPRUCE_WOOD: case STRIPPED_BIRCH_WOOD: case STRIPPED_OAK_WOOD: case STRIPPED_ACACIA_WOOD:
		case STRIPPED_DARK_OAK_LOG: case STRIPPED_JUNGLE_LOG: case STRIPPED_SPRUCE_LOG: case STRIPPED_BIRCH_LOG: case STRIPPED_OAK_LOG: case STRIPPED_ACACIA_LOG: 
			return 20;
		
		case SUNFLOWER: case PEONY: case LILAC: case LILY_OF_THE_VALLEY: case ROSE_BUSH: return 30;
			
		case DARK_OAK_LOG: return 34;
		case JUNGLE_LOG: return 36;
		case SPRUCE_LOG: return 38;
		case BIRCH_LOG: return 40;
		case OAK_LOG: return 42;
		case ACACIA_LOG: return 44;
		
		case WARPED_STEM: case CRIMSON_STEM: return 52;
		
		case WITHER_ROSE: case SPORE_BLOSSOM: return 100;
		default: 
			return 0;
		}
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}
	
}
