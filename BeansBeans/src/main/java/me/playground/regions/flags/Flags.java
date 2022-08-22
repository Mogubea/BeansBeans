package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import me.playground.items.lore.Lore;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

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
	public static final FlagMember 	DOOR_ACCESS 		= register(new FlagMember("door-access", "Door Access", MemberLevel.VISITOR, MemberLevel.NONE))
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
	
	/*
	 * Player related boolean flags
	 */
	
	// Applies only to non Members
	public static final FlagBoolean TELEPORT_IN 		= register(new FlagBoolean("teleport-in", "Non-Member Warping In", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 into the region."));

	public static final FlagBoolean TELEPORT_OUT 		= register(new FlagBoolean("teleport-out", "Non-Member Warping Out", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to \u00a7b/warp"),
					Component.text("\u00a77and \u00a7b/tp\u00a77 out of the region."));

	public static final FlagBoolean ENDERPEARLS 		= register(new FlagBoolean("enderpearls", "Non-Member Enderpearls", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to use \u00a72Enderpearls"),
					Component.text("\u00a77and \u00a7dChorus Fruit\u00a77 to get into the region."));

	public static final FlagBoolean WARP_CREATION 		= register(new FlagBoolean("warp-creation", "Non-Member Warp Creation", true))
			.setFlagCategory(Flag.FlagCategory.TELEPORTING)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to create \u00a7dWarps\u00a77,"),
					Component.text("\u00a7b Homes\u00a77 and \u00a7bSpawn Points\u00a77 inside of the region."));

	public static final FlagBoolean SHOP_ACCESS 		= register(new FlagBoolean("shop-access", "Non-Member Shop Access", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a79Non-Members\u00a77 will be able to access \u00a7eShops"),
					Component.text("\u00a77that are inside of the region."));

	public static final FlagBoolean PROTECT_ANIMALS 	= register(new FlagBoolean("protect-animals", "Animal Protection", true, false, false))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Prevent \u00a74Hostile mobs\u00a77 and \u00a79Non-Members\u00a77 from harming"),
					Component.text("\u00a77or capturing animals, captured fish and villagers inside"),
					Component.text("\u00a77of the region."));
	
	public static final FlagBoolean ENTITY_TRAILS		= register(new FlagBoolean("entity-trail", "Mob Trails", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a7fSnowmen \u00a77leave a \u00a7fSnow Trail\u00a77 and can"),
					Component.text("\u00a7bFrost Walker\u00a74 mobs\u00a77 cover \u00a79water\u00a77 in \u00a7bice\u00a77?"),
					Component.text("\u00a78(Does not effect Frost Walking players, and note"),
					Component.text("\u00a78that Snowmen die without snow on the ground!)"));
	public static final FlagBoolean MOB_HOSTILE_SPAWNS	= register(new FlagBoolean("hostile-spawning", "Hostile Mob Spawning", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a74hostile mobs \u00a77like Creepers, Zombies etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	public static final FlagBoolean MOB_PASSIVE_SPAWNS	= register(new FlagBoolean("passive-spawning", "Passive Mob Spawning", true))
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setDescription(
					Component.text("\u00a77Can \u00a7apassive mobs \u00a77like Sheep, Pigs, Cows etc."),
					Component.text("\u00a77naturally spawn inside of the region boundaries?"));
	
	/*
	 * Misc
	 */
	public static final FlagBoolean SEND_ENTER_ACTION_BAR = register(new FlagBoolean("send-enter-action-bar-msg", "Send Enter Action Bar Message", true))
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

	public static final FlagBoolean SEND_LEAVE_ACTION_BAR = register(new FlagBoolean("send-leave-action-bar-msg", "Send Leave Action Bar Message", true))
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

	public static final FlagBoolean BLOCK_SPREAD		= register(new FlagBoolean("block-spread", "Block Spreading", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Blocks such as \u00a72vines\u00a77, \u00a7asugar canes"),
					Component.text("\u00a77and \u00a7cmushrooms\u00a77 can grow and spread."));

	public static final FlagBoolean SNOW_FORMATION		= register(new FlagBoolean("snow-form", "Form Snow Layers", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Form \u00a7fSnow Layers\u00a77 when it's \u00a77Snowing\u00a77."));

	public static final FlagBoolean ICE_FORMATION		= register(new FlagBoolean("ice-form", "Form Ice", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a77Automatically form \u00a7bIce Blocks\u00a77 in cold biomes."));

	public static final FlagBoolean STONE_FORMATION		= register(new FlagBoolean("stone-form", "Form Stones", false, false, true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.getBuilder("Form &#444444Stone &rand &#444444Cobblestone &rwhenever flowing lava mixes with water.").build().getLore());

	public static final FlagBoolean OBSIDIAN_FORMATION	= register(new FlagBoolean("obsidian-form", "Form Obsidian", true))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(Lore.getBuilder("Form &#770077Obsidian &rwhenever still lava mixes with water.").build().getLore());

	public static final FlagBoolean TNT					= register(new FlagBoolean("tnt", "Block Explosions", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77TNT and End Cystal Block Damage."));

	public static final FlagBoolean EXPLOSIONS			= register(new FlagBoolean("explosions", "Mob Explosions", false))
			.setFlagCategory(Flag.FlagCategory.BLOCKS)
			.setDescription(
					Component.text("\u00a77Creeper and Bed Block Damage."));

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

	public static final FlagColour 	DYNMAP_COLOUR		= register(new FlagColour("dynmap-colour", "Dynmap Colour", 0x6655bf84, 0, false))
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setDescription(Lore.fastBuild(false, 40, "The colour that this region will show up as on the Dynmap."));

	public static final FlagColour 	NAME_COLOUR			= register(new FlagColour("name-colour", "Name Colour", BeanColor.REGION.value(), BeanColor.REGION_WORLD.value(), false))
			.setFlagCategory(Flag.FlagCategory.MISCELLANEOUS)
			.setNeedsPermission()
			.setDescription(Lore.fastBuild(false, 40, "The colour that this region will show up as on the Dynmap."));

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
			.setFlagCategory(Flag.FlagCategory.ENTITIES)
			.setNeedsPermission()
			.setDescription(
					Component.text("\u00a7fMultiply \u00a77the amount of \u00a7aexperience dropped"),
					Component.text("\u00a77by mobs by the specified value."));
	public static final FlagFloat 	MOB_DROP_COIN		= register(new FlagFloat("mob-coin-drop-mult", "Mob Coin Drop Multiplier", 1F, 0F, 10F))
			.setMinimumValue(0F)
			.setMaximumValue(10F)
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
