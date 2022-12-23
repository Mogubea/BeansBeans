package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

public class SkillBuilding extends Skill {

	protected SkillBuilding() {
		super("Building", 0xffbbff, BarColor.PINK, 'd', "\ud83d\udd14", Material.BRICK, Material.PINK_DYE,
				"Earn Building XP by placing blocks and building structures!");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		if (!(e instanceof BlockPlaceEvent event)) return false;
		float hardness = event.getBlock().getType().getHardness();
		if (hardness <= 0F) return false;
		
		s.addExperience(this, hardness < 1F ? 6 : 12);
		return true;
	}

}
