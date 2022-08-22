package me.playground.punishments;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.Instant;
import java.util.*;

public class PunishmentDatasource extends PrivateDatasource {

    private final PunishmentManager manager;

    protected PunishmentDatasource(PunishmentManager manager, Main plugin) {
        super(plugin);
        this.manager = manager;
    }

    protected Punishment<?> createPunishment(Type type, int punisher, int punished, Object punishedIdentifier, Instant time, Instant endTime, String reason, boolean enabled, boolean canAppeal) {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO punishments (type, punisher, punished, punishedIdentifier, punishTime, expirationTime, reason, disabled, canAppeal) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            int idx = 0;
            s.setString(++idx, type.getIdentifier());
            s.setInt(++idx, punisher);
            s.setInt(++idx, punished);
            s.setString(++idx, punishedIdentifier.toString());
            s.setTimestamp(++idx, Timestamp.from(time));
            s.setTimestamp(++idx, (endTime == null) ? null : Timestamp.from(endTime));
            s.setString(++idx, reason);
            s.setBoolean(++idx, !enabled);
            s.setBoolean(++idx, canAppeal);

            s.executeUpdate();

            ResultSet rs = s.getGeneratedKeys();
            if (!rs.next()) return null;
            int id = rs.getInt(1);

            if (punishedIdentifier instanceof UUID uuid)
                return new PunishmentMinecraft(manager, id, punisher, type, punished, uuid, time, endTime, reason, enabled, canAppeal);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    protected List<Punishment<?>> loadPunishments(PlayerProfile pp) {
        List<Punishment<?>> punishments = new ArrayList<>();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM punishments WHERE `punished` = ? OR `punishedIdentifier` = ? OR `punishedIdentifier` = ? ORDER BY id ASC")) {
            s.setInt(1, pp.getId());
            s.setString(2, pp.getUniqueId().toString());
            s.setString(3, pp.getId()+"");

            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                try {
                    Punishment<?> punishment;
                    int id = rs.getInt("id");

                    Type type = Type.fromIdentifier(rs.getString("type"));
                    if (type == null) continue;

                    int punisher = rs.getInt("punisher");
                    int punished = rs.getInt("punished");
                    String punishedIdentifier = rs.getString("punishedIdentifier");

                    Instant startTime = rs.getTimestamp("punishTime").toInstant();
                    Timestamp endStamp = rs.getTimestamp("expirationTime");
                    Instant endTime = endStamp == null ? null : endStamp.toInstant();

                    String reason = rs.getString("reason");
                    boolean enabled = !rs.getBoolean("disabled");
                    boolean canAppeal = rs.getBoolean("canAppeal");

                    try {
                        UUID uuid = UUID.fromString(punishedIdentifier);
                        punishment = new PunishmentMinecraft(manager, id, punisher, type, punished, uuid, startTime, endTime, reason, enabled, canAppeal);
                    } catch (Exception ignored) {
                        continue;
                    }

                    punishments.add(punishment);
                } catch (Exception ignored) {
                }
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return punishments;
    }

    @Override
    public void loadAll() {

    }

    @Override
    public void saveAll() throws Exception {
        Set<Punishment<?>> dirtyPunishments = new HashSet<>(manager.getDirtyPunishments()); // Clone to prevent ConcurrentModificationExceptions.

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE punishments SET type = ?, expirationTime = ?, reason = ?, disabled = ?, canAppeal = ? WHERE id = ?")) {
            for(Punishment<?> punishment : dirtyPunishments) {
                int idx = 0;
                s.setString(++idx, punishment.getType().getIdentifier());
                s.setTimestamp(++idx, punishment.getPunishmentEnd() == null ? null : Timestamp.from(punishment.getPunishmentEnd()));
                s.setString(++idx, punishment.getReason());
                s.setBoolean(++idx, !punishment.isActive());
                s.setBoolean(++idx, punishment.canAppeal());

                s.setInt(++idx, punishment.getId());
                s.addBatch();
            }
            s.executeBatch();

            manager.getDirtyPunishments().removeAll(dirtyPunishments); // Perform a removal all rather than a clear just in-case an additional entry was added.
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
