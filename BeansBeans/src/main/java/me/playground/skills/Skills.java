package me.playground.skills;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import org.jetbrains.annotations.NotNull;

public class Skills {

	private static final String[] levelTitles = {"F-", "F", "F+", "E-", "E", "E+", "D-", "D", "D+", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+", "S-", "S", "S+"};

	private final PlayerProfile profile;
	private final BossBar xpBar = Bukkit.getServer().createBossBar("Default Bar", BarColor.BLUE, BarStyle.SEGMENTED_10);
	private final Map<Skill, SkillInfo> skills;
	private Skill highestSkill;
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

	/**
	 * Grab the mean level across all skills.
	 * @return the mean level of all the skills.
	 */
	public double getAverageLevel() {
		if (averageGradeDirty) {
			double totalSkillLevel = 0;
			List<Skill> skills = Skill.getRegisteredSkills();

			for (Skill skill : skills) {
				if (highestSkill == null)
					highestSkill = skill;
				else if (getLevel(highestSkill) < getLevel(skill))
					highestSkill = skill;

				totalSkillLevel += getLevel(skill);
			}

			averageGrade = totalSkillLevel / skills.size();
			this.averageGradeDirty = false;
		}
		return averageGrade;
	}

	/**
	 * Grab the average grade across all skills.
	 * @return the mean grade of all the skills.
	 */
	public String getAverageGrade() {
		if (getAverageLevel() < 0) return levelTitles[0];
		if (getAverageLevel() >= levelTitles.length) return levelTitles[levelTitles.length-1];
		return levelTitles[(int) getAverageLevel()];
	}

	@NotNull
	public Skill getBestSkill() {
		if (highestSkill == null) {
			averageGradeDirty = true;
			getAverageLevel();
		}

		return highestSkill;
	}

	/**
	 * Fire the Skill Event for the specified Skills.
	 * @return true if something actually happened
	 */
	public boolean doSkillEvents(@NotNull Event e, @NotNull Skill...skills) {
		if (e instanceof Cancellable c && c.isCancelled()) return false;

		for (Skill sk : skills)
			if (sk.doSkillEvent(this, e)) return true;
		return false;
	}
	
	/**
	 * Add experience to the specified Skill
	 */
	public void addExperience(@NotNull Skill skill, int exp) {
		if (exp == 0) return;

		int lvl = getLevel(skill);
		skills.get(skill).addXP(exp);

		if (!getProfile().isOnline()) return;

		if (getLevel(skill) > lvl) { // On Level Up
			notifyingLevelUp = true;
			averageGradeDirty = true;
			getProfile().getPlayer().playSound(getProfile().getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.75F, 1F);
			notifyLevelUp(skill);
			Bukkit.getOnlinePlayers().forEach(player -> {
				if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_LEVEL_UP_MESSAGES)) {
					player.sendMessage(Component.text("\u00a76\u2605 ").append(getProfile().getComponentName()).append(Component.text("\u00a77 just reached ")
							.append(Component.text("\u00a7lGrade " + getSkillInfo(skill).getGrade(), skill.getColour()).append(Component.text("\u00a77 in ")).append(skill.toComponent()))));
				}
			});

			Main.getInstance().getDiscord().sendChatBroadcast(":star: **" + getProfile().getDisplayName() + "** just reached **Grade " + getSkillInfo(skill).getGrade() + "** in ** " + skill.getName() + "**!");
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
		averageGradeDirty = true;
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

	/**
	 * Send the skill boss bar to the player
	 * @param skill The skill being displayed. This impacts the colour, text etc.
	 * @param dura The length of time to be displayed for.
	 */
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

	/**
	 * Send the level up skill boss bar to the player
	 * @param skill The skill being displayed.
	 */
	private void notifyLevelUp(Skill skill) {
		SkillInfo info = getSkillInfo(skill);
		xpBar.setColor(skill.getBarColour());
		xpBar.setProgress(1);

		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);

		xpBar.setVisible(true);

		AtomicInteger rotation = new AtomicInteger();
		int eck = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			rotation.set((rotation.get() + 1) >= colRotation.length ? 0 : rotation.get() + 1);
			xpBar.setTitle("\u00a7" + skill.getColourCode() + skill.getNameWithIcon() + "\u00a78 (\u00a7"+skill.getColourCode()+info.getGrade()+"\u00a78)\u00a78 | \u00a7b" + "+" + dff.format(recentAccumulation) + " XP\u00a78 | \u00a7"+colRotation[rotation.get()] + "\u00a7l GRADE UP! ");
		}, 0L, 5L);

		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideBossBar();
			notifyingLevelUp = false;
			Bukkit.getScheduler().cancelTask(eck);
		}, 120);
	}

	@NotNull
	public SkillInfo getSkillInfo(Skill skill) {
		SkillInfo info = skills.get(skill);
		if (info == null) {
			info = new SkillInfo();
			skills.put(skill, info);
		}
		return info;
	}
	
	private final static DecimalFormat dff = new DecimalFormat("#,###");
	private final static DecimalFormat df = new DecimalFormat("#.##");
	
}
