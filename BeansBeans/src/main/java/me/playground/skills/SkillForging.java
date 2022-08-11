package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillForging extends Skill {

	protected SkillForging() {
		super("Forging", 0xdddddd, BarColor.WHITE, 'f', Material.ANVIL);
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		// TODO Auto-generated method stub
		return false;
	}

}
