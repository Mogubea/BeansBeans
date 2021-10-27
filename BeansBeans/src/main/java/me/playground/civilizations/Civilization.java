package me.playground.civilizations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;

import me.playground.civilizations.jobs.Job;
import me.playground.civilizations.structures.Structure;
import me.playground.civilizations.structures.Structure.Status;
import me.playground.playerprofile.PlayerProfile;
import me.playground.civilizations.structures.Structures;
import net.kyori.adventure.text.Component;

public class Civilization {
	private static final LinkedHashMap<String, Civilization> civilizations = new LinkedHashMap<String, Civilization>();
	private static final HashMap<Integer, Civilization> civilizationsById = new HashMap<Integer, Civilization>();
	
	public static Civilization getByName(String name) {
		return civilizations.get(name.toLowerCase());
	}
	
	public static Civilization getCivilization(int id) {
		return civilizationsById.get(id);
	}
	
	public static Collection<Civilization> getCivilizations() {
		return civilizations.values();
	}
	
	public static int size() {
		return civilizations.size();
	}
	
	private final int id;
	private final int founderId;
	
	private final Map<Integer, CitizenTier> citizens = new HashMap<Integer, CitizenTier>();
	private final Map<Integer, Long> treasuryParticipants = new HashMap<Integer, Long>();
	private final List<Structure> structures = new ArrayList<Structure>();
//	private final Map<Job, Float> jobTax = new HashMap<Job, Float>();
	
	// To save having to check the structures every single time someone checks if the job is available.
	// These checks will instead go off whenever a new Structure is APPROVED or when Civilizations are loaded.
	private final Set<Job> unlockedJobs = new HashSet<Job>();
	
//	private Set<Region> territory = new HashSet<Region>();
	
	private long balance;
	private String name;
	private Location spawn;
	
	private boolean dirty;
	
	public Civilization(int id, int founderId, String name, long balance) {
		this.id = id;
		this.founderId = founderId;
		this.name = name;
		this.balance = balance;
		
		civilizations.put(name.toLowerCase(), this);
		civilizationsById.put(getId(), this);
	}
	
	@Nonnull
	public boolean hasSpawn() {
		return spawn != null && spawn.getWorld() != null;
	}
	
	@Nullable
	public Location getSpawn() {
		return spawn;
	}
	
	public void setSpawn(Location location) {
		this.spawn = location;
		setDirty(true);
	}
	
	@Nonnull
	public Map<Integer, CitizenTier> getCitizens() {
		return citizens;
	}
	
	public CitizenTier getCitizen(int playerId) {
		return citizens.get(playerId);
	}
	
	public void addCitizen(int playerId) {
		addCitizen(playerId, CitizenTier.CITIZEN);
	}
	
	public void addCitizen(int playerId, CitizenTier tier) {
		PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
		if (pp.isInCivilization() && pp.getCivilization() != this)
			pp.getCivilization().kickCitizen(pp.getId());
		
		pp.setCivilization(this);
		citizens.put(playerId, CitizenTier.CITIZEN);
		setDirty(true);
	}
	
	public void kickCitizen(int playerId) {
		PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
		if (pp.isOnline())
			pp.getPlayer().sendMessage(Component.text("\u00a77You were kicked from ").append(this.toComponent()).append(Component.text("\u00a77.")));
		pp.setCivilization(null);
		setDirty(true);
	}
	
	@Nonnull
	public boolean isCitizen(int playerId) {
		return citizens.containsKey(playerId);
	}
	
	@Nonnull
	public long getTreasury() {
		return balance;
	}
	
	public void addToTreasury(long amount) {
		this.balance+= amount;
		setDirty(true);
	}
	
	@Nonnull
	public String getName() {
		return name;
	}
	
	@Nonnull
	public int getId() {
		return id;
	}
	
	@Nonnull
	public int getFounderId() {
		return founderId;
	}
	
	/**
	 * trolled.
	 */
	@Nonnull
	@SuppressWarnings("unlikely-arg-type")
	public boolean isStructureUnlocked(Structures type) {
		return structures.contains(type);
	}
	
	/**
	 * Unlock a {@link Structure} illegitimately for the {@link Civilization}.
	 */
	public void unlockStructure(Structures type) {
		if (unlockStructure(type, 0)) return;
		
		structures.add(new Structure(this, type, spawn, 0, 0, 0, Status.APPROVED));
		checkUnlocks();
		setDirty(true);
	}
	
	/**
	 * Unlock a {@link Structure} legitimately for the {@link Civilization}.
	 */
	@Nonnull
	public boolean unlockStructure(Structures type, int reviewerId) {
		int size = structures.size();
		
		// loop existing structures to find if an entry already exists.
		for (int x = -1; ++x < size;) {
			Structure structure = structures.get(x);
			if (structure.getType() == type) {
				structure.approve(reviewerId);
				checkUnlocks();
				setDirty(true);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return Returns all {@link Structure}s in this {@link Civilization} that have a {@link Status}.
	 */
	@Nonnull
	public List<Structure> getStructures() {
		return structures;
	}
	
	/**
	 * @return Creates a list returning all of the UNLOCKED {@link Structure}s within this {@link Civilization}.
	 */
	@Nonnull
	public List<Structure> getUnlockedStructures() {
		List<Structure> str = new ArrayList<Structure>();
		structures.forEach((structure) -> {
			if (structure.isUnlocked())
				str.add(structure);
		});
		return str;
	}
	
	@Nonnull
	public Map<Integer, Long> getTreasuryParticipants() {
		return treasuryParticipants;
	}
	
	@Nonnull
	public Set<Job> getUnlockedJobs() {
		return unlockedJobs;
	}
	
	@Nonnull
	public boolean hasUnlocked(Job job) {
		return this.unlockedJobs.contains(job);
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	@Nonnull
	public boolean isDirty() {
		return dirty;
	}
	
	@Nonnull
	public Component toComponent() {
		return Component.text(name);
	}
	
	/**
	 * Refreshes all of the {@link Civilization}'s unlocks.
	 * This should be run as little as possible for optimisation sake.
	 */
	public void checkUnlocks() {
		// Job Unlocks "Cache"
		Collection<Job> jobs = Job.getJobs();
		for (Job j : jobs)
			if (j.hasUnlocked(this))
				unlockedJobs.add(j);
	}
	
}
