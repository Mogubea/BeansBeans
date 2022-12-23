package me.playground.skills;

import java.util.*;

import me.playground.items.lore.Lore;
import me.playground.main.Main;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Skill {
	
	protected static final Random rand = new Random();
	private static final Map<String, Skill> skills = new LinkedHashMap<>();
	
	public static final SkillMining MINING = new SkillMining();
	public static final SkillForaging FORAGING = new SkillForaging();
	public static final SkillAgriculture AGRICULTURE = new SkillAgriculture();
	public static final SkillFishing FISHING = new SkillFishing();
	public static final Skill BUILDING = new SkillBuilding();
	public static final SkillCombat COMBAT = new SkillCombat();
	public static final SkillForging FORGING = new SkillForging();
	public static final Skill DUNGEONEERING = new SkillDungeoneering();
//	public static final SkillAcrobatics ACROBATICS = new SkillAcrobatics();
	public static final SkillEnchanting ENCHANTING = new SkillEnchanting();
	public static final SkillAlchemy ALCHEMY = new SkillAlchemy();
	public static final SkillTrading TRADING = new SkillTrading();

	private final Main plugin;
	private final SkillTree<Skill> tree;
	private final String stringName;
	private final TextColor colour;
	private final Component simpleName;
	private final BarColor barColour;
	private final char colourChar;
	private final ItemStack displayStack;
	private final Material skillTreeItem;
	private final Material glassPaneItem;
	private final List<TextComponent> description;

	private final String icon;
	
	protected Skill(String name, int colour, BarColor barColour, char colourChar, String icon, Material displayStack, Material skillTreeItem, String description) {
		this.plugin = Main.getInstance();
		this.tree = SkillTree.getSkillTree(this);
		this.colour = TextColor.color(colour);
		this.simpleName = Component.text(name, this.colour);
		this.stringName = name;
		this.barColour = barColour;
		this.colourChar = colourChar;
		this.icon = icon;
		this.displayStack = new ItemStack(displayStack);
		this.skillTreeItem = skillTreeItem;
		this.glassPaneItem = Material.valueOf(getDye().name().replace("DYE", "STAINED_GLASS_PANE"));
		this.description = Lore.fastBuild(false, 30, description);
		
		skills.put(name.toUpperCase(), this);
	}

	@NotNull
	public String getName() {
		return stringName;
	}

	@NotNull
	public String getNameWithIcon() {
		return icon + " " + stringName;
	}

	@NotNull
	public TextColor getColour() {
		return colour;
	}

	@NotNull
	public Component toComponent() {
		return simpleName;
	}

	@NotNull
	public BarColor getBarColour() {
		return barColour;
	}

	public char getColourCode() {
		return colourChar;
	}

	@NotNull
	public ItemStack getDisplayStack() {
		return displayStack;
	}

	@NotNull
	public Material getDye() {
		return skillTreeItem;
	}

	@NotNull
	public Material getGlassPane() {
		return glassPaneItem;
	}
	
	public boolean performSkillEvent(PlayerSkillData s, Event e) {
		return doSkillEvent(s, e);
	}
	
	protected abstract boolean doSkillEvent(PlayerSkillData s, Event e);

	@NotNull
	public List<TextComponent> getDescription() {
		return description;
	}

	@NotNull
	public String getIcon() {
		return icon;
	}

	/**
	 * @return An unmodifiable list of all the registered skills.
	 */
	public static List<Skill> getRegisteredSkills() {
		return List.copyOf(skills.values());
	}

	@Nullable
	public static Skill getByName(String name) {
		if (name == null) return null;

		return skills.getOrDefault(name.toUpperCase(), null);
	}

	public SkillTree<?> getSkillTree() {
		return tree;
	}

	/**
	 * Helper for {@link me.playground.main.BlockTracker#isBlockNatural(Block)}.
	 */
	protected boolean isBlockNatural(Block block) {
		return plugin.getBlockTracker().isBlockNatural(block);
	}
	
}
