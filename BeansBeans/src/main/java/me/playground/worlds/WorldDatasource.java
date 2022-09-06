package me.playground.worlds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.World.Environment;

import me.playground.data.PrivateDatasource;
import me.playground.main.Main;

public class WorldDatasource extends PrivateDatasource {
	private final WorldManager manager;

	protected WorldDatasource(Main plugin, WorldManager manager) {
		super(plugin);
		this.manager = manager;
	}

	@Override
	public void loadAll() {
		List<World> toRemake = new ArrayList<>();
		
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM worlds WHERE enabled = 1"); ResultSet r = s.executeQuery()) {
			while(r.next()) {
				short id = r.getShort("id");

				String name = r.getString("name");
				if (name == null) continue;

				String uuidString = r.getString("uuid");
				UUID uuid = null;
				try {
					uuid = UUID.fromString(uuidString);
				} catch (Exception ignored) {}

				long seed = r.getLong("seed");
				String predefinedWorldString = r.getString("predefinedGenerator");

				WorldType type = WorldType.NORMAL;
				try {
					type = WorldType.valueOf(r.getString("type"));
				} catch (Exception ignored) {}

				Environment environment = Environment.NORMAL;
				try {
					environment = Environment.valueOf(r.getString("environment"));
				} catch (Exception ignored) {}

				final int border = r.getInt("borderSize");
				WorldCreator wc = new WorldCreator(name);
				wc.type(type);
				wc.environment(environment);
				if (seed != 0) // If 0 randomize
					wc.seed(seed);

				PredefinedWorld predefinedWorld = PredefinedWorld.getPredefinedWorld(predefinedWorldString);
				World world = predefinedWorld == null ? wc.createWorld() : predefinedWorld.createWorld(wc);

				if (world != null) {
					world.setGameRule(GameRule.DISABLE_RAIDS, true);
					world.setGameRule(GameRule.MOB_GRIEFING, true);
					world.getWorldBorder().setSize(border);

					manager.addWorldToMap(id, world);

					if (uuid != world.getUID())
						toRemake.add(world);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			for(World w : toRemake)
				remakeWorldEntry(w);
		}
		getPlugin().getSLF4JLogger().info("Loaded " + manager.getWorlds().size() + " Worlds");
	}

	@Override
	public void saveAll() {
	}
	
	// Exists to make sure the database is up-to-date when a world happens to be remade on server start up.
	private void remakeWorldEntry(World world) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("UPDATE worlds SET uuid = ?, creationDate = ? WHERE name = ?")) {
			s.setString(1, world.getUID().toString());
			s.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			s.setString(3, world.getName());
			s.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation") // TODO: Different way to get the world type.
	protected void registerWorld(World world) {
		try(Connection c = getNewConnection(); PreparedStatement s = c.prepareStatement("INSERT INTO worlds (name, uuid, seed, environment, type) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
			s.setString(1, world.getName());
			s.setString(2, world.getUID().toString());
			s.setLong(3, world.getSeed());
			s.setString(4, world.getEnvironment().name());
			s.setString(5, world.getWorldType().name());
			s.executeUpdate();
			
			ResultSet rs = s.getGeneratedKeys();
			rs.next();
			
			manager.addWorldToMap(rs.getShort(1), world);
			plugin.regionManager().initWorldRegion(world);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
