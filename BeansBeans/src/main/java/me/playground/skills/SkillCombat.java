package me.playground.skills;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.kyori.adventure.text.Component;

public class SkillCombat extends Skill {
	
	protected SkillCombat() {
		super("Combat", 0xff4455, BarColor.RED, 'c', "\ud83c\udff9", Material.SHIELD, Material.RED_DYE,
				"Earn Combat XP by fighting hostile mobs and bosses!");
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		if (!(e instanceof EntityDamageByEntityEvent event)) return false;

		float mult = 0.2F;
		if (!event.getEntity().fromMobSpawner() && !(event.getEntity().getNearbyEntities(3, 7, 3).size() > 6))
			mult = isPassiveMob(event.getEntityType()) ? 1.5F : 3.25F;
		
		s.addExperience(this, (int) (mult * event.getDamage()));
		return true;
	}
	
	private boolean isPassiveMob(EntityType e) {
		return switch (e) {
			case AXOLOTL, BAT, BEE, CAT, CHICKEN, COD, COW, DOLPHIN, DONKEY, FOX, HORSE, IRON_GOLEM, LLAMA, MULE, MUSHROOM_COW, OCELOT, PANDA, PARROT, PIG,
					POLAR_BEAR, PUFFERFISH, RABBIT, SALMON, SHEEP, SKELETON_HORSE, SNOWMAN, SQUID, GLOW_SQUID, TRADER_LLAMA, TROPICAL_FISH, TURTLE, VILLAGER,
					WANDERING_TRADER, WOLF, ZOMBIE_HORSE -> true;
			default -> false;
		};
	}

	@Override
	public List<Component> getGUIDescription(Skills s) {
		return null;
	}

}
