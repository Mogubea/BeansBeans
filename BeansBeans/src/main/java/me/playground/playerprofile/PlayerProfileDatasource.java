package me.playground.playerprofile;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import me.playground.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

public class PlayerProfileDatasource extends PrivateDatasource {
    private final String table_profiles = "player_profiles";

    private final PlayerProfileManager manager;

    public PlayerProfileDatasource(Main plugin, PlayerProfileManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @Override
    public void loadAll() {
        // Profiles are loaded live when a player joins or their profile is called upon.
    }

    @Override
    public void saveAll() throws Exception {
        Collection<PlayerProfile> profiles = manager.getCache().asMap().values();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE " + table_profiles + " SET " + "coins = ?, ranks = ?, permissions = ?,"
                + "namecolour = ?, nickname = ?, booleanSettings = ?, warpCount = ?, civilization = ?, job = ?, donorRankExpiration = ? WHERE id = ?")) {
            for (PlayerProfile pp : profiles) {
                byte idx = 1;

                s.setDouble(idx++, pp.getBalance());
                s.setString(idx++, Utils.toString(pp.getRanks(), true, ","));
                s.setString(idx++, Utils.toString(pp.getPrivatePermissions(), true, ","));
                s.setInt(idx++, pp.getNameColour().value());
                s.setString(idx++, pp.getNickname());
                s.setLong(idx++, pp.getSettings());
                s.setShort(idx++, pp.getWarpCount());
                s.setInt(idx++, pp.getCivilizationId());
                s.setString(idx++, pp.getJob() == null ? null : pp.getJob().getName());
                long exp = pp.getCheckDonorExpiration(); // Needed since 0 isn't allowed apparently....?
                s.setTimestamp(idx++, (exp > 60000 * 60 * 24) ? new Timestamp(exp) : null);
                s.setInt(idx, pp.getId());

                s.addBatch();
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

            /*saveSkills(pp.getId(), pp.getSkills());
            saveArmourWardrobe(pp);
            savePickupBlacklist(pp.getId());
            savePlayerHeirlooms(pp);
            savePlayerStats(pp);
            setHome(pp.getOfflinePlayer(), pp.getHome());
            refreshPlayerInbox(pp);*/
    }
}
