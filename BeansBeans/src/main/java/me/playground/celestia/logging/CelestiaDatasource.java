package me.playground.celestia.logging;

import me.playground.data.LoggingDatasource;
import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import org.bukkit.Location;

import java.sql.*;
import java.util.List;

public class CelestiaDatasource extends PrivateDatasource implements LoggingDatasource<CelestiaLog> {

    protected CelestiaDatasource(Main plugin) {
        super(plugin);
    }

    @Override
    public void loadAll() {

    }

    @Override
    public void saveAll() throws Exception {

    }

    @Override
    public boolean saveLogs(List<CelestiaLog> logs) {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO celestia (date, playerId, action, world, x, y, z, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            int size = logs.size();
            for (int x = -1; ++x < size;) {
                CelestiaLog log = logs.get(x);
                Location loc = log.getLocation();

                int idx = 0;
                s.setTimestamp(++idx, Timestamp.from(log.getTime()));
                s.setInt(++idx, log.getPlayerId());
                s.setString(++idx, log.getAction().getIdentifier());
                s.setInt(++idx, getWorldId(loc.getWorld()));
                s.setShort(++idx, (short)loc.getX());
                s.setShort(++idx, (short)loc.getY());
                s.setShort(++idx, (short)loc.getZ());
                s.setString(++idx, log.getData());
                s.addBatch();
            }

            s.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
