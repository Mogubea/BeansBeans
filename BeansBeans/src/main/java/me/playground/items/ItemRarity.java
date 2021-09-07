package me.playground.items;

import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum ItemRarity {
	
	TRASH(0x594949, 0x392D2D),
	COMMON(0xFEFEFE, 0xBABABA),
	UNCOMMON(0x00ff40),
	RARE(0x3050ff),
	EPIC(0x9f158f),
	LEGENDARY(0xef9a05),
	MYTHIC(0xef249f),
	IRIDESCENT(0xFF9FFF),
	
	UNTOUCHABLE(0xCF0F0F);
	
	final private TextColor colour;
	final private TextColor attributeColour;
	final private TextComponent component;
	final private TextComponent stars;
	
	ItemRarity(int rgb) {
		this(rgb, 0x3d3d3d | rgb);
	}
	
	ItemRarity(int rgb, int attcol) {
		this.colour = TextColor.color(rgb);
		this.attributeColour = TextColor.color(attcol);
		this.component = Component.text(Utils.firstCharUpper(toString())).color(colour);
		
		String s = "";
		for (int x = 0; x < this.ordinal(); x++)
			s += "\u2730";
		
		this.stars = Component.text(s).color(colour).decoration(TextDecoration.ITALIC, false);
	}
	
	public TextColor getColour() {
		return colour;
	}
	
	public TextColor getAttributeColour() {
		return attributeColour;
	}
	
	public TextComponent toComponent() {
		return component;
	}
	
	public TextComponent toStars() {
		return stars;
	}
	
	public ItemRarity upOne() {
		if (this.ordinal()>=ItemRarity.values().length)
			return this;
		return ItemRarity.values()[this.ordinal()+1];
	}
	
}
