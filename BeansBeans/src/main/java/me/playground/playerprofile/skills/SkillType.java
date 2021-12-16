package me.playground.playerprofile.skills;

import java.awt.Color;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.inventory.ItemStack;

import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

public enum SkillType {
	
	// blue, green, pink, red, purple, white, yellow
	
	BUILDING(Material.BRICKS, BarColor.PURPLE, Material.PURPLE_CONCRETE, Material.PURPLE_STAINED_GLASS, Material.PURPLE_DYE, NamedTextColor.DARK_PURPLE),
	MINING(Material.DIAMOND_PICKAXE, BarColor.BLUE, Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_STAINED_GLASS, Material.LIGHT_BLUE_DYE, NamedTextColor.AQUA),
	EXCAVATION(Material.STONE_SHOVEL, BarColor.GREEN, Material.LIME_CONCRETE, Material.LIME_STAINED_GLASS, Material.LIME_DYE, NamedTextColor.GREEN),
	LOGCUTTING(Material.WOODEN_AXE, BarColor.PINK, Material.PINK_CONCRETE, Material.PINK_STAINED_GLASS, Material.PINK_DYE, NamedTextColor.LIGHT_PURPLE),
	AGRICULTURE(Material.GOLDEN_HOE, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, Material.YELLOW_DYE, NamedTextColor.GOLD),
	//HERBALISM(Material.POPPY, BarColor.GREEN, Material.LIME_CONCRETE, Material.LIME_STAINED_GLASS, ChatColor.GREEN),
	FISHING(Material.FISHING_ROD, BarColor.BLUE, Material.BLUE_CONCRETE, Material.BLUE_STAINED_GLASS, Material.BLUE_DYE, NamedTextColor.BLUE),
	COMBAT(Material.SHIELD, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, Material.RED_DYE, NamedTextColor.RED),
	//ARCHERY(Material.BOW, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, ChatColor.RED),
	REPAIR(Material.ANVIL, BarColor.WHITE, Material.WHITE_CONCRETE, Material.WHITE_STAINED_GLASS, Material.WHITE_DYE, NamedTextColor.WHITE),
	//DUNGEONEERING(Material.GOLDEN_SWORD, BarColor.RED, Material.RED_CONCRETE, Material.RED_STAINED_GLASS, ChatColor.DARK_RED),
	//CRAFTING(Material.CRAFTING_TABLE, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, ChatColor.YELLOW),
	//SHEARING(Material.SHEARS, BarColor.WHITE, Material.WHITE_CONCRETE, Material.WHITE_STAINED_GLASS, ChatColor.WHITE),
	SELLING(Material.GOLD_INGOT, BarColor.YELLOW, Material.YELLOW_CONCRETE, Material.YELLOW_STAINED_GLASS, Material.YELLOW_DYE, NamedTextColor.GOLD);
	
	final Material icon;
	final BarColor barcol;
	final Material concrete;
	final Material glass;
	final Material dye;
	final ChatColor cCol;
	final TextColor chatColour;
	final Component displayName;
	String plainName;
	
	SkillType(Material m, BarColor b, Material concrete, Material glass, Material skillDye, TextColor color) {
		barcol = b;
		icon = m;
		this.concrete = concrete;
		this.glass = glass;
		this.dye = skillDye;
		this.chatColour = color;
		this.cCol = ChatColor.of(new Color(color.value())); //afjndgdsnfdjhgkdsghs
		this.plainName = Utils.firstCharUpper(toString());
		this.displayName = Component.text(plainName, color).decoration(TextDecoration.ITALIC, false);
	}
	
	public ItemStack getDisplayStack() {
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
	
	public Component getDisplayName() {
		return displayName;
	}
	
	public String getPlainName() {
		return plainName;
	}
	
	public TextColor getColour() {
		return this.chatColour;
	}
	
	/**
	 * So stupid that this still needs to be used for boss bars.
	 */
	public ChatColor getChatColour() {
		return this.cCol;
	}
	
	public int getSkillId() {
		return this.ordinal();
	}
	
}
