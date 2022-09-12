package me.playground.highscores;

import java.util.ArrayList;
import java.util.Collections;

import me.playground.data.Datasource;
import me.playground.playerprofile.stats.StatType;

public class HighscoreStat extends Highscore {

	protected final StatType type;
	protected final String stat;
	
	public HighscoreStat(StatType type, String stat) {
		super();
		this.type = type;
		this.stat = stat;
	}

	@Override
	public void updateScores() {
		this.orderedScores = Datasource.getStatHighscores(type, stat);
		this.preservedOrder = new ArrayList<>(orderedScores.keySet());
		Collections.reverse(preservedOrder);
	}

}
