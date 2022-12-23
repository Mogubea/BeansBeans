package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;

@Deprecated
public class SkillAcrobatics extends Skill {

	protected SkillAcrobatics() {
		super("Acrobatics", 0x35b759, BarColor.GREEN, '2', "\u2602", Material.LEATHER_BOOTS, Material.GREEN_DYE,
				"Earn Acrobatics XP by breaking your ankles and making leaps of faith. Currently a joke skill.");
	}

	@Override
	protected boolean doSkillEvent(PlayerSkillData s, Event e) {
		if (!(e instanceof EntityDamageEvent event)) return false;
		s.addExperience(this, (int)(event.getDamage() * (12 + rand.nextInt(4))));
		return true;
	}

}
