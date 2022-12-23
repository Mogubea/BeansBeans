package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;

import net.kyori.adventure.text.Component;

public class SkillFishing extends Skill {
	
	protected SkillFishing() {
		super("Fishing", 0x5575ff, BarColor.BLUE, '9', "\ud83c\udfa3", Material.FISHING_ROD, Material.BLUE_DYE,
				"Earn Fishing XP by hunting for fish or using the fishing rod!");
	}
	
	@Override
	protected boolean doSkillEvent(final PlayerSkillData s, final Event e) {
		if (!(e instanceof PlayerFishEvent event)) return false;
		if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH)
			s.addExperience(this, (60 + rand.nextInt(5)) * event.getExpToDrop());
		return true;
	}

}
