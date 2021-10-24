package me.playground.civilizations.jobs;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import me.playground.civilizations.Civilization;
import me.playground.civilizations.structures.Structure;
import me.playground.civilizations.structures.Structures;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class Job {
	private static final Map<String, Job> registeredJobs = new LinkedHashMap<String, Job>();
	
	@Nullable
	public static Job getByName(String name) {
		if (name == null) return null;
		return registeredJobs.get(name.toLowerCase());
	}
	
	public static Collection<Job> getJobs() {
		return registeredJobs.values();
	}
	
	public static final JobGatherer 	GATHERER	 = new JobGatherer();
	public static final JobMiner 		MINER		 = new JobMiner();
	public static final JobLumberjack 	LUMBERJACK	 = new JobLumberjack();
	public static final JobFisherman 	FISHERMAN	 = new JobFisherman();
	
	// Structure Requirements to unlock the Job within a Civilization.
	private final Set<Structures> structureRequirements = new HashSet<Structures>();
	private int structureCount;
	
	// Skill Requirements for a Citizen to be able to use the Job.
	private final TreeMap<SkillType, Short> skillRequirements = new TreeMap<SkillType, Short>();
	
	// Payouts
	private final Map<String, Integer> pay = new TreeMap<String, Integer>();
	
	// Job info
	private final Component description;
	private final Component simpleComponent;
	private final String name;
	private TextColor color;
	
	protected Job(String name, int colour, Component jobHoverDetails) {
		this.color = TextColor.color(colour);
		this.simpleComponent = Component.text(name, color).decoration(TextDecoration.ITALIC, false);
		this.description = jobHoverDetails;
		this.name = name.toLowerCase();
		
		registeredJobs.put(this.name, this);
	}
	
	/**
	 * 
	 * @param amountNeeded - The amount of the specified structures needed to unlock the job for Citizens.
	 * @param options - The options the civilization leaders have
	 */
	protected void addStructureRequirements(int amountNeeded, String... options) {
		int size = options.length;
		for (int x = -1; ++x < size;) {
			Structures structure = Structures.getStructure(name);
			if (structure == null) continue;
			this.structureRequirements.add(structure);
		}
		this.structureCount = amountNeeded;
	}
	
	/**
	 * @param amountNeeded - The amount of any structures needed to unlock the job for Citizens.
	 */
	protected void addStructureRequirements(int amountNeeded) {
		this.structureCount = amountNeeded;
	}
	
	protected void addSkillRequirement(SkillType type, int level) {
		this.skillRequirements.put(type, (short) level);
	}
	
	@Nonnull
	public String getName() {
		return this.name;
	}
	
	public boolean canPlayerApply(Player p) {
		PlayerProfile pp = PlayerProfile.from(p);
		if (skillRequirements.isEmpty()) return true;
		for (Entry<SkillType, Short> ent : skillRequirements.entrySet())
			if (pp.getSkillLevel(ent.getKey()) < ent.getValue()) return false;
		
		return true;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public boolean hasUnlocked(Civilization c) {
		if (structureCount <= 0) return true;
		List<Structure> unlocked = c.getUnlockedStructures();
		
		if (structureRequirements.isEmpty())
			return unlocked.size() >= structureCount;
		
		int count = 0;
		for (Structures structure : structureRequirements)
			if (unlocked.contains(structure)) count++;
		
		return count >= structureCount;
	}
	
	@Nonnull
	public Component getDescription() {
		return description;
	}
	
	@Nonnull
	public Component toComponent() {
		return simpleComponent;
	}
	
	@Nonnull
	public Component toComponent(PlayerProfile pp) {
		return simpleComponent.hoverEvent(HoverEvent.showText(simpleComponent.append(description)));
	}
	
	@Nonnull
	public int addPayment(String id, int payout) {
		Integer old = pay.put(id, payout);
		if (old == null) old = 0;
		return old;
	}
	
	@Nonnull
	public int getPay(String id) {
		return pay.getOrDefault(id, 0);
	}
	
	public Map<String, Integer> getPayouts() {
		return pay;
	}
	
}
