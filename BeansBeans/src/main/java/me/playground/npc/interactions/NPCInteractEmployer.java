package me.playground.npc.interactions;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.playground.civilizations.jobs.Job;
import me.playground.gui.BeanGuiConfirm;
import me.playground.npc.NPC;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class NPCInteractEmployer extends NPCInteraction {
	
	protected NPCInteractEmployer() {
		super("employer");
	}
	
	@Override
	public void onInteract(final NPC<?> npc, final Player p) {
		final PlayerProfile pp = PlayerProfile.from(p);
		final Job job = npc.getJob();
		
		if (!pp.isInCivilization()) {
			npc.sendMessage(p, Component.text("I can't hire those who aren't part of a Civilization!"), Sound.ENTITY_VILLAGER_NO);
			return;
		}
		
		if (pp.getJob() == job) {
			int cheque = pp.getStat(StatType.OCCUPATION, "currentPay");
			if (cheque < 250) {
				npc.sendMessage(p, Component.text("Hello again.. Wait, you're trying to cash in your cheque already?! "
						+ "No can do, " + p.getName() + "! You gotta have at least \u00a76250 Coins\u00a7f saved up before cashing it in!"), Sound.ENTITY_VILLAGER_NO);
				npc.spawnParticle(Particle.VILLAGER_ANGRY, 0.25, 0.25, 0.25, 3);
				return;
			}
			
			npc.sendMessage(p, Component.text("Hey, " + p.getName() + "! Looking to cash in your cheque? Alright, let's see here..."));
			
			pp.getStats().setStat(StatType.OCCUPATION, "currentPay", 0);
			pp.getStats().addToStat(StatType.OCCUPATION, "cashedPay", cheque);
			pp.getStats().addToStat(StatType.OCCUPATION, "cashedPay_"+job.getNiceName(), cheque);
			
			// TODO: tax
			pp.addToBalance(cheque, "Pay Cheque for " + job.getNiceName() + " occupation");
			npc.sendMessage(p, Component.text("There you go! After " + pp.getCivilization().getName() + "'s taxes, you've earned yourself \u00a76" + cheque + " Coins\u00a7f!"));
			npc.spawnParticle(Particle.VILLAGER_HAPPY, 0.25, 0.25, 0.25, 6);
			npc.getLocation().getWorld().playSound(npc.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 1F);
			return;
		}
		
		if (pp.onCooldown("changeJob")) {
			npc.sendMessage(p, Component.text("Hey there! It looks like you've changed your job fairly recently, please wait " + Utils.timeStringFromNow(pp.getCooldown("changeJob"))), Sound.ENTITY_VILLAGER_AMBIENT);
			return;
		}
		
		if (!pp.getCivilization().hasUnlocked(job)) {
			npc.sendMessage(p, Component.text("Sorry! It appears that the Civilization of " + pp.getCivilization().getName() + " hasn't unlocked this job yet!"), Sound.ENTITY_VILLAGER_NO);
			return;
		}
		
		if (!job.canPlayerApply(p)) {
			npc.sendMessage(p, Component.text("I'm sorry, you don't fit the qualifications for this job."), Sound.ENTITY_VILLAGER_NO);
			return;
		}
		
		new BeanGuiConfirm(p, Arrays.asList(
						Component.text("\u00a77Confirming will change your occupation to").append(job.toComponent()), 
						Component.text("\u00a7cThere is a 2 hour cooldown between"), 
						Component.text("\u00a7cchanging jobs!"))) {
			@Override
			public void onAccept() {
				// TODO: Cash in cheque and change jobs, tax needs to be done soon.
				if (pp.hasJob()) {
					int cheque = pp.getStat(StatType.OCCUPATION, "currentPay");
					pp.getStats().setStat(StatType.OCCUPATION, "currentPay", 0);
					pp.getStats().addToStat(StatType.OCCUPATION, "cashedPay", cheque);
					pp.getStats().addToStat(StatType.OCCUPATION, "cashedPay_"+pp.getJob().getNiceName(), cheque);
					pp.addToBalance(cheque, "Pay Cheque for " + pp.getJob().getNiceName() + " occupation");
				}
				npc.spawnParticle(Particle.VILLAGER_HAPPY, 0.4, 0.4, 0.4, 6);
				
				if (!pp.setJob(job, false))
					npc.sendMessage(p, Component.text("\u00a7cLooks like there was a problem updating your job.."));
				else {
					Bukkit.getOnlinePlayers().forEach(player -> { 
						if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_JOB_MESSAGES)) { 
							player.sendMessage(Component.text("\u00a7b» ").append(pp.getComponentName()).append(Component.text("\u00a77 is now a ")).append(job.toComponent(pp))); 
							}
						});
				}
			}

			@Override
			public void onDecline() {
				//sendMessage(player, Component.text("Oh, did you have second thoughts?"));
			}
			
		}.openInventory();
	}

	@Override
	public void onInit(NPC<?> npc) {
		npc.setTitle(Component.text("<" + npc.getJob().getNiceName() + " Employer>"), false);
	}

	@Override
	public boolean hasLockedTitle() {
		return true;
	}

}
