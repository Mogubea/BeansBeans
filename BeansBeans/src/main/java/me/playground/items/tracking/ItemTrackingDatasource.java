package me.playground.items.tracking;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import me.playground.playerprofile.stats.DirtyLong;

import java.sql.*;
import java.util.Map;

public class ItemTrackingDatasource extends PrivateDatasource {

    private final ItemTrackingManager manager;
    private final String tableCreation = "item_manifestation_counter";
    private final String tableRemoval = "item_demanifestation_counter";

    public ItemTrackingDatasource(Main pl, ItemTrackingManager manager) {
        super(pl);
        this.manager = manager;
    }

    @Override
    public void loadAll() {
        manager.clearCounter();
        long then = System.currentTimeMillis();
        int count = 0;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT identifier,reason,total FROM " + tableCreation); ResultSet r = s.executeQuery()) {
            while(r.next()) {
                ManifestationReason reason = ManifestationReason.fromIdentifier(r.getString("reason"));
                if (reason == null) continue;
                String identifier = r.getString("identifier");
                if (identifier == null) continue;
                long total = r.getLong("total");

                manager.setTimesManifested(identifier, reason, total);
                manager.setTimesManifested(identifier, ManifestationReason.TOTAL, manager.getTimesManifested(identifier, ManifestationReason.TOTAL) + total);
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT identifier,reason,total FROM " + tableRemoval); ResultSet r = s.executeQuery()) {
            while(r.next()) {
                DemanifestationReason reason = DemanifestationReason.fromIdentifier(r.getString("reason"));
                if (reason == null) continue;
                String identifier = r.getString("identifier");
                if (identifier == null) continue;
                long total = r.getLong("total");

                manager.setTimesDemanifested(identifier, reason, total);
                manager.setTimesDemanifested(identifier, DemanifestationReason.TOTAL, manager.getTimesDemanifested(identifier, DemanifestationReason.TOTAL) + total);
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getPlugin().getSLF4JLogger().info("Loaded " + count + " Tracking Counters for custom items in " + (System.currentTimeMillis()-then) + "ms");
    }

    @Override
    public void saveAll() throws Exception {
        Map<String, Map<ManifestationReason, DirtyLong>> manifestMap = manager.getAllManifestations();
        Map<String, Map<DemanifestationReason, DirtyLong>> demanifestMap = manager.getAllDemanifestations();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + tableCreation + " (identifier, reason, total) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE total = VALUES(total)")) {
            for(Map.Entry<String, Map<ManifestationReason, DirtyLong>> entry : manifestMap.entrySet()) {
                String identifier = entry.getKey();
                for (Map.Entry<ManifestationReason, DirtyLong> manifestEntry : entry.getValue().entrySet()) {
                    DirtyLong dirtyLong = manifestEntry.getValue();
                    if (!dirtyLong.isDirty()) continue;

                    ManifestationReason reason = manifestEntry.getKey();

                    s.setString(1, identifier);
                    s.setString(2, reason.getIdentifier());
                    s.setLong(3, dirtyLong.getValue());
                    s.addBatch();

                    dirtyLong.setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + tableRemoval + " (identifier, reason, total) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE total = VALUES(total)")) {
            for(Map.Entry<String, Map<DemanifestationReason, DirtyLong>> entry : demanifestMap.entrySet()) {
                String identifier = entry.getKey();
                for (Map.Entry<DemanifestationReason, DirtyLong> demanifestEntry : entry.getValue().entrySet()) {
                    DirtyLong dirtyLong = demanifestEntry.getValue();
                    if (!dirtyLong.isDirty()) continue;

                    DemanifestationReason reason = demanifestEntry.getKey();

                    s.setString(1, identifier);
                    s.setString(2, reason.getIdentifier());
                    s.setLong(3, dirtyLong.getValue());
                    s.addBatch();

                    dirtyLong.setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}