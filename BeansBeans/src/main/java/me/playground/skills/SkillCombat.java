package me.playground.skills;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class SkillCombat extends Skill {
	
	protected SkillCombat() {
		super("Combat", 0xff4455, BarColor.RED, 'c', Material.SHIELD);
	}
	
	@Override
	protected boolean doSkillEvent(final Skills s, final Event e) {
		if (!(e instanceof EntityDamageByEntityEvent)) return false;
		EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
		
		float mult = 0.15F;
		if (!event.getEntity().fromMobSpawner())
			mult = isPassiveMob(event.getEntityType()) ? 1.5F : 3.75F;
		
		s.addExperience(this, (int) (mult * event.getDamage()));
		return true;
	}
	
	private boolean isPassiveMob(EntityType e) {
		switch(e) {
		case AXOLOTL:
		case BAT:
		case BEE:
		case CAT:
		case CHICKEN:
		case COD:
		case COW:
		case DOLPHIN:
		case DONKEY:
		case FOX:
		case HORSE:
		case IRON_GOLEM:
		case LLAMA:
		case MULE:
		case MUSHROOM_COW:
		case OCELOT:
		case PANDA:
		case PARROT:
		case PIG:
		case POLAR_BEAR:
		case PUFFERFISH:
		case RABBIT:
		case SALMON:
		case SHEEP:
		case SKELETON_HORSE:
		case SNOWMAN:
		case SQUID:
		case GLOW_SQUID:
		case TRADER_LLAMA:
		case TROPICAL_FISH:
		case TURTLE:
		case VILLAGER:
		case WANDERING_TRADER:
		case WOLF:
		case ZOMBIE_HORSE:
			return true;
		default:
			return false;
		}
	}
}
