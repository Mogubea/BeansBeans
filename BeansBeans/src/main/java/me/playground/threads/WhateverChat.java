package me.playground.threads;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.permessage_deflate.PerMessageDeflateExtension;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import net.kyori.adventure.text.Component;

public class WhateverChat extends WebSocketServer {

	private final Main plugin;
//	private final VoteRSA rsa = new VoteRSA();
//	private KeyPair keyPair;
	
//	private int openCounter = 0;
//	private int closeCounter = 0;
//	private int limit = Integer.MAX_VALUE;
	
	private boolean enabled = true;

	public WhateverChat(Main plugin, int port) {
		super(new InetSocketAddress(port), Collections.singletonList(new Draft_6455(new PerMessageDeflateExtension())));
		this.plugin = plugin;
		init();
	}
	
	public void init() {
		getPlugin().getSLF4JLogger().info("[WEBCHAT] Attempting to initialise Web Chat.");
		//File voteRSA = new File(plugin.getDataFolder() + "/rsa/webChat");
		try {
			/*if (!voteRSA.exists()) {
				voteRSA.mkdir();
				keyPair = rsa.generate(4096);
				rsa.save(voteRSA, keyPair);
			} else {
				keyPair = rsa.load(voteRSA);
			}*/
			
			this.enabled = true;
			setConnectionLostTimeout(300);
			start();
		} catch (Exception e) {
			getPlugin().getSLF4JLogger().warn("[WEBCHAT] Failure initialising Web Chat.");
			e.printStackTrace();
			this.enabled = false;
			return;
		}
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
//	    openCounter++;
	    plugin.getSLF4JLogger().info("[WEBCHAT] Web Chat Client connected ("+conn.getRemoteSocketAddress()+")");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//	    closeCounter++;
	    plugin.getSLF4JLogger().info("[WEBCHAT] Web Chat Client disconnected ("+conn.getRemoteSocketAddress()+").");
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		plugin.getSLF4JLogger().error("[WEBCHAT] Web Chat Exception;");
		ex.printStackTrace();
	}

	@Override
	public void onStart() {
		plugin.getSLF4JLogger().info("[WEBCHAT] Web Chat Server has started.");
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		try {
			/*int blockPos = 0;
			byte[] block = rsa.decrypt(Base64.getDecoder().decode(message.getBytes()), keyPair.getPrivate());
			String opcode = rsa.readString(block, blockPos);
			blockPos += opcode.length() + 1;
			if (!opcode.equals("WCM"))
				throw new Exception("Unable to decode RSA");
			String msg = rsa.readString(block, blockPos);*/
			String msg = message;
			
			broadcast(conn.getRemoteSocketAddress() + ": " + msg);
			Bukkit.getOnlinePlayers().forEach(player -> {
				player.sendMessage(Component.text(conn.getRemoteSocketAddress() + " [Web]: " + msg));
			});
			plugin.getSLF4JLogger().info("[WEBCHAT] " + conn.getRemoteSocketAddress() + ": " + msg);
		} catch (Exception e) { // Due to RSA, not a String blob etc...
			//conn.close("An unknown error has occured.");
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer blob) {
		conn.close();
	}
	
	/**
	 * Broadcast a message from a player to all connected clients.
	 */
	public void sendWebMessage(int playerId, String message) {
		if (!enabled) return;
		final ProfileStore ps = ProfileStore.from(playerId);
		getPlugin().getWebChatServer().broadcast(
				"{\"c\": \""+ps.getNameColour().asHexString()+"\"," +
				" \"n\": \""+ps.getRealName()+"\"," +
				" \"dn\": \""+ps.getDisplayName()+"\"," +
				" \"id\": "+playerId+"," +
				" \"msg\": \""+message.replace('"', '“').replace("\\", "\\\\")+"\"}");
	}
	
	public Main getPlugin() {
		return plugin;
	}
	
	public void shutdown() {
		try {
			this.stop();
			this.enabled = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
