package me.playground.main;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class TeamManager {
	
	public static TeamManager instance;
	private final Scoreboard scoreboard;
	
	public TeamManager() {
		this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		if (scoreboard.getObjective("showhealth") == null) {
			Objective o = scoreboard.registerNewObjective("showhealth", "health", Component.text("\u00a7c\u2764"));
			o.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}
	
	public void updatePlayerTeam(PlayerProfile pp) {
		pp.getPlayer().setScoreboard(scoreboard);
		
		Team team = scoreboard.getTeam("id"+pp.getId());
		Component prefix = pp.isAFK() ? Component.text("[AFK] ", NamedTextColor.GRAY) : Component.empty();
		
		if (team == null)
			team = scoreboard.registerNewTeam("id"+pp.getId());
		
		team.color(NamedTextColor.nearestTo(pp.getNameColour()));
		if (pp.isRank(Rank.MODERATOR))
			prefix = prefix.append(Component.text("\u24E2 ", Rank.MODERATOR.getRankColour()));
		
		if (!team.hasEntry(pp.getPlayer().getName()))
			team.addEntry(pp.getPlayer().getName());
		
		team.prefix(prefix);
	}
	
	public Team getTeam(PlayerProfile pp) {
		return scoreboard.getTeam("id"+pp.getId());
	}
	
}
