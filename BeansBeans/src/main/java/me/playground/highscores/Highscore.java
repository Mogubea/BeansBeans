package me.playground.highscores;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import me.playground.playerprofile.PlayerProfile;

public abstract class Highscore {
	
	protected LinkedHashMap<Integer, Long> orderedScores;
	protected List<Integer> preservedOrder;
	
	final protected String name;
	
	public Highscore(String name) {
		this.name = name;
	}
	
	public abstract void updateScores();
	
	public long getScoreOf(int playerId) {
		return orderedScores.get(playerId);
	}
	
	public int getPositionOf(int playerId) {
		return (preservedOrder.indexOf(playerId)+1);
	}
	
	public int getPositionOf(UUID uuid) {
		return getPositionOf(PlayerProfile.getDBID(uuid));
	}
	
	public int getSize() {
		return preservedOrder.size();
	}
	
	public LinkedHashMap<Integer, Long> getMap() {
		return orderedScores;
	}
	
	public List<Integer> getOrder() {
		return preservedOrder;
	}
	
}
