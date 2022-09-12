package me.playground.skills;

import java.util.*;

import me.playground.items.lore.Lore;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public abstract class Skill {
	
	protected static final Random rand = new Random();
	private static Map<String, Skill> skills = new LinkedHashMap<>();
	
	public static final SkillMining MINING = new SkillMining();
	public static final SkillForaging FORAGING = new SkillForaging();
	public static final SkillAgriculture AGRICULTURE = new SkillAgriculture();
	public static final SkillFishing FISHING = new SkillFishing();
	public static final Skill BUILDING = new SkillBuilding();
	public static final SkillCombat COMBAT = new SkillCombat();
	public static final SkillForging FORGING = new SkillForging();
	public static final Skill DUNGEONEERING = new SkillDungeoneering();
	public static final SkillAcrobatics ACROBATICS = new SkillAcrobatics();
	public static final SkillEnchanting ENCHANTING = new SkillEnchanting();
	public static final SkillAlchemy ALCHEMY = new SkillAlchemy();
	public static final SkillTrading TRADING = new SkillTrading();
	
	static {
		skills = Collections.unmodifiableMap(skills);
		for (Skill skill : skills.values())
			skill.perkTree = Collections.unmodifiableMap(skill.perkTree);
	}
	
	private final String stringName;
	private final TextColor colour;
	private final Component simpleName;
	private final BarColor barColour;
	private final char colourChar;
	private final ItemStack displayStack;
	private final Material skillTreeItem;
	private final List<TextComponent> description;

	private final String icon;
	
	private Map<Integer, SkillPerk> perkTree = new HashMap<>();
	
	protected Skill(String name, int colour, BarColor barColour, char colourChar, String icon, Material displayStack, Material skillTreeItem, String description) {
		this.colour = TextColor.color(colour);
		this.simpleName = Component.text(name, this.colour);
		this.stringName = name;
		this.barColour = barColour;
		this.colourChar = colourChar;
		this.icon = icon;
		this.displayStack = new ItemStack(displayStack);
		this.skillTreeItem = skillTreeItem;
		this.description = Lore.fastBuild(false, 30, description);
		
		skills.put(name.toLowerCase(), this);
	}
	
	protected Skill addPerk(int row, int column, SkillPerk perk) {
		if (row < 0 || row > 10) row = 0;
		if (column < 0 || column > 4) column = 0;
		perkTree.put((row * 5) + column, perk);
		perk.setSkill(this);
		return this;
	}
	
	public String getName() {
		return stringName;
	}

	public String getNameWithIcon() {
		return icon + " " + stringName;
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
	
	public Material getDye() {
		return skillTreeItem;
	}
	
	public boolean performSkillEvent(Skills s, Event e) {
		return doSkillEvent(s, e);
	}
	
	public Map<Integer, SkillPerk> getPerkTree() {
		return perkTree;
	}
	
	protected abstract boolean doSkillEvent(Skills s, Event e);
	
	public abstract List<Component> getGUIDescription(Skills s);
	
	public List<TextComponent> getDescription() {
		return description;
	}

	public String getIcon() {
		return icon;
	}

	/**
	 * @return An unmodifiable list of all the registered skills.
	 */
	public static List<Skill> getRegisteredSkills() {
		return List.copyOf(skills.values());
	}
	
	public static Skill getByName(String name) {
		return skills.getOrDefault(name, null);
	}
	
}
