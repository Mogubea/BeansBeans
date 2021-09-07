package me.playground.currency;

import org.bukkit.ChatColor;

import me.playground.utils.Utils;

public enum Currency {
	
	COINS("\u00a2", '6');

	public static final double COIN_TAX_RATE = 1.0;
	
	//
	
	String shortt = "C";
	char colour = '7';

	Currency(String shortt, char colourChar) {
		this.shortt = shortt;
		this.colour = colourChar;
	}

	public ChatColor getColour() {
		return ChatColor.getByChar(colour);
	}

	public String getFriendlyString() {
		return getColour() + Utils.firstCharUpper(this.toString());
	}

}
