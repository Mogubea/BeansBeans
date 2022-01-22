package me.playground.threads;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;

public class StatusResponseThread extends Thread {
	
	private final Main plugin;
	private final String host;
	private final int port;
	
	private ServerSocket server;
	
	private boolean enabled = true;
	
	public StatusResponseThread(final Main plugin, String host, int port) throws Exception {
		this.plugin = plugin;
		this.host = host;
		this.port = port;
		
		initialize();
	}
	
	private void initialize() throws Exception {
		server = new ServerSocket();
		server.bind(new InetSocketAddress(host, port));
		start();
	}
	
	public void shutdown() {
		enabled = false;
		if (server == null) return;
		
		try {
			server.close();
			plugin.getLog4JLogger().error("No longer listening for Status Requests.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// Main loop.
		while (enabled) {
			try {
				Socket socket = server.accept();
				socket.setSoTimeout(5000); // Don't hang on slow connections.
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				JSONObject obj = new JSONObject();
				obj.put("status", "Online");
				obj.put("version", plugin.getServer().getMinecraftVersion());
				obj.put("since", Main.STARTUP_TIME);
				JSONArray arr = new JSONArray();
				int size = 0;
				for (Player player : plugin.getServer().getOnlinePlayers()) {
					PlayerProfile pp = PlayerProfile.from(player);
					JSONObject pObj = new JSONObject();
					pObj.put("name", pp.getRealName());
					pObj.put("displayName", pp.getDisplayName());
					pObj.put("nameColour", "#"+pp.getNameColour().asHexString());
					pObj.put("since", pp.getLoadTime());
					size++;
				}
				
				obj.put("playerCount", size);
				obj.put("playerMax", plugin.getServer().getMaxPlayers());
				
				obj.put("players", arr);
				
				writer.write(obj.toString());
				writer.flush();
				
				// Clean up.
				writer.close();
				//in.close();
				socket.close();
			} catch (SocketException e) {
				e.printStackTrace(); // Protocol error
				shutdown();
			} catch (Exception e) {
				e.printStackTrace(); // General error
				shutdown();
			}
		}
	}
}