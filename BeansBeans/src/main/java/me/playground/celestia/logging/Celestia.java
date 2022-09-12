package me.playground.celestia.logging;

import me.playground.main.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.playground.data.Datasource;
import me.playground.playerprofile.PlayerProfile;
import me.playground.discord.voting.Vote;

/**
 * Simple class to forward logs to
 */
public final class Celestia {

	private static final CelestiaManager manager = Main.getInstance().getCelestiaManager();

	public static void logRegionChange(Player player, String information) {
		PlayerProfile pp = PlayerProfile.from(player);
		manager.log(pp.getId(), CelestiaAction.REGION_CHANGE, player.getLocation(), information);
	}
	
	public static void logVote(Vote vote) {
		PlayerProfile pp = vote.getPlayerProfile();
		int id = pp != null ? pp.getId() : 0;
		Location location = pp != null && pp.isOnline() ? pp.getPlayer().getLocation() : null;

		manager.log(id, CelestiaAction.VOTE, location, vote.getService().getServiceName() + " (" + vote.getAddress() + ")");
	}
	
	public static void logModify(int profileId, String information) {
		manager.log(profileId, CelestiaAction.MODIFY, null, information);
	}
	
}
