package me.playground.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import me.playground.celestia.logging.CelestiaAction;
import me.playground.civilizations.CitizenTier;
import me.playground.civilizations.Civilization;
import me.playground.civilizations.jobs.IFishingJob;
import me.playground.civilizations.jobs.IHuntingJob;
import me.playground.civilizations.jobs.IMiningJob;
import me.playground.civilizations.jobs.Job;
import me.playground.civilizations.structures.Structure;
import me.playground.civilizations.structures.Structure.Status;
import me.playground.civilizations.structures.Structures;
import me.playground.gui.UpdateEntry;
import me.playground.items.BeanItem;
import me.playground.loot.LootEntry;
import me.playground.loot.LootManager;
import me.playground.loot.LootTable;
import me.playground.main.Main;
import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.DeliveryType;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.ProfileModifyRequest.ModifyType;
import me.playground.playerprofile.ProfileStore;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillInfo;
import me.playground.playerprofile.skills.SkillType;
import me.playground.playerprofile.stats.DirtyInteger;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.utils.Utils;
import me.playground.voting.VoteService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;

public class Datasource {
	
	private static Connection connection;
	private static String host, database, username, password;
	private static int port;

	public static String table_profiles = "player_profiles";
	static String table_moneylogs = "transaction_logs";
	static String table_experience = "player_experience";
	static String table_npcs = "npcs";

