package me.playground.npc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.bukkit.Location;
import org.bukkit.World;
import org.json.JSONObject;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;

/**
 * An instance of {@link PrivateDatasource} for {@link NPC} data management.
 * @author Mogubean
 */
public class NPCDatasource extends PrivateDatasource {
	private final NPCManager manager;
	
	public NPCDatasource(Main plugin, NPCManager manager) {
		super(plugin);
		this.manager = manager;
	}
	
	/**
	 * Load all NPCs
	 */
	@Override
	public void loadAll() {
		long then = System.currentTimeMillis();
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT npcId,npcName,type,creatorId,world,x,y,z,yaw,p,data FROM npcs"); ResultSet r = s.executeQuery()) {
			while (r.next()) {
				int npcId = r.getInt("npcId");
				int creatorId = r.getInt("creatorId");
				
				NPCType type = NPCType.HUMAN;
				try {
					type = NPCType.valueOf(r.getString("type"));
				} catch (Exception ignored) {}
				
				String npcName = r.getString("npcName");
				String json = r.getString("data");
				Location npcLoc = new Location(getWorld((r.getShort("world"))), r.getFloat("x"), r.getFloat("y"), r.getFloat("z"), r.getInt("yaw"), r.getInt("p"));
				
				World world = npcLoc.getWorld();
				if (world == null) continue;
				
				manager.loadNPC(creatorId, npcLoc, type, npcName, npcId, json != null ? new JSONObject(json) : null);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		getPlugin().getSLF4JLogger().info("Loaded " + manager.getDatabaseNPCs().size() + " NPCs in " + (System.currentTimeMillis()-then) + "ms");
	}

	/**
	 * Save all dirty NPCs
	 */
	@Override
	public void saveAll() {
		manager.getDatabaseNPCs().forEach((npc) -> {
			if (!npc.isDirty()) return;
			saveDirtyNPC(npc);
		});
	}
	
	/**
	 * Put new NPC info into the database
	 * @return an ID for the new NPC to use.
	 */
	protected int createNewNPC(int playerId, String npcName, Location npcLoc) {
		int npcId = -1;
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO npcs (npcName,creatorId,creationTime,world,x,y,z,yaw,p) VALUES (?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);) {
			int idx = 1;
			
			s.setString(idx++, npcName);
			s.setInt(idx++, playerId);
			s.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));
			s.setInt(idx++, getWorldId(npcLoc.getWorld()));
			s.setFloat(idx++, (float) npcLoc.getX());
			s.setFloat(idx++, (float) npcLoc.getY());
			s.setFloat(idx++, (float) npcLoc.getZ());
			s.setInt(idx++, (int) npcLoc.getYaw()); // No real need for the depth of floats
			s.setInt(idx++, (int) npcLoc.getPitch());
			s.executeUpdate();
			
			ResultSet rs = s.getGeneratedKeys();
			if (rs.next())
				npcId = rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return npcId;
	}

	private void saveDirtyNPC(NPC<?> npc) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE npcs SET npcName = ?,creatorId = ?,world = ?,x = ?, y = ?, z = ?, yaw = ?, p = ?, data = ? WHERE npcId = ?");) {
			int idx = 1;
			Location npcLoc = npc.getLocation();
			
			s.setString(idx++, npc.getEntity().getBukkitEntity().getName());
			s.setInt(idx++, npc.getCreatorId());
			s.setInt(idx++, getWorldId(npcLoc.getWorld()));
			s.setFloat(idx++, (float) npcLoc.getX());
			s.setFloat(idx++, (float) npcLoc.getY());
			s.setFloat(idx++, (float) npcLoc.getZ());
			s.setInt(idx++, (int) npcLoc.getYaw()); // No real need for the depth of floats
			s.setInt(idx++, (int) npcLoc.getPitch());
			
			JSONObject cunt = npc.getJsonData();
			
			s.setString(idx++, cunt == null ? null : cunt.toString());
			s.setInt(idx++, npc.getId());
			s.executeUpdate();
			npc.setClean();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
