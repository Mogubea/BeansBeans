package me.playground.playerprofile;

import jline.internal.Nullable;
import me.playground.data.Datasource;
import me.playground.data.PrivateDatasource;
import me.playground.main.Main;
import me.playground.playerprofile.stats.DirtyByte;
import me.playground.playerprofile.stats.DirtyInteger;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.skills.*;
import me.playground.utils.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

/**
 * A {@link PrivateDatasource} for saving and loading {@link PlayerProfile} related objects.
 */
public class PlayerProfileDatasource extends PrivateDatasource {
    private final String table_profiles = "player_profiles";
    private final String table_blacklist = "pickup_blacklists";
    private final String table_skills = "player_experience";
    private final String table_skillPerks = "player_skill_perks";
    private final String table_skillMilestones = "player_skill_milestones";
    private final String table_homes = "homes";

    private final String statement_saveskills;

    private final PlayerProfileManager manager;

    public PlayerProfileDatasource(Main plugin, PlayerProfileManager manager) {
        super(plugin);
        this.manager = manager;

        // Create the statement for saving skills.
        StringBuilder statementStr = new StringBuilder("UPDATE " + table_skills + " SET ");
        int max = Skill.getRegisteredSkills().size();
        for (Skill src : Skill.getRegisteredSkills()) {
            max--;
            statementStr.append(src.getName()).append(" = ?,").append(src.getName()).append("_xp = ?,").append(src.getName()).append("_ess = ?").append(max > 0 ? "," : "");
        }

        statementStr.append(" WHERE playerId = ?");
        statement_saveskills = statementStr.toString();
    }

    @Override
    public void loadAll() {
        loadProfileCache();

        // Profiles are loaded live when a player joins or their profile is called upon.
    }

