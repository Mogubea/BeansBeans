package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

public class SkillBuilding extends Skill {

	protected SkillBuilding() {
		super("Building", 0xffbbff, BarColor.PINK, 'd', Material.BRICK);
	}

	@Override
	protected boolean doSkillEvent(Skills s, Event e) {
		if (!(e instanceof BlockPlaceEvent)) return false;
		BlockPlaceEvent event = (BlockPlaceEvent) e;
		float hardness = event.getBlock().getType().getHardness();
		if (hardness <= 0F) return false;
		
		s.addExperience(this, hardness < 1F ? 6 : 12);
		return true;
	}

}
