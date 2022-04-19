package me.playground.playerprofile.settings;

import java.util.ArrayList;
import java.util.Collection;

import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;

public enum PlayerSetting {
	
	ALLOW_TP("Teleport Attempts", false, "\u00a77When enabled, players can \u00a7b/tp\u00a77 to you", "\u00a77when inside a \u00a79region\u00a77 or \u00a72world\u00a77 that permits it."),
	CHAT_TIMESTAMPS("Chat Timestamps", false, "\u00a77Doesn't do anything right now."),
	PING_SOUNDS("@Ping Sounds", true, "\u00a77When enabled, you will hear a soft", "\u00a7dsound of amethyst\u00a77 when pinged."),
	DISCORD("Discord Linking", false, "\u00a77Only enable this when trying to \u00a7b/link\u00a77", "\u00a77your \u00a79Discord Account\u00a77 from within", "\u00a77the \u00a79Discord Server\u00a77."),
	SHOW_ACHIEVEMENTS("Achievements in Chat", true, "\u00a77Display whenever a player gets an \u00a76Achievement\u00a77."),
	SHOW_DEATH_MESSAGES("Death Messages in Chat", true, "\u00a77Display whenever a player kicks the bucket."),
	SHOW_JOB_MESSAGES("Job Messages in Chat", true, "\u00a77Display whenever a player changes their job."),
	QUICK_WOOL_DYE("Quick Wool Dye (Plebeian+)", Permission.QUICK_WOOL_DYE, true, "\u00a77Right Click a Block of Wool or Carpet","\u00a77with a dye to change its colour."),
	
	;
	
	final private static long defaultSetting;
	static {
		long ack = 0;
		for (PlayerSetting ps : values()) {
			if (ps.isEnabledByDefault())
			ack |= 1 << ps.ordinal();
		}
		defaultSetting = ack;
	}
	
	private final String displayName;
	private final ArrayList<Component> description = new ArrayList<Component>();
	private final boolean enabled;
	private final String permString;
	
	PlayerSetting(String displayName, String permString, boolean enabled, String...description) {
		this.displayName = displayName;
		this.enabled = false;
		for (String s : description)
			this.description.add(Component.text(s));
		this.permString = "";
	}
	
	PlayerSetting(String displayName, boolean enabled, String...description) {
		this(displayName, "", enabled, description);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	private boolean isEnabledByDefault() {
		return enabled;
	}
	
	public Collection<Component> getDescription() {
		return description;
	}
	
	public static long getDefaultSettings() {
		return defaultSetting;
	}
	
	public String getPermissionString() {
		return permString;
	}
	
}
