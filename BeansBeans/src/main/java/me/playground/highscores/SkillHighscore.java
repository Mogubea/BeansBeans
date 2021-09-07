package me.playground.highscores;

import java.util.ArrayList;
import java.util.Collections;

import me.playground.data.Datasource;
import me.playground.playerprofile.skills.SkillType;

public class SkillHighscore extends Highscore {

	private final SkillType skill;
	
	public SkillHighscore(SkillType skill) {
		super(skill.name() + "_xp");
		this.skill = skill;
	}

	@Override
	public void updateScores() {
		this.orderedScores = Datasource.getSkillHighscores(skill);
		this.preservedOrder = new ArrayList<Integer>(orderedScores.keySet());
		Collections.reverse(preservedOrder);
	}

}
