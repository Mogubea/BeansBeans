package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

import net.kyori.adventure.text.Component;

public class SkillForging extends Skill {

	protected SkillForging() {
		super("Forging", 0xdddddd, BarColor.WHITE, 'f', "\u2692", Material.ANVIL, Material.WHITE_DYE,
				"Earn Forging XP by refining, repairing and creating equipment such as tools and armour!");
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		return false;
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}

}
