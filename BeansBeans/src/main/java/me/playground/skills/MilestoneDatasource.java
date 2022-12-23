package me.playground.skills;

import me.playground.data.PrivateDatasource;
import me.playground.items.lore.Lore;
import me.playground.main.Main;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneDatasource extends PrivateDatasource {

    private final MilestoneManager manager;
    private final String milestones = "skill_milestones";
    private final String skill_milestones = "player_skill_milestones";

    public MilestoneDatasource(Main pl, MilestoneManager manager) {
        super(pl);
        this.manager = manager;
    }

    @Override
    public void loadAll() {
        manager.clear();
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT identifier,name,skill,lore,attributes FROM " + milestones); ResultSet r = s.executeQuery()) {
            while(r.next()) {
                String identifier = r.getString("identifier");
                String name = r.getString("name");
                Skill skill = Skill.getByName(r.getString("skill"));
                if (skill == null) continue;

                Lore lore = Lore.getBuilder(r.getString("lore")).setLineLimit(28).build();
                if (lore == null) continue;

                JSONObject object = new JSONObject(r.getString("attributes"));

                manager.register(Milestone.fromJson(skill, identifier, name, lore, object));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected boolean registerNewMilestone(@NotNull Milestone milestone) {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + milestones + " (identifier,name,skill,lore,attributes) VALUES (?,?,?,?,?)")) {
            int idx = 0;
            s.setString(++idx, milestone.getIdentifier());
            s.setString(++idx, milestone.getName());
            s.setString(++idx, milestone.getSkill().getName().toUpperCase());
            s.setString(++idx, milestone.getLore().getBaseContent());
            s.setString(++idx, milestone.getJson().toString());
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveAll() throws Exception {
        List<MilestoneTierUpEntry> entries = new ArrayList<>(manager.getPendingTierUpEntries());
        int size = entries.size();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + skill_milestones + " (playerId,time,milestone,tier) VALUES (?,?,?,?)")) {
            for (int x = -1; ++x < size;) {
                MilestoneTierUpEntry entry = entries.get(x);

                int idx = 0;
                s.setInt(++idx, entry.getPlayerId());
                s.setTimestamp(++idx, Timestamp.from(entry.getInstant()));
                s.setString(++idx, entry.getMilestone().getIdentifier());
                s.setString(++idx, entry.getTier().getIdentifier());
                s.addBatch();
            }

            s.executeBatch();
            manager.getPendingTierUpEntries().removeAll(entries);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}