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
	private final Scoreboard scoreboard;
	
	public TeamManager(Main plugin) {
		this.plugin = plugin;
		this.scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();
		
		if (scoreboard.getObjective("showhealth") == null) {
			Objective o = scoreboard.registerNewObjective("showhealth", "health", Component.text("\u00a7c\u2764"));
			o.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}
	
	public void initScoreboard(Player p) {
		createScoreboard(p);
		updatePlayerTeam(p);
		updatePlayerScoreboard(p);
	}
	
	private Scoreboard createScoreboard(Player p) {
		Scoreboard playerBoard = plugin.getServer().getScoreboardManager().getNewScoreboard();
		p.setScoreboard(playerBoard);
		return playerBoard;
	}
	
	public void updatePlayerTeam(Player p) {
		PlayerProfile pp = PlayerProfile.from(p);
		
		Team team = scoreboard.getTeam("id"+pp.getId());
		Component prefix = pp.isAFK() ? Component.text("[AFK] ", NamedTextColor.GRAY) : Component.empty();
		
		if (team == null)
			team = scoreboard.registerNewTeam("id"+pp.getId());
		
		team.color(NamedTextColor.nearestTo(pp.getNameColour()));
		if (pp.isRank(Rank.MODERATOR))
			prefix = prefix.append(Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()));
		
		if (!team.hasEntry(p.getName()))
			team.addEntry(p.getName());
		team.prefix(prefix);
	}
	
	public void updatePlayerScoreboard(Player p) {
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
		
		int size = scores.size();
		for (int x = -1; ++x < size;)
			obj.getScore(scores.get(x)).setScore(size - x - 1);
	}
	
	public Team getTeam(PlayerProfile pp) {
		return scoreboard.getTeam("id"+pp.getId());
	}
	
	private final DecimalFormat df = new DecimalFormat("#,###");
	
}
