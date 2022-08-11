package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillDungeoneering extends Skill {

	protected SkillDungeoneering() {
		super("Dungeoneering", 0xff3053, BarColor.RED, '4', Material.ENDER_CHEST);
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		// TODO Auto-generated method stub
		return false;
	}

}
