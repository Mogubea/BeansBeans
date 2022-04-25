package me.playground.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.playground.civilizations.Civilization;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Maybe remove in the future as it only ever interacts with PlayerProfile anyway.
 */
public class TeamManager {
	
	public static TeamManager instance;
	private final Main plugin;
	
	public TeamManager(Main plugin) {
		this.plugin = plugin;
	}
	
	public void initScoreboard(Player p) {
		createScoreboard(p);
		loadTeamsFor(p);
		updateTeam(p);
		
		if (PlayerProfile.from(p).isSettingEnabled(PlayerSetting.SHOW_SIDEBAR))
			updateSidebar(p);
	}
	
	private Scoreboard createScoreboard(Player p) {
		Scoreboard playerBoard = plugin.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = playerBoard.registerNewObjective("showhealth", "health", Component.text("\u00a7c\u2764"));
		obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
		obj.getScore(p).setScore((int)p.getHealth());
		p.setScoreboard(playerBoard);
		return playerBoard;
	}
	
	/**
	 * Send an update to all online players to update their team information about this player.
	 */
	public void updateTeam(Player p) {
		final PlayerProfile pp = PlayerProfile.from(p);
		final Component prefix = Component.empty().append(pp.isAFK() ? Component.text("[AFK] ", NamedTextColor.GRAY) : Component.empty()).append(pp.isRank(Rank.MODERATOR) ? Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()) : Component.empty());
		final Component suffix = pp.isRank(Rank.PLEBEIAN) ? Component.text(" \u272d", pp.getDonorRank().getRankColour()) : Component.empty();
		final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());
		
		// This is required due to how scoreboards function per player
		// Team colouration is exclusive per scoreboard and must be redefined for every single player's scoreboard.
		// It is uncertain how memory intensive this can become the more players get online.. But thankfully Scoreboards are WeakReferenced.
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			String id = "id" + pp.getId() + "-" + PlayerProfile.from(player).getId();
			Team team = player.getScoreboard().getTeam(id);
			if (team == null) team = player.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(p.getName()))
				team.addEntry(p.getName());
			
			team.color(color);
			team.prefix(prefix);
			team.suffix(suffix);
		});
		
		// Cannot use p.teamDisplayName()
		p.playerListName(prefix.append(pp.getColouredName()).append(suffix));
	}
	
	/**
	 * Generate a team for each of the currently online players
	 */
	private void loadTeamsFor(Player p) {
		plugin.getServer().getOnlinePlayers().forEach((player) -> {
			final PlayerProfile pp = PlayerProfile.from(player);
			final Component prefix = Component.empty().append(pp.isAFK() ? Component.text("[AFK] ", NamedTextColor.GRAY) : Component.empty()).append(pp.isRank(Rank.MODERATOR) ? Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()) : Component.empty());
			final Component suffix = pp.isRank(Rank.PLEBEIAN) ? Component.text(" \u272d", pp.getDonorRank().getRankColour()) : Component.empty();
			final NamedTextColor color = NamedTextColor.nearestTo(pp.getNameColour());
			
			String id = "id" + pp.getId() + "-" + PlayerProfile.from(p).getId();
			Team team = p.getScoreboard().getTeam(id);
			if (team == null) team = p.getScoreboard().registerNewTeam(id);
			if (!team.hasEntry(player.getName()))
				team.addEntry(player.getName());
			
			team.color(color);
			team.prefix(prefix);
			team.suffix(suffix);
		});
	}
	
	public void hideSidebar(Player p) {
		Objective obj = p.getScoreboard().getObjective("id" + PlayerProfile.from(p).getId() + "-side");
		if (obj != null) obj.setDisplaySlot(null);
	}
	
	public void showSidebar(Player p) {
		Objective obj = p.getScoreboard().getObjective("id" + PlayerProfile.from(p).getId() + "-side");
		if (obj != null) obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	// TODO: micro optimize and neaten up
	public void updateSidebar(Player p) {
		PlayerProfile pp = PlayerProfile.from(p);
		
		Scoreboard playerBoard = p.getScoreboard();
		
		Objective obj = playerBoard.getObjective("id"+pp.getId()+"-side");
		if (obj != null) obj.unregister();
		
		obj = playerBoard.registerNewObjective("id"+pp.getId()+"-side", "dummy", pp.getColouredName().append(Component.text(pp.isAFK() ? "\u00a77 [AFK]" : "")));
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		Region r = pp.getCurrentRegion();
		String c = "\u00a7" + ChatColor.charOf(pp.getHighestRank().getRankColour());
		String rc = "\u00a7" + ChatColor.charOf(pp.getHighestRank().getRankColour());
		
		List<String> scores = new ArrayList<String>();
		scores.add("      ");
		
		scores.add(c + "\u25D9 \u00a7fRank: " + rc + pp.getHighestRank().getNiceName());
		
		if (pp.getDonorRank() != null) {
			rc = "\u00a7" + ChatColor.charOf(pp.getDonorRank().getRankColour());
			scores.add(c + "\u25D9 \u00a7fSupp: " + rc + pp.getDonorRank().getNiceName());
		}
		
		scores.add(c + "\u25D9 \u00a7fCoins: \u00a76" + df.format(pp.getBalance()) + " \u26C2");
		scores.add(c + "\u25D9 \u00a7fSapphire: \u00a79" + df.format(pp.getSapphire()) + " \u2666");
		
		if (pp.isInCivilization()) {
			scores.add(" ");
			Civilization civ = pp.getCivilization();
			scores.add("\u00a72\u25D9 \u00a7fCiv:\u00a72 " + civ.getName());
			String jc = "\u00a7" + (pp.hasJob() ? ChatColor.charOf(pp.getJob().toComponent().color()) + pp.getJob().getNiceName() : "2Citizen");
			scores.add("\u00a72\u25D9 \u00a7fJob: " + jc);
		}
		
		scores.add("");
		
		if (r != null && !r.isWorldRegion()) {
			String rName = r.getName();
			if (rName.length() > 9)
				rName = rName.substring(0, 10) + "..";
			
			scores.add("\u00a7b\u25D9 \u00a7fRegion: \u00a79" + rName);
			MemberLevel level = r.getMember(p);
			scores.add("\u00a7b\u25D9 \u00a7fPerms: \u00a7b" + level.toString());
		} else {
			scores.add("\u00a7b\u25D9 \u00a7fWorld: \u00a72" + p.getWorld().getName());
		}
		
		// TODO: add a toggle.
		if (pp.isRank(Rank.ADMINISTRATOR)) {
			scores.add("        ");
			scores.add("\u00a78\u25D9 " + getTPS());
		}
		
		int size = scores.size();
		for (int x = -1; ++x < size;)
			obj.getScore(scores.get(x)).setScore(size - x - 1);
	}
	
	private String getTPS() {
		double[] tps = plugin.getServer().getTPS();
		String tpString = "\u00a77TPS:";
		for (int x = -1; ++x < tps.length;)
			tpString += (tps[x]>=19.5 ? "\u00a72" : (tps[x] >= 12 ? "\u00a76" : "\u00a74")) + " " + tpsf.format(tps[x]);
		return tpString;
	}
	
	private final DecimalFormat df = new DecimalFormat("#,###");
	private final DecimalFormat tpsf = new DecimalFormat("#.#");
	
}
