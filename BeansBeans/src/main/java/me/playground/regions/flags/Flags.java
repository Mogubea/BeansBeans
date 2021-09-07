package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.HashMap;

public final class Flags {
	
	private static final HashMap<String, Flag<?>> flagsByName = new HashMap<String, Flag<?>>();
	
	/*
	 * Member related flag levels
	 * If a player is equal to, or higher, than the specified level on the flag, they will have access to whatever this flag implies.
	 * Administrators, by default, will have MemberLevel.MASTER
	 * 
	 * FlagMember's do not inherit the world flag by default.
	 */
	public static final FlagMember 	BUILD_ACCESS 		= register(new FlagMember("build-access", MemberLevel.MEMBER));
	public static final FlagMember 	CONTAINER_ACCESS 	= register(new FlagMember("container-access", MemberLevel.MEMBER)); // Overridden by BLOCK_BREAK
	public static final FlagMember 	DOOR_ACCESS 		= register(new FlagMember("door-access", MemberLevel.VISITOR)); // Overridden by BLOCK_BREAK
	public static final FlagMember 	CROP_ACCESS 		= register(new FlagMember("crop-access", MemberLevel.MEMBER)); // Overridden by BLOCK_BREAK
	public static final FlagMember 	VILLAGER_ACCESS 	= register(new FlagMember("villager-access", MemberLevel.MEMBER));
	
	/*
	 * Player related boolean flags
	 */
	
	// Applies only to non Members
	public static final FlagBoolean TELEPORT_IN 		= register(new FlagBoolean("teleport-in", true));
	public static final FlagBoolean TELEPORT_OUT 		= register(new FlagBoolean("teleport-out", true));
	public static final FlagBoolean ENDERPEARLS 		= register(new FlagBoolean("enderpearls", true));
	public static final FlagBoolean WARP_CREATION 		= register(new FlagBoolean("warp-creation", true));
	public static final FlagBoolean SHOP_ACCESS 		= register(new FlagBoolean("shop-access", true));
	public static final FlagBoolean PROTECT_ANIMALS 	= register(new FlagBoolean("protect-animals", true, false));
	
	// Applies regardless of Membership
	public static final FlagBoolean KEEP_INVENTORY 		= register(new FlagBoolean("keep-inventory", true));
	public static final FlagBoolean PVP					= register(new FlagBoolean("pvp", false));
	
	/*
	 * Mob related flags
	 */
	public static final FlagBoolean MOB_HOSTILE_SPAWNS	= register(new FlagBoolean("hostile-spawning", true));
	public static final FlagBoolean MOB_PASSIVE_SPAWNS	= register(new FlagBoolean("passive-spawning", true));
	
	public static final FlagFloat 	MOB_DAMAGE_TO		= register(new FlagFloat("mob-damage-to", 1F));
	public static final FlagFloat 	MOB_DAMAGE_FROM		= register(new FlagFloat("mob-damage-from", 1F));
	
	public static final FlagFloat 	MOB_DROP_EXP		= register(new FlagFloat("mob-exp-drop-mult", 1F));
	public static final FlagFloat 	MOB_DROP_COIN		= register(new FlagFloat("mob-coin-drop-mult", 1F));
	public static final FlagFloat 	MOB_DROP_ITEM		= register(new FlagFloat("mob-item-drop-mult", 1F));
	
	/*
	 * Misc
	 */
	public static final FlagBoolean TNT					= register(new FlagBoolean("tnt", false));
	public static final FlagBoolean EXPLOSIONS			= register(new FlagBoolean("explosions", false));
	
	
	//public static final FlagBoolean REDSTONE			= register(new FlagBoolean("redstone", true));
	
	private static <T extends Flag<?>> T register(final T flag) {
		flagsByName.put(flag.getName(), flag);
		return flag;
	}
	
	public static ArrayList<Flag<?>> getRegisteredFlags() {
		return new ArrayList<Flag<?>>(flagsByName.values());
	}
	
	public static <T extends Flag<?>> Flag<?> getFlag(String name) {
		return flagsByName.get(name);
	}
	
}
