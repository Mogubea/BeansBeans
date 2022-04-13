package me.playground.highscores;

import java.util.ArrayList;
import java.util.Collections;

import me.playground.data.Datasource;
import me.playground.playerprofile.skills.SkillType;

public class HighscoreSkills extends Highscore {

	private final SkillType skill;
	
	public HighscoreSkills(SkillType skill) {
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
