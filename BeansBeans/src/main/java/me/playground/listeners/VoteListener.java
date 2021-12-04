package me.playground.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import me.playground.celestia.logging.Celestia;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.voting.VoteEvent;
import me.playground.voting.VoteService;

public class VoteListener extends EventListener {

	final private String notifAdv = "beansbeans:advancements/utility/votenotification";
	
	public VoteListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler
	public void onVote(VoteEvent e) {
		if (e.getVote().isValid()) {
			final PlayerProfile pp = e.getVoterProfile();
			final VoteService service = e.getVote().getService();
			
			pp.getStats().addToStat(StatType.VOTING, "votes", 1);
			
			int totalSapphire = service.getSapphireReward();
			int totalCoins = service.getCoinReward();
			
			try {
				long ts = Long.parseLong(e.getVote().getTimeStamp()) * 1000;
				int doy = (int) (ts / 86400000L);
				
				int lastVoteDay = pp.getStat(StatType.VOTING, "lastVoteDay");
				int daysSinceLastVote = doy - lastVoteDay;
				
				if (daysSinceLastVote > 1) { // Streak Broken!
					pp.getStats().setStat(StatType.VOTING, "voteStreak", 0);
				} else if (daysSinceLastVote == 1) { // First vote of the day, continuing a streak, bonus of 1/2/3 sapphire
					pp.getStats().addToStat(StatType.VOTING, "voteStreak", daysSinceLastVote);
					totalSapphire += Math.min(3, (pp.getStat(StatType.VOTING, "voteStreak") + 1) / 2);
				}
				
				pp.getStats().addToStat(StatType.VOTING, "voteStreak", 1); // Add to streak
				pp.getStats().setStat(StatType.VOTING, "lastVoteDay", doy);
			} catch (Exception ex) {
				return;
			}
			
			pp.addToSapphire(totalSapphire);
			pp.addToBalance(totalCoins, "Voting on " + service.getServiceName());
			pp.getStats().addToStat(StatType.VOTING, "sapphireEarned", totalSapphire);
			if (pp.isOnline()) { // Notify them via fake advancement pop up
				pp.grantAdvancement(notifAdv);
				// Need to delay slightly or the toast won't show up
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
					@Override
					public void run() {
						pp.revokeAdvancement(notifAdv);
					}
				}, 2L);
			}
		}
		Celestia.logVote(e.getVote());
	}

}