	public static void init(JavaPlugin pl) {
		host = pl.getConfig().getString("host");
		port = pl.getConfig().getInt("port");
		username = pl.getConfig().getString("username");
		database = pl.getConfig().getString("database");
		password = pl.getConfig().getString("password");
		
		try {
			synchronized (pl) {
				if (connection != null && !connection.isClosed()) return;
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = getNewConnection();
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public static Connection getConnection() {
		return connection;
	}

	public static Connection getNewConnection() {
		try {
			if (connection != null)
				connection.close();
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username,
					password);
		} catch (SQLException e) {
			Main.getInstance().getSLF4JLogger().error("Could not establish a new MySQL Connection instance.");
			e.printStackTrace();
		}
		return connection;
	}
	
	private static World getWorld(int databaseId) {
		return Main.getInstance().getWorldManager().getWorld(databaseId);
	}
	
	private static int getWorldId(World world) {
		return Main.getInstance().getWorldManager().getWorldId(world);
	}
	
	public static boolean hasProfile(UUID playerUUID) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		try {
			c = getNewConnection();
			statement = connection.prepareStatement("SELECT id FROM " + table_profiles + " WHERE uuid = ?");
			statement.setString(1, playerUUID.toString());
			r = statement.executeQuery();
			return r.next();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return false;
	}
	
	public static PlayerProfile getProfileFromName(String nameOrNickname) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		try {
			c = getNewConnection();
			statement = connection
					.prepareStatement("SELECT * FROM " + table_profiles + " WHERE name = ? OR nickname = ?");
			statement.setString(1, nameOrNickname);
			statement.setString(2, nameOrNickname);
			r = statement.executeQuery();
			if (r.next())
				return forgeProfileUsingResultSet(r);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return null;
	}
	
	public static PlayerProfile getProfileFromId(int databaseId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		try {
			c = getNewConnection();
			statement = connection
					.prepareStatement("SELECT * FROM " + table_profiles + " WHERE id = ?");
			statement.setInt(1, databaseId);
			r = statement.executeQuery();
			if (r.next())
				return forgeProfileUsingResultSet(r);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return null;
	}

	public static PlayerProfile getOrMakeProfile(UUID playerUUID) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;

		try {
			c = getNewConnection();
			statement = connection.prepareStatement("SELECT * FROM " + table_profiles + " WHERE uuid = ?");
			statement.setString(1, playerUUID.toString());
			r = statement.executeQuery();
			if (r.next()) {
				return forgeProfileUsingResultSet(r);
			} else {
				close(r, c, statement);
				c = getNewConnection();
				statement = connection.prepareStatement("INSERT INTO " + table_profiles + "(uuid,ranks,namecolour,booleanSettings) VALUES (?,?,?,?)");
				statement.setString(1, playerUUID.toString());
				statement.setString(2, Rank.NEWBEAN.lowerName());
				statement.setInt(3, Rank.NEWBEAN.getRankHex());
				statement.setLong(4, PlayerSetting.getDefaultSettings());
				statement.executeUpdate();
				return getOrMakeProfile(playerUUID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return null;
	}

	private static PlayerProfile forgeProfileUsingResultSet(ResultSet rs) {
		try {
			final ArrayList<Rank> ranks = new ArrayList<Rank>();
			final HashSet<String> perms = new HashSet<String>();
			
			String[] ranksStr;
			ranksStr = rs.getString("ranks").split(",");
			for (String rankStr : ranksStr) {
				try {
					ranks.add(Rank.fromString(rankStr));
				} catch (IllegalArgumentException e) {
				}
			}
			
			// Load perms
			String permz = rs.getString("permissions");
			if (permz != null) {
				String[] permsStr = permz.split(",");
				for (String permStr : permsStr)
					perms.add(permStr);
			}
			
			final Civilization civ = Civilization.getById(rs.getInt("civilization"));
			final Job job = Job.getByName(rs.getString("job"));
			final Timestamp donoExpiry = rs.getTimestamp("donorRankExpiration");
			final long donoExpiration = donoExpiry == null ? 0L : donoExpiry.getTime();
			final PlayerProfile pp = new PlayerProfile(rs.getInt("id"), UUID.fromString(rs.getString("uuid")), ranks, perms,
					rs.getInt("namecolour"), rs.getString("name"), rs.getString("nickname"),
					rs.getLong("coins"), rs.getLong("booleanSettings"), rs.getShort("warpCount"));
			pp.setDonorExpiration(donoExpiration);
			pp.setCivilization(civ);
			pp.setJob(job, true);
			
			return pp;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveProfile(PlayerProfile pp) {
		Connection c = null;
		PreparedStatement statement = null;
		try {
			c = getNewConnection();
			statement = connection.prepareStatement("UPDATE " + table_profiles + " SET " + "coins = ?, ranks = ?, permissions = ?,"
					+ "namecolour = ?, nickname = ?, booleanSettings = ?, warpCount = ?, civilization = ?, job = ?, donorRankExpiration = ? WHERE id = ?");
			byte idx = 1;

			statement.setLong(idx++, pp.getBalance());
			statement.setString(idx++, Utils.toString(pp.getRanks(), true, ","));
			statement.setString(idx++, Utils.toString(pp.getPrivatePermissions(), true, ","));
			statement.setInt(idx++, pp.getNameColour().value());
			statement.setString(idx++, pp.getNickname());
			statement.setLong(idx++, pp.getSettings());
			statement.setShort(idx++, pp.getWarpCount());
			statement.setInt(idx++, pp.getCivilizationId());
			statement.setString(idx++, pp.getJob() == null ? null : pp.getJob().getName());
			
			long exp = pp.getCheckDonorExpiration(); // Needed since 0 isn't allowed apparently....?
			statement.setTimestamp(idx++, (exp > 60000 * 60 * 24) ? new Timestamp(exp) : null);
			
			statement.setInt(idx++, pp.getId());

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		saveBeanExperience(pp.getId(), pp.getSkills());
		saveArmourWardrobe(pp);
		savePickupBlacklist(pp.getId());
		savePlayerHeirlooms(pp);
		savePlayerStats(pp);
		refreshPlayerInbox(pp);
	}

	public static void saveProfileColumn(int playerId, String column, Object value) {
		Connection c = null;
		PreparedStatement statement = null;
		try {
			c = getNewConnection();
			statement = connection
					.prepareStatement("UPDATE " + table_profiles + " SET " + column + " = ? WHERE id = ?");
			if (value instanceof Character) // stupid
				statement.setString(1, ((Character)value).toString());
			else
				statement.setObject(1, value);
			statement.setInt(2, playerId);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}

	public static SkillData loadOrMakeBeanExperience(int databaseId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;

		try {
			c = getNewConnection();
			statement = connection.prepareStatement("SELECT * FROM " + table_experience + " WHERE playerId = ?");
			statement.setInt(1, databaseId);
			r = statement.executeQuery();
			if (r.next()) {
				HashMap<SkillType, SkillInfo> xpSources = new HashMap<SkillType, SkillInfo>();
				final SkillType[] st = SkillType.values();
				for (int x = 0; x < st.length; x++) {
					final SkillType src = st[x];
					xpSources.put(src, new SkillInfo(r.getLong(src.toString().toLowerCase() + "_xp")));
				}
				return new SkillData(xpSources);
			} else {
				close(c, statement);
				c = getNewConnection();
				statement = connection.prepareStatement("INSERT INTO " + table_experience + "(playerId) VALUES (?)");
				statement.setInt(1, databaseId);
				statement.executeUpdate();
				return new SkillData();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return null;
	}

	public static void saveBeanExperience(int databaseId, SkillData sd) {
		Connection c = null;
		PreparedStatement statement = null;
		try {
			c = getNewConnection();

			String statementStr = "UPDATE " + table_experience + " SET ";
			int max = SkillType.values().length;
			for (SkillType src : SkillType.values()) {
				max--;
				statementStr += src.toString().toLowerCase() + " = ?," + src.toString().toLowerCase() + "_xp = ?"
						+ (max > 0 ? "," : "");
			}

			statementStr += " WHERE playerId = ?";
			statement = connection.prepareStatement(statementStr);
			int idx = 1;

			for (SkillType src : SkillType.values()) {
				statement.setInt(idx++, sd.getSkillInfo(src).getLevel());
				statement.setLong(idx++, sd.getSkillInfo(src).getXp());
			}

			statement.setInt(idx++, databaseId);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}

	public static void logTransaction(int playerId, long amount, String log) {
		Connection c = null;
		PreparedStatement statement = null;
		try {
			c = getNewConnection();
			statement = connection.prepareStatement(
					"INSERT INTO " + table_moneylogs + " (time,playerId,amount,source) VALUES (?,?,?,?)");
			statement.setLong(1, System.currentTimeMillis() / 1000);
			statement.setInt(2, playerId);
			statement.setLong(3, amount);
			statement.setString(4, log);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void saveAll() {
		long then = System.currentTimeMillis();
		PlayerProfile.profileCache.asMap().values().forEach(profile -> saveProfile(profile));
		Main.getInstance().getDatasourceCore().saveAll();
		saveDirtyLootEntries();
		Utils.sendActionBar(Rank.ADMINISTRATOR, Component.text("\u00a7dSaved everything in roughly \u00a75" + (System.currentTimeMillis()-then) + "ms"));
	}

	public static void close(Object... c) {
		try {
			for (int i = 0; c != null && i < c.length; i++) {
				if (c[i] instanceof ResultSet && !((ResultSet) c[i]).isClosed()) {
					((ResultSet) c[i]).close();
				}
			}
			for (int i = 0; c != null && i < c.length; i++) {
				if (c[i] instanceof Statement && !((Statement) c[i]).isClosed()) {
					((Statement) c[i]).close();
				}
			}
			for (int i = 0; c != null && i < c.length; i++) {
				if (c[i] instanceof Connection && !((Connection) c[i]).isClosed()) {
					((Connection) c[i]).close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setHome(OfflinePlayer p, Location loc) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			PlayerProfile pp = PlayerProfile.from(p.getUniqueId());
			
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO homes (playerId, world, x, y, z, yaw, p) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
					+ "world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw), p = VALUES(p)");
			
			statement.setInt(1, pp.getId());
			statement.setInt(2, getWorldId(loc.getWorld()));
			statement.setFloat(3, (float)loc.getX());
			statement.setFloat(4, (float)loc.getY());
			statement.setFloat(5, (float)loc.getZ());
			statement.setFloat(6, (float)loc.getYaw());
			statement.setFloat(7, (float)loc.getPitch());
			
			statement.executeUpdate();
			statement.close();
			
			pp.setHome(loc);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static Location loadHome(int playerid) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			
			c = getNewConnection();
			statement = c.prepareStatement("SELECT world,x,y,z,yaw,p FROM homes WHERE playerID = ?");
			statement.setInt(1, playerid);
			
			r = statement.executeQuery();
			
			while(r.next())
				return new Location(getWorld(r.getShort("world")), r.getFloat("x"), r.getFloat("y"), r.getFloat("z"), r.getFloat("yaw"), r.getFloat("p"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		return null;
	}
	
	public static JSONObject loadPlayerHeirlooms(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT heirloomData FROM player_heirlooms WHERE playerId = ?");
			statement.setInt(1, playerId);
			r = statement.executeQuery();
			
			if (r.next()) 
				return new JSONObject(r.getString("heirloomData"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return null;
	}
	
	public static void savePlayerStats(PlayerProfile pp) {
		Connection c = null;
		PreparedStatement statement = null;
		
		PlayerStats stats = pp.getStats();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO stats (playerId, category, stat, value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value = VALUES(value)");
			statement.setInt(1, pp.getId());
			
			HashMap<StatType, HashMap<String, DirtyInteger>> map = stats.getMap();
			
			for (Entry<StatType, HashMap<String, DirtyInteger>> ent : map.entrySet()) {
				statement.setByte(2, ent.getKey().getId());
				
				for(Entry<String, DirtyInteger> entt : ent.getValue().entrySet()) {
					if (!entt.getValue().isDirty()) continue;
					
					statement.setString(3, entt.getKey());
					statement.setInt(4, entt.getValue().getValue());
					statement.executeUpdate();
					
					entt.getValue().setDirty(false);
				}
			}
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static PlayerStats loadPlayerStats(PlayerProfile pp) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final PlayerStats stats = new PlayerStats(pp);
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT category,stat,value FROM stats WHERE playerId = ?");
			statement.setInt(1, pp.getId());
			r = statement.executeQuery();
			
			while(r.next())
				stats.setStat(StatType.fromId(r.getByte(1)), r.getString(2), r.getInt(3), false);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return stats;
	}
	
	public static void savePlayerHeirlooms(PlayerProfile pp) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO player_heirlooms (playerId,heirloomData) VALUES (?,?) ON DUPLICATE KEY UPDATE heirloomData = VALUES(heirloomData)");
			
			statement.setInt(1, pp.getId());
			statement.setString(2, pp.getHeirlooms().getJsonData().toString());
			
			statement.executeUpdate();
			statement.close();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static ItemStack[] loadArmourWardrobe(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		ItemStack[] stacks = new ItemStack[20];
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT compressedArmorInventory FROM wardrobes WHERE playerId = ?");
			statement.setInt(1, playerId);
			
			r = statement.executeQuery();
			
			while(r.next())
				stacks = Utils.itemStackArrayFromBase64(r.getString("compressedArmorInventory"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		return stacks;
	}
	
	public static void saveArmourWardrobe(PlayerProfile pp) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO wardrobes (playerId,compressedArmorInventory) VALUES (?,?) ON DUPLICATE KEY UPDATE compressedArmorInventory = VALUES(compressedArmorInventory)");
			
			statement.setInt(1, pp.getId());
			statement.setString(2, Utils.itemStackArrayToBase64(pp.getArmourWardrobe()));
			
			statement.executeUpdate();
			statement.close();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static LinkedHashMap<Integer, Long> getStatHighscores(StatType type, String stat) {
		Connection c = null;
		PreparedStatement statement = null;
		LinkedHashMap<Integer, Long> map = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT playerId,value FROM stats WHERE category = ? AND stat = ? ORDER BY value ASC");
			statement.setInt(1, type.getId());
			statement.setString(2, stat);
			ResultSet rs = statement.executeQuery();
			
			map = new LinkedHashMap<Integer, Long>();
			
			while(rs.next())
				map.put(rs.getInt("playerId"), rs.getLong("value"));
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		
		return map;
	}
	
	public static LinkedHashMap<Integer, Long> getSkillHighscores(SkillType skill) {
		Connection c = null;
		PreparedStatement statement = null;
		LinkedHashMap<Integer, Long> map = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT playerId,"+skill.name()+"_xp FROM "+table_experience+" ORDER BY "+skill.name()+"_xp ASC");
			ResultSet rs = statement.executeQuery();
			
			map = new LinkedHashMap<Integer, Long>();
			
			while(rs.next())
				map.put(rs.getInt("playerId"), rs.getLong(skill.name()+"_xp"));
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		
		return map;
	}
	
	public static LinkedHashMap<Integer, Long> getHighscoreTotalSkillXp() {
		Connection c = null;
		PreparedStatement statement = null;
		LinkedHashMap<Integer, Long> map = null;
		
		try {
			c = getNewConnection();
			
			String st = "SELECT playerId, (";
			SkillType[] skills = SkillType.values();
			int size = skills.length;
			for (int x = -1; ++x < size;)
				st += skills[x].name().toLowerCase()+"_xp" + ((x < size-1) ? "+" : "");
			st += ") AS totalxp FROM " + table_experience + " ORDER BY totalxp ASC";
			
			statement = c.prepareStatement(st);
			ResultSet rs = statement.executeQuery();
			
			map = new LinkedHashMap<Integer, Long>();
			
			while(rs.next())
				map.put(rs.getInt("playerId"), rs.getLong("totalxp"));
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		
		return map;
	}
	
	public static void banPlayer(int bannerId, UUID playerUUID, long dura, String reason, String notes) {
		/*Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO bans (bannedUUID) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			
			statement.executeUpdate();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
			
		}*/
	}
	
	/**
	 * Gets a player's ban reason if they're banned.
	 */
	public static BanEntry getBanEntry(UUID playerUUID) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT banStart,banEnd,banReason FROM bans WHERE (disabled = 0 AND bannedUUID = ? AND (banEnd > ? OR banEnd <= ?)) ORDER BY banStart ASC");
			statement.setString(1, playerUUID.toString());
			statement.setLong(2, System.currentTimeMillis()/1000);
			statement.setLong(3, 0);
			
			rs = statement.executeQuery();
			
			while(rs.next())
				return new BanEntry(rs.getLong("banStart"), rs.getLong("banEnd"), rs.getString("banReason"));
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		
		return null;
	}
	
	/**
	 * Grab a player's ban entries
	 */
	public static BanEntry[] getBanEntries(UUID playerUUID) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		BanEntry[] entries = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT banStart,banEnd,banReason FROM bans WHERE (bannedUUID = ?) ORDER BY banStart ASC");
			statement.setString(1, playerUUID.toString());
			statement.setLong(2, System.currentTimeMillis()/1000);
			statement.setLong(3, 0);
			
			rs = statement.executeQuery();
			
			entries = new BanEntry[rs.getFetchSize()];
			
			int i = 0;
			
			while(rs.next())
				entries[i++] = new BanEntry(rs.getLong("banStart"), rs.getLong("banEnd"), rs.getString("banReason"));
			
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		
		return entries;
	}
	
	/**
	 * XXX: Used for the ProfileCache class on start-up, saves having to grab a profile in order to get their display name.
	 */
	public static void loadProfileCache() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		try {
			c = getNewConnection();
			statement = connection.prepareStatement("SELECT id,uuid,namecolour,name,nickname FROM " + table_profiles);
			r = statement.executeQuery();
			while (r.next()) {
				final String nick = r.getString("nickname");
				final String name = (nick != null ? nick : r.getString("name"));
				
				ProfileStore.updateStore(r.getInt("id"), UUID.fromString(r.getString("uuid")), r.getString("name"), name, r.getInt("namecolour"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
	}
	
	// XXX: Worlds
	
	public static void logCelestia(CelestiaAction action, LivingEntity entity, Location location, String data) {
		logCelestia(action, entity instanceof Player ? PlayerProfile.from(((Player)entity)).getId() : 0, location, data);
	}
	
	public static void logCelestia(CelestiaAction action, int id, Location loc, String data) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO celestia (playerId, action, world, x, y, z, data) VALUES (?,?,?,?,?,?,?)");
			
			if (loc == null)
				loc = new Location(Bukkit.getWorlds().get(0), 0, 80, 0);
			
			statement.setInt(1, id);
			statement.setInt(2, action.ordinal());
			statement.setInt(3, getWorldId(loc.getWorld()));
			statement.setShort(4, (short)loc.getX());
			statement.setShort(5, (short)loc.getY());
			statement.setShort(6, (short)loc.getZ());
			statement.setString(7, data);
			
			statement.executeUpdate();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static ArrayList<String> loadPickupBlacklist(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		final ArrayList<String> array = new ArrayList<String>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT blacklist FROM pickup_blacklists WHERE playerId = ?");
			statement.setInt(1, playerId);
			
			r = statement.executeQuery();
			
			while(r.next()) {
				String string = r.getString("blacklist");
				if (string == null || string.isEmpty())
					break;
				String[] list = string.split(",");
				for (String s : list)
					if (!s.isEmpty())
						array.add(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return array;
	}
	
	public static ArrayList<UpdateEntry> loadNews() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		final ArrayList<UpdateEntry> array = new ArrayList<UpdateEntry>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM update_books WHERE visible = 1 ORDER BY id DESC");
			r = statement.executeQuery();
			
			while(r.next()) {
				ArrayList<Component> stuff = new ArrayList<Component>();
				
				final String[] pages = r.getString("contents").replace("\r", "").split("`");
				
				for (String page : pages) {
					Component pageContent = Component.text("");
					final String[] linkScan = page.split("¬");
					
					int x;
					for (x = 0; x < linkScan.length; x++) {
						if (x % 2 == 1) {
							// Format: ¬Text to be Clicked,ACTION,VALUE OF ACTION,Hover Information¬
							String[] link = linkScan[x].split(",");
							pageContent = pageContent.append(Component.text(ChatColor.translateAlternateColorCodes('&', link[0]))
									.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(link[1]), link[2]))
									.hoverEvent(HoverEvent.showText(Component.text(ChatColor.translateAlternateColorCodes('&', link[3])))));
						} else {
							pageContent = pageContent.append(Component.text(ChatColor.translateAlternateColorCodes('&', linkScan[x])));
						}
					}
					
					stuff.add(pageContent);
				}
				
				array.add(new UpdateEntry(r.getInt("writer"), r.getString("title"), r.getString("description"), r.getTimestamp("creationTime"), Material.matchMaterial(r.getString("materialCover")), stuff));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return array;
	}
	
	public static void savePickupBlacklist(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			final PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
			StringBuilder sb = new StringBuilder();
			for (String entry : pp.getPickupBlacklist())
				sb.append(entry + ",");
			
			
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO pickup_blacklists (playerId, blacklist) VALUES (?,?) ON DUPLICATE KEY UPDATE blacklist = VALUES(blacklist)");
			statement.setInt(1, playerId);
			statement.setString(2, sb.toString());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	// XXX: Loot
	
	public static void loadAllLoot() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final LootManager lm = Main.getInstance().lootManager();
		long then = System.currentTimeMillis();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM loot");
			r = statement.executeQuery();
			
			while(r.next()) {
				final String itemType = r.getString("itemType");
				final String compressedStack = r.getString("compressedStack");
				final LootTable table = lm.getOrCreateTable(r.getString("tableName"));
				ItemStack i = null;
				
				if (compressedStack != null) {
					i = Utils.itemStackFromBase64(compressedStack);
				} else {
					try {
						i = new ItemStack(Material.valueOf(itemType));
					} catch (Exception e) {
						BeanItem bi = BeanItem.from(itemType);
						if (bi == null)
							i = new ItemStack(Material.DIAMOND);
						else
							i = bi.getItemStack();
					}
				}
				
				String s = r.getString("data");
				Material cookedType = null;
				try {
					cookedType = Material.valueOf(r.getString("cookedType"));
				} catch (Exception e) {
				}
				
				table.addEntry(new LootEntry(r.getInt("id"), table, i, cookedType, r.getInt("minStack"), r.getInt("maxStack"), r.getFloat("chance"), r.getFloat("chanceLuckChange"), s == null ? null : new JSONObject(s)).setFlags(r.getByte("flags")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
			Main.getInstance().getLogger().info("Loaded " + lm.getAllEntries().size() + " Loot Entries in " + (System.currentTimeMillis()-then) + "ms");
		}
	}
	
	public static void saveDirtyLootEntries() {
		final ArrayList<LootEntry> entries = Main.getInstance().lootManager().getAllEntries();
		final int size = entries.size();
		
		for (int x = -1; ++x < size;) {
			LootEntry entry = entries.get(x);
			if (!entry.isDirty()) continue;
			updateLootEntry(entry);
			entry.setDirty(false);
		}
	}
	
	public static void updateLootEntry(LootEntry entry) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE loot SET tableName = ?, chance = ?, chanceLuckChange = ?, minStack = ?, maxStack = ?, itemType = ?, flags = ?, compressedStack = ?, data = ?, cookedType = ? WHERE id = ?");
			statement.setString(1, entry.getTable().getName());
			statement.setFloat(2, entry.getChance());
			statement.setFloat(3, entry.getLuckEffectiveness());
			statement.setInt(4, entry.getMinStackSize());
			statement.setInt(5, entry.getMaxStackSize());
			
			String s = entry.getDisplayStack().getType().name();
			BeanItem bi = BeanItem.from(entry.getDisplayStack());
			if (bi != null)
				s = bi.getIdentifier();
			
			statement.setString(6, s);
			statement.setByte(7, entry.getFlags());
			
			if (entry.shouldCompress())
				statement.setString(8, Utils.toBase64(entry.getDisplayStack()));
			else
				statement.setString(8, null);
			
			statement.setInt(9, entry.getId());
			
			JSONObject data = entry.getJsonData(); // Additional json information such as enchantments, levels and chances.
			statement.setString(9, data != null ? data.toString() : null);
			statement.setString(10, entry.getCookedMaterial().name());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static LootEntry registerLootEntry(LootTable table, ItemStack itemStack, int min, int max, int chance, int luckEffectiveness, byte flags) throws SQLException {
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO loot (tableName, chance, chanceLuckChange, minStack, maxStack, itemType, flags) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, table.getName());
			statement.setFloat(2, chance);
			statement.setFloat(3, luckEffectiveness);
			statement.setInt(4, min);
			statement.setInt(5, max);
			
			String s = itemStack.getType().name();
			BeanItem bi = BeanItem.from(itemStack);
			if (bi != null)
				s = bi.getIdentifier();
			
			statement.setString(6, s);
			statement.setByte(7, flags);
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if (rs.next())
				return new LootEntry(rs.getInt(1), table, itemStack, min, max, chance, luckEffectiveness).setFlags(flags);
			else
				throw new SQLException("Could not create new Loot Entry.");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, rs, statement);
		}
		return null;
	}
	
	public static HashMap<Integer, Long> grabLinkedDiscordAccounts() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		HashMap<Integer, Long> discords = new HashMap<Integer, Long>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM discords");
			r = statement.executeQuery();
			
			while(r.next()) {
				if (r.getLong("discordId") < 1) continue;
				discords.put(r.getInt("playerId"), r.getLong("discordId"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return discords;
	}
	
	public static boolean setDiscordLink(int playerId, long discordId) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO discords (playerId, discordId) VALUES (?,?) ON DUPLICATE KEY UPDATE discordId = VALUES(discordId)");
			statement.setInt(1, playerId);
			statement.setLong(2, discordId);
			
			statement.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		return false;
	}
	
	public static void loadAllCivilizations() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		long then = System.currentTimeMillis();
		
		loadStructures();
		loadJobPayouts();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT id,founderId,name,bank FROM civilizations");
			r = statement.executeQuery();
			
			while(r.next())
				new Civilization(r.getInt("id"), r.getInt("founderId"), r.getString("name"), r.getLong("bank"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
			loadCivilizationCitizens();
			loadCivilizationStructures();
			Civilization.getCivilizations().forEach((civ) -> civ.checkUnlocks());
			Main.getInstance().getLogger().info("Loaded " + Civilization.size() + " Civilizations in " + (System.currentTimeMillis()-then) + "ms");
		}
	}
	
	// XXX: Civilization and Jobs
	public static void saveCivilization(Civilization civ) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE civilizations SET name = ?, bank = ? WHERE id = ?");
			byte idx = 1;
			
			statement.setString(idx++, civ.getName());
			statement.setLong(idx++, civ.getTreasury());
			//statement.setInt(idx++, civ.tie);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static Civilization createCivilization(int creator, String name) throws SQLException {
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO civilizations (name, founderId) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			byte idx = 1;
			statement.setString(idx++, name);
			statement.setInt(idx++, creator);
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			Civilization civ = new Civilization(rs.getInt(1), creator, name, 0);
			if (creator > 0)
				PlayerProfile.fromIfExists(creator).setCivilization(civ);
			return civ;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, rs, statement);
		}
		return null;
	}
	
	public static void setCitizenship(int civId, int playerId, CitizenTier level) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO civilization_citizens (civId, playerId, level) VALUES (?,?,?) ON DUPLICATE KEY UPDATE civId = VALUES(civId), level = VALUES(level)");
			statement.setInt(1, civId);
			statement.setInt(2, playerId);
			statement.setInt(3, level.ordinal());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void removeCitizenship(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM civilization_citizens WHERE playerId = ?");
			statement.setInt(1, playerId);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	private static void loadStructures() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM structure_types");
			rs = statement.executeQuery();
			
			while(rs.next()) {
				String reqJson = rs.getString("requirements");
				new Structures(rs.getInt("id"), rs.getString("name"), rs.getInt("cost"), rs.getString("description"), reqJson != null ? new JSONObject(reqJson) : null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
	}
	
	private static void loadJobPayouts() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM job_payouts WHERE enabled = 1");
			rs = statement.executeQuery();
			
			while(rs.next()) {
				Job job = Job.getByName(rs.getString("job"));
				String object = rs.getString("object");
				int pay = rs.getInt("pay");
				try {
					if (job instanceof IMiningJob || job instanceof IFishingJob) {
						Material m = Material.valueOf(object.toUpperCase());
						if (m != null) {
							if (job instanceof IMiningJob && m.isBlock()) {
								job.addPayment(m.name(), pay);
								continue;
							} else if (job instanceof IFishingJob && !m.isBlock()) {
								job.addPayment(m.name(), pay);
								continue;
							}
						}
					}
					if (job instanceof IHuntingJob) {
						EntityType m = EntityType.valueOf(object.toUpperCase());
						if (m != null) {
							job.addPayment(m.name(), pay);
							continue;
						}
					}
				} catch (Exception e) {
					continue;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
	}
	
	private static void loadCivilizationCitizens() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM civilization_citizens");
			rs = statement.executeQuery();
			
			final CitizenTier[] levels = CitizenTier.values();
			
			while(rs.next()) {
				final Civilization civ = Civilization.getById(rs.getInt("civId"));
				civ.getCitizens().put(rs.getInt("playerId"), levels[rs.getInt("level")]);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
	}
	
	private static void loadCivilizationStructures() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM civilization_structures");
			rs = statement.executeQuery();
			
			while(rs.next()) {
				final Civilization civ = Civilization.getById(rs.getInt("civId"));
				World w = getWorld(rs.getShort("world"));
				if (w == null) w = Bukkit.getWorlds().get(0);
				Location loc = new Location(w, rs.getFloat("x"), rs.getFloat("y"), rs.getFloat("z"), rs.getFloat("yaw"), rs.getFloat("p"));
				
				Status status = Status.PENDING;
				try {
					status = Status.values()[rs.getInt("status")];
				} catch (Exception e) {}
				
				civ.getStructures().add(new Structure(civ, Structures.getStructure(rs.getInt("structure")), loc, rs.getInt("requesterId"), rs.getInt("reviewerId"), rs.getInt("cost"), status));
				
			//	civ.addCitizen(r.getInt("playerId"), levels[r.getInt("rank")]);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
	}
	
	public static HashMap<String, VoteService> getValidVoteServices() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		HashMap<String, VoteService> services = new LinkedHashMap<String, VoteService>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT service,displayName,sapphireOut,coinsOut FROM valid_vote_services WHERE enabled = 1");
			rs = statement.executeQuery();
			
			while(rs.next()) {
				String service = rs.getString("service");
				services.put(service, new VoteService(service, rs.getString("displayName"), rs.getInt("sapphireOut"), rs.getInt("coinsOut")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		return services;
	}
	
	// XXX: Modify Requests
	
	public static ArrayList<ProfileModifyRequest> loadPendingModifyRequests() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		ArrayList<ProfileModifyRequest> requests = new ArrayList<ProfileModifyRequest>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM player_modify_requests WHERE status = 0 ORDER BY requestDate DESC"); // order from oldest to newest
			rs = statement.executeQuery();
			
			while(rs.next()) {
				int id = rs.getInt("id");
				ModifyType type = null;
				try {
					type = ModifyType.valueOf(rs.getString("requestType"));
				} catch (Exception e) {
					continue;
				}
				
				requests.add(new ProfileModifyRequest(id, rs.getInt("playerId"), rs.getTimestamp("requestDate").getTime(), rs.getString("data"), type));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		return requests;
	}
	
	public static ProfileModifyRequest createModifyRequest(int playerId, ModifyType type, String newData) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO player_modify_requests (playerId, requestType, data) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, playerId);
			statement.setString(2, type.name());
			statement.setString(3, newData);
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			
			return new ProfileModifyRequest(rs.getInt(1), playerId, System.currentTimeMillis(), newData, type);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		return null;
	}
	
	public static void reviewModifyRequest(ProfileModifyRequest request) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE player_modify_requests SET status = ?, reviewerId = ?, reviewDate = ? WHERE id = ?");
			statement.setInt(1, request.getStatus().ordinal());
			statement.setInt(2, request.getReviewerId());
			statement.setTimestamp(3, new Timestamp(request.getReviewTime()));
			
			statement.setInt(4, request.getId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	// XXX: Delivery shit
	
	public static boolean registerDelivery(int playerId, int senderId, long expiryTime, DeliveryType type, String title, String message, JSONObject content) {
		if (content == null || content.isEmpty()) return false;
		
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO player_inbox (playerId, senderId, expiryDate, type, title, message, content) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, playerId);
			statement.setInt(2, senderId);
			statement.setTimestamp(3, expiryTime <= 0L ? null : new Timestamp(expiryTime));
			statement.setString(4, type.name());
			statement.setString(5, title);
			statement.setString(6, message);
			statement.setString(7, content.toString());
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			
			if (ProfileStore.from(playerId, true) != null && ProfileStore.from(playerId).isOnline()) {
				PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
				Timestamp tsOne = rs.getTimestamp(4);
				
				pp.getInbox().add(new Delivery(rs.getInt(1), playerId, senderId, tsOne == null ? 0L : tsOne.getTime(), 0L, expiryTime, type, title, message, content));
			}
			
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		return false;
	}
	
	public static void updateDelivery(Delivery delivery) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE player_inbox SET expiryDate = ?, openDate = ?, content = ?, deleted = ? WHERE id = ?");
			statement.setTimestamp(1, delivery.getExpiryDate() <= 0L ? null : new Timestamp(delivery.getExpiryDate()));
			statement.setTimestamp(2, delivery.getOpenDate() <= 0L ? null : new Timestamp(delivery.getOpenDate()));
			statement.setString(3, delivery.getJson().toString());
			statement.setBoolean(4, delivery.toBeRemoved());
			statement.setInt(5, delivery.getId());
			
			statement.executeUpdate();
			delivery.setDirty(false);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	private static List<Delivery> loadPlayerInbox(PlayerProfile profile) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		final List<Delivery> deliveries = new ArrayList<Delivery>();
		
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
				DeliveryType type = DeliveryType.PACKAGE;
				try {
					type = DeliveryType.valueOf(r.getString("type"));
				} catch (Exception e) {}
				
				deliveries.add(new Delivery(r.getInt("id"), r.getInt("playerId"), r.getInt("senderId"), tsOne == null ? 0L : tsOne.getTime(), tsTwo == null ? 0L : tsTwo.getTime(), 
						tsThree == null ? 0L : tsThree.getTime(), type, r.getString("title"), r.getString("message"), content == null ? null : new JSONObject(content)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		return deliveries;
	}
	
	public static void savePlayerDirtyInbox(PlayerProfile profile) {
		List<Delivery> deliveries = profile.getInbox();
		int size = deliveries.size();
		for (int x = -1; ++x < size;) {
			Delivery delivery = deliveries.get(x);
			if (delivery.isDirty())
				updateDelivery(delivery);
		}
	}
	
	public static void refreshPlayerInbox(PlayerProfile profile) {
		savePlayerDirtyInbox(profile);
		profile.getInbox().clear();
		profile.getInbox().addAll(loadPlayerInbox(profile));
	}
	
}
