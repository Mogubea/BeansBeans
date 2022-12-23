package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillDungeoneering extends Skill {

	protected SkillDungeoneering() {
		super("Dungeoneering", 0xff3053, BarColor.RED, '4', "\ud83d\udde1", Material.ENDER_CHEST, Material.RED_DYE,
				"Earn Dungeoneering XP by participating in Dungeons. Currently work in progress.");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		return false;
	}

}
