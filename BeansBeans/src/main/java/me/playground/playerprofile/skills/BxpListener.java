package me.playground.playerprofile.skills;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.playground.listeners.EventListener;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.MaterialHelper;

public class BxpListener extends EventListener {
	
	public BxpListener(Main plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		SkillData sd = PlayerProfile.from(e.getPlayer()).getSkills();
		if (!MaterialHelper.isInstantBlock(e.getBlock().getType()))
			sd.addXp(SkillType.BUILDING, 8); // XXX: expand
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onMobAttack(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;
		
		if (e.getDamage() < 1)
			return;
		
		if (!(e.getEntity() instanceof LivingEntity))
			return;
		
		// XXX: Retaliation Perk for Combat
		/*if (e.getDamage() > 2 && e.getEntityType() == EntityType.PLAYER) {
			Player owch = (Player) e.getEntity();
			PlayerProfile opp = PlayerProfile.from(owch);
			if (owch.isBlocking() && opp.getSkillLevel(SkillType.COMBAT)>=10) {
				
			}
		}*/
		
		if (e.getEntityType() == EntityType.ARMOR_STAND)
			return;
		
		double damage = e.getDamage();
		final Entity attackerEnt = e.getDamager();
		//final Entity defenderEnt = e.getEntity();
		Player p = null;
		
		if (attackerEnt instanceof Player) {
			p = (Player) attackerEnt;
		} else if (attackerEnt instanceof Projectile) {
			if (((Projectile)attackerEnt).getShooter() instanceof Player)
				p = (Player) ((Projectile)attackerEnt).getShooter();
		}
		
		if (p == null || p.getGameMode() != GameMode.SURVIVAL)
			return;
		
		final boolean passive = BxpValues.isPassiveMob(e.getEntityType());
		final float mult = passive ? 1.5F : 3.75F;
		
		SkillData sd = PlayerProfile.from(p).getSkills();
		sd.addXp(SkillType.COMBAT,(int)(damage * mult));
		
	}
	
}
