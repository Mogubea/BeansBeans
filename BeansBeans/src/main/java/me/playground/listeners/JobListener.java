package me.playground.listeners;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

import me.playground.civilizations.jobs.IFarmingJob;
import me.playground.civilizations.jobs.IFishingJob;
import me.playground.civilizations.jobs.IHuntingJob;
import me.playground.civilizations.jobs.IMiningJob;
import me.playground.civilizations.jobs.Job;
import me.playground.listeners.events.PlayerRightClickHarvestEvent;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import net.kyori.adventure.text.Component;

/**
 * The actions executed within this class could easily be done in appropriate listener classes like {@link BlockListener} and {@link PlayerListener}
 * but for the sake of readability and debug ability of the server project, this is better, even if it's not the most optimal route taken.
 * Furthermore, it creates a sense of independence for the {@link Job} system.
 */
@Deprecated
public class JobListener extends EventListener {

	public JobListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.getBlock().hasMetadata("placed")) return;
		
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		Job job = pp.getJob();
		
		if (job == null || !(job instanceof IMiningJob)) return;
		
		doJobPay(pp, job, job.getPay(e.getBlock().getType().name()));
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onMobKill(EntityDeathEvent e) {
		if (e.getEntity().getKiller() == null) return;
		PlayerProfile pp = PlayerProfile.from(e.getEntity().getKiller());
		Job job = pp.getJob();
		
		if (job == null || !(job instanceof IHuntingJob)) return;
		
		doJobPay(pp, job, job.getPay(e.getEntityType().name()));
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(PlayerFishEvent e) {
		if (e.getState() != State.CAUGHT_FISH) return;
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		Job job = pp.getJob();
		
		if (job == null || !(job instanceof IFishingJob)) return;
		
		doJobPay(pp, job, job.getPay(((Item)e.getCaught()).getItemStack().getType().name()));
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCropHarvest(PlayerRightClickHarvestEvent e) {
		PlayerProfile pp = e.getProfile();
		Job job = pp.getJob();
		
		if (job == null || !(job instanceof IFarmingJob)) return;
		
		doJobPay(pp, job, job.getPay(e.getMaterial().name()));
	}
	
	private void doJobPay(PlayerProfile pp, Job job, int pay) {
		if (pay <= 0) return;
		int newTotal = pp.getStats().addToStat(StatType.OCCUPATION, "currentPay", pay);
		pp.getPlayer().sendActionBar(job.toComponent().append(Component.text("\u00a77 Pay Cheque: \u00a76" + newTotal + " \u00a77(\u00a7a+"+pay+"\u00a77)")));
	}
	
}