    @Override
    public void saveAll() throws Exception {
        Collection<PlayerProfile> profiles = manager.getCache().asMap().values();
        for (PlayerProfile pp : profiles) {
            try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE " + table_profiles + " SET " + "coins = ?, ranks = ?, permissions = ?,"
                    + "namecolour = ?, nickname = ?, booleanSettings = ?, warpCount = ?, civilization = ?, job = ?, donorRankExpiration = ? WHERE id = ?")) {
                byte idx = 0;

                s.setDouble(++idx, pp.getBalance());
                s.setString(++idx, Utils.toString(pp.getRanks(), true, ","));
                s.setString(++idx, Utils.toString(pp.getPrivatePermissions(), true, ","));
                s.setInt(++idx, pp.getNameColour().value());
                s.setString(++idx, pp.getNickname());
                s.setLong(++idx, pp.getSettings());
                s.setShort(++idx, (short)pp.getWarpCount());
                s.setInt(++idx, pp.getCivilizationId());
                s.setString(++idx, pp.getJob() == null ? null : pp.getJob().getName());

                long exp = pp.getCheckDonorExpiration(); // Needed since 0 isn't allowed apparently....?
                s.setTimestamp(++idx, (exp > 60000 * 60 * 24) ? new Timestamp(exp) : null);

                s.setInt(++idx, pp.getId());
                s.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Save the rest
            saveSkills(pp);
            saveArmourWardrobe(pp);
            savePickupBlacklist(pp);
            savePlayerHeirlooms(pp);
            savePlayerStats(pp);

            if (pp.getHome() != null)
                setHome(pp, pp.getHome());

            refreshPlayerInbox(pp);

            // Invalidate the profile if the player is currently offline.
            if (!pp.isOnline())
                manager.getCache().invalidate(pp.getUniqueId());
        }
    }

    /**
     * Loads a small cache of every player's id, uuid, name colour and display name.
     */
    protected void loadProfileCache() {
        ResultSet rs = null;
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT id,uuid,namecolour,name,nickname FROM " + table_profiles)) {
            rs = s.executeQuery();
            while (rs.next()) {
                final String nick = rs.getString("nickname");
                final String name = (nick != null ? nick : rs.getString("name"));

                ProfileStore.updateStore(rs.getInt("id"), UUID.fromString(rs.getString("uuid")), rs.getString("name"), name, rs.getInt("namecolour"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
    }

    /**
     * Load a player's pre-existing home from the database.
     * @return Home location or null.
     */
    @Nullable
    protected Location loadHome(int playerId) {
        ResultSet rs = null;
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT world,x,y,z,yaw,p FROM " + table_homes + " WHERE playerID = ?")) {
            s.setInt(1, playerId);

            rs = s.executeQuery();

            if (rs.next())
                return new Location(getWorld(rs.getShort("world")), rs.getFloat("x"), rs.getFloat("y"), rs.getFloat("z"), rs.getFloat("yaw"), rs.getFloat("p"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
        return null;
    }

    /**
     * Attempt to set a player's home.
     * @return Whether the home was updated or not.
     */
    protected boolean setHome(@NotNull final PlayerProfile pp, @NotNull final Location loc) {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO " + table_homes + " (playerId, world, x, y, z, yaw, p) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
                + "world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw), p = VALUES(p)")) {
            int idx = 0;

            s.setInt(++idx, pp.getId());
            s.setInt(++idx, getWorldId(loc.getWorld()));
            s.setFloat(++idx, (float)loc.getX());
            s.setFloat(++idx, (float)loc.getY());
            s.setFloat(++idx, (float)loc.getZ());
            s.setFloat(++idx, loc.getYaw());
            s.setFloat(++idx, loc.getPitch());

            s.executeUpdate();
            s.close();

            pp.setHome(loc);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Load a player's item blacklist
     * @return The player's blacklist
     */
    @NotNull
    protected List<String> loadPickupBlacklist(int playerId) {
        ResultSet rs = null;
        List<String> array = new ArrayList<>();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT blacklist FROM " + table_blacklist + " WHERE playerId = ?")) {
            s.setInt(1, playerId);

            rs = s.executeQuery();

            while(rs.next()) {
                String string = rs.getString("blacklist");
                if (string == null || string.isEmpty())
                    break;
                String[] list = string.split(",");
                for (String str : list)
                    if (!str.isEmpty())
                        array.add(str);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
        return array;
    }

    /**
     * Save the player's item blacklist
     */
    protected void savePickupBlacklist(@NotNull final PlayerProfile pp) {
        try(Connection c = getNewConnection(); PreparedStatement statement = c.prepareStatement("INSERT INTO " + table_blacklist + " (playerId, blacklist) VALUES (?,?) ON DUPLICATE KEY UPDATE blacklist = VALUES(blacklist)")) {

            StringBuilder sb = new StringBuilder();
            for (String entry : pp.getPickupBlacklist())
                sb.append(entry).append(",");

            statement.setInt(1, pp.getId());
            statement.setString(2, sb.toString());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load a player's Wardrobe.
     * @return The player's wardrobe in an array of 20 {@link ItemStack}s.
     */
    @NotNull
    protected ItemStack[] loadArmourWardrobe(int playerId) {
        ResultSet rs = null;
        ItemStack[] stacks = new ItemStack[20];

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT compressedArmorInventory FROM wardrobes WHERE playerId = ?")) {
            s.setInt(1, playerId);

            rs = s.executeQuery();

            while(rs.next()) {
                ItemStack[] stackz = Utils.itemStackArrayFromBase64(rs.getString("compressedArmorInventory"));
                if (stackz != null) stacks = stackz;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }

        return stacks;
    }

    /**
     * Save the player's wardrobe
     */
    protected void saveArmourWardrobe(PlayerProfile pp) {
        String armour = Utils.itemStackArrayToBase64(pp.getArmourWardrobe());

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO wardrobes (playerId,compressedArmorInventory) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE compressedArmorInventory = VALUES(compressedArmorInventory)")) {

            s.setInt(1, pp.getId());
            s.setString(2, armour);

            s.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected JSONObject loadPlayerHeirlooms(int playerId) {
        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT heirloomData FROM player_heirlooms WHERE playerId = ?")) {
            s.setInt(1, playerId);
            rs = s.executeQuery();

            if (rs.next())
                return new JSONObject(rs.getString("heirloomData"));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
        return null;
    }

    /**
     * Save the player's {@link PlayerStats}.
     */
    protected void savePlayerStats(PlayerProfile pp) {
        PlayerStats stats = pp.getStats();

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO stats (playerId, category, stat, value) VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
            s.setInt(1, pp.getId());

            HashMap<StatType, HashMap<String, DirtyInteger>> map = stats.getMap();

            for (Map.Entry<StatType, HashMap<String, DirtyInteger>> ent : map.entrySet()) {
                s.setByte(2, ent.getKey().getId());

                for(Map.Entry<String, DirtyInteger> entt : ent.getValue().entrySet()) {
                    if (!entt.getValue().isDirty()) continue;

                    s.setString(3, entt.getKey());
                    s.setInt(4, entt.getValue().getValue());
                    s.addBatch();
                    entt.getValue().setDirty(false);
                }
            }

            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the player's {@link PlayerStats}.
     */
    @NotNull
    protected PlayerStats loadPlayerStats(@NotNull final PlayerProfile pp) {
        final PlayerStats stats = new PlayerStats(pp, getPlugin().getMilestoneManager());
        ResultSet rs = null;

        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT category,stat,value FROM stats WHERE playerId = ?")) {
            s.setInt(1, pp.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                StatType type = StatType.fromId(rs.getByte(1));
                if (type != null)
                    stats.setStat(type, rs.getString(2), rs.getInt(3), false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
        return stats;
    }

    /**
     * Save the player's Heirloom inventory.
     */
    protected void savePlayerHeirlooms(@NotNull final PlayerProfile pp) {
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO player_heirlooms (playerId,heirloomData) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE heirloomData = VALUES(heirloomData)")) {

            s.setInt(1, pp.getId());
            s.setString(2, pp.getHeirlooms().getJsonData().toString());

            s.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public PlayerSkillData loadSkills(@NotNull final PlayerProfile pp) {
        Connection c = null;
        PreparedStatement s = null;
        ResultSet rs = null;

        try {
            Map<Skill, SkillData> xpSources = new HashMap<>();
            Map<SkillTreeEntry<?>, DirtyByte> treeLevels = new HashMap<>();
            Map<Milestone, SingleMilestoneData> milestoneData = new HashMap<>();

            c = getNewConnection();
            s = c.prepareStatement("SELECT * FROM " + table_skills + " WHERE playerId = ?");
            s.setInt(1, pp.getId());
            rs = s.executeQuery();
            if (rs.next()) {
                for (Skill skill : Skill.getRegisteredSkills())
                    xpSources.put(skill, new SkillData(rs.getDouble(skill.getName() + "_xp"), rs.getInt(skill.getName() + "_ess")));
            } else {
                close(c, s);
                c = getNewConnection();
                s = c.prepareStatement("INSERT INTO " + table_skills + "(playerId) VALUES (?)");
                s.setInt(1, pp.getId());
                s.executeUpdate();
            }

            close(c, s, rs);
            c = getNewConnection();
            s = c.prepareStatement("SELECT treePerk,level FROM " + table_skillPerks + " WHERE playerId = ?");
            s.setInt(1, pp.getId());
            rs = s.executeQuery();

            while (rs.next()) {
                SkillTreeEntry<?> entry = SkillTreeEntry.getByName(rs.getString("treePerk"));
                if (entry == null) continue;
                treeLevels.put(entry, new DirtyByte(rs.getByte("level"))); // "What a dirty byte" - minibike 1/10/22
            }

            close(c, s, rs);
            c = getNewConnection();
            s = c.prepareStatement("SELECT time,milestone,tier FROM " + table_skillMilestones + " WHERE playerId = ? ORDER BY time ASC, milestone ASC, tier ASC");
            s.setInt(1, pp.getId());
            rs = s.executeQuery();

            while(rs.next()) {
                Milestone milestone = getPlugin().getMilestoneManager().getMilestone(rs.getString("milestone"));
                if (milestone == null) continue;

                SingleMilestoneData smd = milestoneData.get(milestone);
                if (smd == null)
                    smd = new SingleMilestoneData(pp, milestone);

                smd.addTierUpTime(MilestoneTier.fromIdentifier(rs.getString("tier")), rs.getTimestamp("time").toInstant());
                milestoneData.put(milestone, smd);
            }

            return new PlayerSkillData(pp, getPlugin().getMilestoneManager(), xpSources, treeLevels, milestoneData);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, c, s);
        }

        return new PlayerSkillData(pp, getPlugin().getMilestoneManager());
    }

    /**
     * Save the player's {@link PlayerSkillData}.
     */
    protected void saveSkills(@NotNull final PlayerProfile pp) {
        PlayerSkillData skills = pp.getSkills();
        try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement(statement_saveskills)) {
            int idx = 0;

            for (Skill src : Skill.getRegisteredSkills()) {
                s.setInt(++idx, skills.getLevel(src));
                s.setDouble(++idx, skills.getTotalExperience(src));
                s.setInt(++idx, skills.getEssence(src));
            }

            s.setInt(++idx, pp.getId());

            s.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the player's delivery inbox.
     */
    @NotNull
    private List<Delivery> loadPlayerInbox(PlayerProfile profile) {
        Connection c = null;
        PreparedStatement statement = null;
        ResultSet r = null;
        final List<Delivery> deliveries = new ArrayList<>();

        try {
            c = getNewConnection();
            statement = c.prepareStatement("SELECT * FROM player_inbox WHERE playerId = ? AND deleted = 0 ORDER BY creationDate DESC");
            statement.setInt(1, profile.getId());

            r = statement.executeQuery();

            while(r.next()) {
                Timestamp tsOne = r.getTimestamp("creationDate");
                Timestamp tsTwo = r.getTimestamp("openDate");
                Timestamp tsThree = r.getTimestamp("expiryDate");
                String content = r.getString("content");
                DeliveryType type;
                try {
                    type = DeliveryType.valueOf(r.getString("type"));

                    deliveries.add(new Delivery(r.getInt("id"), r.getInt("playerId"), r.getInt("senderId"), tsOne == null ? 0L : tsOne.getTime(), tsTwo == null ? 0L : tsTwo.getTime(),
                            tsThree == null ? 0L : tsThree.getTime(), type, r.getString("title"), r.getString("message"), content == null ? null : new JSONObject(content)));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(r, c, statement);
        }
        return deliveries;
    }

    /**
     * Save the player's delivery inbox.
     */
    public void savePlayerDirtyInbox(PlayerProfile profile) {
        List<Delivery> deliveries = profile.getInbox();
        int size = deliveries.size();
        for (int x = -1; ++x < size;) {
            Delivery delivery = deliveries.get(x);
            if (delivery.isDirty())
                Datasource.updateDelivery(delivery);
        }
    }

    /**
     * Refresh the player's delivery inbox.
     */
    public void refreshPlayerInbox(PlayerProfile profile) {
        savePlayerDirtyInbox(profile);
        profile.getInbox().clear();
        profile.getInbox().addAll(loadPlayerInbox(profile));
    }

}
