package me.playground.items;

import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public enum ItemRarity {
	
	TRASH(0x594949, 0x392D2D, false),
	COMMON(0xFEFEFE, 0xBABABA, true),
	UNCOMMON(0x00ff40, true),
	RARE(0x3050ff, true),
	EPIC(0xe439c2, true),
	LEGENDARY(0xef9a05, true),
	MYTHIC(0xef249f, true),
	IRIDESCENT(0xFF9FFF, true),

	ASTRAL(BeanColor.ENCHANT_ASTRAL.value(), false),
	/**
	 * Special Rarity is typically reserved for items that can only be obtained via some sort of Crystal purchase.
	 */
	SPECIAL(0xFFFC87, false),
	/**
	 * Event Rarity is reserved for event exclusive items.
	 */
	EVENT(0xFF7767, false),

	/**
	 * Untouchable Rarity is reserved for items that regular users aren't allowed to hold.
	 */
	UNTOUCHABLE(0xCF0F0F, false);
	
	final private TextColor colour;
	final private TextColor attributeColour;
	final private TextColor refinementColour;
	final private TextComponent component;
	final private boolean improvable;

	ItemRarity(int rgb, boolean improvable) {
		this(rgb, 0x3d3d3d | rgb, improvable);
	}
	
	ItemRarity(int rgb, int attcol, boolean improvable) {
		this.colour = TextColor.color(rgb);
		this.refinementColour = TextColor.color((rgb & 0xfefefe) >> 1);
		this.attributeColour = TextColor.color(attcol);
		this.component = Component.text(Utils.firstCharUpper(toString())).color(colour);
		this.improvable = improvable;
	}
	
	public TextColor getColour() {
		return colour;
	}

	public TextColor getRefinementColour() {
		return refinementColour;
	}
	
	public TextColor getAttributeColour() {
		return attributeColour;
	}
	
	public TextComponent toComponent() {
		return component;
	}

	public boolean isImprovable() {
		return improvable;
	}

	public ItemRarity upOne() {
		if (this.ordinal()>=IRIDESCENT.ordinal())
			return this;
		return ItemRarity.values()[this.ordinal()+1];
	}
	
	public boolean is(ItemRarity rarity) {
		return this.ordinal() >= rarity.ordinal();
	}
	
}
