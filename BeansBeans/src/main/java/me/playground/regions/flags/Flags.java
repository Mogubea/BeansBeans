package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.kyori.adventure.text.Component;

public final class Flags {
	
	private static final LinkedHashMap<String, Flag<?>> flagsByName = new LinkedHashMap<String, Flag<?>>();
	
	/*
	 * Member related flag levels
	 * If a player is equal to, or higher, than the specified level on the flag, they will have access to whatever this flag implies.
	 * Administrators, by default, will have MemberLevel.MASTER
	 * 
	 * FlagMember's do not inherit the world flag by default.
	 */
	public static final FlagMember 	BUILD_ACCESS 		= register(new FlagMember("build-access", "Build Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to place,"),
					Component.text("\u00a77break and use blocks."));
	public static final FlagMember 	CONTAINER_ACCESS 	= register(new FlagMember("container-access", "Container Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access"),
					Component.text("\u00a77containers such as chests, furnaces"),
					Component.text("\u00a77and brewing stands."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	ANVIL_ACCESS 	= register(new FlagMember("anvil-access", "Anvil Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access"),
					Component.text("\u00a77and utilise the functionality of anvils."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	DOOR_ACCESS 		= register(new FlagMember("door-access", "Door Access", MemberLevel.VISITOR, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to open and"),
					Component.text("\u00a77close doors/trapdoors/fence gates."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	CROP_ACCESS 		= register(new FlagMember("crop-access", "Crop Harvesting Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to harvest"),
					Component.text("\u00a77planted crops by right clicking."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	VILLAGER_ACCESS 	= register(new FlagMember("villager-access", "Villager Trade Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access and"),
					Component.text("\u00a77trade with \u00a7aVillagers\u00a77."));
	
	/*
	 * Player related boolean flags
	 */
	
	// Applies only to non Members
	public static final FlagBoolean TELEPORT_IN 		= register(new FlagBoolean("teleport-in", "Non-Member Warping In", true))
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 into the region."));
	public static final FlagBoolean TELEPORT_OUT 		= register(new FlagBoolean("teleport-out", "Non-Member Warping Out", true))
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 out of the region."));
	public static final FlagBoolean ENDERPEARLS 		= register(new FlagBoolean("enderpearls", "Non-Member Enderpearls", true))
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to use \u00a72Enderpearls"),
					Component.text("\u00a77and \u00a7dChorus Fruit\u00a77 to get into the region."));
	public static final FlagBoolean WARP_CREATION 		= register(new FlagBoolean("warp-creation", "Non-Member Warp Creation", true))
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to create \u00a7dWarps\u00a77,"),
					Component.text("\u00a7b Homes\u00a77 and \u00a7bSpawn Points\u00a77 inside of the region."));
	public static final FlagBoolean SHOP_ACCESS 		= register(new FlagBoolean("shop-access", "Non-Member Shop Access", true))
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to access \u00a7eShops"),
					Component.text("\u00a77that are inside of the region."));
	public static final FlagBoolean PROTECT_ANIMALS 	= register(new FlagBoolean("protect-animals", "Animal Protection", true, false, false))
			.setDescription(
					Component.text("\u00a74Hostile mobs\u00a77 and \u00a79Non-Members\u00a77 will be able"),
					Component.text("\u00a77to harm animals and villagers inside of the region."));
	
	public static final FlagBoolean ENTITY_TRAILS		= register(new FlagBoolean("entity-trail", "Mob Trails", true))
			.setDescription(
					Component.text("\u00a77Can \u00a7fSnowmen \u00a77leave a \u00a7fSnow Trail\u00a77 and can"),
					Component.text("\u00a7bFrost Walker\u00a74 mobs\u00a77 cover \u00a79water\u00a77 in \u00a7bice\u00a77?"),
					Component.text("\u00a78(Does not effect Frost Walking players, and note"),
					Component.text("\u00a78that Snowmen die without snow on the ground!)"));
	public static final FlagBoolean MOB_HOSTILE_SPAWNS	= register(new FlagBoolean("hostile-spawning", "Hostile Mob Spawning", true))
			.setDescription(
					Component.text("\u00a77Can \u00a74hostile mobs \u00a77like Creepers, Zombies etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	public static final FlagBoolean MOB_PASSIVE_SPAWNS	= register(new FlagBoolean("passive-spawning", "Passive Mob Spawning", true))
			.setDescription(
					Component.text("\u00a77Can \u00a7apassive mobs \u00a77like Sheep, Pigs, Cows etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	
	/*
	 * Misc
	 */
	public static final FlagBoolean ANVIL_DEGRADATION	= register(new FlagBoolean("anvil-degradation", "Anvil Degradation", true))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Anvils degrade when used by players."));
	public static final FlagBoolean BLOCK_SPREAD		= register(new FlagBoolean("block-spread", "Block Spreading", true))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Blocks such as \u00a72vines\u00a77, \u00a7asugar canes"),
					Component.text("\u00a77and \u00a7cmushrooms\u00a77 can grow and spread."));
	public static final FlagBoolean SNOW_FORMATION		= register(new FlagBoolean("snow-form", "Form Snow Layers", true))
			.setDescription(
					Component.text("\u00a77Form \u00a7fSnow Layers\u00a77 when it's \u00a77Snowing\u00a77."));
	public static final FlagBoolean ICE_FORMATION		= register(new FlagBoolean("ice-form", "Form Ice", true))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Automatically form \u00a7bIce Blocks\u00a77 in cold biomes."));
	public static final FlagBoolean TNT					= register(new FlagBoolean("tnt", "Block Explosions", false))
			.setDescription(
					Component.text("\u00a77TNT and End Cystal Block Damage."));
	public static final FlagBoolean EXPLOSIONS			= register(new FlagBoolean("explosions", "Mob Explosions", false))
			.setDescription(
					Component.text("\u00a77Creeper and Bed Block Damage."));
	public static final FlagBoolean KEEP_INVENTORY 		= register(new FlagBoolean("keep-inventory", "Keep Inventory", true))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Do players keep their Inventory when"),
					Component.text("\u00a77dying inside of the region boundaries?"));
	public static final FlagBoolean PVP					= register(new FlagBoolean("pvp", "PVP", false))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Can players fight each-other?"));
	
	public static final FlagFloat 	MOB_DAMAGE_TO		= register(new FlagFloat("mob-damage-to", "Damage Multiplier against Mobs", 1F, 0F, 100F))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the \u00a7cdamage dealt \u00a77to"),
					Component.text("\u00a77mobs by the specified value."));
	public static final FlagFloat 	MOB_DAMAGE_FROM		= register(new FlagFloat("mob-damage-from", "Damage Multiplier from Mobs", 1F, 0F, 100F))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the \u00a7cdamage taken \u00a77from"),
					Component.text("\u00a77mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_EXP		= register(new FlagFloat("mob-exp-drop-mult", "Mob Experience Drop Multiplier", 1F, 0F, 100F))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a7aexperience dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_COIN		= register(new FlagFloat("mob-coin-drop-mult", "Mob Coin Drop Multiplier", 1F, 0F, 10F))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a76coins dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_ITEM		= register(new FlagFloat("mob-item-drop-mult", "Mob Item Drop Multiplier", 1F, 0F, 5F))
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a7ditems dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	
	
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
