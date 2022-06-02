package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillAlchemy extends Skill {

	protected SkillAlchemy() {
		super("Alchemy", 0xdd88dd, BarColor.PURPLE, 'd', Material.BREWING_STAND);
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		// TODO Auto-generated method stub
		return false;
	}

}
