package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

public class SkillEnchanting extends Skill {

	protected SkillEnchanting() {
		super("Enchanting", 0x97caff, BarColor.BLUE, 'b', "\u270e", Material.ENCHANTING_TABLE, Material.LIGHT_BLUE_DYE,
				"Earn Enchanting experience by applying enchantments and runes to your various tools, weapons and armours.");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		return false;
	}

}
