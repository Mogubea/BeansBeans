package me.playground.utils;

import me.playground.ranks.Rank;
import net.kyori.adventure.text.format.TextColor;

public class BeanColor implements TextColor {
	
	public static final BeanColor WORLD 		= new BeanColor(0x109b3a);
	public static final BeanColor COMMAND		= new BeanColor(0x95CAFF);
	public static final BeanColor ENCHANT 		= new BeanColor(0x75EFFF);
	public static final BeanColor ENCHANT_OP 	= new BeanColor(0x35FFFF);
	public static final BeanColor REGION 		= new BeanColor(0x5755bf);
	public static final BeanColor REGION_WORLD 	= new BeanColor(0x109b5a);
	public static final BeanColor WARP 			= new BeanColor(0xef89ef);
	public static final BeanColor WARP_SHOP 	= new BeanColor(0xffdd66);
	public static final BeanColor WARP_SERVER 	= new BeanColor(0xcf79ff);
	public static final BeanColor HEIRLOOM		= new BeanColor(0x98baff);
	public static final BeanColor BESTIARY		= new BeanColor(0x23df37);
	public static final BeanColor CIVILIZATION  = new BeanColor(0x45ffaa);
	public static final BeanColor STRUCTURE		= new BeanColor(0x88ffcc);
	public static final BeanColor NPC			= new BeanColor(0x77aaff);
	public static final BeanColor SAPPHIRE		= new BeanColor(0x0f42df);
	public static final BeanColor STAFF			= new BeanColor(Rank.MODERATOR.getRankHex());
	
	final int val;
	BeanColor(int val) {
		this.val = val;
	}

	@Override
	public int value() {
		return val;
	}
	
	
}
