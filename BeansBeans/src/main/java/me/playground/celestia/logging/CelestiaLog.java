package me.playground.celestia.logging;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.time.Instant;

public class CelestiaLog {

    private final Instant time; // Time of change
    private final Timestamp timestamp;
    private final int playerId;
    private final CelestiaAction action;
    private final Location location;
    private final String data;

    protected CelestiaLog(int playerId, @NotNull CelestiaAction action, @Nullable Location location, @NotNull String data) {
        this.playerId = playerId;
        this.action = action;
        if (location == null)
            location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        this.location = location;
        this.data = data;
        this.time = Instant.now();
        this.timestamp = Timestamp.from(time);
    }

    @NotNull
    public Instant getTime() {
        return time;
    }

    @NotNull
    protected Timestamp getTimestamp() {
        return timestamp;
    }

    public int getPlayerId() {
        return playerId;
    }

    @NotNull
    public CelestiaAction getAction() {
        return action;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    @NotNull
    public String getData() {
        return data;
    }
}
