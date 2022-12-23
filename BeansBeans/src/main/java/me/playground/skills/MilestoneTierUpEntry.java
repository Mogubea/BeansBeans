package me.playground.skills;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class MilestoneTierUpEntry {

    private final int playerId;
    private final Instant instant;
    private final Milestone milestone;
    private final MilestoneTier tier;

    protected MilestoneTierUpEntry(MilestoneManager manager, int playerId, @NotNull Instant instant, @NotNull Milestone milestone, @NotNull MilestoneTier tier) {
        this.playerId = playerId;
        this.instant = instant;
        this.milestone = milestone;
        this.tier = tier;
        manager.addTierUpEntry(this);
    }

    @NotNull
    protected MilestoneTier getTier() {
        return tier;
    }

    @NotNull
    protected Milestone getMilestone() {
        return milestone;
    }

    @NotNull
    protected Instant getInstant() {
        return instant;
    }

    protected int getPlayerId() {
        return playerId;
    }
}
