package me.playground.playerprofile.settings;

import java.util.ArrayList;
import java.util.Collection;

import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;

public enum PlayerSetting {
	
	ALLOW_TP("Teleport Attempts", false, "\u00a77While enabled players can \u00a7b/tp\u00a77 to you", "\u00a77when inside a \u00a79region\u00a77 or \u00a72world\u00a77 that permits it."),
	MENU_ITEM("Player Menu Item", true, "\u00a77While enabled you will have a \u00a7aNether Star\u00a77", "\u00a77in the top left of your inventory acting", "\u00a77as a shortcut to the \u00a7b/menu\u00a77."),
	PING_SOUNDS("@Ping Sounds", true, "\u00a77While enabled you will hear a soft", "\u00a7dsound of amethyst\u00a77 when pinged."),
	DISCORD("Null Setting", false, "This setting is empty for now."),
	SHOW_ACHIEVEMENTS("Achievements in Chat", true, "\u00a77Display whenever a player gets an \u00a76Achievement\u00a77."),
	SHOW_DEATH_MESSAGES("Death Messages in Chat", true, "\u00a77Display whenever a player kicks the bucket."),
	SHOW_JOB_MESSAGES("Job Messages in Chat", true, "\u00a77Display whenever a player changes their job."),
	QUICK_WOOL_DYE("Quick Wool Dye (Plebeian+)", Permission.QUICK_WOOL_DYE, true, "\u00a77Right Click a Block of Wool or Carpet","\u00a77with a dye to change its colour."),
	SHOW_SIDEBAR("Show Sidebar", true, "\u00a77Display the Sidebar on the right."),
	HIDE("Hide Test", "bean.cmd.hide", false, "\u00a7b/hide\u00a77 toggle test.")
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
		this.enabled = enabled;
		for (String s : description)
			this.description.add(Component.text(s));
		this.permString = permString;
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
