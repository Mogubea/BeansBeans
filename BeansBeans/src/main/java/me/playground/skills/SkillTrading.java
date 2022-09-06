package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

import net.kyori.adventure.text.Component;

public class SkillTrading extends Skill {

	protected SkillTrading() {
		super("Trading", 0xffff66, BarColor.YELLOW, 'e', "\u23fc", Material.EMERALD, Material.YELLOW_DYE,
				"Earn Trading XP by striking deals with Villagers, Wandering Traders and Piglins! Currently work in progress.");
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
