package me.playground.celestia.logging;

import org.bukkit.entity.Player;

import me.playground.data.Datasource;
import me.playground.playerprofile.PlayerProfile;
import me.playground.voting.Vote;

/**
 * Simple class to forward logs to {@link Datasource#logCelestia(CelestiaAction, int, org.bukkit.Location, String)}
 */
public final class Celestia {
	
	public static void logRegionChange(Player player, String information) {
		Datasource.logCelestia(CelestiaAction.REGION_CHANGE, player, player.getLocation(), information);
	}
	
	public static void logVote(Vote vote) {
		PlayerProfile pp = vote.getPlayerProfile();
		int id = pp != null ? pp.getId() : 0;
		
		Datasource.logCelestia(CelestiaAction.VOTE, id, pp.isOnline() ? pp.getPlayer().getLocation() : null, vote.getService().getServiceName() + " (" + vote.getAddress() + ")");
	}
	
	public static void logModify(int profileId, String information) {
		Datasource.logCelestia(CelestiaAction.MODIFY, profileId, null, information);
	}
	
}
