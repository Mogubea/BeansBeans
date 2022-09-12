package me.playground.items.values;

import me.playground.data.LoggingDatasource;
import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.DirtyDouble;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemValueDatasource extends PrivateDatasource implements LoggingDatasource<ItemValueLog> {

    private ItemValueManager manager;
    private final String tableValues = "item_values";
    private final String tableHistory = "item_value_history";

    protected ItemValueDatasource(Main plugin) {
        super(plugin);
    }

    @Override
    protected void postCreation() {
        manager = plugin.getItemValueManager();
        loadAll();
    }

    @Override
    public void loadAll() {
        long then = System.currentTimeMillis();
        Map<String, DirtyDouble> map = new HashMap<>();
        Map<String, DirtyDouble> map2 = new HashMap<>();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT identifier,value,isEnforced FROM " + tableValues); ResultSet r = s.executeQuery()) {
            while(r.next()) {
                String identifier = r.getString("identifier");
                double value = r.getDouble("value");
                if (identifier == null || identifier.isEmpty() || value < 0) continue;
                boolean isEnforced = r.getBoolean("isEnforced");
                if (isEnforced)
                    map.put(identifier, new DirtyDouble(value));
                else
                    map2.put(identifier, new DirtyDouble(value));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getPlugin().getSLF4JLogger().info("Loaded " + map.size() + " Enforced and " + map2.size() + " Previously Calculated Item Values in " + (System.currentTimeMillis()-then) + "ms");
        manager.getItemValues().updateAllValues(map, map2);
    }

    @Override
    public void saveAll() throws Exception {
        Map<String, DirtyDouble> map = manager.getItemValues().getEnforced();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + tableValues + " (identifier, value, isEnforced) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value), isEnforced = VALUES(isEnforced)")) {
            for (int x = -1; ++x < 2;) {
                if (x == 1) map = manager.getItemValues().getCalculated();

                for (Map.Entry<String, DirtyDouble> entry : map.entrySet()) {
                    String identifier = entry.getKey();
                    DirtyDouble dirtyDouble = entry.getValue();
                    if (!dirtyDouble.isDirty()) continue;

                    s.setString(1, identifier);
                    s.setDouble(2, dirtyDouble.getValue());
                    s.setBoolean(3, x == 0); // isEnforced
                    s.addBatch();

                    dirtyDouble.setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean saveLogs(List<ItemValueLog> logs) {
        try (Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + tableHistory + " (identifier, time, newValue, oldValue, updaterId, isEnforced) VALUES (?, ?, ?, ?, ?, ?)")) {
            int size = logs.size();
            for (int x = -1; ++x < size;) {
                ItemValueLog log = logs.get(x);

                s.setString(1, log.getIdentifier());
                s.setTimestamp(2, Timestamp.from(log.getTime()));
                s.setDouble(3, log.getNewValue());
                s.setDouble(4, log.getOldValue());
                s.setInt(5, log.getUpdaterId());
                s.setBoolean(6, log.isEnforcedChange());
                s.addBatch();
            }

            s.executeBatch();
            if (logs.size() > 0)
                notifyStaff(logs);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Notify Staff so that everyone is aware of any changes.
    private void notifyStaff(List<ItemValueLog> logs) {
        if (getPlugin().getDiscord().isOnline()) {
            StringBuilder stringBuilder = new StringBuilder();
            int size = logs.size();
            for (int x = -1; ++x < size;) {
                if (x > 20) {
                    stringBuilder.append("*And ").append(size - x).append(" more...*");
                    break;
                }

                ItemValueLog log = logs.get(x);
                PlayerProfile pp = PlayerProfile.fromIfExists(log.getUpdaterId());
                String updater = pp != null ? pp.getDiscordMember() != null ? pp.getDiscordMember().getAsMention() : pp.getDisplayName() : "Server";
                stringBuilder.append("**• **").append(log.getIdentifier()).append(" updated from ~~").append(df.format(log.getOldValue())).append("~~ to **")
                        .append(df.format(log.getNewValue())).append(" :coin: ** by ").append(updater).append(" (<t:").append(log.getTime().getEpochSecond()).append(":R>)\n");
            }

            Utils.notifyAllStaff(Component.text("The NPC coin value of " + logs.size() + " item(s) have been changed. Specific details are in the Staff Discord."),
                    "Item Value Changes", stringBuilder.toString());
        } else {
            Utils.notifyAllStaff(Component.text("The NPC coin value of " + logs.size() + " item(s) have been changed."), null, null);
        }
    }

    protected final DecimalFormat df = new DecimalFormat("#,###.##");
}
