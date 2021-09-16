package me.playground.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerSet;
import org.json.JSONObject;

import me.playground.celestia.logging.CelestiaAction;
import me.playground.gui.UpdateEntry;
import me.playground.items.BeanItem;
import me.playground.loot.LootEntry;
import me.playground.loot.LootManager;
import me.playground.loot.LootTable;
import me.playground.main.Main;
import me.playground.npc.NPC;
import me.playground.npc.NPCManager;
import me.playground.npc.NPCType;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillInfo;
import me.playground.playerprofile.skills.SkillType;
import me.playground.playerprofile.stats.DirtyInteger;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.regions.flags.Flag;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.shop.Shop;
import me.playground.shop.ShopLog;
import me.playground.utils.Utils;
import me.playground.warps.Warp;
import me.playground.warps.WarpType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
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
				if (connection != null && !connection.isClosed()) {
					return;
				}
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = getNewConnection();
				Main.getInstance().getLogger().info("Connection to MySQL Database was successful!");
			}
		} catch (SQLException e) {
			Main.getInstance().getLogger().severe("There was an issue connecting to the MySQL Database.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
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
			e.printStackTrace();
		}
		return connection;
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
				statement.setInt(3, Rank.NEWBEAN.getRankColour());
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
		ArrayList<Rank> ranks = new ArrayList<Rank>();
		String[] ranksStr;
		try {
			ranksStr = rs.getString("ranks").split(",");
			for (String rankStr : ranksStr) {
				try {
					ranks.add(Rank.fromString(rankStr));
				} catch (IllegalArgumentException e) {
				}
			}
			return new PlayerProfile(rs.getInt("id"), UUID.fromString(rs.getString("uuid")), ranks,
					rs.getInt("namecolour"), rs.getString("name"), rs.getString("nickname"),
					rs.getLong("coins"), rs.getLong("booleanSettings"), rs.getShort("warpCount"));
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
			statement = connection.prepareStatement("UPDATE " + table_profiles + " SET " + "coins = ?, ranks = ?,"
					+ "namecolour = ?, nickname = ?, booleanSettings = ?, warpCount = ? WHERE id = ?");
			byte idx = 1;

			statement.setLong(idx++, pp.getBalance());
			statement.setString(idx++, Utils.toString(pp.getRanks(), true, ","));
			statement.setInt(idx++, pp.getNameColour().value());
			statement.setString(idx++, pp.getNickname());
			statement.setLong(idx++, pp.getSettings());
			statement.setShort(idx++, pp.getWarpCount());
			
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
	}

	public static void saveProfileColumn(PlayerProfile pp, String column, Object value) {
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
			statement.setInt(2, pp.getId());

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

	// XXX: NPCs
	
	public static int saveNewNPC(int playerId, String npcName, Location npcLoc) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		int npcId = -1;
		
		try {
			c = getNewConnection();
			statement = connection.prepareStatement("INSERT INTO " + table_npcs
					+ " (npcName,creatorId,creationTime,world,xyzyp) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, npcName);
			statement.setInt(2, playerId);
			statement.setLong(3, System.currentTimeMillis() / 1000);
			statement.setShort(4, WorldUUIDToId.get(npcLoc.getWorld().getUID()));

			statement.setString(5, npcLoc.getX() + "," + npcLoc.getY() + "," + npcLoc.getZ() + "," + npcLoc.getYaw()
					+ "," + npcLoc.getPitch());

			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if (rs.next())
				npcId = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		return npcId;
	}

	public static void saveDirtyNPC(NPC<?> npc) {
		Connection c = null;
		PreparedStatement statement = null;
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE " + table_npcs + " SET npcName = ?,creatorId = ?,world = ?,xyzyp = ?, data = ? WHERE npcId = ?");
			statement.setString(1, npc.getEntity().getName());
			
			Location npcLoc = npc.getLocation();
			
			statement.setInt(2, npc.getCreatorId());
			statement.setShort(3, WorldUUIDToId.get(npcLoc.getWorld().getUID()));
			statement.setString(4, npcLoc.getX() + "," + npcLoc.getY() + "," + npcLoc.getZ() + "," + npcLoc.getYaw()
			+ "," + npcLoc.getPitch());
			
			JSONObject cunt = npc.getJsonData();
			
			
			statement.setString(5, cunt == null ? null : cunt.toString());
			statement.setInt(6, npc.getDatabaseId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void loadAllNPCs() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final NPCManager npcManager = Main.getInstance().npcManager();
		
		try {
			c = getNewConnection();
			statement = connection
					.prepareStatement("SELECT npcId,npcName,creatorId,world,xyzyp,data FROM " + table_npcs);
			r = statement.executeQuery();
			while (r.next()) {
				int npcId = r.getInt("npcId");
				int creatorId = r.getInt("creatorId");
				String npcName = r.getString("npcName");
				String json = r.getString("data");
				String[] nls = r.getString("xyzyp").split(",");
				Location npcLoc = new Location(Bukkit.getWorld(WorldIdToUUID.get(r.getShort("world"))),
						Double.parseDouble(nls[0]), Double.parseDouble(nls[1]), Double.parseDouble(nls[2]),
						Float.parseFloat(nls[3]), Float.parseFloat(nls[4]));
				
				World world = npcLoc.getWorld();
				if (world == null)
					continue;
				
				
				npcManager.loadNPC(creatorId, npcLoc, NPCType.HUMAN, npcName, npcId, json != null ? new JSONObject(json) : null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		Main.getInstance().getLogger().info("Loaded " + npcManager.getDatabaseNPCs().size() + " NPCs!");
	}
	
	public static void saveDirtyNPCs() {
		Main.getInstance().npcManager().getDatabaseNPCs().forEach((npc) -> {
			if (npc.isDirty()) {
				saveDirtyNPC(npc);
				npc.setClean();
			}
		});
	}

	public static void saveAll() {
		PlayerProfile.profileCache.asMap().values().forEach(profile -> saveProfile(profile));
		saveDirtyRegions();
		saveDirtyShops();
		saveDirtyWarps();
		saveDirtyNPCs();
		saveDirtyLootEntries();
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
			statement.setShort(2, WorldUUIDToId.get(loc.getWorld().getUID()));
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
				return new Location(Bukkit.getWorld(WorldIdToUUID.get(r.getShort("world"))), r.getFloat("x"), r.getFloat("y"), r.getFloat("z"), r.getFloat("yaw"), r.getFloat("p"));
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
			statement = c.prepareStatement("SELECT heirloomData FROM heirlooms WHERE playerId = ?");
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
			statement = c.prepareStatement("INSERT INTO STATS (playerId, category, stat, value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value = VALUES(value)");
			statement.setInt(1, pp.getId());
			
			HashMap<StatType, HashMap<String, DirtyInteger>> map = stats.getMap();
			
			for (Entry<StatType, HashMap<String, DirtyInteger>> ent : map.entrySet()) {
				statement.setByte(2, ent.getKey().getId());
				
				for(Entry<String, DirtyInteger> entt : ent.getValue().entrySet()) {
					if (!entt.getValue().isDirty()) return;
					
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
			statement = c.prepareStatement("INSERT INTO heirlooms (playerId,heirloomData) VALUES (?,?) ON DUPLICATE KEY UPDATE heirloomData = VALUES(heirloomData)");
			
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
	
	// XXX: WARPS
	
	public static HashMap<String, Warp> loadAllWarps() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final HashMap<String, Warp> warpList = new HashMap<String, Warp>();
		long then = System.currentTimeMillis();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM warps ORDER BY warpId ASC");
			r = statement.executeQuery();
			
			final WarpType[] warpVals = WarpType.values();
			
			while(r.next()) {
				final String name = r.getString("warpName");
				final String materialName = r.getString("warpItem");
				
				final String invitedIdsS = r.getString("invitedIds");
				final String[] invitedIds = invitedIdsS == null ? null : invitedIdsS.split(",");
				final List<Integer> invIds = new ArrayList<Integer>();
				if (invitedIds != null)
					for (String number : invitedIds)
						invIds.add(Integer.parseInt(number.trim()));
				
				final String bannedIdsS = r.getString("bannedIds");
				final String[] bannedIds = bannedIdsS == null ? null : bannedIdsS.split(",");
				final List<Integer> banIds = new ArrayList<Integer>();
				if (bannedIds != null)
					for (String number : bannedIds)
						banIds.add(Integer.parseInt(number.trim()));
				
				final Material m = materialName != null ? Material.valueOf(materialName) : null;
				Location l = null;
				try {
					l = new Location(Bukkit.getWorld(WorldIdToUUID.get(r.getShort("world"))), r.getFloat("x"), r.getFloat("y"), r.getFloat("z"), r.getFloat("yaw"), r.getFloat("p"));
				} catch (Exception e) {
					Main.getInstance().getLogger().warning("There was a problem loading the warp: " + name);
					e.printStackTrace();
				}
					final Warp w = new Warp(
							r.getInt("warpId"), 
							r.getInt("playerId"), 
							r.getInt("creatorId"), 
							name, 
							m,
							r.getString("description"),
							r.getBoolean("public"), 
							r.getBoolean("locked"), 
							r.getInt("useCount"), 
							invIds, 
							banIds,
							l);
					w.setType(warpVals[r.getInt("type")]);
					warpList.put(name.toLowerCase(), w);
					if (w.isPublic() && warpset != null)
						markPublicWarp(w);
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		Main.getInstance().getLogger().info("Loaded " + warpList.size() + " Warps in " + (System.currentTimeMillis()-then) + "ms");
		return warpList;
	}
	
	public static boolean deleteWarp(Warp warp) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM warps WHERE warpId = ?");
			statement.setInt(1, warp.getWarpId());
			statement.executeUpdate();
			unmarkWarp(warp);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		return false;
	}
	
	public static Warp saveNewWarp(int creatorId, String warpName, Location loc) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO warps (playerId,creatorId,warpName,description,world,x,y,z,yaw,p) VALUES (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			statement.setInt(1, creatorId);
			statement.setInt(2, creatorId);
			statement.setString(3, warpName);
			statement.setString(4, "A warp by " + PlayerProfile.getDisplayName(creatorId).content());
			statement.setInt(5, WorldUUIDToId.get(loc.getWorld().getUID()));
			statement.setFloat(6, (float)loc.getX());
			statement.setFloat(7, (float)loc.getY());
			statement.setFloat(8, (float)loc.getZ());
			statement.setFloat(9, (float)loc.getYaw());
			statement.setFloat(10, (float)loc.getPitch());
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			Warp w = new Warp(rs.getInt(1), creatorId, creatorId, warpName, null, "A warp by " + PlayerProfile.getDisplayName(creatorId).content(), false, false, 0, new ArrayList<Integer>(), new ArrayList<Integer>(), loc);
			Main.getWarpManager().addNewWarp(w);
			return w;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(rs, c, statement);
		}
		return null;
	}
	
	public static void saveDirtyWarps() {
		for (Warp warp : Main.getWarpManager().getWarps().values()) {
			if (warp.isDirty()) {
				saveDirtyWarp(warp);
				warp.setDirty(false);
			}
		}
	}
	
	public static void saveDirtyWarp(Warp warp) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement(""
					+ "UPDATE warps SET playerId = ?, warpName = ?, warpItem = ?, description = ?, public = ?, type = ?, locked = ?, useCount = ?, invitedIds = ?, bannedIds = ?, "
					+ "world = ?, x = ?, y = ?, z = ?, yaw = ?, p = ? WHERE warpId = ?");
			
			statement.setInt(1, warp.getOwnerId());
			statement.setString(2, warp.getName());
			statement.setString(3, (warp.getItem()) == null ? null : warp.getItem().toString());
			statement.setString(4, warp.getDescription());
			statement.setBoolean(5, warp.isPublic());
			statement.setInt(6, warp.getType().ordinal());
			statement.setBoolean(7, warp.isLocked());
			statement.setInt(8, warp.getUseCount());
			
			StringBuilder invited = new StringBuilder();
			final List<Integer> invitedIds = warp.getInvitedIds();
			for (int x = 0; x < invitedIds.size(); x++)
				invited.append(invitedIds.get(x) + ((x+1) < invitedIds.size() ? "," : ""));
			
			statement.setString(9, invitedIds.size() < 1 ? null : invited.toString());
			
			StringBuilder banned = new StringBuilder();
			final List<Integer> bannedIds = warp.getBannedIds();
			for (int x = 0; x < bannedIds.size(); x++)
				invited.append(bannedIds.get(x) + ((x+1) < bannedIds.size() ? "," : ""));
			statement.setString(10, bannedIds.size() < 1 ? null : banned.toString());
			
			final Location loc = warp.getLocation();
			
			statement.setInt(11, WorldUUIDToId.get(loc.getWorld().getUID()));
			statement.setFloat(12, (float)loc.getX());
			statement.setFloat(13, (float)loc.getY());
			statement.setFloat(14, (float)loc.getZ());
			statement.setFloat(15, (float)loc.getYaw());
			statement.setFloat(16, (float)loc.getPitch());
			
			statement.setInt(17, warp.getWarpId());
			
			statement.executeUpdate();
			statement.close();
			
			unmarkWarp(warp);
			if (warp.isPublic() && warpset != null)
				markPublicWarp(warp);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}

	// XXX: SHOPS
	public static HashMap<Integer, Shop> loadAllShops() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final HashMap<Integer, Shop> shopList = new HashMap<Integer, Shop>();
		final long then = System.currentTimeMillis();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM shops");
			r = statement.executeQuery();
			
			while(r.next()) {
				final int id = r.getInt("id");
				final String item = r.getString("compressedItem");
				final World w = Bukkit.getWorld(WorldIdToUUID.get(r.getShort("world")));
				
				// Don't bother loading shops that reside in non-loaded worlds.
				if (w == null)
					continue;
				
				final Shop s = new Shop(
						id, 
						r.getInt("ownerId"), 
						new Location(w, r.getShort("x")+0.5F, r.getShort("y")-0.38F, r.getShort("z")+0.5F, r.getShort("yaw"), 0), 
						r.getInt("maxStorage"),
						r.getInt("itemsStored"),
						item == null ? null : Utils.itemStackFromBase64(item), 
						r.getInt("storedMoney"), 
						r.getInt("totalMoneyEarned"), 
						r.getInt("totalMoneyTaxed"),
						r.getInt("sellPrice"),
						r.getInt("buyPrice"));
				shopList.put(id, s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		Main.getInstance().getLogger().info("Loaded " + shopList.size() + " Shops in " + (System.currentTimeMillis()-then) + "ms");
		return shopList;
	}
	
	public static void saveDirtyShops() {
		for (Shop shop : Main.getShopManager().getShops()) {
			if (shop.isDirty()) {
				saveDirtyShop(shop);
				shop.setDirty(false);
			}
		}
	}
	
	public static void saveDirtyShop(Shop shop) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement(""
					+ "UPDATE shops SET maxStorage = ?, itemsStored = ?, compressedItem = ?, storedMoney = ?, totalMoneyEarned = ?, totalMoneyTaxed = ?, sellPrice = ?, buyPrice = ?, ownerId = ? WHERE id = ?");
			
			statement.setInt(1, shop.getMaxItemQuantity());
			statement.setInt(2, shop.getItemQuantity());
			statement.setString(3, shop.getItemStack() == null ? null : Utils.itemStackToBase64(shop.getItemStack()));
			statement.setInt(4, shop.getStoredMoney());
			statement.setInt(5, shop.getTotalMoneyEarned());
			statement.setInt(6, shop.getTotalMoneyTaxed());
			statement.setInt(7, shop.getSellPrice());
			statement.setInt(8, shop.getBuyPrice());
			statement.setInt(9, shop.getOwnerId());
			statement.setInt(10, shop.getShopId());
			
			statement.executeUpdate();
			
			markShop(shop);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static boolean deleteShop(Shop shop) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM shops WHERE id = ?");
			statement.setInt(1, shop.getShopId());
			statement.executeUpdate();
			unmarkShop(shop);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
		return false;
	}
	
	public static Shop createNewShop(int creatorId, Location loc) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO shops (ownerId,world,x,y,z,yaw) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			statement.setInt(1, creatorId);
			statement.setInt(2, WorldUUIDToId.get(loc.getWorld().getUID()));
			statement.setFloat(3, (int)loc.getX());
			statement.setFloat(4, (int)loc.getY());
			statement.setFloat(5, (int)loc.getZ());
			statement.setFloat(6, (int)loc.getYaw());
			
			statement.executeUpdate();
			r = statement.getGeneratedKeys();
			if (r.next()) {
				return new Shop(r.getInt(1), creatorId, loc.add(0.5, -0.38, 0.5), 1728);
			} else {
				throw new SQLException("Failed to create shop, ID couldn't be obtained from Database..");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement, r);
		}
		return null;
	}
	
	public static boolean logShopAction(int shopId, int playerId, String comment, String data) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO shops_log (shopId,playerId,comment,data) VALUES (?,?,?,?)");
			
			statement.setInt(1, shopId);
			statement.setInt(2, playerId);
			statement.setString(3, comment);
			statement.setString(4, data);
			
			statement.executeUpdate();
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement, r);
		}
		return false;
	}
	
	public static ArrayList<ShopLog> loadShopLogs(Integer shopId) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final ArrayList<ShopLog> logList = new ArrayList<ShopLog>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT time,playerId,comment,data FROM shops_log WHERE shopId = ?");
			statement.setInt(1, shopId);
			r = statement.executeQuery();
			
			while(r.next()) {
				final ShopLog s = new ShopLog(
						r.getTimestamp("time"),
						r.getInt("playerId"),
						r.getString("comment"),
						r.getString("data")
						);
				logList.add(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
		
		return logList;
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
				
				ProfileStore.updateStore(r.getInt("id"), UUID.fromString(r.getString("uuid")), r.getString("name"), Component.text(name).color(TextColor.color(r.getInt("namecolour"))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
	}
	
	// XXX: Worlds
	
	@SuppressWarnings("deprecation")
	public static void registerWorld(World world) {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO worlds (name, uuid, seed, environment, type) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			statement.setString(1, world.getName());
			statement.setString(2, world.getUID().toString());
			statement.setLong(3, world.getSeed());
			statement.setString(4, world.getEnvironment().name());
			statement.setString(5, world.getWorldType().name());
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			WorldIdToUUID.put(rs.getShort(1), world.getUID());
			WorldUUIDToId.put(world.getUID(), rs.getShort(1));
			Main.getRegionManager().registerWorld(-rs.getShort(1), world);
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void logCelestia(CelestiaAction action, LivingEntity entity, Location location, String data) {
		logCelestia(action, entity instanceof Player ? PlayerProfile.from(((Player)entity)).getId() : 0, location, data);
	}
	
	public static void logCelestia(CelestiaAction action, int id, Location location, String data) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO celestia (playerId, action, world, x, y, z, data) VALUES (?,?,?,?,?,?,?)");
			
			statement.setInt(1, id);
			statement.setInt(2, action.ordinal());
			statement.setInt(3, WorldUUIDToId.get(location.getWorld().getUID()));
			statement.setShort(4, (short)location.getX());
			statement.setShort(5, (short)location.getY());
			statement.setShort(6, (short)location.getZ());
			statement.setString(7, data);
			
			statement.executeUpdate();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	private static HashMap<Short, UUID> WorldIdToUUID = new HashMap<Short, UUID>();
	public static HashMap<UUID, Short> WorldUUIDToId = new HashMap<UUID, Short>();
	
	public static void loadWorlds() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM worlds");
			r = statement.executeQuery();
			
			while(r.next()) {
				final short id = r.getShort("id");
				final UUID uuid = UUID.fromString(r.getString("uuid"));
				WorldIdToUUID.put(id, uuid);
				WorldUUIDToId.put(uuid, id);
				
				WorldCreator wc = new WorldCreator(r.getString("name"));
				wc.type(WorldType.valueOf(r.getString("type")));
				wc.environment(Environment.valueOf(r.getString("environment")));
				wc.seed(r.getLong("seed"));
				
				final World w = wc.createWorld();
				w.setGameRule(GameRule.DISABLE_RAIDS, true);
				w.setGameRule(GameRule.KEEP_INVENTORY, true);
				w.setGameRule(GameRule.MOB_GRIEFING, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
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
	
	// XXX: Regions
	
	private final static DynmapAPI dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
	private final static MarkerSet markerset = dynmap != null ? dynmap.getMarkerAPI().createMarkerSet("me.playground.markers.regions", "Regions", dynmap.getMarkerAPI().getMarkerIcons(), false) : null;
	private final static MarkerSet warpset = dynmap != null ? dynmap.getMarkerAPI().createMarkerSet("me.playground.markers.warps", "Warps", dynmap.getMarkerAPI().getMarkerIcons(), false) : null;
	private static MarkerSet shopset = dynmap != null ? dynmap.getMarkerAPI().createMarkerSet("me.playground.markers.shops", "Shops", dynmap.getMarkerAPI().getMarkerIcons(), false) : null;
	
	public static void deleteShopMarkers() {
		shopset = dynmap != null ? dynmap.getMarkerAPI().createMarkerSet("me.playground.markers.shops", "Shops", dynmap.getMarkerAPI().getMarkerIcons(), false) : null;
	}
	
	public static void markShop(Shop s) {
		if (shopset == null)
			return;
		if (s.getItemStack() == null || (s.getBuyPrice() == 0 && s.getSellPrice() == 0))
			return;
		
		Location l = s.getLocation();
		
		shopset.createMarker("shop."+s.getShopId(), "Player Shop", true, l.getWorld().getName(), l.getX(), l.getY()+1, l.getZ(), dynmap.getMarkerAPI().getMarkerIcon("diamond"), false);
	}
	
	public static void unmarkShop(Shop s) {
		if (shopset == null)
			return;
		
		Marker m = shopset.findMarker("shop."+s.getShopId());
		if (m != null)
			m.deleteMarker();
	}
	
	public static void unmarkWarp(Warp w) {
		if (warpset == null)
			return;
		
		Marker m = warpset.findMarker("warp."+w.getWarpId());
		if (m != null)
			m.deleteMarker();
	}
	
	private static void markRegion(Region r) {
		if (markerset == null)
			return;
		BlockVector min = r.getMinimumPoint();
		BlockVector max = r.getMaximumPoint();
		
		AreaMarker am = markerset.createAreaMarker("region."+r.getName(), r.getName(), true, r.getWorld().getName(), 
				new double[] {min.getX(), max.getX()+1}, 
				new double[] {min.getZ(), max.getZ()+1}, false);
		if (am != null) {
			am.setRangeY(min.getY(), max.getY()+1);
			am.setDescription("Region: " + r.getName());
			am.setFillStyle(1, 0x5755bf54);
			am.setLineStyle(1, 2.2, 0x5755bf84);
		}
	}
	
	private static void markPublicWarp(Warp w) {
		if (warpset == null)
			return;
		if (w.getWorld() == null)
			return;
		
		Location l = w.getLocation();
		Marker m = warpset.createMarker("warp."+w.getWarpId(), w.getName(), true, w.getWorld().getName(), l.getX(), l.getY(), l.getZ(), dynmap.getMarkerAPI().getMarkerIcon("portal"), false);
		if (m != null)
			m.setDescription("/warp "+w.getName());
	}
	
	public static void loadAllRegions() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final RegionManager rm = Main.getRegionManager();
		
		rm.getAllRegions().clear();
		long then = System.currentTimeMillis();
		int count = 0;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM regions");
			r = statement.executeQuery();
			
			while(r.next()) {
				int id = r.getInt("id");
				if (id < 1)
					continue;
				World w = Bukkit.getWorld(WorldIdToUUID.get(r.getShort("world")));
				
				Region reg = new Region(rm, id, r.getInt("creatorId"), r.getInt("priority"), r.getString("name"), w, r.getInt("minX"), r.getInt("minY"), r.getInt("minZ"), r.getInt("maxX"), r.getInt("maxY"), r.getInt("maxZ"));
				if (dynmap != null)
					markRegion(reg);
				
				count++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
			loadAllRegionsMembers();
			loadAllRegionsFlags();
			Main.getInstance().getLogger().info("Loaded " + count + " Regions in " + (System.currentTimeMillis()-then) + "ms");
		}
	}
	
	public static void saveDirtyRegions() {
		for (Region region : Main.getRegionManager().getAllRegions()) {
			if (region.isDirty()) {
				saveRegion(region.getRegionId(), region.getPriority(), region.getParentId(), region.getName(), region.getWorld(), region.getMinimumPoint(), region.getMaximumPoint());
				region.setDirty(false);
			}
		}
	}
	
	public static void saveRegion(int regionId, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("UPDATE regions SET name = ?, priority = ?, parent = ?, world = ?, minX = ?, minY = ?, minZ = ?, maxX = ?, maxY = ?, maxZ = ? WHERE id = ?");
			statement.setString(1, name);
			statement.setInt(2, priority);
			statement.setInt(3, parent);
			statement.setShort(4, WorldUUIDToId.get(world.getUID()));
			statement.setInt(5, min.getBlockX());
			statement.setInt(6, min.getBlockY());
			statement.setInt(7, min.getBlockZ());
			statement.setInt(8, max.getBlockX());
			statement.setInt(9, max.getBlockY());
			statement.setInt(10, max.getBlockZ());
			statement.setInt(11, regionId);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static Region createNewRegion(int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) throws SQLException {
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO regions (name, priority, parent, creatorId, world, minX, minY, minZ, maxX, maxY, maxZ) VALUES (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, name);
			statement.setInt(2, priority);
			statement.setInt(3, parent);
			statement.setInt(4, creator);
			statement.setShort(5, WorldUUIDToId.get(world.getUID()));
			statement.setInt(6, min.getBlockX());
			statement.setInt(7, min.getBlockY());
			statement.setInt(8, min.getBlockZ());
			statement.setInt(9, max.getBlockX());
			statement.setInt(10, max.getBlockY());
			statement.setInt(11, max.getBlockZ());
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			rs.next();
			Region r = new Region(Main.getRegionManager(), rs.getInt(1), creator, priority, parent, name, world, min, max);
			markRegion(r);
			return r;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, rs, statement);
		}
		return null;
	}
	
	public static void setRegionMember(int regionId, int playerId, MemberLevel level) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO region_members (id, playerId, rank) VALUES (?,?,?) ON DUPLICATE KEY UPDATE rank = VALUES(rank)");
			statement.setInt(1, regionId);
			statement.setInt(2, playerId);
			statement.setInt(3, level.ordinal());
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void removeRegionMember(int regionId, int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM region_members WHERE id = ? AND playerId = ?");
			statement.setInt(1, regionId);
			statement.setInt(2, playerId);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	private static void loadAllRegionsMembers() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM region_members");
			r = statement.executeQuery();
			
			final RegionManager rm = Main.getRegionManager();
			final MemberLevel[] levels = MemberLevel.values();
			
			while(r.next()) {
				final Region region = rm.getRegion(r.getInt("id"));
				region.addMember(r.getInt("playerId"), levels[r.getInt("rank")], false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
		}
	}
	
	public static void setRegionFlag(int regionId, String flagName, String marshal) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO region_flags (id, flag, value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = VALUES(value)");
			statement.setInt(1, regionId);
			statement.setString(2, flagName);
			statement.setString(3, marshal);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static void removeRegionFlag(int regionId, String flagName) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM region_flags WHERE id = ? AND flag = ?");
			statement.setInt(1, regionId);
			statement.setString(2, flagName);
			
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	private static void loadAllRegionsFlags() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		final RegionManager rm = Main.getRegionManager();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM region_flags");
			r = statement.executeQuery();
			
			while(r.next()) {
				final Region region = rm.getRegion(r.getInt("id"));
				final Flag<?> flag = Flags.getFlag(r.getString("flag"));
				if (flag == null)
					continue;
				region.setFlag(flag, flag.unmarshal(r.getString("value")), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(r, c, statement);
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
					Utils.itemStackFromBase64(compressedStack);
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
				
				table.addEntry(new LootEntry(r.getInt("id"), table, i, r.getInt("minStack"), r.getInt("maxStack"), r.getInt("chance"), s == null ? null : new JSONObject(s)).setFlags(r.getByte("flags")));
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
			statement = c.prepareStatement("UPDATE loot SET tableName = ?, chance = ?, minStack = ?, maxStack = ?, itemType = ?, flags = ?, compressedStack = ?, data = ? WHERE id = ?");
			statement.setString(1, entry.getTable().getName());
			statement.setInt(2, entry.getChance());
			statement.setInt(3, entry.getMinStackSize());
			statement.setInt(4, entry.getMaxStackSize());
			
			String s = entry.getDisplayStack().getType().name();
			BeanItem bi = BeanItem.from(entry.getDisplayStack());
			if (bi != null)
				s = bi.getIdentifier();
			
			statement.setString(5, s);
			statement.setByte(6, entry.getFlags());
			
			if (entry.shouldCompress())
				statement.setString(7, Utils.itemStackToBase64(entry.getDisplayStack()));
			else
				statement.setString(7, null);
			
			statement.setInt(8, entry.getId());
			
			JSONObject data = entry.getJsonData(); // Additional json information such as enchantments, levels and chances.
			statement.setString(9, data != null ? data.toString() : null);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(c, statement);
		}
	}
	
	public static LootEntry registerLootEntry(LootTable table, ItemStack itemStack, int min, int max, int chance, byte flags) throws SQLException {
		Connection c = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("INSERT INTO loot (tableName, chance, minStack, maxStack, itemType, flags) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, table.getName());
			statement.setInt(2, chance);
			statement.setInt(3, min);
			statement.setInt(4, max);
			
			String s = itemStack.getType().name();
			BeanItem bi = BeanItem.from(itemStack);
			if (bi != null)
				s = bi.getIdentifier();
			
			statement.setString(5, s);
			statement.setByte(6, flags);
			
			statement.executeUpdate();
			rs = statement.getGeneratedKeys();
			if (rs.next())
				return new LootEntry(rs.getInt(1), table, itemStack, min, max, chance).setFlags(flags);
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
	
}
