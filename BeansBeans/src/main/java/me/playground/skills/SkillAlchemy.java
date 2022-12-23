package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

import net.kyori.adventure.text.Component;

public class SkillAlchemy extends Skill {

	protected SkillAlchemy() {
		super("Alchemy", 0xdd88dd, BarColor.PURPLE, 'd', "\u2697", Material.BREWING_STAND, Material.MAGENTA_DYE,
				"Earn Alchemy XP by harvesting nether warts and brewing potions! Currently work in progress.");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		return false;
	}

}
