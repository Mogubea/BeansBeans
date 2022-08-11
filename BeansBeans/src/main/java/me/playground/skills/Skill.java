package me.playground.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public abstract class Skill {
	
	protected static final Random rand = new Random();
	private static List<Skill> skills = new ArrayList<Skill>();
	
	public static final Skill MINING = new SkillMining();
	public static final Skill FORAGING = new SkillForaging();
	public static final Skill AGRICULTURE = new SkillAgriculture();
	public static final Skill FISHING = new SkillFishing();
	public static final Skill COMBAT = new SkillCombat();
	public static final Skill ALCHEMY = new SkillAlchemy();
	public static final Skill FORGING = new SkillForging();
	public static final Skill TRADING = new SkillTrading();
	public static final Skill DUNGEONEERING = new SkillDungeoneering();
	public static final Skill BUILDING = new SkillBuilding();
	
	static {
		skills = List.copyOf(skills);
	}
	
	private final String stringName;
	private final TextColor colour;
	private final Component simpleName;
	private final BarColor barColour;
	private final char colourChar;
	private final ItemStack displayStack;
	
	protected Skill(String name, int colour, BarColor barColour, char colourChar, Material displayStack) {
		this.colour = TextColor.color(colour);
		this.simpleName = Component.text(name, this.colour);
		this.stringName = name;
		this.barColour = barColour;
		this.colourChar = colourChar;
		this.displayStack = new ItemStack(displayStack);
		
		skills.add(this);
	}
	
	public String getName() {
		return stringName;
	}
	
	public TextColor getColour() {
		return colour;
	}
	
	public Component toComponent() {
		return simpleName;
	}
	
	public BarColor getBarColour() {
		return barColour;
	}
	
	public char getColourCode() {
		return colourChar;
	}
	
	public ItemStack getDisplayStack() {
		return displayStack;
	}
	
	public boolean performSkillEvent(Skills s, Event e) {
		return doSkillEvent(s, e);
	}
	
	protected abstract boolean doSkillEvent(Skills s, Event e);
	
	public static List<Skill> getRegisteredSkills() {
		return skills;
	}
	
}
