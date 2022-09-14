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

		return switch (material) {
			case ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, JUNGLE_LEAVES, OAK_LEAVES, SPRUCE_LEAVES, AZALEA_LEAVES, FLOWERING_AZALEA_LEAVES -> 1;
			case WARPED_WART_BLOCK, NETHER_WART_BLOCK, WARPED_ROOTS, CRIMSON_ROOTS -> 3;
			case MUSHROOM_STEM, RED_MUSHROOM_BLOCK, BROWN_MUSHROOM_BLOCK -> 4;
			case VINE, GLOW_LICHEN, WEEPING_VINES -> 4;
			case TUBE_CORAL, TUBE_CORAL_BLOCK, TUBE_CORAL_FAN, TUBE_CORAL_WALL_FAN, BRAIN_CORAL, BRAIN_CORAL_BLOCK, BRAIN_CORAL_FAN, BRAIN_CORAL_WALL_FAN, BUBBLE_CORAL,
					BUBBLE_CORAL_BLOCK, BUBBLE_CORAL_FAN, BUBBLE_CORAL_WALL_FAN, FIRE_CORAL, FIRE_CORAL_BLOCK, FIRE_CORAL_FAN, FIRE_CORAL_WALL_FAN, HORN_CORAL, HORN_CORAL_BLOCK,
					HORN_CORAL_FAN, HORN_CORAL_WALL_FAN, LILY_PAD -> 5;
			case POPPY, DANDELION, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP, RED_TULIP, CORNFLOWER, BLUE_ORCHID, ALLIUM, AZURE_BLUET, OXEYE_DAISY -> 6;
			case BROWN_MUSHROOM, RED_MUSHROOM -> 8;
			case WARPED_FUNGUS, CRIMSON_FUNGUS -> 10;
			case WARPED_HYPHAE, CRIMSON_HYPHAE, SHROOMLIGHT, DARK_OAK_WOOD, JUNGLE_WOOD, SPRUCE_WOOD, BIRCH_WOOD, OAK_WOOD, ACACIA_WOOD, STRIPPED_DARK_OAK_WOOD,
					STRIPPED_JUNGLE_WOOD, STRIPPED_SPRUCE_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_OAK_WOOD, STRIPPED_ACACIA_WOOD, STRIPPED_DARK_OAK_LOG, STRIPPED_JUNGLE_LOG,
					STRIPPED_SPRUCE_LOG, STRIPPED_BIRCH_LOG, STRIPPED_OAK_LOG, STRIPPED_ACACIA_LOG -> 20;
			case SUNFLOWER, PEONY, LILAC, LILY_OF_THE_VALLEY, ROSE_BUSH -> 30;
			case DARK_OAK_LOG -> 34;
			case JUNGLE_LOG -> 36;
			case SPRUCE_LOG -> 38;
			case BIRCH_LOG -> 40;
			case OAK_LOG -> 42;
			case ACACIA_LOG -> 44;
			case WARPED_STEM, CRIMSON_STEM -> 52;
			case WITHER_ROSE, SPORE_BLOSSOM -> 100;
			default -> 0;
		};
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}
	
}
