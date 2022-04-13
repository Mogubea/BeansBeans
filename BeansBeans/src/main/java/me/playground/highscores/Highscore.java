package me.playground.highscores;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import me.playground.playerprofile.PlayerProfile;

public abstract class Highscore {
	
	protected LinkedHashMap<Integer, Long> orderedScores;
	protected List<Integer> preservedOrder;
	protected String suffix;
	
	public abstract void updateScores();
	
	public long getScoreOf(int playerId) {
		return orderedScores.getOrDefault(playerId, 0L);
	}
	
	public int getPositionOf(int playerId) {
		int idx = preservedOrder.indexOf(playerId);
		if (idx < 0) return playerId;
		
		return idx + 1;
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
	
	public String getSuffix() {
		return suffix;
	}
	
}
