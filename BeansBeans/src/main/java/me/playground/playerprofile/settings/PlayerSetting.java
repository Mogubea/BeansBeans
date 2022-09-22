package me.playground.playerprofile.settings;

import java.util.ArrayList;
import java.util.Collection;

import me.playground.ranks.Permission;
import net.kyori.adventure.text.Component;

public enum PlayerSetting {
	
	ALLOW_TP("Teleport Attempts", false, "\u00a77While enabled players can \u00a7b/tp\u00a77 to you", "\u00a77when inside a \u00a79region\u00a77 or \u00a72world\u00a77 that permits it."),
	MENU_ITEM("Player Menu Item", true, "\u00a77While enabled you will have a \u00a7aNether Star\u00a77", "\u00a77in the top left of your inventory acting", "\u00a77as a shortcut to the \u00a7b/menu\u00a77."),
	PING_SOUNDS("@Ping Sounds", true, "\u00a77While enabled you will hear a soft", "\u00a7dsound of amethyst\u00a77 when pinged."),
	FLIGHT("Flight Test", "bean.cmd.fly", false, "Toggle flight."),
	SHOW_ACHIEVEMENTS("Achievements in Chat", true, "\u00a77Display whenever a player gets an \u00a76Achievement\u00a77."),
	SHOW_DEATH_MESSAGES("Death Messages in Chat", true, "\u00a77Display whenever a player kicks the bucket."),
	SHOW_LEVEL_UP_MESSAGES("Skill Level Up Messages in Chat", true, "\u00a77Display whenever a player levels up", "\u00a77a skill."),
	QUICK_WOOL_DYE("Quick Wool Dye (Plebeian+)", Permission.QUICK_WOOL_DYE, true, "\u00a77Right Click a Block of Wool or Carpet","\u00a77with a dye to change its colour."),
	SHOW_SIDEBAR("Show Sidebar", true, "\u00a77Display the Sidebar on the right."),
	HIDE("Hide Test", "bean.cmd.hide", false, "\u00a7b/hide\u00a77 toggle test."),
	SKILL_EXPERIENCE_SOUND("Skill Experience Sounds", true, "\u00a77Play a subtle experience sound", "\u00a77when obtaining \u00a7bSkill XP\u00a77."),
	REGION_WARNING("Region Boundary Warning", true, "\u00a77Display a \u00a7bRegion's Boundaries \u00a77when", "\u00a77building close to their borders."),
	REGION_BOUNDARIES("Show Region Boundaries", true, "\u00a77Display the \u00a7bRegion Boundaries\u00a77of any", "\u00a79Region\u00a77 you are currently inside of."),
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
	private final ArrayList<Component> description = new ArrayList<>();
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
