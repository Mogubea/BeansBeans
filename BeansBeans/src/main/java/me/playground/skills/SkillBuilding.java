package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

import net.kyori.adventure.text.Component;

public class SkillBuilding extends Skill {

	protected SkillBuilding() {
		super("Building", 0xffbbff, BarColor.PINK, 'd', "\u2b50", Material.BRICK, Material.PINK_DYE,
				"Earn Building XP by placing blocks and building structures!");
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

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}

}
