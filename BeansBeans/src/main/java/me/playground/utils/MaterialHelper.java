package me.playground.utils;

import org.bukkit.Material;

public class MaterialHelper {

	public static boolean isLog(Material m, boolean inclStripped) {
		switch (m) {
		case OAK_LOG:
		case SPRUCE_LOG:
		case BIRCH_LOG:
		case JUNGLE_LOG:
		case DARK_OAK_LOG:
		case ACACIA_LOG:
		case CRIMSON_STEM:
		case WARPED_STEM:
			return true;
		case STRIPPED_OAK_LOG:
		case STRIPPED_SPRUCE_LOG:
		case STRIPPED_BIRCH_LOG:
		case STRIPPED_JUNGLE_LOG:
		case STRIPPED_DARK_OAK_LOG:
		case STRIPPED_ACACIA_LOG:
			return inclStripped;
		default:
			return false;
		}
	}
	
	public static boolean isInstantBlock(Material m) {
		switch (m) {
		case TORCH: case WALL_TORCH: case REDSTONE_WALL_TORCH: case REDSTONE_TORCH: case REDSTONE: case REPEATER: case COMPARATOR: case SOUL_TORCH: case SOUL_WALL_TORCH:
		case SUNFLOWER: case WHEAT_SEEDS: case CARROTS: case POTATOES: case BEETROOTS: case TRIPWIRE_HOOK: case TRIPWIRE: case OAK_SAPLING: case SPRUCE_SAPLING: case BIRCH_SAPLING:
		case JUNGLE_SAPLING: case DARK_OAK_SAPLING: case RED_MUSHROOM: case BROWN_MUSHROOM: case SUGAR_CANE: case TUBE_CORAL: case BRAIN_CORAL: case BUBBLE_CORAL: case FIRE_CORAL: case HORN_CORAL:
		case DEAD_TUBE_CORAL: case DEAD_BRAIN_CORAL: case DEAD_BUBBLE_CORAL: case DEAD_FIRE_CORAL: case DEAD_HORN_CORAL: case SCAFFOLDING: case DRIED_KELP_BLOCK: case SLIME_BLOCK: case HONEY_BLOCK:
			return true;
		default:
			return false;
		}
	}

}
