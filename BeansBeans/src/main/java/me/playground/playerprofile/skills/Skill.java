package me.playground.playerprofile.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public abstract class Skill {
	
	public static final Skill MINING = new SkillMining("Mining", 'b', BarColor.BLUE, Material.NETHERITE_PICKAXE, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
	
	protected final String displayName;
	protected final char displayColor;
	protected final BarColor barColor;
	protected final Material uiItem;
	protected final Material uiBarItem;
	
	public Skill(String name, char color, BarColor bar, Material ui, Material uiBar) {
		this.displayName = name;
		this.displayColor = color;
		this.barColor = bar;
		this.uiItem = ui;
		this.uiBarItem = uiBar;
	}

	public String getColouredName() {
		return "\u00a7"+displayColor+displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public char getDisplayColor() {
		return displayColor;
	}

	public BarColor getBarColor() {
		return barColor;
	}

	public Material getUiItem() {
		return uiItem;
	}

	public Material getUiBarItem() {
		return uiBarItem;
	}
	
	public abstract boolean doSkillEvent(final SkillData skill, final Event event);
	protected abstract SkillType getSkillType();
	
}
