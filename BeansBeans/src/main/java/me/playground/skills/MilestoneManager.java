package me.playground.skills;

import me.playground.items.lore.Lore;
import me.playground.main.Main;
import me.playground.playerprofile.stats.StatCombo;
import me.playground.playerprofile.stats.StatType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MilestoneManager {

    private final MilestoneDatasource datasource;

    private final Map<Skill, List<Milestone>> milestonesBySkill = new HashMap<>();
    private final Map<String, Milestone> milestonesById = new HashMap<>();
    private final Map<StatCombo, List<Milestone>> milestonesByCombo = new HashMap<>();

    private final Map<Skill, Integer> maximumMilestoneScore = new HashMap<>();
    private final List<MilestoneTierUpEntry> pendingTierUpEntries = new ArrayList<>();

    public MilestoneManager(Main plugin) {
        clear();

        long then = System.currentTimeMillis();

        datasource = new MilestoneDatasource(plugin, this);
        datasource.loadAll();

        registerIfNonExistent(Milestone.getBuilder(Skill.MINING, "ROCK_AND_STONE", "Rock and Stone").addStats(new StatCombo(StatType.BLOCK_BREAK, Material.STONE.name())).setTierRequirements(100, 500, 2500, 15000, 50000, 250000, 1000000, 5000000).setTierValues(1, 3, 5, 10, 15, 25, 50, 100).setLore(Lore.getBuilder("Mine some plain old Stone.").build()));
        registerIfNonExistent(Milestone.getBuilder(Skill.MINING, "TEST_MILESTONE_2", "Test Milestone 2", MilestoneTier.STONE).addStats(new StatCombo(StatType.BLOCK_BREAK, Material.NETHERRACK.name())).setTierRequirements(2500, 15000, 50000, 250000, 1000000, 5000000).setTierValues(1, 3, 5, 10, 15, 25).setLore(Lore.getBuilder("Mine the rack from the depths of hell.").build()));
        registerIfNonExistent(Milestone.getBuilder(Skill.MINING, "IRON_MAN", "Iron Man").addStats(new StatCombo(StatType.BLOCK_BREAK, Material.IRON_ORE.name()), new StatCombo(StatType.BLOCK_BREAK, Material.DEEPSLATE_IRON_ORE.name())).setTierRequirements(50, 250, 1000, 2500, 5000, 10000, 25000, 50000).setTierValues(1, 3, 5, 10, 15, 25, 50, 100).setLore(Lore.getBuilder("Mine Iron Ore.").build()));


        plugin.getSLF4JLogger().info("Loaded " + milestonesById.size() + " Skill Milestones in " + (System.currentTimeMillis()-then) + "ms");
    }

    /**
     * Register a {@link Milestone} if the database doesn't already have it.
     */
    private void registerIfNonExistent(@NotNull Milestone.MilestoneBuilder milestoneBuilder) {
        Milestone milestone = milestoneBuilder.build();
        if (milestonesById.containsKey(milestone.getIdentifier())) return;

        // Write this milestone to the database
        if (datasource.registerNewMilestone(milestone))
            register(milestone);
    }

    /**
     * Called when loading or creating a new {@link Milestone}. This methods job is to throw the milestone into all the relevant lists.
     */
    protected void register(@NotNull Milestone milestone) {
        if (milestonesById.containsKey(milestone.getIdentifier())) throw new UnsupportedOperationException("A Milestone with this ID already exists!");

        milestonesBySkill.get(milestone.getSkill()).add(milestone);
        milestonesById.put(milestone.getIdentifier(), milestone);
        List<StatCombo> tracked = milestone.getTrackedStats();
        int size = tracked.size();
        for (int x = -1; ++x < size;) {
            if (!milestonesByCombo.containsKey(tracked.get(x)))
                milestonesByCombo.put(tracked.get(x), new ArrayList<>());
            milestonesByCombo.get(tracked.get(x)).add(milestone);
        }

        maximumMilestoneScore.computeIfPresent(milestone.getSkill(), (Skill, integer) -> integer + milestone.getValueOf(MilestoneTier.EMERALD));
    }

    protected void addTierUpEntry(@NotNull MilestoneTierUpEntry entry) {
        this.pendingTierUpEntries.add(entry);
    }

    protected List<MilestoneTierUpEntry> getPendingTierUpEntries() {
        return pendingTierUpEntries;
    }

    protected void clear() {
        this.milestonesByCombo.clear();
        this.milestonesById.clear();
        this.milestonesBySkill.clear();
        this.maximumMilestoneScore.clear();

        for (Skill skill : Skill.getRegisteredSkills()) {
            milestonesBySkill.put(skill, new ArrayList<>());
            maximumMilestoneScore.put(skill, 0);
        }
    }

    /**
     * Grabs an immutable list of {@link Milestone}s that track the provided {@link StatCombo}.
     * @return An immutable list.
     */
    @NotNull
    public List<Milestone> getMilestones(@NotNull StatCombo combo) {
        if (!milestonesByCombo.containsKey(combo)) return List.of();
        return List.copyOf(milestonesByCombo.get(combo));
    }

    /**
     * Grabs an immutable list of {@link Milestone}s that are categorised under the specified {@link Skill}.
     * @return An immutable list.
     */
    @NotNull
    public List<Milestone> getMilestones(@NotNull Skill skill) {
        if (!milestonesBySkill.containsKey(skill)) return List.of();
        return List.copyOf(milestonesBySkill.get(skill));
    }

    public int getMaxMilestoneScore(@NotNull Skill skill) {
        return maximumMilestoneScore.get(skill);
    }

    @Nullable
    public Milestone getMilestone(@NotNull String identifier) {
        return milestonesById.get(identifier);
    }

}
