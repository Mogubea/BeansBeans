package me.playground.ranks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.playground.playerprofile.PlayerProfile;
import me.playground.regions.flags.Flags;
import me.playground.utils.Utils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * MOVE INTO DATABASE
 * Mostly so there's a proper sync between Website, Discord and In-Game
 */
public enum Rank {
	
	// Donor Ranks
	PLEBEIAN(1, 0xf19fd1, 20, 0, 873535964423794709L,
			"The simplest but most accessible **Supporter Rank**. This rank provides access to **/workbench**, "
					+ "right-click wool dyeing and the ability to open Shulker Boxes from the Inventory!",
			
			"bean.cmd.workbench",
			Permission.QUICK_SHULKER_BOX,
			Permission.QUICK_WOOL_DYE),
	PATRICIAN(2, 0xd64dd6, 50, 1, 873916287682756668L,
			"The second tier of **Supporter Rank**. This rank provides access to custom nicknames, **/anvil**, **/hat**, delivery "
					+ "claim shortcuts and everything that <@&873535964423794709> offers!",
					
			"bean.cmd.anvil",
			"bean.cmd.hat",
			"bean.cmd.enderchest",
			Permission.NICKNAME_APPLY,
			Permission.DELIVERY_CLAIMALL),
	SENATOR(3, 0xaa32da, 100, 1, 914921526158041089L,
			"The final tier of permanent **Supporter Rank**. This rank provides access to custom name colours, the ability to rename your regions "
					+ "and everything that <@&873916287682756668> offers!",
			"bean.cmd.value",
			"bean.region.rename",
			Permission.NAMECOLOUR_CUSTOM),
	VIP(4, 0xffffff, 0, 0, 998171528678080652L,
			"Work in Progress. This subscription based **Supporter Rank** will not be released for a long while."),
	
	// Playtime Ranks
	NEWBEAN(5, 0x2faf2f, 10, 3, 546771060415135747L, 0,
			"Every single user that has ever played on Bean's Beans will be given this rank!",
			
			"bean.cmd.region",
			"bean.cmd.region.info",
			"bean.cmd.teleport",
			"bean.cmd.return"),
	ROOKIE(10, 0x3fdf3f, 5, 1, 879785289642545192L, 60 * 60 * 6,
			"The 2nd playtime rank. Light Green name!"),
	APPRENTICE(15, 0x5fff5f, 5, 1, 879785586234384384L, 60 * 60 * 24,
			"The 3rd playtime rank. Lighter Green name! Provides access to **/rtp**.",
			
			"bean.cmd.randomtp"),
	FAMILIAR(20, 0xff937a, 10, 1, 879785746364506162L, 60 * 60 * 24 * 3,
			"The 4th playtime rank. Peach name!"),
	JOURNEYMAN(25, 0xff636a, 10, 1, 879785828216369242L, 60 * 60 * 24 * 7,
			"The 5th playtime rank. Red name!"),
	VETERAN(30, 0xff3f5f, 20, 1, 879785962668982303L, 60 * 60 * 24 * 21,
			"The 6th playtime rank. Fucshia name!"),
	PRESTIGIOUS(35, 0xffca3f, 40, 1, 879786041920327681L, 60 * 60 * 24 * 42,
			"The 7th playtime rank. Golden name!"),
	EXALTED(40, 0xffff6a, 100, 1, 879786137714065449L, 60 * 60 * 24 * 84,
			"The current final playtime rank. Yellow name!"),
	
