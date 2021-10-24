package me.playground.highscores;

import me.playground.playerprofile.skills.SkillType;

public class Highscores {
	
	public final static int UPDATE_INTERVAL = 1000 * 60 * 15;
	private static int idx = 0;
	
	final public Highscore[] highscores = new Highscore[10];
	
	public Highscores() {
		for (SkillType skill : SkillType.values())
			highscores[idx++] = new HighscoreSkills(skill);
		highscores[idx++] = new HighscoreTotalSkill();
	}
	
	public void updateStoredHighscores() {
		for (int x = 0; x < highscores.length; x++) {
			final Highscore hs = highscores[x];
			hs.updateScores();
		}
	}
	
}
