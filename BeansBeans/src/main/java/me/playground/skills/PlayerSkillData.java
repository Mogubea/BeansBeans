package me.playground.skills;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.playground.playerprofile.stats.DirtyByte;
import me.playground.utils.ChatColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerSkillData {

	private final PlayerProfile profile;
	private final MilestoneManager milestoneManager;
	private final BossBar skillBar = Bukkit.getServer().createBossBar("Default Bar", BarColor.BLUE, BarStyle.SEGMENTED_10);
	private final BossBar milestoneBar = Bukkit.getServer().createBossBar("Milestone Bar", BarColor.PURPLE, BarStyle.SEGMENTED_10);
	private final Map<Skill, SkillData> skills;
	private Skill highestSkill;
	private Skill favouriteSkill;
	private boolean notifyingLevelUp;

	private double averageGrade;
	private boolean averageGradeDirty = true;

	private final List<Milestone> flaggedForUpdate = new ArrayList<>();

	private final Map<SkillTreeEntry<?>, DirtyByte> treeLevels;
	private final Map<Skill, Integer> cumulativeLevels = new HashMap<>();

	// Milestones
	private final Map<Milestone, SingleMilestoneData> milestoneData;
	private final Map<Skill, Integer> cumulativeMilestoneScore = new HashMap<>();
	
	public PlayerSkillData(@NotNull PlayerProfile profile, @NotNull MilestoneManager manager, @NotNull Map<Skill, SkillData> skills, @NotNull Map<SkillTreeEntry<?>, DirtyByte> treeLevels, @NotNull Map<Milestone, SingleMilestoneData> milestoneData) {
		this.milestoneManager = manager;
		this.profile = profile;
		this.skills = skills;
		this.treeLevels = treeLevels;
		this.milestoneData = milestoneData;
		skillBar.setVisible(false);
		milestoneBar.setVisible(false);

		// Fill up the map with 0s
		for (Skill skill : Skill.getRegisteredSkills()) {
			cumulativeLevels.put(skill, 0);
			cumulativeMilestoneScore.put(skill, 0);
		}

		// Update the map with the cumulative levels
		treeLevels.forEach((skillTreeEntry, dirtyByte) -> cumulativeLevels.computeIfPresent(skillTreeEntry.getSkill(), (skill, old) -> old + dirtyByte.getValue()));

		// Update the milestone map with the cumulative skill points
		milestoneData.forEach(((milestone, singleMilestoneData) -> cumulativeMilestoneScore.computeIfPresent(milestone.getSkill(), (skill, old) -> old + milestone.getValueOf(singleMilestoneData.getTier()))));
	}

	public PlayerSkillData(@NotNull PlayerProfile profile, @NotNull MilestoneManager manager) {
		this(profile, manager, new HashMap<>(), new HashMap<>(), new HashMap<>());
	}

	public int getPerkLevel(SkillTreeEntry<?> perk) {
		return hasPerk(perk) ? treeLevels.get(perk).getValue() : 0;
	}

	public boolean hasPerk(SkillTreeEntry<?> perk) {
		return treeLevels.containsKey(perk);
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
		return Grade.fromLevel((int) getAverageLevel()).toString();
	}

	@NotNull
	public Skill getBestSkill() {
		if (highestSkill == null) {
			averageGradeDirty = true;
			getAverageLevel();
		}

		return highestSkill;
	}

	@Nullable
	public Skill getFavouriteSkill() {
		return favouriteSkill;
	}

	public void setFavouriteSkill(@Nullable Skill favourite) {
		this.favouriteSkill = favourite;
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
		skills.get(skill).addExperience(exp);

		if (!getProfile().isOnline()) return;

		if (getLevel(skill) > lvl) { // On Level Up
			notifyingLevelUp = true;
			averageGradeDirty = true;
			getProfile().getPlayer().playSound(getProfile().getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.75F, 1F);
			notifyLevelUp(skill);
			Bukkit.getOnlinePlayers().forEach(player -> {
				if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_LEVEL_UP_MESSAGES)) {
					player.sendMessage(Component.text("\u00a76\u2605 ").append(getProfile().getComponentName()).append(Component.text("\u00a77 just reached ")
							.append(Component.text("\u00a7lGrade " + getSkillData(skill).getGrade(), skill.getColour()).append(Component.text("\u00a77 in ")).append(skill.toComponent()))));
				}
			});

			Main.getInstance().getDiscord().sendChatBroadcast(":star: **" + getProfile().getDisplayName() + "** just reached **Grade " + getSkillData(skill).getGrade() + "** in **" + skill.getName() + "**!");
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
	public void hideExperienceBar() {
		skillBar.setVisible(false);
	}

	public void hideMilestoneBar() {
		milestoneBar.setVisible(false);
	}

	protected void notifyMilestoneBar(Milestone milestone) {
		MilestoneTier tier = getMilestoneTier(milestone);

		if (!tier.lowerThan(milestone.getMaxTier())) return;
		int progress = getMilestoneProgress(milestone);
		int required = milestone.getRequirementFor(tier.tierUp());
		double percent = Math.min(1, ((double)progress / (double)required));

		milestoneBar.setProgress(percent);
		milestoneBar.setTitle("\u00a79\u2B50 " + milestone.getName() + "\u00a78 | \u00a7" + ChatColor.charOf(tier.getColour()) + "\u00a7l" + tier.getName() + "\u00a78 | \u00a79" + dff.format(progress) + "\u00a77/\u00a79" + dff.format(required) +
				" \u00a78(\u00a77" + df.format(percent * 100) + "%\u00a78)");

		if (milestoneSche != 0)
			Bukkit.getScheduler().cancelTask(milestoneSche);

		milestoneBar.setVisible(true);

		milestoneSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideMilestoneBar();
			recentAccumulation = 0;
		}, 50L);
	}

	
	/**
	 * Assign the player to the boss bar.
	 */
	public void setBarPlayer() {
		if (!profile.isOnline()) return;
		skillBar.addPlayer(profile.getPlayer());
		milestoneBar.addPlayer(profile.getPlayer());
	}
	
	private int xpSche, milestoneSche;
	private int recentAccumulation = 0;
	private Skill lastSkill = null;

	/**
	 * Send the skill boss bar to the player
	 * @param skill The skill being displayed. This impacts the colour, text etc.
	 * @param dura The length of time to be displayed for.
	 */
	protected void notifyLevelBar(Skill skill, long dura) {
		SkillData info = getSkillData(skill);
		
		if (profile.isSettingEnabled(PlayerSetting.SKILL_EXPERIENCE_SOUND) && profile.isOnline())
			profile.getPlayer().playSound(profile.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.05F, 1.3F);
		
		skillBar.setColor(skill.getBarColour());
		skillBar.setProgress(Math.min(1, info.getLevelProgress()));
		skillBar.setTitle("\u00a7" + skill.getColourCode() + skill.getNameWithIcon() + "\u00a78 (\u00a7"+skill.getColourCode()+info.getGrade()+"\u00a78)\u00a78 | \u00a7b" + "+" + dff.format(recentAccumulation) + " XP\u00a78 | \u00a7" +
		skill.getColourCode() + dff.format(info.getLevelExperience()) + "\u00a77/\u00a7" + skill.getColourCode() + dff.format(info.getXPRequirement()) + "\u00a78 (\u00a77" + df.format((info.getLevelProgress()*100.0)) + "%\u00a78)");
		
		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);
		
		skillBar.setVisible(true);
		
		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideExperienceBar();
			recentAccumulation = 0;
		}, dura);
	}

	private final String[] colRotation = {"c", "e", "a", "b", "d", "f"};

	/**
	 * Send the level up skill boss bar to the player
	 * @param skill The skill being displayed.
	 */
	private void notifyLevelUp(Skill skill) {
		SkillData info = getSkillData(skill);
		skillBar.setColor(skill.getBarColour());
		skillBar.setProgress(1);

		if (xpSche != 0)
			Bukkit.getScheduler().cancelTask(xpSche);

		skillBar.setVisible(true);

		AtomicInteger rotation = new AtomicInteger();
		int eck = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), () -> {
			rotation.set((rotation.get() + 1) >= colRotation.length ? 0 : rotation.get() + 1);
			skillBar.setTitle("\u00a7" + skill.getColourCode() + skill.getNameWithIcon() + "\u00a78 (\u00a7"+skill.getColourCode()+info.getGrade()+"\u00a78)\u00a78 | \u00a7b" + "+" + dff.format(recentAccumulation) + " XP\u00a78 | \u00a7"+colRotation[rotation.get()] + "\u00a7l GRADE UP! ");
		}, 0L, 5L);

		xpSche = Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			hideExperienceBar();
			notifyingLevelUp = false;
			Bukkit.getScheduler().cancelTask(eck);
		}, 120);
	}

	@NotNull
	public SkillData getSkillData(Skill skill) {
		SkillData info = skills.get(skill);
		if (info == null) {
			info = new SkillData();
			skills.put(skill, info);
		}
		return info;
	}

	public int getMilestoneScore(@NotNull Skill skill) {
		return cumulativeMilestoneScore.getOrDefault(skill, 0);
	}

	public void flagMilestoneUpdate(@NotNull Milestone milestone, boolean instant) {
		if (instant) {
			updateMilestoneData(milestone);
			justChimed = false;
			justBarred = false;
		} else if (!flaggedForUpdate.contains(milestone)) { // Don't want it in there multiple times.
			flaggedForUpdate.add(milestone);
		}
	}

	/**
	 * Called every 3 seconds in {@link Main} loop.
	 * Only have this function while the player is online, so they don't miss any level up notifications.
 	 */
	public void updatePendingMilestoneData() {
		int size = flaggedForUpdate.size();
		for (int x = size; --x > -1;)
			updateMilestoneData(flaggedForUpdate.get(x));

		justChimed = false;
		justBarred = false;
	}

	private boolean justChimed = false;
	private boolean justBarred = false;

	public void updateMilestoneData(@NotNull Milestone milestone) {
		Player p = profile.getPlayer();

		SingleMilestoneData mData = milestoneData.get(milestone);
		MilestoneTier previous = MilestoneTier.NONE;

		// This should only ever be null if a player has never obtained a tier up for this milestone before.
		if (mData == null) {
			mData = new SingleMilestoneData(profile, milestone);
			milestoneData.put(milestone, mData);
		} else {
			previous = mData.getTier();
			mData.setValue(milestone.getStatOf(profile));
		}

		MilestoneTier newTier = previous.tierUp().ordinal() < milestone.getTier().ordinal() ? milestone.getTier() : previous.tierUp();

		// Check for tier ups
		while (previous.ordinal() < milestone.getMaxTier().ordinal() && milestone.getRequirementFor(newTier) <= mData.getValue()) {
			Instant now = Instant.now();
			mData.addTierUpTime(newTier, now); // Tier up
			new MilestoneTierUpEntry(milestoneManager, profile.getId(), now, milestone, newTier); // Save this tier up to the database

			if (p != null)
				p.sendMessage(Component.text("\u00a79\u00a7l\u2B50 \u00a77You have upgraded").append(milestone.toComponent()).append(Component.text("\u00a77 to ")).append(Component.text(newTier.getName(), newTier.getColour())).append(Component.text("\u00a77 tier!")));

			int valueIncrease = (milestone.getValueOf(newTier)-milestone.getValueOf(previous));

			addEssence(milestone.getSkill(), valueIncrease * 2);
			addExperience(milestone.getSkill(), valueIncrease * 1500);
			profile.addToBalance(valueIncrease * 250, "Milestone: " + milestone.getName() + " to " + newTier + " tier");

			previous = newTier;
			newTier = newTier.tierUp();

			if (p != null && !justChimed) {
				p.playSound(p.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5F, 0.8F);
				p.spawnParticle(Particle.ELECTRIC_SPARK, p.getEyeLocation().add(0.5, -0.4, 0.5), 4, 0.4, 0,4, 0.4);
				justChimed = true;
			}
		}

		// Notify the user of their progress in this milestone.
		if (profile.isWatching(milestone) && !justBarred) {
			notifyMilestoneBar(milestone);
			justBarred = true;
		}

		flaggedForUpdate.remove(milestone);
	}

	/**
	 * @return When the player obtained this milestone at the specific tier.
	 */
	@Nullable
	public Instant getMilestoneTime(@NotNull Milestone milestone, @Nullable MilestoneTier tier) {
		SingleMilestoneData data = milestoneData.get(milestone);
		if (data == null) return null;
		if (tier == null) tier = milestone.getTier();

		return data.getTierUnlockTime(tier);
	}

	public boolean hasMilestone(Milestone milestone) {
		return milestoneData.containsKey(milestone);
	}

	public MilestoneTier getMilestoneTier(Milestone milestone) {
		return milestoneData.containsKey(milestone) ? milestoneData.get(milestone).getTier() : MilestoneTier.NONE;
	}

	public int getMilestoneProgress(Milestone milestone) {
		return milestoneData.containsKey(milestone) ? milestoneData.get(milestone).getValue() : 0;
	}

	public void addEssence(Skill skill, int amount) {
		getSkillData(skill).addEssence(amount);
	}

	@NotNull
	protected Map<SkillTreeEntry<?>, DirtyByte> getPerkLevels() {
		return treeLevels;
	}

	public int getLevel(Skill skill) {
		return getSkillData(skill).getLevel();
	}

	public double getLevelExperience(Skill skill) {
		return getSkillData(skill).getLevelExperience();
	}

	public double getTotalExperience(Skill skill) {
		return getSkillData(skill).getTotalExperience();
	}

	public int getEssence(Skill skill) {
		return getSkillData(skill).getEssence();
	}

	public int getSpentEssence(Skill skill) {
		return getSkillData(skill).getSpentEssence();
	}

	protected int getCumulativePerkLevel(Skill skill) {
		return cumulativeLevels.get(skill);
	}

	public MilestoneManager getMilestoneManager() {
		return milestoneManager;
	}

	@NotNull
	public PlayerProfile getProfile() {
		return profile;
	}
	
	private final static DecimalFormat dff = new DecimalFormat("#,###");
	private final static DecimalFormat df = new DecimalFormat("#.##");

}
