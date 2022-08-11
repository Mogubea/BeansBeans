package me.playground.skills;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Event;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;

public class Skills {
	
	private final PlayerProfile profile;
	private final BossBar xpBar = Bukkit.getServer().createBossBar("Default Bar", BarColor.BLUE, BarStyle.SEGMENTED_10);
	private final Map<Skill, SkillInfo> skills;
	private boolean notifyingLevelUp;

	private double averageGrade;
	private boolean averageGradeDirty = true;

	public Skills(PlayerProfile profile) {
		this.profile = profile;
		this.skills = new HashMap<>();
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
	
	public int getPerkLevel(SkillPerk perk) {
		return getSkillInfo(perk.getSkill()).getPerkLevel(perk.getSkillIdx());
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

	public double getAverageGrade() {
		if (averageGradeDirty) {
			double totalSkillLevel = 0;
			for (Skill skill : Skill.getRegisteredSkills())
				totalSkillLevel += getLevel(skill);
		}

		return averageGrade;
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
		int lvl = getLevel(skill);
		skills.get(skill).addXP(exp);

		if (!getProfile().isOnline()) return;

		if (getLevel(skill) > lvl) { // On Level Up
			notifyingLevelUp = true;
			averageGradeDirty = true;
			getProfile().getPlayer().playSound(getProfile().getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.75F, 1F);
			notifyLevelUp(skill);
			Bukkit.getOnlinePlayers().forEach(player -> {
				if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_JOB_MESSAGES)) {
					player.sendMessage(Component.text("\u00a76\u2605 ").append(getProfile().getComponentName()).append(Component.text("\u00a77 just reached ")
							.append(Component.text("\u00a7lGrade " + getSkillInfo(skill).getGrade(), skill.getColour()).append(Component.text("\u00a77 in ")).append(skill.toComponent()))));
				}
			});
		} else if (!notifyingLevelUp) { // Not Leveling Up
			if (lastSkill != skill)
				recentAccumulation = 0;
			lastSkill = skill;
			recentAccumulation += exp;
			notifyLevelBar(skill, 50L);
		} else { // Still Leveling Up
			if (lastSkill == skill)
				recentAccumulation += exp;
		}
	}
	
	/**
	 * Add experience to the specified Skill
	 */
	public void setLevel(Skill skill, int level) {
		if (lastSkill != skill)
			recentAccumulation = 0;
		lastSkill = skill;
		skills.get(skill).setLevel(level);
		notifyLevelBar(skill, 50L);
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
	private int recentAccumulation = 0;
	private Skill lastSkill = null;
	protected void notifyLevelBar(Skill skill, long dura) {
		SkillInfo info = getSkillInfo(skill);
		
		if (profile.isSettingEnabled(PlayerSetting.SKILL_EXPERIENCE_SOUND) && profile.isOnline())
			profile.getPlayer().playSound(profile.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.05F, 1.3F);
		
		xpBar.setColor(skill.getBarColour());
		xpBar.setProgress(Math.min(1, info.getLevelProgress()));
		xpBar.setTitle("\u00a7" + skill.getColourCode() + skill.getNameWithIcon() + "\u00a78 (\u00a7"+skill.getColourCode()+info.getGrade()+"\u00a78)\u00a78 | \u00a7b" + "+" + dff.format(recentAccumulation) + " XP\u00a78 | \u00a7" +
		skill.getColourCode() + dff.format(info.getLevelXP()) + "\u00a77/\u00a7" + skill.getColourCode() + dff.format(info.getXPRequirement()) + "\u00a78 (\u00a77" + df.format((info.getLevelProgress()*100.0)) + "%\u00a78)");
		
		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);
		
		xpBar.setVisible(true);
		
		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideBossBar();
			recentAccumulation = 0;
		}, dura);
	}

	private final String[] colRotation = {"c", "e", "a", "b", "d", "f"};
	private void notifyLevelUp(Skill skill) {
		SkillInfo info = getSkillInfo(skill);
		xpBar.setColor(skill.getBarColour());
		xpBar.setProgress(1);

		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);

		xpBar.setVisible(true);

		AtomicInteger rotation = new AtomicInteger();
		int eck = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			rotation.getAndAdd(rotation.get() >= colRotation.length ? -colRotation.length : 1);
			xpBar.setTitle("\u00a7" + skill.getColourCode() + skill.getNameWithIcon() + "\u00a78 (\u00a7"+skill.getColourCode()+info.getGrade()+"\u00a78)\u00a78 | \u00a7b" + "+" + dff.format(recentAccumulation) + " XP\u00a78 | \u00a7"+colRotation[rotation.get()] + "\u00a7l GRADE UP! ");
		}, 0L, 5L);

		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideBossBar();
			notifyingLevelUp = false;
			averageGradeDirty = false;
			Bukkit.getScheduler().cancelTask(eck);
		}, 80);
	}
	
	public SkillInfo getSkillInfo(Skill skill) {
		return skills.get(skill);
	}
	
	private final static DecimalFormat dff = new DecimalFormat("#,###");
	private final static DecimalFormat df = new DecimalFormat("#.##");
	
}
