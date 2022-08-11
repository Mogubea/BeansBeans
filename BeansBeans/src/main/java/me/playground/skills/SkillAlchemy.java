package me.playground.skills;

import java.util.List;

import me.playground.items.lore.Lore;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;

import net.kyori.adventure.text.Component;

public class SkillAlchemy extends Skill {

	protected SkillAlchemy() {
		super("Alchemy", 0xdd88dd, BarColor.PURPLE, 'd', "\u2697", Material.BREWING_STAND, Material.MAGENTA_DYE,
				"Earn Alchemy XP by harvesting nether warts and brewing potions! &cCurrently work in progress.");
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
