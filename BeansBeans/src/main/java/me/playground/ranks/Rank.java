package me.playground.ranks;

import me.playground.regions.flags.Flags;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

/**
 * MOVE INTO DATABASE
 * Mostly so there's a proper sync between Website, Discord and In-Game
 */
public enum Rank {
	
	// Donor Ranks
	PLEB(1, 0xf19fd1, 50, 873535964423794709L,
			"bean.cmd.enderchest"),
	NOBLE(2, 0xaa22aa, 100, 873916287682756668L),
	
	// Playtime Ranks
	NEWBEAN(5, 0x2faf2f, 10, 546771060415135747L, 0,
			"bean.cmd.region",
			"bean.cmd.region.info",
			"bean.cmd.teleport",
			"bean.cmd.return",
			"bean.region.rename"),
	ROOKIE(10, 0x3fdf3f, 5, 879785289642545192L, 60 * 60 * 6),
	APPRENTICE(15, 0x5fff5f, 5, 879785586234384384L, 60 * 60 * 24,
			"bean.cmd.randomtp"),
	FAMILIAR(20, 0xff937a, 10, 879785746364506162L, 60 * 60 * 24 * 3),
	JOURNEYMAN(25, 0xff636a, 10, 879785828216369242L, 60 * 60 * 24 * 7),
	VETERAN(30, 0xff3f5f, 20, 879785962668982303L, 60 * 60 * 24 * 21),
	PRESTIGIOUS(35, 0xffca3f, 40, 879786041920327681L, 60 * 60 * 24 * 42),
	EXALTED(40, 0xffff6a, 100, 879786137714065449L, 60 * 60 * 24 * 84),
	
	// Staff Ranks
	MODERATOR(70, 0x55CAFF, 0, 546771449365528604L,
			"bean.cmd.say",
			"bean.cmd.gamemode",
			"bean.cmd.world",
			"bean.cmd.region.search",
			"bean.cmd.teleport.bypass",
			"bean.cmd.teleport.others",
			"bean.cmd.home.others",
			"bean.region.modifyothers",
			Flags.BLOCK_SPREAD.getPermission(),
			"bean.gm.moderator"),
	ADMINISTRATOR(90, 0x3378FF, 500, 546771706769965070L,
			"bean.cmd.tocoord",
			"bean.cmd.perform",
			"bean.cmd.enderchest",
			"bean.cmd.enderchest.others",
			"bean.cmd.fly",
			"bean.cmd.fly.others",
			"bean.cmd.god",
			"bean.cmd.god.others",
			"bean.cmd.warp.*",
			"bean.cmd.region.*",
			"bean.cmd.gamemode.others",
			"bean.cmd.i",
			"bean.region.override",
			Flags.PVP.getPermission(),
			Flags.KEEP_INVENTORY.getPermission(),
			Flags.MOB_DAMAGE_FROM.getPermission(),
			Flags.MOB_DAMAGE_TO.getPermission(),
			Flags.MOB_DROP_EXP.getPermission(),
			Flags.MOB_DROP_COIN.getPermission(),
			Flags.MOB_DROP_ITEM.getPermission(),
			Flags.ANVIL_DEGRADATION.getPermission(),
			Flags.ICE_FORMATION.getPermission(),
			"bean.shop.override",
			"bean.gm.spectator",
			"bean.gm.creative"),
	OWNER(100, 0x6550ff, 1000, 546771982167965716L,
			"*",
			"bean.cmd.op");
	final short rankLevel;
	final short warpBonus;
	final int col;
	final String[] permissions;
	final long discordRankID;
	final int playtimeReq;
	final TextComponent component;
	
	Rank(int rankLevel, int rgbColour, int warpLimit, long discordRankID, int playtimeReq, String...permissions) {
		this.rankLevel = (short) rankLevel;
		this.col = rgbColour;
		this.warpBonus = (short) warpLimit;
		this.permissions = permissions;
		this.discordRankID = discordRankID;
		this.playtimeReq = playtimeReq;
		this.component = Component.text(Utils.firstCharUpper(this.toString())).color(TextColor.color(col));
	}
	
	Rank(int rankLevel, int rgbColour, int warpLimit, long discordRankID, String...permissions) {
		this(rankLevel, rgbColour, warpLimit, discordRankID, -1, permissions);
	}
	
	public int power() {
		return rankLevel;
	}
	
	public String[] getPermissions() {
		return permissions;
	}
	
	public int getRankColour() {
		return col;
	}
	
	public TextComponent toComponent() {
		return component;
	}
	
	public int getWarpBonus() {
		return warpBonus;
	}
	
	public String lowerName() {
		return this.name().toLowerCase();
	}
	
	public long getDiscordId() {
		return discordRankID;
	}
	
	public int getPlaytimeRequirement() {
		return playtimeReq;
	}
	
	public boolean isPlaytimeRank() {
		return playtimeReq>-1;
	}
	
	public boolean isStaffRank() {
		return rankLevel>69;
	}
	
	public boolean isDonorRank() {
		return rankLevel<5;
	}
	
	public static Rank fromString(String s) {
		return Rank.valueOf(s.toUpperCase());
	}
}