	// Staff Ranks
	MODERATOR(70, 0x55CAFF, 0, 0, 546771449365528604L,
			"All members of Bean's Beans Staff will possess this rank and/or its permissions. " +
			"If you ever need assistance with anything, don't be afraid to ask a Moderator for help!",
			
			Permission.NICKNAME_OVERRIDE,
			"bean.cmd.who.extra",
			"bean.cmd.say",
			"bean.cmd.gamemode",
			"bean.cmd.world",
			"bean.cmd.celestia",
			"bean.cmd.region.search",
			"bean.cmd.teleport.bypass",
			"bean.cmd.teleport.others",
			"bean.cmd.home.others",
			"bean.region.modifyothers",
			Flags.BLOCK_SPREAD.getPermission(),
			Flags.OBSIDIAN_FORMATION.getPermission(),
			"bean.gm.moderator"),
	ADMINISTRATOR(90, 0x3378FF, 500, 0, 546771706769965070L,
			"Administrators are in charge of managing the Moderator team, operating various systems " +
			"and assisting with more severe issues that may arise.",
					
			"minecraft.command.playsound",
			"minecraft.command.save-all",
			"minecraft.command.seed",
			"minecraft.command.summon",
			"minecraft.command.weather",
			"minecraft.command.xp",
			Permission.BYPASS_COOLDOWNS,
			Permission.NAMECOLOUR_RANKS,
			Permission.NAMECOLOUR_CUSTOM,
			Permission.QUICK_WOOL_DYE,
			"bean.cmd.tocoord",
			"bean.cmd.perform",
			"bean.cmd.workbench",
			"bean.cmd.anvil",
			"bean.cmd.enderchest",
			"bean.cmd.enderchest.others",
			"bean.cmd.fly",
			"bean.cmd.fly.others",
			"bean.cmd.god",
			"bean.cmd.god.others",
			"bean.cmd.heal",
			"bean.cmd.heal.others",
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
			Flags.CROP_REPLENISH.getPermission(),
			"bean.shop.override",
			"bean.gm.spectator",
			"bean.gm.creative"),
	OWNER(100, 0x6550ff, 30000, 0, 546771982167965716L,
			"The rank that represents Ownership of all that is Bean's Beans. There will only ever " + 
			"be one Owner, <@170538991693725696>.",
			
			"*",
			"bean.cmd.op");
	final short rankLevel;
	final short warpBonus;
	final short regionBonus;
	final int col;
	final Set<String> permissions;
	final long discordRankID;
	final int playtimeReq;
	final TextComponent component;
	final TextColor textCol;
	final String discordInformation;
	final String niceName;
	
	Rank(int rankLevel, int rgbColour, int warpBonus, int regionBonus, long discordRankID, int playtimeReq, String discordInfo, String...permissions) {
		this.rankLevel = (short) rankLevel;
		this.col = rgbColour;
		this.textCol = TextColor.color(rgbColour);
		this.warpBonus = (short) warpBonus;
		this.regionBonus = (short) regionBonus;
		
		this.discordInformation = discordInfo;

		Set<String> perms = new HashSet<>(Arrays.asList(permissions));
		
		this.permissions = Collections.unmodifiableSet(perms);
		this.discordRankID = discordRankID;
		this.playtimeReq = playtimeReq;
		this.niceName = Utils.firstCharUpper(this.toString());
		this.component = Component.text(niceName).color(TextColor.color(col)).decoration(TextDecoration.ITALIC, false);
	}
	
	Rank(int rankLevel, int rgbColour, int warpLimit, int regionBonus, long discordRankID, String discordInfo, String...permissions) {
		this(rankLevel, rgbColour, warpLimit, regionBonus, discordRankID, -1, discordInfo, permissions);
	}
	
	public int power() {
		return rankLevel;
	}
	
	/**
	 * TODO: Allow for rank permission modification live in future by moving things to database.
	 * @return immutable set of permissions for this rank.
	 */
	public Set<String> getPermissions() {
		return permissions;
	}
	
	public boolean hasPermission(String permissionString) {
		return permissions.contains(permissionString);
	}
	
	public int getRankHex() {
		return col;
	}
	
	public TextColor getRankColour() {
		return textCol;
	}
	
	public TextComponent toComponent() {
		return component;
	}
	
	/**
	 * A more informative version of {@link #toComponent()}.
	 */
	public TextComponent toComponent(PlayerProfile pp) {
		TextComponent component = this.component;
		Component rankType = Component.text("\n\u00a77Playtime Rank");
		if (this.isStaffRank()) 
			rankType = Component.text("\nStaff Rank", Rank.MODERATOR.getRankColour());
		else if (this.isDonorRank())
			rankType = Component.text("\nSupporter Rank", Rank.PLEBEIAN.getRankColour());
		else if (this != Rank.NEWBEAN)
			rankType = Component.text("\n\u00a77Playtime Rank\n\n\u00a77Playtime Requirement: \u00a7f" + Utils.timeStringFromMillis(this.getPlaytimeRequirement() * 1000L));
		
		component = component.hoverEvent(this.component.append(rankType));
		
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
	
	/**
	 * @return the rank's playtime requirement in SECONDS.
	 */
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
	
	public static OptionData retrieveDiscordOptionData() {
		OptionData data = new OptionData(OptionType.STRING, "name", "The name of the Rank.", true);
		for (Rank rank : Rank.values()) {
			String n = Utils.firstCharUpper(rank.name());
			data.addChoice(n, n);
		}
		return data;
	}
	
	public String getDiscordInformation() {
		return discordInformation;
	}
	
	public String getNiceName() {
		return niceName;
	}

	public int getRegionBonus() {
		return regionBonus;
	}
}
