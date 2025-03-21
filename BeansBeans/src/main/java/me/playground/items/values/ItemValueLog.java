package me.playground.items.values;

import java.time.Instant;

public class ItemValueLog {

    private final Instant time; // Time of change
    private final String identifier; // item
    private final double oldValue; // Old Value of the item
    private final double newValue; // New Value of the item
    private final int updaterId; // Updater
    private final boolean enforcedChange; // Whether it was an enforced or calculated change.

    protected ItemValueLog(String identifier, double oldValue, double newValue, int updaterId, boolean enforcedChange) {
        this.time = Instant.now();
        this.identifier = identifier;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.updaterId = updaterId;
        this.enforcedChange = enforcedChange;
    }

    public Instant getTime() {
        return time;
    }

    public String getIdentifier() {
        return identifier;
    }

    public double getOldValue() {
        return oldValue;
    }

    public double getNewValue() {
        return newValue;
    }

    public int getUpdaterId() {
        return updaterId;
    }

    public boolean isEnforcedChange() {
        return enforcedChange;
    }
}
