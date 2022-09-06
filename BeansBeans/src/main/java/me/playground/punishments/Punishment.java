package me.playground.punishments;

import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public abstract class Punishment<T> {

    protected final PunishmentManager manager;

    private final int id;
    private final int punisherId;
    private Type type;

    private final int punished;
    private final T punishedIdentifier;

    private final Instant punishmentStart;
    private Instant punishmentEnd;

    private String reason;
    private List<String> notes; // TODO: Allow staff members to add notes to punishments.

    private boolean isEnabled;
    private boolean canAppeal;

    protected Punishment(@NotNull PunishmentManager manager, int id, int punisherId, @NotNull Type type, int punished, @NotNull T punishedIdentifier, @NotNull Instant punishmentStart, @Nullable Instant punishmentEnd, @Nullable String reason, boolean enabled, boolean appealable) {
        this.manager = manager;

        this.id = id;
        this.punisherId = punisherId;
        this.type = type;
        this.punished = punished;
        this.punishedIdentifier = punishedIdentifier;
        this.punishmentStart = punishmentStart;
        this.punishmentEnd = punishmentEnd;
        this.reason = reason;
        this.canAppeal = appealable;
        this.isEnabled = enabled;
    }

    public T getPunishedIdentifier() {
        return punishedIdentifier;
    }

    public Instant getPunishmentStart() {
        return punishmentStart;
    }

    public Instant getPunishmentEnd() {
        return punishmentEnd;
    }

    /**
     * Get whether this {@link Punishment} is intended to last forever or not.
     * @return true if it lasts forever.
     */
    public boolean isPermanent() {
        return getPunishmentEnd() == null;
    }

    /**
     * Get whether this punishment can be appealed or not. This is typically determined by an Administrator or Owner.
     * Otherwise, a punishment is typically appeal-able.
     */
    public boolean canAppeal() {
        return canAppeal;
    }

    /**
     * Set when this {@link Punishment} intends to end.
     * <p>Setting a duration which is in the future will re-enable this {@link Punishment}.
     * @param duration If this is below 0, the {@link Punishment} will be made {@link #isPermanent()}.
     */
    public void setPunishmentEnd(long duration) {
        this.punishmentEnd = duration <= 0 ? null : Instant.ofEpochMilli(punishmentStart.toEpochMilli() + duration);

        // Check if it should still be active or not.
        this.isEnabled = true;
        isActive();

        // Dirty
        setDirty(true);
    }

    /**
     * If this {@link Punishment} has a valid profile target, return it. Otherwise null.
     * @return The punished profile, else null.
     */
    @Nullable
    public PlayerProfile getPunishedProfile() {
        return PlayerProfile.fromIfExists(punished);
    }

    /**
     * Get the reason displayed to the punished {@link org.bukkit.entity.Player} if one is set.
     * @return The reason if one is set, else null.
     */
    @Nullable
    public String getReason() {
        return reason;
    }

    /**
     * Get the reason displayed to the punished {@link org.bukkit.entity.Player}.
     * @return The reason if one is set, else default reason.
     */
    @NotNull
    public String getNonnullReason() {
        return reason == null ? "No reason given" : reason;
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
        setDirty(true);
    }

    public void setCanAppeal(boolean canAppeal) {
        this.canAppeal = canAppeal;
        setDirty(true);
    }

    public void setType(Type type) {
        this.type = type;
        setDirty(true);
    }

    // Meh
    public void setDirty(boolean dirty) {
        if (dirty) manager.addDirty(this);
        else manager.removeDirty(this);
    }

    public int getId() {
        return id;
    }

    public int getPunisherId() {
        return punisherId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled != enabled)
            setDirty(true);
        this.isEnabled = enabled;
    }

    /**
     * Check if the punishment is still active.
     * @return true if still active
     */
    public boolean isActive() {
        if (getType().hasDuration() && isEnabled()) {
            boolean enabled = getPunishmentEnd() == null || (System.currentTimeMillis() < getPunishmentEnd().toEpochMilli());
            setEnabled(enabled);
        }

        return isEnabled();
    }

    public Type getType() {
        return type;
    }

    public boolean isBan() {
        return type.name().endsWith("BAN");
    }

    public boolean isMute() {
        return type.name().endsWith("MUTE");
    }

    public boolean isKick() { return type.name().endsWith("KICK"); }

    public String getRemainingString() {
        return isPermanent() ? "never" : Utils.timeStringFromNow(getPunishmentEnd().toEpochMilli());
    }

    public String getTotalString() {
        return isPermanent() ? "Forever" : Utils.timeStringFromMillis(getPunishmentStart().toEpochMilli() - getPunishmentEnd().toEpochMilli());
    }

    public Component toComponent() {
        Component component = Component.text("Punishment Entry", BeanColor.BAN).append(Component.text(" [#" + getId() + "]", NamedTextColor.RED).decorate(TextDecoration.ITALIC));
        component = component.append(Component.text("\nGenerated by ", NamedTextColor.DARK_GRAY).append(Component.text(ProfileStore.from(punisherId).getDisplayName()).append(Component.text(" at " + getPunishmentStart().toString(), NamedTextColor.DARK_GRAY))));

        String what = (isKick() ? "" : (isPermanent() ? "Permanently " : "Temporarily ")) + getType().getPastTense();

        if (getPunishedProfile() != null) {
            component = component.append(Component.text("\n\nThe player ", NamedTextColor.GRAY).append(getPunishedProfile().getColouredName()).append(Component.text(" was ", NamedTextColor.GRAY)));
        } else {
            component = component.append(Component.text("\n\nThis player was ", NamedTextColor.GRAY));
        }

        component = component.append(Component.text(what + " for:", NamedTextColor.GRAY)).append(Component.text("\n\"" + getNonnullReason() + "\"", NamedTextColor.WHITE));

        if (!isPermanent()) {
            component = component.append(Component.text("\n\nTotal Duration: ", NamedTextColor.GRAY).append(Component.text(Utils.timeStringFromMillis(getPunishmentEnd().toEpochMilli() - getPunishmentStart().toEpochMilli()), NamedTextColor.LIGHT_PURPLE)));
            component = component.append(Component.text("\n" + (isActive() ? "Expires in: " : "Expired: "), NamedTextColor.GRAY).append(Component.text(Utils.timeStringFromNow(getPunishmentEnd().toEpochMilli()), NamedTextColor.AQUA)));
        }

        return component;
    }

    public abstract void enact();

    public abstract void pardon();
}
