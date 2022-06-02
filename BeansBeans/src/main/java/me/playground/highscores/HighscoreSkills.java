package me.playground.highscores;

import java.util.ArrayList;
import java.util.Collections;

import me.playground.data.Datasource;
import me.playground.skills.Skill;

public class HighscoreSkills extends Highscore {

	private final Skill skill;
	
	public HighscoreSkills(Skill skill) {
		super();
		this.skill = skill;
		this.suffix = " XP";
	}

	@Override
	public void updateScores() {
		this.orderedScores = Datasource.getSkillHighscores(skill);
		this.preservedOrder = new ArrayList<Integer>(orderedScores.keySet());
		Collections.reverse(preservedOrder);
	}

}
