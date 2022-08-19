package me.playground.punishments;

import me.playground.data.Dirty;
import me.playground.playerprofile.PlayerProfile;
import org.bukkit.OfflinePlayer;

import java.time.Instant;
import java.util.UUID;

public class Punishment implements Dirty {

    private final int id;
    private final int punisherId;
    private final Type type;
    private final Category category;

    // Multiple things can be punished at once.
    private UUID punishedUUID; // Kick Mute and Ban from the Server
    private long punishedDiscord; // Kick Mute and Ban from the Discord
    private int punishedWebId; // Mute from posting anything

    private Instant punishmentStart;
    private Instant punishmentEnd;

    private String reason;

    private boolean isEnabled;
    private boolean dirty;

    protected Punishment(PunishmentManager manager, int id, int punisherId, Type type, Category category, UUID punishedUUID, long punishedDiscord, int punishedWebId, Instant punishmentStart, Instant punishmentEnd, String reason) {
        this.id = id;
        this.punisherId = punisherId;
        this.type = type;
        this.category = category;
        this.punishedUUID = punishedUUID;
        this.punishedDiscord = punishedDiscord;
        this.punishedWebId = punishedWebId;
        this.punishmentStart = punishmentStart;
        this.punishmentEnd = punishmentEnd;
        this.reason = reason;
    }

    public Instant getPunishmentEnd() {
        return punishmentEnd;
    }

    public void setPunishmentEnd(long duration) {
        this.punishmentEnd = Instant.ofEpochMilli(punishmentStart.toEpochMilli() + duration);
        this.dirty = true;
    }

    public PlayerProfile getPunishedProfile() {
        if (punishedUUID != null)
            return PlayerProfile.from(punishedUUID);
        if (punishedWebId > 0)
            return PlayerProfile.fromIfExists(punishedWebId);
        return null;
    }

    public UUID getPunishedUUID() {
        return punishedUUID;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}
