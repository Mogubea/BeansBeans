package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import me.playground.items.lore.Lore;
import me.playground.regions.Region;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public final class Flags {
	
	private static final LinkedHashMap<String, Flag<?>> flagsByName = new LinkedHashMap<>();
	
	/*
	 * Member related flag levels
	 * If a player is equal to, or higher, than the specified level on the flag, they will have access to whatever this flag implies.
	 * Administrators, by default, will have MemberLevel.MASTER
	 * 
	 * FlagMember's do not inherit the world flag by default.
	 */
	public static final FlagMember 	BUILD_ACCESS 		= register(new FlagMember("build-access", "Build Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to place,"),
					Component.text("\u00a77break and use blocks."));
	public static final FlagMember 	CONTAINER_ACCESS 	= register(new FlagMember("container-access", "Container Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access"),
					Component.text("\u00a77containers such as chests, furnaces"),
					Component.text("\u00a77and brewing stands."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	ANVIL_ACCESS 	= register(new FlagMember("anvil-access", "Anvil Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access"),
					Component.text("\u00a77and utilise the functionality of anvils."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	DOOR_ACCESS 		= register(new FlagMember("door-access", "Door Access", MemberLevel.NONE, MemberLevel.NONE))
			.setPlayerDefault(MemberLevel.VISITOR)
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to open and"),
					Component.text("\u00a77close doors/trapdoors/fence gates."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	CROP_ACCESS 		= register(new FlagMember("crop-access", "Crop Harvesting Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to harvest"),
					Component.text("\u00a77planted crops. Crops will automatically"),
					Component.text("\u00a77act as if \u00a7bReplenish\u00a77 is active."),
					Component.empty(),
					Component.text("\u00a7cPlayers with Build Access ignore this flag."));
	public static final FlagMember 	VILLAGER_ACCESS 	= register(new FlagMember("villager-access", "Villager Trade Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Players equal to or above the designated"),
					Component.text("\u00a79Membership Level\u00a77 will be able to access and"),
					Component.text("\u00a77trade with \u00a7aVillagers\u00a77."));
	public static final FlagMember 	VEHICLE_ACCESS 		= register(new FlagMember("vehicle-access", "Vehicle Access", MemberLevel.MEMBER, MemberLevel.NONE))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(Lore.fastBuild(true, 40, """
					Players with the specified &9Membership Level&r are able to create, ride and destroy vehicles such as &#aaaaaaMinecarts&r and &#bb9944Boats&r.
					
					&8&oPlayers will always be able to access and destroy their own vehicles regardless of this setting."""));
	
	/*
	 * Player related boolean flags
	 */
	
	// Applies only to non Members
	public static final FlagMemberBoolean TELEPORT_IN 	= register(new FlagMemberBoolean("teleport-in", "Non-Member Warping In", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 into the region."));

	public static final FlagMemberBoolean TELEPORT_OUT 	= register(new FlagMemberBoolean("teleport-out", "Non-Member Warping Out", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 out of the region."));

	public static final FlagMemberBoolean ENDERPEARLS 	= register(new FlagMemberBoolean("enderpearls", "Non-Member Enderpearls", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to use \u00a72Enderpearls"),
					Component.text("\u00a77and \u00a7dChorus Fruit\u00a77 to get into the region."));

	public static final FlagMemberBoolean WARP_CREATION = register(new FlagMemberBoolean("warp-creation", "Non-Member Warp Creation", true))
			.setPlayerDefault(false)
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to create \u00a7dWarps\u00a77,"),
					Component.text("\u00a7b Homes\u00a77 and \u00a7bSpawn Points\u00a77 inside of the region."));

	public static final FlagMemberBoolean SHOP_ACCESS 	= register(new FlagMemberBoolean("shop-access", "Non-Member Shop Access", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to access \u00a7eShops"),
					Component.text("\u00a77that are inside of the region."));

	public static final FlagMemberBoolean PROTECT_ANIMALS 	= register(new FlagMemberBoolean("protect-animals", "Animal Protection", true, false, false))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Prevent \u00a74Hostile mobs\u00a77 and \u00a79Non-Members\u00a77 from harming"),
					Component.text("\u00a77or capturing animals, captured fish and villagers inside"),
					Component.text("\u00a77of the region. Also prevents lightning transforming."));
	
	public static final FlagBoolean ENTITY_TRAILS		= register(new FlagBoolean("entity-trail", "Mob Trails", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a7fSnowmen \u00a77leave a \u00a7fSnow Trail\u00a77 and can"),
					Component.text("\u00a7bFrost Walker\u00a74 mobs\u00a77 cover \u00a79water\u00a77 in \u00a7bice\u00a77?"),
					Component.text("\u00a78(Does not effect Frost Walking players, and note"),
					Component.text("\u00a78that Snowmen die without snow on the ground!)"));
	public static final FlagBoolean MOB_HOSTILE_SPAWNS	= register(new FlagBoolean("hostile-spawning", "Hostile Mob Spawning", true))
			.setPlayerDefault(false)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a74hostile mobs \u00a77like Creepers, Zombies etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	public static final FlagBoolean MOB_PASSIVE_SPAWNS	= register(new FlagBoolean("passive-spawning", "Passive Mob Spawning", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a7apassive mobs \u00a77like Sheep, Pigs, Cows etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	public static final FlagBoolean MOB_SPAWNERS 		= register(new FlagBoolean("spawner-spawning", "Mob Spawners", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(Lore.fastBuild(true, 40, "&#666988Mob Spawners&r can summon mobs."));
	
	/*
	 * Misc
	 */
	public static final FlagBoolean SEND_ENTER_ACTION_BAR = register(new FlagBoolean("send-enter-action-bar-msg", "Send Enter Action Bar Message", false))
			.setPlayerDefault(true)
			.setFlagCategory(Flag.FlagCategory.NOTIFICATION)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Send the player an action bar message"),
					Component.text("\u00a77when entering the region."));

	public static final FlagString 	ACTION_BAR_MESSAGE	= register(new FlagString("enter-action-bar-msg", "Enter Action Bar Message", ""))
			.setFlagCategory(Flag.FlagCategory.NOTIFICATION)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77The Action Bar Message that will be sent"),
					Component.text("\u00a77to the player upon entering the region if"),
					Component.text("\u00a79Send Enter Action Bar\u00a77 is enabled.")
			);

	public static final FlagBoolean SEND_LEAVE_ACTION_BAR = register(new FlagBoolean("send-leave-action-bar-msg", "Send Leave Action Bar Message", false))
			.setPlayerDefault(true)
			.setFlagCategory(Flag.FlagCategory.NOTIFICATION)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Send the player an action bar message"),
					Component.text("\u00a77when leaving the region."));

	public static final FlagBoolean ANVIL_DEGRADATION	= register(new FlagBoolean("anvil-degradation", "Anvil Degradation", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Anvils degrade when used by players."));
	public static final FlagBoolean CROP_REPLENISH 		= register(new FlagBoolean("crop-replenish", "Crop Replenish", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Should crops automatically act as"),
					Component.text("\u00a77if \u00a7bReplenish\u00a77 is active?"),
					Component.empty(),
					Component.text("\u00a7aThis will allow CROP_ACCESS members to"),
					Component.text("\u00a7aharvest crops without right clicking."));

	public static final FlagBoolean GRASS_SPREAD 		= register(new FlagBoolean("grass-spread", "Grass Spreading", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Grass can spread across dirt."));

	public static final FlagBoolean SCULK_SPREAD		= register(new FlagBoolean("sculk-spread", "Sculk Spreading", false))
			.setPlayerDefault(true)
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "&eSkulk Catalysts&r can spread &bSculk&r by replacing blocks like &#444444Stone&r and &#888833Dirt&r when a nearby mob dies."));

	public static final FlagBoolean CROP_GROWTH			= register(new FlagBoolean("crop-growth", "Crop Growth", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "Single block crops like &#998844Wheat&r or &#cccc99Potatoes&r can grow."));

	public static final FlagBoolean BIG_CROP_GROWTH		= register(new FlagBoolean("big-crop-growth", "Multi-Block Crop Growth", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "Multi block crops like &#aaddbbSugar Cane&r, &#88cc99Bamboo&r or &aMelons&r can grow."));

	public static final FlagBoolean VINE_GROWTH			= register(new FlagBoolean("vine-growth", "Vine Growth", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Vine based blocks can grow and yield crops."));

	public static final FlagBoolean MUSHROOM_GROWTH		= register(new FlagBoolean("mushroom-growth", "Mushroom Growth", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "Mushrooms can grow and spread around in dark places or on &dMycelium&r."));

	public static final FlagBoolean FIRE_SPREAD 		= register(new FlagBoolean("fire-spread", "Fire Spreading", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Fire can form and spread across flammable blocks."));

	public static final FlagBoolean FIRE_BURN 			= register(new FlagBoolean("fire-burn", "Fire Burn", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Fire can burn and destroy flammable blocks."));

	public static final FlagBoolean FIRE_EXTINGUISH 	= register(new FlagBoolean("fire-extinguish", "Fire Extinguish", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Fire can naturally extinguish."));

	public static final FlagBoolean PISTONS				= register(new FlagBoolean("piston", "Pistons", true, false, false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "&#666666Pistons &rto react to &#ef6666Redstone &rsignals."));

	public static final FlagBoolean SNOW_MELT			= register(new FlagBoolean("snow-melt", "Melt Snow Layers", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "&fSnow Layers&r will melt when next to a heat source such as a Torch or Lava."));

	public static final FlagBoolean SNOW_FORMATION		= register(new FlagBoolean("snow-form", "Form Snow Layers", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Form \u00a7fSnow Layers\u00a77 when it's \u00a77Snowing\u00a77."));

	public static final FlagBoolean ICE_FORMATION		= register(new FlagBoolean("ice-form", "Form Ice", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Automatically form \u00a7bIce Blocks\u00a77 in cold biomes."));

	public static final FlagBoolean ICE_MELT			= register(new FlagBoolean("ice-melt", "Melt Ice", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "&bIce Blocks&r will melt when next to a heat source such as a Torch or Lava."));

	public static final FlagBoolean STONE_FORMATION		= register(new FlagBoolean("stone-form", "Form Stones", false, false, true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "Form &#666666Stone &rand &#666666Cobblestone &rwhenever flowing lava mixes with water."));

	public static final FlagBoolean OBSIDIAN_FORMATION	= register(new FlagBoolean("obsidian-form", "Form Obsidian", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(true, 40, "Form &#770077Obsidian &rwhenever still lava mixes with water."));

	public static final FlagBoolean BLOCK_EXPLOSIONS = register(new FlagBoolean("block-explosions", "Block Explosions", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Blocks can be destroyed due to block explosions. For example; Beds, TNT and End Crystals."));

	public static final FlagBoolean ENTITY_EXPLOSIONS = register(new FlagBoolean("mob-explosions", "Mob Explosions", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.fastBuild(false, 40, "Blocks can be destroyed due to entity explosions. For example; Creepers and Fireballs."));

	public static final FlagBoolean KEEP_INVENTORY 		= register(new FlagBoolean("keep-inventory", "Keep Inventory", true))
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Do players keep their Inventory when"),
					Component.text("\u00a77dying inside of the region boundaries?"));
	public static final FlagBoolean PVP					= register(new FlagBoolean("pvp", "PVP", false))
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Can players fight each-other?"));

	public static final FlagColour 	DYNMAP_COLOUR		= register(new FlagColour("dynmap-colour", "Dynmap Colour", 0x7755bf84, 0, false))
			.setPlayerDefault(0x7766cfbb)
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setDescription(Lore.fastBuild(false, 40, "The colour that this region will show up as on the Dynmap."));

	public static final FlagColour 	NAME_COLOUR			= register(new FlagColour("name-colour", "Name Colour", BeanColor.REGION.value(), BeanColor.REGION_WORLD.value(), false))
			.setPlayerDefault(BeanColor.REGION_PLAYER.value())
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setConsumerOnUpdate(Region::updateColouredName)
			.setDescription(Lore.fastBuild(false, 40, "The name colour of this region."));

	public static final FlagFloat 	MOB_DAMAGE_TO		= register(new FlagFloat("mob-damage-to", "Damage Multiplier against Mobs", 1F, 0F, 100F))
			.setMinimumValue(0F)
			.setMaximumValue(100F)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the \u00a7cdamage dealt \u00a77to"),
					Component.text("\u00a77mobs by the specified value."));
	public static final FlagFloat 	MOB_DAMAGE_FROM		= register(new FlagFloat("mob-damage-from", "Damage Multiplier from Mobs", 1F, 0F, 100F))
			.setMinimumValue(0F)
			.setMaximumValue(100F)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the \u00a7cdamage taken \u00a77from"),
					Component.text("\u00a77mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_EXP		= register(new FlagFloat("mob-exp-drop-mult", "Mob Experience Drop Multiplier", 1F, 0F, 100F))
			.setMinimumValue(0F)
			.setMaximumValue(100F)
			.setPlayerDefault(0.75F)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a7aexperience dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_COIN		= register(new FlagFloat("mob-coin-drop-mult", "Mob Coin Drop Multiplier", 1F, 0F, 10F))
			.setMinimumValue(0F)
			.setMaximumValue(10F)
			.setPlayerDefault(0.75F)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a76coins dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_ITEM		= register(new FlagFloat("mob-item-drop-mult", "Mob Item Drop Multiplier", 1F, 0F, 5F))
			.setMinimumValue(0F)
			.setMaximumValue(5F)
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
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
		return new ArrayList<>(flagsByName.values());
	}
	
	public static Flag<?> getFlag(String name) {
		return flagsByName.get(name);
	}
	
}
