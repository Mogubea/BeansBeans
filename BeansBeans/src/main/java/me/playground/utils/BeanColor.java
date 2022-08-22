package me.playground.utils;

import me.playground.ranks.Rank;
import net.kyori.adventure.text.format.TextColor;

public class BeanColor implements TextColor {
	
	public static final BeanColor WORLD 		= new BeanColor(0x109b3a);
	public static final BeanColor COMMAND		= new BeanColor(0xA5AAFF);
	public static final BeanColor ENCHANT 		= new BeanColor(0x75EFFF);
	public static final BeanColor ENCHANT_OP 	= new BeanColor(0x35FFFF);
	public static final BeanColor ENCHANT_BURDEN= new BeanColor(0xef256f);
	public static final BeanColor ENCHANT_ASTRAL= new BeanColor(0x4a1fbf);
	public static final BeanColor ENCHANT_LAPIS = new BeanColor(0x3a53ff);
	public static final BeanColor EXPERIENCE	= new BeanColor(0x87ff67);
	public static final BeanColor REGION 		= new BeanColor(0x5755bf);
	public static final BeanColor REGION_WORLD 	= new BeanColor(0x109b5a);
	public static final BeanColor WARP 			= new BeanColor(0xef89ef);
	public static final BeanColor WARP_SHOP 	= new BeanColor(0xffdd66);
	public static final BeanColor WARP_SERVER 	= new BeanColor(0xcf79ff);
	public static final BeanColor HEIRLOOM		= new BeanColor(0x98baff);
	public static final BeanColor BESTIARY		= new BeanColor(0x23df37);
	public static final BeanColor CIVILIZATION  = new BeanColor(0x45ffaa);
	public static final BeanColor STRUCTURE		= new BeanColor(0x88ffcc);
	public static final BeanColor NPC			= new BeanColor(0xaccfff);
	public static final BeanColor CRYSTALS		= new BeanColor(0xdf9bff);
	public static final BeanColor STAFF			= new BeanColor(Rank.MODERATOR.getRankHex());

	public static final BeanColor BAN			= new BeanColor(0xf53c3c);
	public static final BeanColor BAN_REASON    = new BeanColor(0xffcfcf);
	
	final int val;
	BeanColor(int val) {
		this.val = val;
	}

	@Override
	public int value() {
		return val;
	}
	
	public TextColor edit(int bitAlter) {
		return TextColor.color(val | bitAlter);
	}
	
}
