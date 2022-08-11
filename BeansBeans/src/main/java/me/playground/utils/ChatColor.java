package me.playground.utils;

import java.util.HashMap;
import java.util.Map;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Utility ChatColor class since neither NamedTextColor nor previous ChatColor 
 * instances provide NO way to go to and from types of Color or obtain char codes.
 * @author Mogubean
 */
public enum ChatColor {

	BLACK('0', 0x000000), 
	DARK_BLUE('1', 0x0000aa), 
	DARK_GREEN('2', 0x00aa00),
	DARK_AQUA('3', 0x00aaaa), 
	DARK_RED('4', 0xaa0000),
	DARK_PURPLE('5', 0xaa00aa), 
	GOLD('6', 0xffaa00), 
	GRAY('7', 0xaaaaaa),
	DARK_GRAY('8', 0x555555), 
	BLUE('9', 0x5555ff), 
	GREEN('a', 0x55ff55),
	AQUA('b', 0x55ffff),
	RED('c', 0xff5555), 
	LIGHT_PURPLE('d', 0xff55ff),
	YELLOW('e', 0xffff55), 
	WHITE('f', 0xffffff);
	
	private final static Map<Integer, ChatColor> BY_INT = new HashMap<>();
	private final static Map<Character, ChatColor> BY_CHAR = new HashMap<>();
	
	private final char code;
	private final int colour;

	ChatColor(char code, int colour) {
		this.code = code;
		this.colour = colour;
	}
	
	public static char charOf(TextColor color) {
		return BY_INT.get(NamedTextColor.nearestTo(color).value()).code;
	}

	public static ChatColor ofChar(char character) {
		return BY_CHAR.getOrDefault(character, ChatColor.WHITE);
	}

	public static NamedTextColor namedOfChar(char character) {
		return NamedTextColor.namedColor(ofChar(character).colour);
	}

	static {
        for (ChatColor color : values()) {
			BY_INT.put(color.colour, color);
			BY_CHAR.put(color.code, color);
		}
    }
}
