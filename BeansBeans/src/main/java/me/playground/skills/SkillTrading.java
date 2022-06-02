package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillTrading extends Skill {

	protected SkillTrading() {
		super("Trading", 0xffff66, BarColor.YELLOW, 'e', Material.EMERALD);
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		// TODO Auto-generated method stub
		return false;
	}

}
