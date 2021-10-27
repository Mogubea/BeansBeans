package me.playground.civilizations;

import org.bukkit.command.CommandSender;

import me.playground.utils.Utils;

public enum CitizenTier {
	
	CITIZEN, // Can utilise Civilization benefits and downsides.
	OFFICER, // Can manage citizens
	LEADER, // Similar to owner, just not the owner
	OWNER; // Can manage the civilization
	
	private final String niceName;
	
	private CitizenTier() {
		this.niceName = Utils.firstCharUpper(name());
	}
	
	public static CitizenTier fromCmd(CommandSender sender, String s) {
		try {
			return valueOf(s);
		} catch (Exception e) {
			sender.sendMessage("Couldn't find tier '"+s+"'");
			return null;
		}
	}
	
	public boolean isOrAbove(CitizenTier tier) {
		return this.ordinal() >= tier.ordinal();
	}
	
	public String getNiceName() {
		return niceName;
	}
	
}
