package me.playground.highscores;

import java.util.HashMap;

import me.playground.playerprofile.skills.SkillType;
import me.playground.playerprofile.stats.StatType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Highscores {
	
	public final static int UPDATE_INTERVAL = 1000 * 60 * 15;
	
	final public HashMap<String, Highscore> highscores = new HashMap<String, Highscore>();
	
	public Highscores() {
		for (SkillType skill : SkillType.values())
			highscores.put(skill.getPlainName() + " XP", new HighscoreSkills(skill));
		highscores.put("Total Skill XP", new HighscoreTotalSkill());
		highscores.put("Highest Playtime", new HighscoreStat(StatType.GENERIC, "playtime"));
	}
	
	public void updateStoredHighscores() {
		highscores.forEach((name, hs) -> {
			hs.updateScores();
		});
	}
	
	public Highscore getHighscore(String name) {
		return highscores.get(name);
	}
	
	public OptionData retrieveDiscordOptionData() {
		OptionData data = new OptionData(OptionType.STRING, "category", "The name of the Highscore Category.", true);
		highscores.forEach((name, hs) -> {
			data.addChoice(name, name);
		});
		return data;
	}
	
}
