package me.playground.playerprofile.skills;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.inventory.ItemStack;

import me.playground.utils.Utils;

public enum SkillType {
	
	// blue, green, pink, red, purple, white, yellow
	
	BUILDING(Material.BRICKS, BarColor.PURPLE, Material.PURPLE_CONCRETE, Material.PURPLE_STAINED_GLASS, Material.PURPLE_DYE, ChatColor.DARK_PURPLE),
	MINING(Material.DIAMOND_PICKAXE, BarColor.BLUE, Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_DYE, ChatColor.AQUA),
	EXCAVATION(Material.STONE_SHOVEL, BarColor.GREEN, Material.LIME_CONCRETE, Material.LIME_STAINED_GLASS, Material.LIME_DYE, ChatColor.GREEN),
	LOGCUTTING(Material.WOODEN_AXE, BarColor.PINK, Material.PINK_CONCRETE, Material.PINK_STAINED_GLASS, Material.PINK_DYE, ChatColor.LIGHT_PURPLE),
	AGRICULTURE(Material.GOLDEN_HOE, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, Material.YELLOW_DYE, ChatColor.GOLD),
	//HERBALISM(Material.POPPY, BarColor.GREEN, Material.LIME_CONCRETE, Material.LIME_STAINED_GLASS, ChatColor.GREEN),
	FISHING(Material.FISHING_ROD, BarColor.BLUE, Material.BLUE_CONCRETE, Material.BLUE_STAINED_GLASS, Material.BLUE_DYE, ChatColor.BLUE),
	COMBAT(Material.SHIELD, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, Material.RED_DYE, ChatColor.RED),
	//ARCHERY(Material.BOW, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, ChatColor.RED),
	REPAIR(Material.ANVIL, BarColor.WHITE, Material.WHITE_CONCRETE, Material.WHITE_STAINED_GLASS, Material.WHITE_DYE, ChatColor.WHITE),
	//DUNGEONEERING(Material.GOLDEN_SWORD, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, ChatColor.DARK_RED),
	//CRAFTING(Material.CRAFTING_TABLE, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, ChatColor.YELLOW),
	//SHEARING(Material.SHEARS, BarColor.WHITE, Material.WHITE_CONCRETE, Material.WHITE_STAINED_GLASS, ChatColor.WHITE),
	SELLING(Material.GOLD_INGOT, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, Material.YELLOW_DYE, ChatColor.GOLD);
	
	final private static DecimalFormat df = new DecimalFormat("#.##");
	
	final Material icon;
	final BarColor barcol;
	final Material concrete;
	final Material glass;
	final Material dye;
	final ChatColor chatColour;
	final String displayName;
	String plainName;
	
	SkillType(Material m, BarColor b, Material concrete, Material glass, Material skillDye, ChatColor color) {
		barcol = b;
		icon = m;
		this.concrete = concrete;
		this.glass = glass;
		this.dye = skillDye;
		chatColour = color;
		this.displayName = color + Utils.firstCharUpper(toString());
	}
	
	public ItemStack getIconStack() {
		return new ItemStack(icon);
	}
	
	public BarColor getBarColour() {
		return barcol;
	}
	
	public Material getConcrete() {
		return concrete;
	}
	
	public Material getGlass() {
		return glass;
	}
	
	public Material getDye() {
		return dye;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getPlainName() {
		if (plainName == null)
			plainName = ChatColor.stripColor(displayName);
		return plainName;
	}
	
	public ChatColor getColour() {
		return this.chatColour;
	}
	
	public int getSkillId() {
		return this.ordinal();
	}
	
	// skill 1 at level 10
	// skill 2 at level 40
	// skill 3 at level 60
	// skill 4 at level 100
	// skill
	public String getSkillName(int skillLevel) {
		switch(this) {
		case BUILDING:
			break;
		case COMBAT:
			switch(skillLevel) {
			case 0: return "Retaliation"; // Increased damage after blocking a hit
			case 1: return "Reinforced Skin"; // Absorption hearts out of combat
			case 2: return "???";
			case 3: return "???";
			case 4: return "Tracker";
			}
			break;
		case EXCAVATION:
			break;
		case AGRICULTURE:
			break;
		case FISHING:
			break;
		case LOGCUTTING:
			break;
		case MINING:
			switch(skillLevel) {
			case 0: return "Refined Tools"; // Chance to not lose durability on tools
			case 1: return "Magnified Experience"; // Increased XP from ores
			case 2: return "Smelter's Heart"; // Chance to insta smelt iron and gold ores
			case 3: return "Fortunate Finds"; // Chance to find increased amount of materials from an ore
			case 4: return "Miner's Vision"; // Few second Xray
			}
			break;
		case REPAIR:
			switch(skillLevel) {
			case 0: return "Optimised Smithing";
			case 1: return "";
			}
			break;
		case SELLING:
			break;
		default:
			break;
		}
		return "Unnamed Skill";
	}
	
	public List<String> getSkillDescription(int skillLevel, int playerLevel) {
		switch(this) {
		case BUILDING:
			break;
		case COMBAT:
			break;
		case EXCAVATION:
			break;
		case AGRICULTURE:
			break;
		case FISHING:
			break;
		case LOGCUTTING:
			break;
		case MINING:
			switch(skillLevel) {
			case 0:
				float chance = ((float) (Math.min(1000, playerLevel)) / 2000F) * 80;
				return Arrays.asList(
						"\u00a77While mining, your pickaxes have a \u00a7e" + df.format(chance) + "%",
						"\u00a77chance to avoid losing durability when",
						"\u00a77an \u00a7dUnbreaking Enchantment \u00a77fails!",
						"",
						"\u00a78Has a max of \u00a7e40% \u00a78at \u00a79Mining level 1,000\u00a78!");
			case 1:
				float chance1 = playerLevel / 15F;
				return Arrays.asList(
						"\u00a77Mining ores grant \u00a7e" + df.format(chance1) + "%\u00a77 more \u00a7aExperience\u00a77!");
			}
			break;
		case REPAIR:
			switch(skillLevel) {
			case 0:
				int reduc = (int) Math.min(15, ((float)playerLevel / 60F));
				return Arrays.asList(
						"\u00a77The \u00a7fRepair Cost \u00a77of weapons, tools and",
						"\u00a77armour are reduced by \u00a7a" + reduc + " Levels\u00a77, down",
						"\u00a77to a minimum of \u00a7a1 Level\u00a77!",
						"",
						"\u00a78Cost decreases per \u00a7960 Repair Levels\u00a78!",
						"\u00a78Max reduction of \u00a7a15\u00a78 at \u00a79Repair Level 900\u00a78!");
			}
			break;
		case SELLING:
			break;
		default:
			break;
		}
		return Arrays.asList("\u00a78Work in progress...");
	}
	
	
}
