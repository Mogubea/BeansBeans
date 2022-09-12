package me.playground.highscores;

import java.util.ArrayList;
import java.util.Collections;

import me.playground.data.Datasource;

public class HighscoreTotalSkill extends Highscore {
	
	public HighscoreTotalSkill() {
		super();
		this.suffix = " XP";
	}

	@Override
	public void updateScores() {
		this.orderedScores = Datasource.getHighscoreTotalSkillXp();
		this.preservedOrder = new ArrayList<>(orderedScores.keySet());
		Collections.reverse(preservedOrder);
	}

}
