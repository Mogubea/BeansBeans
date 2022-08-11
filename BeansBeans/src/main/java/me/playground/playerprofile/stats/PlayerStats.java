package me.playground.playerprofile.stats;

import java.util.HashMap;

import me.playground.playerprofile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

public class PlayerStats {
	
	final private PlayerProfile profile;
	final private HashMap<StatType, HashMap<String, DirtyInteger>> stats = new HashMap<>();
	
	public PlayerStats(@NotNull PlayerProfile profile) {
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
		return di.getValue();
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
