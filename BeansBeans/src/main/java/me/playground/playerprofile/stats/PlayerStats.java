package me.playground.playerprofile.stats;

import java.util.HashMap;
import java.util.List;

import me.playground.playerprofile.PlayerProfile;
import me.playground.skills.Milestone;
import me.playground.skills.MilestoneManager;
import org.jetbrains.annotations.NotNull;

public class PlayerStats {
	
	private final PlayerProfile profile;
	private final MilestoneManager milestoneManager;
	private final HashMap<StatType, HashMap<String, DirtyInteger>> stats = new HashMap<>();
	
	public PlayerStats(@NotNull PlayerProfile profile, @NotNull MilestoneManager manager) {
		this.milestoneManager = manager;
		this.profile = profile;
		final StatType[] types = StatType.values();
		for (StatType type : types)
			stats.put(type, new HashMap<>());
	}
	
	public int getStat(@NotNull StatType type, @NotNull String name) {
		if (!stats.get(type).containsKey(name)) 
			return 0;
		return stats.get(type).get(name).getValue();
	}
	
	public void setStat(@NotNull StatType type, @NotNull String name, int value) {
		setStat(type, name, value, true);
	}
	
	public void setStat(@NotNull StatType type, @NotNull String name, int value, boolean dirty) {
		HashMap<String, DirtyInteger> ack = stats.get(type);
		
		boolean put = !ack.containsKey(name);
		DirtyInteger di = put ? new DirtyInteger(value).setDirty(dirty) : ack.get(name).setValue(value, dirty);
		if (put) 
			ack.put(name, di);

		// We have this require the dirty flag due to this method being called before Milestones are initialised.
		if (dirty)
			flagMilestoneUpdates(new StatCombo(type, name));
	}
	
	/**
	 * Adds the desired amount to the stat.
	 * @return the new value
	 */
	public int addToStat(@NotNull StatType type, @NotNull String name, int add, boolean pokeAFK) {
		if (pokeAFK)
			profile.pokeAFK();
		return addToStat(type, name, add);
	}
	
	/**
	 * Adds the desired amount to the stat.
	 * @return the new value
	 */
	public int addToStat(@NotNull StatType type, @NotNull String name, int add) {
		HashMap<String, DirtyInteger> ack = stats.get(type);
		
		boolean put = !ack.containsKey(name);
		DirtyInteger di = put ? new DirtyInteger(add).setDirty(true) : ack.get(name).addToValue(add);
		if (put) 
			ack.put(name, di);

		flagMilestoneUpdates(new StatCombo(type, name));
		return di.getValue();
	}

	private void flagMilestoneUpdates(StatCombo combo) {
		List<Milestone> milestones = milestoneManager.getMilestones(combo);
		if (milestones.isEmpty()) return;
		int size = milestones.size();

		for (int x = -1; ++x < size;)
			profile.getSkills().flagMilestoneUpdate(milestones.get(x), profile.isWatching(milestones.get(x)));
	}


	@NotNull
	public HashMap<StatType, HashMap<String, DirtyInteger>> getMap() {
		return stats;
	}

	@NotNull
	public PlayerProfile getProfile() {
		return profile;
	}
	
}
