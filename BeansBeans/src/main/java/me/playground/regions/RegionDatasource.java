package me.playground.regions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.dynmap.markers.AreaMarker;

import me.playground.data.DynmapDatasource;
import me.playground.main.Main;
import me.playground.regions.flags.Flag;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.worlds.WorldManager;

/**
 * An instance of {@link DynmapDatasource} for {@link Region} data management.
 * @author Mogubean
 */
public class RegionDatasource extends DynmapDatasource<Region> {
	
	private final RegionManager manager;
	private final WorldManager wm;
	
	public RegionDatasource(Main plugin, RegionManager manager) {
		super(plugin, "Regions");
		this.manager = manager;
		wm = plugin.getWorldManager();
	}
	
	protected WorldManager getWorldManager() {
		return wm;
	}
	
	/**
	 * Load all of the regions, including world regions.
	 */
	@Override
	public void loadAll() {
		// We clear the existing region list. Also note that the declaration of a region automatically adds it to the respective world's regionmap and list.
		manager.getAllRegions().clear();
		long then = System.currentTimeMillis();
		int count = 0;
		
		for (World world : wm.getWorlds())
			manager.initWorldRegion(world);
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM regions WHERE id > 0"); ResultSet r = s.executeQuery()) {
			while(r.next()) {
				int id = r.getInt("id");
				World w = wm.getWorld(r.getInt("world"));
				if (w == null) continue; // Entirely possible that a world could be deleted, don't bother loading the regions if so.
				Region reg = new Region(manager, id, r.getInt("creatorId"), r.getInt("priority"), r.getString("name"), w, r.getInt("minX"), r.getInt("minY"), r.getInt("minZ"), r.getInt("maxX"), r.getInt("maxY"), r.getInt("maxZ"));
				updateMarker(reg);
				count++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				loadAllRegionsFlags();
				loadAllRegionsMembers();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		getPlugin().getSLF4JLogger().info("Loaded " + count + " Regions in " + (System.currentTimeMillis()-then) + "ms");
	}

	@Override
	public void saveAll() {
		saveDirtyRegions();
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Flag<V>, V> void saveDirtyRegions() {
		try {
			for (Region region : manager.getAllRegions()) {
				if (region.isDirty())
					saveRegion(region);
				
				if (region.getDirtyFlags().isEmpty()) continue;
				final int size = region.getDirtyFlags().size();
				for (int x = -1; ++x < size;) {
					T flag = (T) region.getDirtyFlags().get(x);
					V val = region.getFlag(flag);
					if (val == null)
						removeRegionFlag(region.getRegionId(), flag.getName());
					else
						setRegionFlag(region.getRegionId(), flag.getName(), flag.marshal(val));
				}
				region.getDirtyFlags().clear();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void saveRegion(Region region) throws SQLException {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE regions SET name = ?, priority = ?, parent = ?, world = ?, minX = ?, minY = ?, minZ = ?, maxX = ?, maxY = ?, maxZ = ? WHERE id = ?")) {
			s.setString(1, region.getName());
			s.setInt(2, region.getPriority());
			s.setInt(3, region.getParentId());
			s.setInt(4, wm.getWorldId(region.getWorld()));
			s.setInt(5, region.getMinimumPoint().getBlockX());
			s.setInt(6, region.getMinimumPoint().getBlockY());
			s.setInt(7, region.getMinimumPoint().getBlockZ());
			s.setInt(8, region.getMaximumPoint().getBlockX());
			s.setInt(9, region.getMaximumPoint().getBlockY());
			s.setInt(10, region.getMaximumPoint().getBlockZ());
			s.setInt(11, region.getRegionId());
			s.executeUpdate();
			
			region.setDirty(false);
		}
	}
	
	public Region createNewRegion(int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		Region reg = null;
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO regions (name, priority, parent, creatorId, world, minX, minY, minZ, maxX, maxY, maxZ) VALUES (?,?,?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
			s.setString(1, name);
			s.setInt(2, priority);
			s.setInt(3, parent);
			s.setInt(4, creator);
			s.setInt(5, wm.getWorldId(world));
			s.setInt(6, min.getBlockX());
			s.setInt(7, min.getBlockY());
			s.setInt(8, min.getBlockZ());
			s.setInt(9, max.getBlockX());
			s.setInt(10, max.getBlockY());
			s.setInt(11, max.getBlockZ());
			s.executeUpdate();
			
			ResultSet rs = s.getGeneratedKeys();
			rs.next();
			reg = new Region(manager, rs.getInt(1), creator, priority, parent, name, world, min, max);
			updateMarker(reg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reg;
	}
	
	/**
	 * Delete the specified region's entries from the database.
	 * <p><b>This should only be called once every active reference has been removed.</b>
	 */
	public void deleteRegion(Region region) {
		try(Connection c = getNewConnection(); 
				PreparedStatement s = c.prepareStatement("DELETE r, rm, rf FROM regions AS r "
				+ "LEFT OUTER JOIN region_members AS rm ON r.id = rm.id "
				+ "LEFT OUTER JOIN region_flags AS rf ON r.id = rf.id "
				+ "WHERE r.id = ?")) {
			s.setInt(1, region.getRegionId());
			s.executeUpdate();
			removeMarker(region);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// XXX: Flags
	
	private void setRegionFlag(int regionId, String flagName, String marshal) throws SQLException {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO region_flags (id, flag, value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
			s.setInt(1, regionId);
			s.setString(2, flagName);
			s.setString(3, marshal);
			s.executeUpdate();
		}
	}
	
	private void removeRegionFlag(int regionId, String flagName) throws SQLException {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM region_flags WHERE id = ? AND flag = ?")) {
			s.setInt(1, regionId);
			s.setString(2, flagName);
			s.executeUpdate();
		}
	}
	
	private void loadAllRegionsFlags() throws SQLException {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM region_flags"); ResultSet r = s.executeQuery()) {
			while(r.next()) {
				Flag<?> flag = Flags.getFlag(r.getString("flag"));
				if (flag == null) continue;
				Region region = manager.getRegion(r.getInt("id"));
				if (region == null) continue;
				
				region.setFlag(flag, flag.unmarshal(r.getString("value")), false);
			}
		}
	}
	
	// XXX: Members
	
	public void setRegionMember(int regionId, int playerId, MemberLevel level) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO region_members (id, playerId, rank) VALUES (?,?,?) ON DUPLICATE KEY UPDATE rank = VALUES(rank)");) {
			s.setInt(1, regionId);
			s.setInt(2, playerId);
			s.setInt(3, level.ordinal());
			s.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeRegionMember(int regionId, int playerId) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM region_members WHERE id = ? AND playerId = ?")) {
			s.setInt(1, regionId);
			s.setInt(2, playerId);
			s.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadAllRegionsMembers() throws SQLException {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM region_members"); ResultSet r = s.executeQuery()) {
			MemberLevel[] levels = MemberLevel.values();
			
			while(r.next()) {
				Region region = manager.getRegion(r.getInt("id"));
				if (region == null) continue;
				region.addMember(r.getInt("playerId"), levels[r.getInt("rank")], false);
			}
		}
	}
	
	// XXX: Markers
	
	@Override
	public void updateMarker(Region r) {
		if (!isDynmapEnabled()) return;
		
		BlockVector min = r.getMinimumPoint();
		BlockVector max = r.getMaximumPoint();
		
		AreaMarker am = markerSet.createAreaMarker("region."+r.getName(), r.getName(), true, r.getWorld().getName(), 
				new double[] {min.getX(), max.getX()+1}, new double[] {min.getZ(), max.getZ()+1}, false);
		
		am.setRangeY(min.getY(), max.getY()+1);
		am.setDescription("Region: " + r.getName() + "\nTest");
		am.setLineStyle(1, 4, 0x5755bf84);
		am.setFillStyle(1, 0x5755bf54);
	}

	@Override
	public void removeMarker(Region r) {
		if (!isDynmapEnabled()) return;
		
		AreaMarker m = markerSet.findAreaMarker("region."+r.getName());
		if (m != null) m.deleteMarker();
	}
	
}
