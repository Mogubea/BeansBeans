package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

import net.kyori.adventure.text.Component;

public class SkillDungeoneering extends Skill {

	protected SkillDungeoneering() {
		super("Dungeoneering", 0xff3053, BarColor.RED, '4', "\ud83d\udde1", Material.ENDER_CHEST, Material.RED_DYE,
				"Earn Dungeoneering XP by participating in Dungeons. Currently work in progress.");
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
