package me.playground.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

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
import me.playground.main.Main;
import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.DeliveryType;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.ProfileModifyRequest.ModifyType;
import me.playground.playerprofile.ProfileStore;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.stats.DirtyInteger;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.skills.Skill;
import me.playground.skills.SkillInfo;
import me.playground.skills.Skills;
import me.playground.utils.Utils;
import me.playground.discord.voting.VoteService;
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
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&character_set_server=utf8mb4", username,
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
			final ArrayList<Rank> ranks = new ArrayList<>();
			final HashSet<String> perms = new HashSet<>();
			
			String[] ranksStr;
			ranksStr = rs.getString("ranks").split(",");
			for (String rankStr : ranksStr) {
				try {
					ranks.add(Rank.fromString(rankStr));
				} catch (IllegalArgumentException ignored) {
				}
			}
			
			// Load perms
			String permz = rs.getString("permissions");
			if (permz != null) {
				String[] permsStr = permz.split(",");
				Collections.addAll(perms, permsStr);
			}
			
			final Civilization civ = Civilization.getById(rs.getInt("civilization"));
			final Job job = Job.getByName(rs.getString("job"));
			final Timestamp donoExpiry = rs.getTimestamp("donorRankExpiration");
			final long donoExpiration = donoExpiry == null ? 0L : donoExpiry.getTime();
			final PlayerProfile pp = new PlayerProfile(Main.getInstance().getProfileManager(), rs.getInt("id"), UUID.fromString(rs.getString("uuid")), ranks, perms,
					rs.getInt("namecolour"), rs.getString("name"), rs.getString("nickname"),
					rs.getDouble("coins"), rs.getLong("booleanSettings"));
			pp.setDonorExpiration(donoExpiration);
			pp.setCivilization(civ);
			pp.setJob(job, true);
			
			return pp;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Asynchronous.
	 */
	public static void saveProfileColumn(int playerId, String column, Object value) {
		try(Connection c = getNewConnection(); PreparedStatement statement = c.prepareStatement("UPDATE " + table_profiles + " SET " + column + " = ? WHERE id = ?")) {
			if (value instanceof Character) // stupid
				statement.setString(1, ((Character)value).toString());
			else
				statement.setObject(1, value);
			statement.setInt(2, playerId);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Asynchronous.
	 */
	public static void logTransaction(int playerId, double amount, String log) {
		Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), task -> {
			try(Connection c = getNewConnection(); PreparedStatement statement = c.prepareStatement("INSERT INTO " + table_moneylogs + " (time,playerId,amount,source) VALUES (?,?,?,?)")) {
				statement.setLong(1, System.currentTimeMillis() / 1000);
				statement.setInt(2, playerId);
				statement.setDouble(3, amount);
				statement.setString(4, log);
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
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
			
			map = new LinkedHashMap<>();
			
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
	
	public static LinkedHashMap<Integer, Long> getSkillHighscores(Skill skill) {
		Connection c = null;
		PreparedStatement statement = null;
		LinkedHashMap<Integer, Long> map = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT playerId,"+skill.getName()+"_xp FROM "+table_experience+" ORDER BY "+skill.getName()+"_xp ASC");
			ResultSet rs = statement.executeQuery();
			
			map = new LinkedHashMap<>();
			
			while(rs.next())
				map.put(rs.getInt("playerId"), rs.getLong(skill.getName()+"_xp"));
			
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
			
			for (Skill skill : Skill.getRegisteredSkills())
				st += skill.getName()+"_xp+";
			st = st.substring(0, st.length() - 1);
			st += ") AS totalxp FROM " + table_experience + " ORDER BY totalxp ASC";
			
			statement = c.prepareStatement(st);
			ResultSet rs = statement.executeQuery();
			
			map = new LinkedHashMap<>();
			
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
	
	// XXX: Worlds

	public static ArrayList<UpdateEntry> loadNews() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		final ArrayList<UpdateEntry> array = new ArrayList<>();
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("SELECT * FROM update_books WHERE visible = 1 ORDER BY id DESC");
			r = statement.executeQuery();
			
			while(r.next()) {
				ArrayList<Component> stuff = new ArrayList<>();
				
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
	
	// XXX: Loot
	
	public static HashMap<Integer, Long> grabLinkedDiscordAccounts() {
		Connection c = null;
		PreparedStatement statement = null;
		ResultSet r = null;
		
		HashMap<Integer, Long> discords = new HashMap<>();
		
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
	
	public static boolean breakDiscordLink(int playerId) {
		Connection c = null;
		PreparedStatement statement = null;
		
		try {
			c = getNewConnection();
			statement = c.prepareStatement("DELETE FROM discords WHERE playerId = ?");
			statement.setInt(1, playerId);
			
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
						job.addPayment(m.name(), pay);
					}
				} catch (Exception ignored) {
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
		
		HashMap<String, VoteService> services = new LinkedHashMap<>();
		
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

	public static boolean registerDelivery(int playerId, int senderId, long expiryTime, DeliveryType type, String title, String message, JSONObject content) {
		if (content == null || content.isEmpty()) return false;

		ResultSet rs = null;

		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO player_inbox (playerId, senderId, expiryDate, type, title, message, content) " +
				"VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
			int idx = 0;

			s.setInt(++idx, playerId);
			s.setInt(++idx, senderId);
			s.setTimestamp(++idx, expiryTime <= 0L ? null : new Timestamp(expiryTime));
			s.setString(++idx, type.name());
			s.setString(++idx, title);
			s.setString(++idx, message);
			s.setString(++idx, content.toString());

			s.executeUpdate();
			rs = s.getGeneratedKeys();
			rs.next();

			PlayerProfile pp = PlayerProfile.fromIfExists(playerId);
			pp.getInbox().add(new Delivery(rs.getInt(1), playerId, senderId, System.currentTimeMillis(), 0L, expiryTime, type, title, message, content));

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void updateDelivery(Delivery delivery) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE player_inbox SET expiryDate = ?, openDate = ?, content = ?, deleted = ? WHERE id = ?")) {
			s.setTimestamp(1, delivery.getExpiryDate() <= 0L ? null : new Timestamp(delivery.getExpiryDate()));
			s.setTimestamp(2, delivery.getOpenDate() <= 0L ? null : new Timestamp(delivery.getOpenDate()));
			s.setString(3, delivery.getJson().toString());
			s.setBoolean(4, delivery.toBeRemoved());
			s.setInt(5, delivery.getId());

			s.executeUpdate();
			delivery.setDirty(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
