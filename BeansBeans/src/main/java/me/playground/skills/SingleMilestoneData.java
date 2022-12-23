package me.playground.skills;

import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.DirtyInteger;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

public class SingleMilestoneData {

    private final DirtyInteger statValue;
    private MilestoneTier tier;
    private final Map<MilestoneTier, Instant> tierUpTimes = new TreeMap<>();

    public SingleMilestoneData(PlayerProfile pp, Milestone milestone) {
        this.statValue = new DirtyInteger(milestone.getStatOf(pp));
        this.tier = milestone.getTierOf(pp);
    }

    /**
     * Get the current tier of this milestone data.
     */
    public MilestoneTier getTier() {
        return tier;
    }

    /**
     * Get the value of this milestone data.
     */
    public int getValue() {
        return statValue.getValue();
    }

    /**
     * Set the value of this milestone data.
     */
    protected void setValue(int value) {
        this.statValue.setValue(value);
    }

    public void addTierUpTime(MilestoneTier tier, Instant tierUpTime) {
        tierUpTimes.put(tier, tierUpTime);
        if (this.tier.ordinal() < tier.ordinal())
            this.tier = tier;
    }

    /**
     * Grabs the {@link Instant} when the player upgraded this milestone to the specified {@link MilestoneTier}.
     * @return the Instant or null.
     */
    @Nullable
    public Instant getTierUnlockTime(MilestoneTier tier) {
        return tierUpTimes.get(tier);
    }

}
