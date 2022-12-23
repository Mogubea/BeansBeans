package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillTrading extends Skill {

	protected SkillTrading() {
		super("Trading", 0xffff66, BarColor.YELLOW, 'e', "\u23fc", Material.EMERALD, Material.YELLOW_DYE,
				"Earn Trading XP by striking deals with Villagers, Wandering Traders and Piglins! Currently work in progress.");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		return false;
	}

}
