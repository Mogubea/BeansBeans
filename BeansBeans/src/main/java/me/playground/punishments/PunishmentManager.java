package me.playground.punishments;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class PunishmentManager {

    private final Main plugin;
    private final PunishmentDatasource datasource;

    private final Set<Punishment<?>> dirtyPunishments = new HashSet<>();

    public PunishmentManager(Main plugin) {
        this.plugin = plugin;
        this.datasource = new PunishmentDatasource(this, plugin);
    }

    protected PunishmentDatasource getDatasource() {
        return datasource;
    }

    @NotNull
    public PunishmentMinecraft banPlayer(OfflinePlayer target, long duration) {
        return banPlayer(target.getUniqueId(), 0, duration, null, true, true, false);
    }

    @NotNull
    public PunishmentMinecraft banPlayer(UUID uuid, int punisher, long duration, @Nullable String reason, boolean enabled, boolean appealable, boolean ip) {
        ProfileStore ps = ProfileStore.from(uuid, true);
        int id = ps == null ? 0 : ps.getId();

        PunishmentMinecraft punishment = (PunishmentMinecraft) datasource.createPunishment(ip ? Type.MINECRAFT_IP_BAN : Type.MINECRAFT_BAN, punisher, id, uuid, Instant.now(), (duration < 1) ? null : Instant.ofEpochMilli(System.currentTimeMillis() + duration), reason, enabled, appealable);

        if (ps != null && ps.isOnline())
            PlayerProfile.from(uuid).addPunishment(punishment, true);

        punishment.enact();

        return punishment;
    }

    @NotNull
    public PunishmentMinecraft mutePlayer(UUID uuid, int punisher, long duration, @Nullable String reason, boolean enabled, boolean appealable, boolean full) {
        ProfileStore ps = ProfileStore.from(uuid, true);
        int id = ps == null ? 0 : ps.getId();

        PunishmentMinecraft punishment = (PunishmentMinecraft) datasource.createPunishment(full ? Type.MINECRAFT_FULL_MUTE : Type.MINECRAFT_GLOBAL_MUTE, punisher, id, uuid, Instant.now(), (duration < 1) ? null : Instant.ofEpochMilli(System.currentTimeMillis() + duration), reason, enabled, appealable);

        if (ps != null && ps.isOnline())
            PlayerProfile.from(uuid).addPunishment(punishment, true);

        punishment.enact();

        return punishment;
    }

    @NotNull
    public PunishmentMinecraft kickPlayer(@NotNull Player player, int punisher, @Nullable String reason) {
        PunishmentMinecraft punishment = (PunishmentMinecraft) datasource.createPunishment(Type.MINECRAFT_KICK, punisher, PlayerProfile.from(player).getId(), player.getUniqueId(), Instant.now(), null, reason, false, false);
        PlayerProfile.from(player).addPunishment(punishment, false);
        punishment.enact();

        return punishment;
    }

    public List<Punishment<?>> loadPunishmnents(PlayerProfile pp) {
        return datasource.loadPunishments(pp);
    }

    /**
     * Grab the on-screen message sent when banning a user or when said user attempts to reconnect to the server while banned.
     */
    @NotNull
    public Component getBanMessage(PunishmentMinecraft punishment) {
        Component message = Component.text("\u00a74\u26a0 \u00a7rYou're " + (punishment.isPermanent() ? "permanently" : "currently") + " banned from Bean's Beans! \u00a74\u26a0", BeanColor.BAN);

        if (punishment.getReason() != null)
            message = message.append(Component.text("\n\nReason: ", BeanColor.BAN)).append(Component.text("\"" + punishment.getReason() + "\"", BeanColor.BAN_REASON).append(Component.text(".", BeanColor.BAN)));

        if (!punishment.isPermanent())
            message = message.append(Component.text("\n\n\nYour ban expires in ", BeanColor.BAN)).append(Component.text(punishment.getRemainingString(), NamedTextColor.LIGHT_PURPLE).append(Component.text(".", BeanColor.BAN)));

        if (punishment.canAppeal())
            message = message.append(Component.text("\n\n\n\nWe currently do not have an official Ban Appeal process. However, if you believe this ban was a mistake or is unwarranted, please attempt to contact a member of Staff via the Server's Discord.", NamedTextColor.GRAY));

        return message;
    }

    /**
     * Grab the on-screen message sent when banning a user or when said user attempts to reconnect to the server while banned.
     */
    @NotNull
    public Component getMuteMessage(PunishmentMinecraft punishment) {
        Component message = Component.text("\u26a0", BeanColor.BAN).append(Component.text(" You're " + (punishment.isPermanent() ? "permanently muted!" : "currently muted!"), BeanColor.BAN_REASON));

        if (punishment.getReason() != null)
            message = message.append(Component.text(" • ", BeanColor.BAN)).append(Component.text("Reason: ", NamedTextColor.GRAY)).append(Component.text(punishment.getReason(), NamedTextColor.WHITE));

        if (!punishment.isPermanent())
            message = message.append(Component.text(" • ", BeanColor.BAN)).append(Component.text("Expires in ", NamedTextColor.GRAY)).append(Component.text(punishment.getRemainingString(), NamedTextColor.WHITE));

        return message;
    }

    protected void addDirty(Punishment<?> punishment) {
        this.dirtyPunishments.add(punishment);
    }

    protected void removeDirty(Punishment<?> punishment) {
        this.dirtyPunishments.remove(punishment);
    }

    @NotNull
    protected Set<Punishment<?>> getDirtyPunishments() {
        return dirtyPunishments;
    }
}
