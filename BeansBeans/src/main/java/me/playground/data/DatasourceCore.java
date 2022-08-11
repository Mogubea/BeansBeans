package me.playground.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.dynmap.DynmapAPI;

import me.playground.main.IPluginRef;
import me.playground.main.Main;

public class DatasourceCore implements IPluginRef {
	
	private final DynmapAPI dynmap;
	private final Set<PrivateDatasource> registeredSources = new HashSet<PrivateDatasource>();
	
	private final Main plugin;
	private final String host, database, username, password;
	private final int port;
	
	private Connection connection;
	
	public DatasourceCore(Main pl) {
		host = pl.getConfig().getString("host");
		port = pl.getConfig().getInt("port");
		username = pl.getConfig().getString("username");
		database = pl.getConfig().getString("database");
		password = pl.getConfig().getString("password");
		plugin = pl;
		
		dynmap = (DynmapAPI) pl.getServer().getPluginManager().getPlugin("dynmap");
		if (dynmap == null)
			pl.getSLF4JLogger().warn("DynmapAPI was not found, attempting to continue without DynmapAPI features.");
		
		try {
			synchronized (pl) {
				if (connection != null && !connection.isClosed()) return;
				
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = getNewConnection();
				pl.getSLF4JLogger().info("Successfully established an MySQL Connection!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Establish a new MySQL {@link Connection}.
	 * @return the new {@link Connection}.
	 */
	public Connection getNewConnection() throws SQLException {
		try {
			if (connection != null)
				connection.close();
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
		} catch (SQLException e) {
			getPlugin().getSLF4JLogger().error("Could not establish a new MySQL Connection instance.");
			e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * @return the current {@link Connection}.
	 */
	public Connection getConnection() {
		return connection;
	}
	
	public void close(Object... c) {
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
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	/**
	 * @return is dynmapAPI enabled?
	 */
	public boolean isDynmapEnabled() {
		return dynmap != null;
	}
	
	/**
	 * Please check {@link #isDynmapEnabled()} before using this.
	 * @return the dynmapAPI.
	 */
	public DynmapAPI getDynmapAPI() {
		return dynmap;
	}
	
	protected void registerDatasource(PrivateDatasource source) {
		this.registeredSources.add(source);
	}
	
	/**
	 * Save everything.
	 */
	public void saveAll() {
		registeredSources.forEach(datasource -> { datasource.saveAll(); });
	}
	
}
