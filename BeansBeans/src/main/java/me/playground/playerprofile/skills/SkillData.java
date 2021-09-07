package me.playground.playerprofile.skills;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class SkillData {
	
	private BossBar xpBar = Bukkit.getServer().createBossBar("lel", BarColor.BLUE, BarStyle.SEGMENTED_10);
	private HashMap<SkillType, SkillInfo> skillData = new HashMap<SkillType, SkillInfo>();
	private int xpSche;
	
	public SkillData(HashMap<SkillType, SkillInfo> skills) {
		this.skillData = skills;
		forceHideBar();
	}
	
	public SkillData() {
	}
	
	public SkillData addXp(SkillType skill, long amount) {
		if (amount < 1)
			return this;
		
		SkillInfo info = getSkillInfo(skill);
		skillData.put(skill, info.addXp(amount));
		
		notifyLevelBar(skill, 40l);
		return this;
	}
	
	public SkillInfo getSkillInfo(SkillType skill) {
		return skillData.getOrDefault(skill, new SkillInfo());
	}
	
	private void updateBar(SkillType skill) {
		SkillInfo info = getSkillInfo(skill);
		
		xpBar.setColor(skill.getBarColour());
		xpBar.setProgress(Math.min(1, info.getPercentageDone()));
		xpBar.setTitle(skill.getDisplayName() + "\u00a78 ("+skill.getColour()+"Level "+info.getLevel()+"\u00a78)\u00a78 - " + skill.getColour() + ((int)(info.getPercentageDone()*100)) + "%");
	}
	
	public void forceHideBar() {
		xpBar.setVisible(false);
	}
	
	public void assignBarPlayer(final Player p) {
		if (!xpBar.getPlayers().contains(p))
			xpBar.addPlayer(p);
	}
	
	public void notifyLevelBar(SkillType skill, long dura) {
		updateBar(skill);
		
		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);
		
		xpBar.setVisible(true);
		
		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BeansBeans"), new Runnable() {
			public void run() {
				forceHideBar();
			}
		}, dura);
	}
	
	public Player getBarPlayer() {
		if (xpBar.getPlayers().size()<1)
			return null;
		return xpBar.getPlayers().get(0);
	}
	
	public int getLevel(SkillType skill) {
		return skillData.get(skill).getLevel();
	}
	
}
