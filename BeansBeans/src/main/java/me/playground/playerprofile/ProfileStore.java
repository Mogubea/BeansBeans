package me.playground.playerprofile;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import me.playground.data.Datasource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Stores all the names and ids without having to load profiles.
 * @author Brandon
 */
public class ProfileStore {
	
	private final static HashMap<Integer, ProfileStore> players = new HashMap<>();
	
	static {
		players.put(-1, new ProfileStore(-1, UUID.randomUUID(), "Unknown", "Unknown", NamedTextColor.DARK_GRAY.value()));
		players.put(0, new ProfileStore(0, UUID.randomUUID(), "Server", "Server", NamedTextColor.LIGHT_PURPLE.value()));
	}
	
	private String realName;
	private String nickname;
	private TextColor nameColour;
	private TextComponent colouredName;
	private final UUID uuid;
	private final int dbid;
	
	public ProfileStore(int id, UUID uuid, String realName, String displayName, int nameColour) {
		this.realName = realName;
		this.nameColour = TextColor.color(nameColour);
		TextComponent name = Component.text(displayName, this.nameColour);
		this.nickname = displayName;
		this.colouredName = name;
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
	
	public TextColor getNameColour() {
		return nameColour;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getId() {
		return dbid;
	}

	public static Collection<ProfileStore> getEntries() {
		return players.values();
	}
	
	public static void updateStore(int id, UUID uuid, String realName, String displayName, int nameColour) {
		players.put(id, new ProfileStore(id, uuid, realName, displayName, nameColour));
	}
	
	public static ProfileStore from(int id) {
		return from(id, false);
	}
	
	public static ProfileStore from(int id, boolean forceNull) {
		return players.getOrDefault(id, forceNull ? null : players.get(-1));
	}

	public static ProfileStore fromIfExists(int id) {
		return id > 0 ? players.get(id) : null;
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
