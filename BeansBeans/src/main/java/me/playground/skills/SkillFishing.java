package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;

public class SkillFishing extends Skill {
	
	protected SkillFishing() {
		super("Fishing", 0x5575ff, BarColor.BLUE, '9', Material.FISHING_ROD);
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		if (!(e instanceof PlayerFishEvent)) return false;
		PlayerFishEvent event = (PlayerFishEvent) e;
		switch(event.getState()) {
		case CAUGHT_FISH:
			s.addExperience(this, 56 * event.getExpToDrop());
			break;
		default:
			break;
		}
		
		return true;
	}
}
