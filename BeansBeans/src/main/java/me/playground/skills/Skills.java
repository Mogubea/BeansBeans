package me.playground.skills;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;

public class Skills {
	
	private final PlayerProfile profile;
	private final BossBar xpBar = Bukkit.getServer().createBossBar("Default Bar", BarColor.BLUE, BarStyle.SEGMENTED_10);
	private final Map<Skill, SkillInfo> skills;
	
	public Skills(PlayerProfile profile) {
		this.profile = profile;
		this.skills = new HashMap<Skill, SkillInfo>();
		xpBar.setVisible(false);
	}
	
	public Skills(PlayerProfile profile, Map<Skill, SkillInfo> skills) {
		this.profile = profile;
		this.skills = skills;
		xpBar.setVisible(false);
	}
	
	public PlayerProfile getProfile() {
		return profile;
	}
	
	public int getLevel(Skill skill) {
		return getSkillInfo(skill).getLevel();
	}
	
	public int getLevelExperience(Skill skill) {
		return getSkillInfo(skill).getLevelXP();
	}
	
	public long getTotalExperience(Skill skill) {
		return getSkillInfo(skill).getTotalXP();
	}
	
	public int getSkillPoints(Skill skill) {
		return getSkillInfo(skill).getSkillPoints();
	}
	
	/**
	 * Fire the Skill Event for the specified Skills.
	 * @return true if something actually happened
	 */
	public boolean doSkillEvents(Event e, Skill...skills) {
		for (Skill sk : skills)
			if (sk.doSkillEvent(this, e)) return true;
		return false;
	}
	
	/**
	 * Add experience to the specified Skill
	 */
	public void addExperience(Skill skill, int exp) {
		skills.get(skill).addXP(exp);
		notifyLevelBar(skill, 40L);
	}
	
	/**
	 * Hide the boss bar
	 */
	public void hideBossBar() {
		xpBar.setVisible(false);
	}
	
	/**
	 * Assign the player to the boss bar.
	 */
	public void setBarPlayer() {
		if (!profile.isOnline()) return;
		xpBar.addPlayer(profile.getPlayer());
	}
	
	private int xpSche;
	protected void notifyLevelBar(Skill skill, long dura) {
		SkillInfo info = getSkillInfo(skill);
		
		xpBar.setColor(skill.getBarColour());
		xpBar.setProgress(Math.min(1, info.getLevelProgress()));
		xpBar.setTitle("\u00a7" + skill.getColourCode() + skill.getName() + "\u00a78 (\u00a7"+skill.getColourCode()+"Level "+info.getLevel()+"\u00a78)\u00a78 - \u00a7" + skill.getColourCode() + ((int)(info.getLevelProgress()*100)) + "%");
		
		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);
		
		xpBar.setVisible(true);
		
		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			public void run() {
				hideBossBar();
			}
		}, dura);
	}
	
	public SkillInfo getSkillInfo(Skill skill) {
		return skills.get(skill);
	}
	
}
