package me.playground.skills;

import me.playground.items.lore.Lore;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatCombo;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Milestone {

    private final String identifier;
    private final String name;
    private final Skill skill;
    private final Lore lore;
    private final int steps;
    private final Component component;
    private final List<StatCombo> stats;
    private final List<MilestoneTier> tiers;
    private final List<Integer> milestoneValues;
    private final List<Integer> milestoneRequirements;

    private Milestone(@NotNull Skill skill, @NotNull String identifier, @NotNull String name, @NotNull List<StatCombo> combo, @NotNull List<Integer> points, @NotNull List<Integer> values, @NotNull MilestoneTier min, @NotNull Lore lore) {
        this.identifier = identifier.toUpperCase();
        this.name = name;
        this.component = Component.text(name, BeanColor.MILESTONE);
        this.skill = skill;
        this.stats = List.copyOf(combo);
        this.milestoneValues = List.copyOf(values);
        this.milestoneRequirements = List.copyOf(points);
        this.lore = lore;
        this.steps = values.size();
        this.tiers = List.of(Arrays.copyOfRange(MilestoneTier.values(), min.ordinal(), min.ordinal() + steps));
    }

    /**
     * Get the total stat value of the provided {@link PlayerProfile}.
     * @return Stat value
     */
    public int getStatOf(PlayerProfile pp) {
        int total = 0;
        int size = stats.size();
        for (int x = -1; ++x < size;)
            total += stats.get(x).getStatOf(pp);
        return total;
    }

    /**
     * Get the milestone tier of this Milestone based on the provided {@link PlayerProfile}'s stats.
     * @return Milestone Tier.
     */
    public MilestoneTier getTierOf(PlayerProfile pp) {
        int value = getStatOf(pp);
        MilestoneTier tier = MilestoneTier.NONE;
        for (int x = -1; ++x < steps;) {
            if (milestoneRequirements.get(x) < value) {
                tier = MilestoneTier.fromOrdinal(getTier().ordinal() + x);
            } else {
                break;
            }
        }
        return tier;
    }

    /**
     * Get the point value of this Milestone at the specified tier.
     * @return The value (0 if too low, max value if too high).
     */
    public int getValueOf(@NotNull MilestoneTier tier) {
        if (tier.ordinal() < tiers.get(0).ordinal()) return 0;
        if (tier.ordinal() >= tiers.get(steps - 1).ordinal()) return milestoneValues.get(milestoneValues.size() - 1);

        return milestoneValues.get(tier.ordinal() - tiers.get(0).ordinal());
    }

    /**
     * Get the requirement to tier up to this milestone tier
     * @return The value (0 if too low, max value if too high).
     */
    public int getRequirementFor(@NotNull MilestoneTier tier) {
        if (tier.ordinal() < tiers.get(0).ordinal()) return 0;
        if (tier.ordinal() >= tiers.get(steps - 1).ordinal()) return milestoneRequirements.get(milestoneRequirements.size() - 1);

        return milestoneRequirements.get(tier.ordinal() - tiers.get(0).ordinal());
    }

    @NotNull
    public MilestoneTier getTier() {
        return tiers.get(0);
    }

    @NotNull
    public MilestoneTier getMaxTier() {
        return tiers.get(tiers.size() - 1);
    }

    @NotNull
    public Lore getLore() {
        return lore;
    }

    @NotNull
    public Skill getSkill() {
        return skill;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Component toComponent() {
        return component;
    }

    @NotNull
    public List<StatCombo> getTrackedStats() {
        return stats;
    }

    /**
     * JSON format of the Milestone's additional attributes such as the stats, points, values etc.
     */
    @NotNull
    public JSONObject getJson() {
        JSONObject obj = new JSONObject();

        // Stats that are being tracked in this Milestone
        JSONArray statsArray = new JSONArray();
        int statSize = stats.size();
        for (int x = -1; ++x < statSize;) {
            StatCombo combo = stats.get(x);
            statsArray.put(new JSONObject().put("category", combo.type().getString()).put("stat", combo.stat()));
        }

        // Tiers and their values
        JSONArray tierArray = new JSONArray();
        for (int x = -1; ++x < steps;) {
            int points = milestoneRequirements.get(x);
            int value = milestoneValues.get(x);
            tierArray.put(new JSONObject().put("requirement", points).put("value", value));
        }

        obj.put("stats", statsArray);
        obj.put("tier", tiers.get(0).getIdentifier());
        obj.put("tierValues", tierArray);
        return obj;
    }

    /**
     * For use when being read from the database
     */
    protected static Milestone fromJson(@NotNull Skill skill, @NotNull String identifier, @NotNull String name, @NotNull Lore lore, @NotNull JSONObject json) {
        List<StatCombo> combo = new ArrayList<>();
        List<Integer> points = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // Minimum Tier
        String minTierString = json.optString("tier");
        final MilestoneTier minimumTier = MilestoneTier.fromIdentifier(minTierString);
        if (minimumTier == MilestoneTier.NONE) throw new JSONException("JSON is missing a valid MilestoneTier.");

        // Stats that are being tracked in this Milestone
        JSONArray statsArray = json.optJSONArray("stats");
        if (statsArray == null || statsArray.isEmpty()) throw new JSONException("JSON is missing a valid \"stats\" array.");
        int size = statsArray.length();

        for (int x = -1; ++x < size;) {
            JSONObject object = statsArray.optJSONObject(x);
            if (object == null) continue;
            String categoryString = object.optString("category");
            StatType category = StatType.fromString(categoryString);
            String stat = object.optString("stat");

            if (category == null) throw new JSONException("Stat category does not exist.");
            if (stat == null || stat.isEmpty()) throw new JSONException("Stat from the provided Category was not provided.");

            combo.add(new StatCombo(category, stat));
        }

        if (combo.isEmpty()) throw new JSONException("JSON has no valid StatCombos.");

        // Tiers and their values
        JSONArray tierArray = json.optJSONArray("tierValues");
        if (tierArray == null || tierArray.isEmpty()) throw new JSONException("JSON is missing a valid \"tierValues\" array.");
        size = tierArray.length();

        for (int x = -1; ++x < size;) {
            JSONObject object = tierArray.optJSONObject(x);
            if (object == null) continue;
            int requirement = object.optInt("requirement");
            int value = object.optInt("value");

            if (requirement <= 0) requirement = 1 + x;
            if (value <= 0) value = 1 + x;

            points.add(requirement);
            values.add(value);
        }

        return new Milestone(skill, identifier, name, combo, points, values, minimumTier, lore);
    }

    /**
     * Build a new Milestone
     * @return MilestoneBuilder
     */
    protected static MilestoneBuilder getBuilder(@NotNull Skill skill, @NotNull String identifier, @NotNull String name) {
        return getBuilder(skill, identifier, name, MilestoneTier.DIRT);
    }

    /**
     * Build a new Milestone
     * @return MilestoneBuilder
     */
    protected static MilestoneBuilder getBuilder(@NotNull Skill skill, @NotNull String identifier, @NotNull String name, @Nullable MilestoneTier minimum) {
        if (minimum == null || minimum == MilestoneTier.NONE)
            minimum = MilestoneTier.DIRT;

        return new MilestoneBuilder(skill, identifier, name, minimum);
    }

    protected static class MilestoneBuilder {

        private final String identifier;
        private final String name;
        private final Skill skill;
        private final List<StatCombo> stats = new ArrayList<>();
        private final List<Integer> milestonePoints = new ArrayList<>();
        private final List<Integer> milestoneValues = new ArrayList<>();
        private final MilestoneTier minimum;
        private Lore lore = Lore.getBuilder("This milestone is a work in progress.").dontFormatColours().build();

        private MilestoneBuilder(@NotNull Skill skill, @NotNull String identifier, @NotNull String name, @NotNull MilestoneTier minimum) {
            this.skill = skill;
            this.identifier = identifier;
            this.name = name;
            this.minimum = minimum;
        }

        /**
         * Sets the required value(s) for upgrading the tier of this milestone.
         * @return The current {@link MilestoneBuilder}.
         * @throws IllegalArgumentException If too many tier requirements are provided.
         */
        @NotNull
        protected MilestoneBuilder setTierRequirements(@NotNull Integer... milestones) {
            int max = (MilestoneTier.values().length - minimum.ordinal());
            if (milestones.length > max) throw new IllegalArgumentException("Too many tier requirements were specified (Max allowed: " + max + ")");
            milestonePoints.clear();
            milestonePoints.addAll(Arrays.asList(milestones));
            return this;
        }

        /**
         * Sets the point value of these Milestone Tiers
         * @return The current {@link MilestoneBuilder}.
         * @throws IllegalArgumentException If too many tier values are provided.
         */
        @NotNull
        protected MilestoneBuilder setTierValues(@NotNull Integer... pointValues) {
            int max = (MilestoneTier.values().length - minimum.ordinal());
            if (pointValues.length > max) throw new IllegalArgumentException("Too many tier values were specified (Max allowed: " + max + ")");
            milestoneValues.clear();
            milestoneValues.addAll(Arrays.asList(pointValues));
            return this;
        }

        /**
         * Sets the stats that are going to create the player's cumulative value for this milestone.
         * @return The current {@link MilestoneBuilder}.
         */
        @NotNull
        protected MilestoneBuilder addStats(@NotNull StatCombo... combos) {
            stats.addAll(Arrays.asList(combos));
            return this;
        }

        /**
         * Set the lore of this {@link Milestone}.
         * @return The current {@link MilestoneBuilder}.
         */
        @NotNull
        protected MilestoneBuilder setLore(@NotNull Lore lore) {
            this.lore = lore;
            return this;
        }

        @NotNull
        protected Milestone build() {
            if (milestoneValues.isEmpty() || milestonePoints.isEmpty()) throw new UnsupportedOperationException("There must be at least 1 Milestone Value and Milestone Point Requirement.");
            if (milestoneValues.size() != milestonePoints.size()) throw new UnsupportedOperationException("The amount of values for a Milestone must equal the amount of requirement milestones.");

            return new Milestone(skill, identifier, name, stats, milestonePoints, milestoneValues, minimum, lore);
        }

    }

}
