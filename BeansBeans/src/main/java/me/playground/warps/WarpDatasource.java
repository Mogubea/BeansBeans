package me.playground.warps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.dynmap.markers.Marker;

import me.playground.data.DynmapDatasource;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;

/**
 * An instance of {@link DynmapDatasource} for {@link Warp} data management.
 * @author Mogubean
 */
public class WarpDatasource extends DynmapDatasource<Warp> {
	private final WarpManager manager;
	
	public WarpDatasource(Main plugin, WarpManager manager) {
		super(plugin, "Warps");
		this.manager = manager;
	}
	
	/**
	 * Load all warps
	 */
	@Override
	public void loadAll() {
		long then = System.currentTimeMillis();
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM warps ORDER BY warpId ASC"); ResultSet r = s.executeQuery()) {
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
					l = new Location(getWorld(r.getShort("world")), r.getFloat("x"), r.getFloat("y"), r.getFloat("z"), r.getFloat("yaw"), r.getFloat("p"));
				} catch (Exception e) {
					getPlugin().getLog4JLogger().warn("There was a problem loading the warp: " + name);
					e.printStackTrace();
				}
				final Warp w = new Warp(manager,
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
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getPlugin().getLog4JLogger().info("Loaded " + manager.countWarps() + " Warps in " + (System.currentTimeMillis()-then) + "ms");
	}

	/**
	 * Save all dirty warps
	 */
	@Override
	public void saveAll() {
		manager.getWarps().values().forEach(warp -> {
			if (!warp.isDirty()) return;
			saveDirtyWarp(warp);
		});
	}
	
	/**
	 * Attempt to delete the specified {@link Warp} from the database.
	 * @return whether the deletion was successful or not.
	 */
	public boolean deleteWarp(Warp warp) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM warps WHERE warpId = ?")) {
			s.setInt(1, warp.getWarpId());
			s.executeUpdate();
			removeMarker(warp);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Attempt to create a new {@link Warp}.
	 * @return the newly created {@link Warp}, or {@code null} if unsuccessful.
	 */
	public Warp createWarp(int creatorId, String warpName, Location loc) {
		Warp w = null;
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO warps (playerId,creatorId,warpName,description,world,x,y,z,yaw,p) VALUES (?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);) {
			s.setInt(1, creatorId);
			s.setInt(2, creatorId);
			s.setString(3, warpName);
			s.setString(4, "A warp by " + PlayerProfile.getDisplayName(creatorId).content());
			s.setInt(5, getWorldId(loc.getWorld()));
			s.setFloat(6, (float)loc.getX());
			s.setFloat(7, (float)loc.getY());
			s.setFloat(8, (float)loc.getZ());
			s.setFloat(9, (float)loc.getYaw());
			s.setFloat(10, (float)loc.getPitch());
			s.executeUpdate();
			
			ResultSet r = s.getGeneratedKeys();
			r.next();
			w = new Warp(manager, r.getInt(1), creatorId, creatorId, warpName, null, "A warp by " + PlayerProfile.getDisplayName(creatorId).content(), false, false, 0, new ArrayList<Integer>(), new ArrayList<Integer>(), loc);
			r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return w;
	}
	
	private void saveDirtyWarp(Warp warp) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement(
				"UPDATE warps SET playerId = ?, warpName = ?, warpItem = ?, description = ?, public = ?, type = ?, locked = ?, useCount = ?, invitedIds = ?, bannedIds = ?, "
				+ "world = ?, x = ?, y = ?, z = ?, yaw = ?, p = ? WHERE warpId = ?")) {
			
			s.setInt(1, warp.getOwnerId());
			s.setString(2, warp.getName());
			s.setString(3, (warp.getItem()) == null ? null : warp.getItem().toString());
			s.setString(4, warp.getDescription());
			s.setBoolean(5, warp.isPublic());
			s.setInt(6, warp.getType().ordinal());
			s.setBoolean(7, warp.isLocked());
			s.setInt(8, warp.getUseCount());
			
			StringBuilder invited = new StringBuilder();
			final List<Integer> invitedIds = warp.getInvitedIds();
			for (int x = 0; x < invitedIds.size(); x++)
				invited.append(invitedIds.get(x) + ((x+1) < invitedIds.size() ? "," : ""));
			
			s.setString(9, invitedIds.size() < 1 ? null : invited.toString());
			
			StringBuilder banned = new StringBuilder();
			final List<Integer> bannedIds = warp.getBannedIds();
			for (int x = 0; x < bannedIds.size(); x++)
				invited.append(bannedIds.get(x) + ((x+1) < bannedIds.size() ? "," : ""));
			s.setString(10, bannedIds.size() < 1 ? null : banned.toString());
			
			final Location loc = warp.getLocation();
			
			s.setInt(11, getWorldId(loc.getWorld()));
			s.setFloat(12, (float)loc.getX());
			s.setFloat(13, (float)loc.getY());
			s.setFloat(14, (float)loc.getZ());
			s.setFloat(15, (float)loc.getYaw());
			s.setFloat(16, (float)loc.getPitch());
			s.setInt(17, warp.getWarpId());
			s.executeUpdate();
			
			updateMarker(warp);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// XXX: Markers
	
	@Override
	public void updateMarker(Warp w) {
		if (!isDynmapEnabled()) return;
		if (w.getWorld() == null) return;
		if (!w.isPublic()) { 
			removeMarker(w); 
		} else {
			Location l = w.getLocation();
			Marker m = getMarkerSet().createMarker("warp."+w.getWarpId(), w.getName(), true, w.getWorld().getName(), l.getX(), l.getY(), l.getZ(), getMarkerAPI().getMarkerIcon("portal"), false);
			if (m != null) m.setDescription("/warp "+w.getName());
		}
	}

	@Override
	public void removeMarker(Warp w) {
		if (!isDynmapEnabled()) return;
		
		Marker m = getMarkerSet().findMarker("warp."+w.getWarpId());
		if (m != null) m.deleteMarker();
	}
}
