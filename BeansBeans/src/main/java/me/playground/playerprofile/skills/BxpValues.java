package me.playground.playerprofile.skills;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class BxpValues {
	
	public static int getFarmingValue(Material m) {
		switch (m) {
		case MOSS_CARPET: return 2;
		case MOSS_BLOCK: return 3;
		case AZALEA: return 5;
		case FLOWERING_AZALEA: return 6;
		case BAMBOO: return 6;
		case CHORUS_PLANT: return 7;
		case SUGAR_CANE: return 9;
		case COCOA: return 12;
		case CAVE_VINES_PLANT: return 12;
		case SWEET_BERRY_BUSH: return 12;
		case GLOW_LICHEN: return 16;
		case CACTUS: return 16;
		case WHEAT: return 18;
		case CARROTS: return 18;
		case POTATOES: return 18;
		case BEETROOTS: return 23;
		case NETHER_WART: return 24;
		case MELON: return 33;
		case PUMPKIN: return 33;
		case CHORUS_FLOWER: return 35;
		case BEEHIVE: return 80;
		case BEE_NEST: return 80;
		default:
			return 0;
		}
	}
	
	public static int getDiggingValue(Material m) {
		switch (m) {
		case DIRT: case COARSE_DIRT: case DIRT_PATH: return 7;
		case GRASS_BLOCK: case SAND: case GRAVEL: case SOUL_SAND: case SOUL_SOIL: return 11;
		case PODZOL: return 13;
		case CRIMSON_NYLIUM: case WARPED_NYLIUM: return 16;
		case CLAY: return 21;
		case MYCELIUM: return 25;
		default:
			return 0;
		}
	}
	
	public static boolean isPassiveMob(EntityType e) {
		switch(e) {
		case AXOLOTL:
		case BAT:
		case BEE:
		case CAT:
		case CHICKEN:
		case COD:
		case COW:
		case DOLPHIN:
		case DONKEY:
		case FOX:
		case HORSE:
		case IRON_GOLEM:
		case LLAMA:
		case MULE:
		case MUSHROOM_COW:
		case OCELOT:
		case PANDA:
		case PARROT:
		case PIG:
		case POLAR_BEAR:
		case PUFFERFISH:
		case RABBIT:
		case SALMON:
		case SHEEP:
		case SKELETON_HORSE:
		case SNOWMAN:
		case SQUID:
		case GLOW_SQUID:
		case TRADER_LLAMA:
		case TROPICAL_FISH:
		case TURTLE:
		case VILLAGER:
		case WANDERING_TRADER:
		case WOLF:
		case ZOMBIE_HORSE:
			return true;
		default:
			return false;
		}
	}
	
}
