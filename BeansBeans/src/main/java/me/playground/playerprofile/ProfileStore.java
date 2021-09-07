package me.playground.playerprofile;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.playground.data.Datasource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * Stores all the names and ids without having to load profiles.
 * @author Brandon
 */
public class ProfileStore {
	
	private final static HashMap<Integer, ProfileStore> players = new HashMap<Integer, ProfileStore>();
	
	static {
		Datasource.loadProfileCache();
		players.put(-1, new ProfileStore(-1, UUID.randomUUID(), "Unknown", Component.text("\u00a78Unknown")));
		players.put(0, new ProfileStore(0, UUID.randomUUID(), "Server", Component.text("\u00a7dServer")));
	}
	
	private String realName;
	private String nickname;
	private TextComponent colouredName;
	private UUID uuid;
	private int dbid;
	
	public ProfileStore(int id, UUID uuid, String realName, Component name) {
		this.realName = realName;
		this.nickname = ((TextComponent)name).content();
		this.colouredName = (TextComponent) name;
		this.uuid = uuid;
		this.dbid = id;
	}
	
	public boolean isOnline() {
		OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
		return op != null ? op.isOnline() : false;
	}
	
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
	
	public String getDisplayName() {
		return this.nickname;
	}

	public TextComponent getColouredName() {
		return colouredName;
	}

	public void setColouredName(Component colouredName) {
		this.colouredName = (TextComponent) colouredName;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getDBID() {
		return dbid;
	}

	public void setDBID(int dbid) {
		this.dbid = dbid;
	}

	public static Collection<ProfileStore> getEntries() {
		return players.values();
	}
	
	public static void updateStore(int id, UUID uuid, String realName, Component name) {
		players.put(id, new ProfileStore(id, uuid, realName, name));
	}
	
	public static ProfileStore from(int id) {
		return from(id, false);
	}
	
	public static ProfileStore from(int id, boolean forceNull) {
		return players.getOrDefault(id, forceNull ? null : players.get(-1));
	}
	
	public static ProfileStore from(UUID id, boolean forceNull) {
		for (ProfileStore cache : players.values())
			if (cache.getUniqueId().equals(id))
				return cache;
		return forceNull ? null : players.get(-1);
	}
	
	public static ProfileStore from(String name, boolean forceNull) {
		for (ProfileStore cache : players.values())
			if (cache.getDisplayName().equalsIgnoreCase(name) || cache.getRealName().equalsIgnoreCase(name))
				return cache;
		return forceNull ? null : players.get(-1);
	}
	
}
