package me.playground.playerprofile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.listeners.RedstoneManager;
import me.playground.regions.PlayerRegion;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import me.playground.civilizations.Civilization;
import me.playground.civilizations.jobs.Job;
import me.playground.command.BeanCommand;
import me.playground.data.Datasource;
import me.playground.discord.DiscordBot;
import me.playground.gui.BeanGui;
import me.playground.items.BeanItem;
import me.playground.listeners.ConnectionListener;
import me.playground.main.Main;
import me.playground.main.TeamManager.ScoreboardFlag;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.MemberLevel;
import me.playground.skills.Skill;
import me.playground.skills.Skills;
import me.playground.utils.Calendar;
import me.playground.utils.ChatColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PlayerProfile {

	/**
	 * @see PlayerProfileManager#getProfile(UUID) Method Call
	 */
	@NotNull
	public static PlayerProfile from(@NotNull Player p) {
		return from(p.getUniqueId());
	}

	/**
	 * @see PlayerProfileManager#getProfile(UUID) Method Call
	 */
	@NotNull
	public static PlayerProfile from(@NotNull OfflinePlayer p) {
		return from(p.getUniqueId());
	}

	/**
	 * @see PlayerProfileManager#getProfile(UUID) Method Call
	 */
	@NotNull
	public static PlayerProfile from(@NotNull HumanEntity p) {
		return from(p.getUniqueId());
	}

	/**
	 * @see PlayerProfileManager#getProfile(UUID) Method Call
	 */
	@NotNull
	public static PlayerProfile from(@NotNull UUID playerUUID) {
		return PlayerProfileManager.getProfile(playerUUID);
	}

	/**
	 * @see PlayerProfileManager#getProfile(String) Method Call
	 */
	@Nullable
	public static PlayerProfile fromIfExists(@NotNull String name) {
		return PlayerProfileManager.getProfile(name);
	}

	/**
	 * @see PlayerProfileManager#getProfile(int) Method Call
	 */
	@Nullable
	public static PlayerProfile fromIfExists(int playerId) {
		return PlayerProfileManager.getProfile(playerId);
	}
	
	public static int getDBID(String name) {
		return ProfileStore.from(name, false).getId();
	}
	
	public static int getDBID(OfflinePlayer p) {
		return ProfileStore.from(p.getUniqueId(), false).getId();
	}
	
	public static int getDBID(UUID uuid) {
		return ProfileStore.from(uuid, false).getId();
	}
	
	public static TextComponent getDisplayName(UUID uuid) {
		return ProfileStore.from(uuid, false).getColouredName();
	}
	
	public static TextComponent getDisplayName(int id) {
		return ProfileStore.from(id, false).getColouredName();
	}
	
	// XXX: Class Begins
	private final PlayerProfileManager	manager;

	private final int 					playerId;
	private final OfflinePlayer			player;
	
	private final Set<Integer>			friends = new HashSet<>();
	private final ArrayList<Integer> 	ignoredPlayers = new ArrayList<>();
	private final ArrayList<Rank> 		ranks = new ArrayList<>();
	private final Set<String>			privatePermissions;
	private long 						donorExpirationTime = 0L;
	
	private final Skills 				skillData;
	
	private String 						name;
	private String 						nickname;
	private TextComponent   			colouredName;
	private int 						nameColour;
	
	private float 						coins;
	private short						warpCount;
	
	private final UUID 					playerUUID;
	
	private Location					home;
	private final ItemStack[] 			armourWardrobe;
	private final HeirloomInventory		heirloomInventory;
	private final PlayerStats			stats;
	private final List<String> 			pickupBlacklist;
	private final List<Delivery>		inbox = new ArrayList<>();
	private final List<PlayerRegion>    ownedRegions = new ArrayList<>();
	
	private long 						booleanSettings;
	
	// Saved for performance
	private Civilization				civilization;
	private Job							job;
	
	// Not Saved
	private final long					loadTime; // Time when the profile was loaded.
	private int							warpLimit;
	private int							regionLimit;
	/**
	 * This should always be exactly the same as the online player's permission set.
	 * This exists to minimise or eradicate the use of {@link #isRank(Rank)}. As a 
	 * permission based system is far more reliable.
	 */
	private final Set<String>			permissions = new HashSet<>();
	private final ArrayList<String>		recentWarps = new ArrayList<>(10);
	private final HashMap<String, Long> cooldowns = new HashMap<>();
	private BeanGui			 			currentlyViewedGUI;
	private TextComponent 				chatLine;
	private final Location[] 			lastLocation = new Location[2];
	private Region 						currentRegion;
	private Objective					scoreboardObj;
	
	// Admin
	public UUID profileOverride;
	
	public PlayerProfile(PlayerProfileManager manager, int id, UUID uuid, ArrayList<Rank> ranks, Set<String> perms, int nameColour, String name, String nickname, long coins, long settings, short warpCount) {
		this.manager = manager;
		this.loadTime = System.currentTimeMillis();
		this.player = Bukkit.getOfflinePlayer(uuid);
		this.playerId = id;
		this.profileOverride = uuid; // XXX: TEMP
		this.playerUUID = uuid;
		this.nameColour = nameColour;
		
		this.name = name;
		if (!name.equals(nickname))
			this.nickname = nickname;
		
		this.privatePermissions = perms;
		
		this.coins = coins;
		this.warpCount = warpCount;
		
		this.booleanSettings = settings;
		
		this.home = manager.getDatasource().loadHome(id);
		this.skillData = manager.getDatasource().loadSkills(this);
		this.armourWardrobe = manager.getDatasource().loadArmourWardrobe(id);
		this.pickupBlacklist = manager.getDatasource().loadPickupBlacklist(id);
		this.heirloomInventory = new HeirloomInventory(this, manager.getDatasource().loadPlayerHeirlooms(id));
		this.stats = manager.getDatasource().loadPlayerStats(this);
		
		setRanks(ranks);
		refreshInbox();
		//Bukkit.getConsoleSender().sendMessage(Component.text("Profile was loaded for " + (hasNickname() ? getNickname() + " ("+getRealName()+")" : getRealName())));
	}

	/**
	 * Remove this {@link PlayerProfile} from the cache if the {@link #player} is currently offline.
	 */
	public void invalidateIfOffline() {
		if (!isOnline())
			manager.getCache().invalidate(playerUUID);
	}

	/**
	 * Check if the {@link #player} is currently online.
	 * @return Whether the player is online or not.
	 */
	public boolean isOnline() {
		return getPlayer() != null;
	}

	/**
	 * Get the online version of the {@link #player}.
	 *
	 * {@link #isOnline()} should be checked first.
 	 * @return The player if online, otherwise null.
	 */
	@Nullable
	public Player getPlayer() {
		return player.getPlayer();
	}

	/**
	 * Get the offline version of the {@link #player}.
	 * @return The offline player.
	 */
	@NotNull
	public OfflinePlayer getOfflinePlayer() {
		return player;
	}

	@NotNull
	public UUID getUniqueId() {
		return playerUUID;
	}

	/**
	 * Get the database ID of this {@link PlayerProfile}.
	 * @return The database ID.
	 */
	public int getId() {
		return playerId;
	}

	/**
	 * Get the coin balance.
	 * @return The coin balance.
	 */
	public float getBalance() {
		return coins;
	}
	
	public void setBalance(float amount, String log) {
		coins = amount;
		Datasource.logTransaction(playerId, amount, log);
		flagScoreboardUpdate(ScoreboardFlag.COINS);
	}
	
	public void addToBalance(float amount, String log) {
		addToBalance(amount);
		Datasource.logTransaction(playerId, amount, log);
	}
	
	public void addToBalance(float amount) {
		coins+=amount;
		if (this.isOnline() && amount != 0)
			getPlayer().sendActionBar(Component.text("\u00a76" + df.format(getBalance()) + " Coins \u00a77( " + (amount>-1 ? "\u00a7a+" : "\u00a7c") + df.format(amount) + "\u00a77 )"));
		flagScoreboardUpdate(ScoreboardFlag.COINS);
	}
	
	public int getCrystals() {
		return getStat(StatType.CURRENCY, "crystal");
	}
	
	public void addToCrystals(int amount) {
		stats.addToStat(StatType.CURRENCY, "crystal", amount);
		flagScoreboardUpdate(ScoreboardFlag.CRYSTALS);
	}
	
	public void setCrystals(int amount) {
		stats.setStat(StatType.CURRENCY, "crystal", amount);
		flagScoreboardUpdate(ScoreboardFlag.CRYSTALS);
	}

	/**
	 * Similar to {@link Player#hasPermission(String)} since both the profile and player have the same permissions.
	 * @return If the player has permission.
	 */
	public boolean hasPermission(String permissionString) {
		if (permissionString == null || permissionString.isEmpty()) return true;
		return this.permissions.contains("*") || this.permissions.contains(permissionString);
	}

	/**
	 * Get the permissions exclusive to this profile.
	 * @return The list of permissions.
	 */
	public Set<String> getPrivatePermissions() {
		return this.privatePermissions;
	}

	/**
	 * Get the list of permissions.
	 * @return The list of permissions.
	 */
	public Set<String> getPermissions() {
		return permissions;
	}
	
	/**
	 * Checks whether this player has or inherits the specified {@link Rank}.
	 */
	public boolean isRank(Rank rank) {
		// If it's a donor rank, just check if your current donor rank is equal or higher.
		if (rank.isDonorRank() && getDonorRank() != null && rank.power() <= getDonorRank().power())
			return true;
		// If it's a playtime rank, just check if your current playtime rank is equal or higher.
		if (rank.isPlaytimeRank() && rank.power() <= getPlaytimeRank().power())
			return true;
		// If it's a staff rank, just check if your highest rank is equal or higher.
		if (rank.isStaffRank() && rank.power() <= getHighestRank().power())
			return true;
		return ranks.contains(rank);
	}
	
	/**
	 * @return millis when their donor rank expires. Also demotes if that time has been met.
	 */
	public long getCheckDonorExpiration() {
		if (this.donorExpirationTime != 0L) {
			if (getDonorRank() != null)
				if (System.currentTimeMillis() >= this.donorExpirationTime) {
					if (this.isOnline())
						this.getPlayer().sendMessage(Component.text("\u00a7eYour ").append(donorRank.toComponent()).append(Component.text("\u00a7e rank has expired!")));
					this.removeRank(Rank.PLEBEIAN, Rank.PATRICIAN, Rank.SENATOR);
				}
		}
		return this.donorExpirationTime;
	}
	
	public void setDonorExpiration(long milli) {
		this.donorExpirationTime = milli;
	}
	
	public void addToDonorExpiration(long milli) {
		if (donorExpirationTime < System.currentTimeMillis())
			setDonorExpiration(System.currentTimeMillis() + milli);
		else
			this.donorExpirationTime += milli;
	}
	
	private Rank highestRank;
	public Rank getHighestRank() {
		return highestRank;
	}
	
	private Rank donorRank = null;
	public Rank getDonorRank() {
		return donorRank;
	}
	
	private Rank playtimeRank;
	public Rank getPlaytimeRank() {
		return playtimeRank;
	}
	
	public ArrayList<Rank> getRanks() {
		return ranks;
	}
	
	public Component getComponentRanks() {
		Component c = Component.text("");
		for (int x = 0; x < getRanks().size(); x++) {
			final Rank rank = getRanks().get(x);
			c = c.append(rank.toComponent(this));
			if (x+1 < getRanks().size())
				c = c.append(Component.text("\u00a7r, "));
		}
		return c;
	}

	/**
	 * Adds the specified {@link Rank} to this profile.
	 */
	public void addRank(Rank rank) {
		if (!ranks.contains(rank)) {
			boolean colourIsRank = this.nameColour == getHighestRank().getRankHex();
			ranks.add(rank);
			updateRanksPerms();
			if (colourIsRank)
				this.nameColour = getHighestRank().getRankHex();
			
			flagScoreboardUpdate(ScoreboardFlag.RANK);
			Main.getInstance().getDiscord().updateRoles(this);
		}
	}

	/**
	 * Removes the specified {@link Rank}(s) from this profile. Do note that playtime ranks
	 * will automatically be re-added if this profile playtime requirements demands it.
	 */
	public void removeRank(Rank... rankz) {
		boolean update = false;
		for (Rank rank : rankz) {
			if (ranks.contains(rank)) {
				update = true;
				ranks.remove(rank);
				boolean colourIsRank = this.nameColour == rank.getRankHex();
				if (colourIsRank)
					this.nameColour = getHighestRank().getRankHex();
			}
		}
		
		if (update) {
			updateRanksPerms();
			Main.getInstance().getDiscord().updateRoles(this);
		}
	}
	
	/**
	 * Sets the players ranks. This method does not call the {@link DiscordBot} method
	 * as it's not usually used in situations where ranks are updated, but rather previewed or loaded.
	 * @param ranks The new ranks of the player.
	 */
	public void setRanks(List<Rank> ranks) {
		this.ranks.clear();
		this.ranks.addAll(ranks);
		updateRanksPerms();
	}
	
	/**
	 * Organise the current rank list into the correct order, stores the highest of each rank category,
	 * calculates the current {@link #getWarpLimit()}, update the player's names with {@link #updateShownNames(boolean)}
	 * and then update permissions based on current Ranks.
	 */
	private void updateRanksPerms() {
		// Order the Ranks and do Warp Limit
		List<Rank> rankz = new ArrayList<>(ranks);
		this.warpLimit = 0;
		this.ranks.clear();
		this.permissions.clear();
		
		Rank rNewHigh = Rank.NEWBEAN;
		Rank rNewDonor = null;
		Rank rNewPlaytime = Rank.NEWBEAN;
		
		// Sort out the highest ranks of each category.
		for (Rank rank : Rank.values()) {
			if (!rankz.contains(rank)) continue;
			
			if (rank.isDonorRank())
				rNewDonor = rank;
			if (rank.isPlaytimeRank())
				rNewPlaytime = rank;
				
			rNewHigh = rank;
				
			ranks.add(rank);
			warpLimit += rank.getWarpBonus();
			regionLimit += rank.getRegionBonus();
		}
		
		// Update store.
		this.highestRank = rNewHigh;
		this.playtimeRank = rNewPlaytime;
		this.donorRank = rNewDonor;
		
		// Add permissions.
		for (Rank rank : Rank.values())
			if (this.isRank(rank))
				permissions.addAll(rank.getPermissions());
		
		// For example; the permission -bean.cmd.op would REMOVE that permission, even if the player was a rank that inherited that permission.
		for (String perm : getPrivatePermissions()) {
			if (perm.startsWith("-"))
				permissions.remove(perm.substring(1));
			else
				permissions.add(perm);
		}
		
		updateShownNames(false);
		
		// Update perms
		if (isOnline() && Main.getInstance().isEnabled()) // TODO: possibly find a less static method of handling this..?
			Main.getPermissionManager().updatePlayerPermissions(getPlayer());
	}
	
	public Set<Integer> getFriends() {
		return friends;
	}
	
	public ArrayList<Integer> getIgnoredPlayers() {
		return ignoredPlayers;
	}
	
	public void ignorePlayer(int id) {
		if (!ignoredPlayers.contains(id))
			ignoredPlayers.add(id);
	}
	
	public void unignorePlayer(int id) {
		ignoredPlayers.removeAll(new ArrayList<Integer>(id));
	}
	
	public void setNickname(String nickname) {
		this.nickname = (nickname.equals(name)) ? null : nickname;
		Main.getInstance().getDiscord().updateNickname(this);
		updateShownNames(true);
	}
	
	public void setNameColour(int colour) {
		this.nameColour = colour;
		updateShownNames(false);
	}
	
	/**
	 * @return the player's nickname, or null.
	 */
	public String getNickname() {
		return nickname;
	}
	
	/**
	 * @return if the player has a nickname.
	 */
	public boolean hasNickname() {
		return nickname != null;
	}
	
	/**
	 * This method is only called in {@link ConnectionListener} to update this player's {@link #name} upon login.
	 */
	public void updateRealName(String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			Datasource.saveProfileColumn(getId(), "name", name);
		}
	}

	@NotNull
	public TextColor getNameColour() {
		return TextColor.color(nameColour);
	}

	@NotNull
	public String getDisplayName() {
		return (nickname==null?name:nickname);
	}

	@NotNull
	public String getRealName() {
		return name;
	}

	@NotNull
	public TextComponent getColouredName() {
		return this.colouredName;
	}

	@NotNull
	public Skills getSkills() {
		return skillData;
	}

	@Nullable
	public Location getHome() {
		return this.home;
	}
	
	public void setHome(Location location) {
		this.home = location;
	}

	@NotNull
	public ItemStack[] getArmourWardrobe() {
		return this.armourWardrobe;
	}
	
	public void performWardrobeSwap(int id) {
		if (!isOnline()) return;
		
		final Player p = this.getPlayer();
		final int offset = (id-1)*4;
		final ItemStack[] armor = p.getInventory().getArmorContents().clone();
		final ItemStack[] oldStored = new ItemStack[]
				{
				this.armourWardrobe[offset],
				this.armourWardrobe[offset + 1],
				this.armourWardrobe[offset + 2],
				this.armourWardrobe[offset + 3],
				};
		
		// Set armor
		p.getInventory().setArmorContents(oldStored);
		
		// Save stored armor
		System.arraycopy(armor, 0, this.armourWardrobe, offset, 4);
		
		//Datasource.saveArmourWardrobe(this); saved with full profile
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 3, 1);
	}
	
	public void withdrawWardrobe(int id) {
		if (this.armourWardrobe == null || !isOnline()) return;
		
		final Player p = this.getPlayer();
		final int offset = (id-1)*4;
		
		for (int x = 0; x < 4; x++) {
			if (this.armourWardrobe[offset + x] == null)
				continue;
			
			p.getWorld().dropItem(p.getLocation(), this.armourWardrobe[offset + x]);
			this.armourWardrobe[offset + x] = null;
		}
	}
	
	/**
	 * Update the player's current names + scoreboard
	 * Also fires {@link #updateComponentName()}.
	 */
	@SuppressWarnings("removal")
	public void updateShownNames(boolean updateProfileName) {
		this.colouredName = Component.text(nickname==null?name:nickname).color(TextColor.color(getNameColour()));
			if (isOnline()) {
				if (updateProfileName) {
					com.destroystokyo.paper.profile.PlayerProfile prof = getPlayer().getPlayerProfile();
					prof.setName(getDisplayName());
					getPlayer().setPlayerProfile(prof);
				}
				flagFullScoreboardUpdate();
				getPlayer().displayName(getColouredName()); // Display Name
				Main.getTeamManager().updateTeam(getPlayer()); // Team, Tab list etc.
			}
			ProfileStore.updateStore(playerId, playerUUID, name, getDisplayName(), nameColour);
		updateComponentName();
	}
	
	/**
	 * Updates the player's current {@link #getComponentName()}. {@link Main} calls this every so often.
	 */
	public void updateComponentName() {
		TextComponent chatLine = getColouredName().clickEvent(ClickEvent.suggestCommand("/who "+getDisplayName()));
		Component hoverComponents = getColouredName();
		
		if (hasNickname())
			hoverComponents = hoverComponents.append(Component.text("\n\u00a78\u00a7oReal Name: \u00a7r\u00a7o" + getRealName()).colorIfAbsent(getNameColour()));
		
		// Test Badges
		/*Component badges = Component.empty();
		if (this.isOwner())
			badges = badges.append(Component.text("\u24E2", BeanColor.REGION));
		if (this.isMod())
			badges = badges.append(Component.text("\u24E2", BeanColor.STAFF));
		if (this.getStat(StatType.VOTING, "votes") >= 500)
			badges = badges.append(Component.text("\u272A", BeanColor.SAPPHIRE));
		if (this.getStat(StatType.GENERIC, "bugsReported") >= 1)
			badges = badges.append(Component.text("\u00a7c\u2622"));
		
		if (badges != Component.empty())
			hoverComponents = hoverComponents.append(Component.text("\n").append(badges));*/
		//
		
		Rank donorRank = getDonorRank();
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77 • Rank: ").append(getHighestRank().toComponent()));
		if (donorRank != null)
			hoverComponents = hoverComponents.append(Component.text("\u00a77 (").append(donorRank.toComponent()).append(Component.text("\u00a77)")));

		hoverComponents = hoverComponents.append(Component.text("\n\u00a77 • Playtime: \u00a7f" + Utils.timeStringFromMillis(getPlaytime() * 1000L)));
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77 • Id: \u00a7a" + getId()));
		
		this.chatLine = Component.empty().append(chatLine.hoverEvent(HoverEvent.showText(hoverComponents)));
	}

	/**
	 * Gets the detailed {@link TextComponent} name for the {@link Player} of this profile.
	 * @return The component name.
	 */
	@NotNull
	public TextComponent getComponentName() {
		if (chatLine == null)
			updateComponentName();
		return chatLine;
	}
	
	public void updateLastLocation(Location l, int type) {
		this.lastLocation[type] = l;
	}
	
	public Location getLastLocation(int type) {
		return lastLocation[type];
	}
	
	/**
	 * Returns this player's playtime in seconds.
	 */
	public int getPlaytime() {
		return getStat(StatType.GENERIC, "playtime");
	}

	public int getSkillLevel(Skill skill) {
		return skill != null ? skillData.getLevel(skill) : 0;
	}

	@Contract("null -> null")
	public String getSkillGrade(Skill skill) {
		return skill != null ? skillData.getSkillInfo(skill).getGrade() : null;
	}
	
	public boolean isSettingEnabled(PlayerSetting setting) {
		return (this.booleanSettings & 1<<setting.ordinal()) != 0;
	}
	
	/**
	 * @return the new status of the setting
	 */
	public boolean flipSetting(PlayerSetting setting) {
		this.booleanSettings ^= 1 << setting.ordinal();
		doSettingEffect(setting);
		return isSettingEnabled(setting);
	}
	
	public void setSettingEnabled(PlayerSetting setting, boolean enabled) {
		if (enabled)
			this.booleanSettings |= 1 << setting.ordinal();
		else
			this.booleanSettings &= ~(1 << setting.ordinal());
		doSettingEffect(setting);
	}
	
	private void doSettingEffect(PlayerSetting setting) {
		if (!isOnline() || setting == null) return;
		final boolean enabled = isSettingEnabled(setting);
		switch(setting) {
		case HIDE:
			Main.getTeamManager().updateTeam(getPlayer()); // Team
			flagScoreboardUpdate(ScoreboardFlag.TITLE);
			break;
		case SHOW_SIDEBAR:
			if (enabled) {
				flagFullScoreboardUpdate(); // flag rather than actually update since it's a spammable setting
				showSidebar();
			} else
				hideSidebar();
			break;
		case MENU_ITEM:
			if (enabled) {
				ItemStack prevItem = getPlayer().getInventory().getItem(9);
				getPlayer().getInventory().setItem(9, BeanItem.PLAYER_MENU.getOriginalStack());
				if (prevItem != null && !prevItem.equals(BeanItem.PLAYER_MENU.getOriginalStack()))
					giveItem(prevItem);
			} else {
				getPlayer().getInventory().setItem(9, null);
			}
		default:
			break;
		}
	}
	
	public long getSettings() {
		return this.booleanSettings;
	}
	
	public boolean isHidden() {
		return isSettingEnabled(PlayerSetting.HIDE);
	}

	/**
	 * Get this player's skull.
	 * @return This player's skull.
	 */
	public ItemStack getSkull() {
		return Utils.getSkullFromPlayer(Bukkit.getOfflinePlayer(this.getUniqueId()));
	}

	/**
	 * Get the currently viewed {@link BeanGui} instance.
	 * @return The gui.
	 */
	public BeanGui getBeanGui() {
		return this.currentlyViewedGUI;
	}

	/**
	 * Sets the currently viewed {@link BeanGui} instance.
	 * This does not open up a gui, just tells the profile what is being viewed.
	 */
	public void setBeanGui(BeanGui gui) {
		if (!this.isOnline()) return; // Shouldn't ever happen.
		this.currentlyViewedGUI = gui;
	}
	
	public void closeBeanGui() {
		this.currentlyViewedGUI = null;
	}
	
	public boolean isOverridingProfile() {
		return hasPermission(Permission.PROFILE_OVERRIDE) && !this.profileOverride.equals(this.getUniqueId());
	}
	
	/**
	 * While in a BeanGUI
	 */
	public void clearOverridingProfile() {
		this.profileOverride = this.getUniqueId();
		if (this.currentlyViewedGUI != null) // Shouldn't happen.
			this.currentlyViewedGUI.setProfile(this);
	}
	
	/**
	 * If the player is online, grant them the specified advancement.
	 * @param advancementKey - The exact directory and key.
	 */
	public void grantAdvancement(@Nonnull String advancementKey) {
		if (!isOnline()) return;
		NamespacedKey key = NamespacedKey.fromString(advancementKey);
		if (key == null) return;
		Advancement advancement = Bukkit.getAdvancement(key);
		if (advancement == null) return;
		Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
			AdvancementProgress progress = getPlayer().getAdvancementProgress(advancement);
			progress.getRemainingCriteria().forEach(progress::awardCriteria);
		});
	}
	
	/**
	 * If the player is online, revoke them of the specified advancement.
	 * @param advancementKey - The exact directory and key.
	 */
	public void revokeAdvancement(@Nonnull String advancementKey) {
		if (!isOnline()) return;
		NamespacedKey key = NamespacedKey.fromString(advancementKey);
		if (key == null) return;
		Advancement advancement = Bukkit.getAdvancement(key);
		if (advancement == null) return;
		Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
			AdvancementProgress progress = getPlayer().getAdvancementProgress(advancement);
			progress.getAwardedCriteria().forEach(progress::revokeCriteria);
		});
	}
	
	public HeirloomInventory getHeirlooms() {
		return this.heirloomInventory;
	}
	
	public PlayerStats getStats() {
		return stats;
	}
	
	public int getStat(StatType type, String name) {
		return getStats().getStat(type, name);
	}
	
	public List<String> getPickupBlacklist() {
		return this.pickupBlacklist;
	}
	
	public boolean canPickupItem(Item item) {
		// If the item was thrown by this player and is less than 10 seconds old, allow pickup regardless.
		//if (item.getThrower() == getPlayerUUID() && item.getTicksLived() < 200)
		//	return true;
		
		final BeanItem bi = BeanItem.from(item.getItemStack());
		String itemStr = (bi != null) ? bi.getIdentifier() : item.getItemStack().getType().name();
		return !(this.pickupBlacklist.contains(itemStr));
	}
	
	public void addToPickupBlacklist(ItemStack item) {
		final BeanItem bi = BeanItem.from(item);
		String itemStr = (bi != null) ? bi.getIdentifier() : item.getType().name();
		if (!this.pickupBlacklist.contains(itemStr))
			this.pickupBlacklist.add(itemStr);
	}
	
	public void removeFromPickupBlacklist(ItemStack item) {
		final BeanItem bi = BeanItem.from(item);
		String itemStr = (bi != null) ? bi.getIdentifier() : item.getType().name();
		this.pickupBlacklist.remove(itemStr);
	}
	
	public long getLastLogin() {
		return getStat(StatType.GENERIC, "lastLogin") * 60000L;
	}
	
	public long getLastLogout() {
		return getStat(StatType.GENERIC, "lastLogout") * 60000L;
	}
	
	public void addCooldown(String id, int mili) {
		this.cooldowns.put(id, System.currentTimeMillis() + mili);
	}
	
	public void clearCooldown(String id) {
		this.cooldowns.remove(id);
	}
	
	public boolean onCooldown(String id) {
		final long dura = this.cooldowns.getOrDefault(id, 0L);
		final boolean isCd = System.currentTimeMillis() < dura;
		if (dura > 0 && !isCd)
			this.cooldowns.remove(id);
		return isCd;
	}
	
	public boolean onCdElseAdd(String id, int milli) {
		return onCdElseAdd(id, milli, false);
	}
	
	public boolean onCdElseAdd(String id, int milli, boolean force) {
		if (!force && hasPermission(Permission.BYPASS_COOLDOWNS))
			return false;
		boolean onCd = onCooldown(id);
		if (!onCd)
			addCooldown(id, milli);
		return onCd;
	}
	
	public float getLuck() {
		if (isOnline())
			return (float) getPlayer().getAttribute(Attribute.GENERIC_LUCK).getValue();
		return 0.0F;
	}
	
	/**
	 * Pretty much only used in {@link BeanCommand}
	 * @param id Cooldown identifier
	 * @return the time when this cooldown expires.
	 */
	public long getCooldown(String id) {
		return this.cooldowns.getOrDefault(id, 0L);
	}
	
	public ArrayList<String> recentWarps() {
		return this.recentWarps;
	}
	
	public void addToRecentWarps(String name) {
		if (!recentWarps.contains(name))
			recentWarps.add(0, name);
	}
	
	public int getWarpLimit() {
		return warpLimit;
	}
	
	public short getWarpCount() {
		return warpCount;
	}
	
	public void upWarpCount() {
		this.warpCount++;
	}
	
	public void downWarpCount() {
		this.warpCount--;
	}

	public int getRegionLimit() {
		return regionLimit;
	}

	public Set<Region> getRegions() {
		return Main.getRegionManager().getRegionsMadeBy(getId());
	}

	/**
	 * Updates every 1.5 seconds in {@link Main}. This is never null if the player is online.
	 * @return last region entered
	 */
	@Nullable
	public Region getCurrentRegion() {
		return currentRegion;
	}

	/**
	 * Sets the current region the player is located in.
	 */
	public Region updateCurrentRegion(Region region) {
		return this.currentRegion = region;
	}
	
	public int getCivilizationId() {
		return civilization == null ? 0 : civilization.getId();
	}

	@Nullable
	public Civilization getCivilization() {
		return civilization;
	}
	
	public boolean isInCivilization() {
		return civilization != null;
	}
	
	public void setCivilization(Civilization civ) {
		this.civilization = civ;
		this.job = null;
	}
	
	/**
	 * @return the player's current {@link Job}. Through conventional methods, this 
	 * will usually return null if the player is not in a {@link Civilization}.
	 */
	@Nullable
	public Job getJob() {
		return job;
	}
	
	public boolean hasJob() {
		return job != null;
	}
	
	/**
	 * Will set the player's {@link Job} if possible. Forcing will only assign a job,
	 * regardless of unlocks, if the player is in a {@link Civilization}.
	 * @return if the job change was successful.
	 */
	public boolean setJob(Job job, boolean force) {
		if (job != null) {
			if (!isInCivilization()) return false;
			if (!force && !getCivilization().hasUnlocked(job)) return false;
			if (!force && onCooldown("changeJob")) return false;
		}
		
		this.job = job;
		return true;
	}

	@NotNull
	public List<Delivery> getInbox() {
		return inbox;
	}

	@Nullable
	public Delivery getDeliveryFrom(DeliveryType type, int fromId) {
		int size = inbox.size();
		for (int x = -1; ++x < size;) {
			Delivery d = inbox.get(x);
			if (d.getDeliveryType() != type) continue;
			if (d.getSenderId() != fromId) continue;
			return d;
		}
		return null;
	}
	
	private long lastInboxUpdate;
	public void refreshInbox() {
		manager.getDatasource().refreshPlayerInbox(this);
		lastInboxUpdate = System.currentTimeMillis();
	}
	
	public long getLastInboxUpdate() {
		return lastInboxUpdate;
	}

	@Nullable
	public Delivery giveItem(ItemStack...items) {
		if (isOnline()) {
			Map<Integer, ItemStack> remaining = getPlayer().getInventory().addItem(items);
			if (remaining.values().size() > 0)
				return Delivery.createItemDelivery(playerId, 0, "Item Package", "Some items were sent to you/nwhile Your inventory was full!", 1000L * 60 * 60 * 24 * 31, remaining.values());
			return null;
		} else {
			return Delivery.createItemDelivery(playerId, 0, "Item Package", "Some items were sent to you/nwhile you were away!", 1000L * 60 * 60 * 24 * 31, items);
		}
	}

	/**
	 * Inefficient ban check, make better in the future.
	 * @return whether the player is banned or not.
	 */
	public boolean isBanned() {
		return Datasource.getBanEntry(this.playerUUID) != null;
	}
	
	// AFK Stuff
	private long lastAFKPoke = System.currentTimeMillis();
	private long lastAFK = 0L; // Away from Keyboard
	private long lastRTK = 0L; // Return to Keyboard
	private boolean isAFK = false;
	private String AFKReason = "Away";
	
	public boolean isAFK() {
		return isAFK;
	}
	
	public String getAFKReason() {
		return AFKReason;
	}
	
	/**
	 * Set the player as AFK.
	 */
	public void setAFK(String reason) {
		if (!isOnline()) return;
		
		this.isAFK = true;
		this.lastAFK = System.currentTimeMillis();
		this.AFKReason = reason;
		this.stats.addToStat(StatType.GENERIC, "afk", 1);
		flagScoreboardUpdate(ScoreboardFlag.TITLE);
		Main.getTeamManager().updateTeam(getPlayer()); // Team
		getPlayer().sendActionBar(Component.text("\u00a77You are now AFK."));
	}
	
	/**
	 * Poke the player's AFK checking timer. This will also un-mark them from AFK.
	 */
	public void pokeAFK() {
		if (!isOnline()) return;
		
		long millis = System.currentTimeMillis();
		this.lastAFKPoke = millis;
		if (!this.isAFK) return;
		
		this.isAFK = false;
		this.lastRTK = millis;
		flagScoreboardUpdate(ScoreboardFlag.TITLE);
		Main.getTeamManager().updateTeam(getPlayer()); // Team
		getPlayer().sendActionBar(Component.text("\u00a77You are no longer AFK."));
	}
	
	/**
	 * Checks if the player is currently AFK. If so, mark them as AFK.
	 */
	public void checkAFK() {
		if (isAFK()) return;
		
		boolean isAFK = lastAFKPoke + 1000 * 60 * 5 < System.currentTimeMillis();
		if (isAFK)
			setAFK("Away");
	}
	
	public long getLastRTK() {
		return lastRTK;
	}
	
	public long getLastAFK() {
		return lastAFK;
	}

	/**
	 * If discord is enabled and the player has linked an account, attempt to grab their {@link Member}.
	 * @return Their member or null.
	 */
	@Nullable
	public Member getDiscordMember() {
		if (!Main.getInstance().getDiscord().isOnline()) return null;
		return Main.getInstance().getDiscord().getDiscordAccount(getId());
	}

	/**
	 * @return time in millis when this profile instance was loaded.
	 */
	public long getLoadTime() {
		return loadTime;
	}
	
	// XXX: Scoreboard
	private void showSidebar() {
		if (!isOnline()) return;
		scoreboardObj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	private void hideSidebar() {
		if (!isOnline()) return;
		scoreboardObj.setDisplaySlot(null);
	}
	
	public void updateScoreboard() {
		if (!isOnline()) return;
		if (!isSettingEnabled(PlayerSetting.SHOW_SIDEBAR)) return;
		
		updateSidebar();
		this.scoreboardFlag = 0;
	}
	
	private byte scoreboardFlag;
	public void flagScoreboardUpdate(ScoreboardFlag flag) {
		this.scoreboardFlag |= 1 << flag.ordinal();
		if (flag.isInstant())
			updateScoreboard();
	}
	
	public void flagFullScoreboardUpdate() {
		this.scoreboardFlag = (byte) ScoreboardFlag.all();
	}
	
	public boolean needsScoreboardUpdate() {
		return this.scoreboardFlag != 0;
	}
	
	/**
	 * Keep scoreboard score information here for easy and optimal replacement rather than having to sift through searching for them.
	 */
	private final Map<Integer, String> scoreboardScores = new HashMap<>();
	
	/**
	 * Update this player's sidebar.
	 */
	public void updateSidebar() {
		if (!isOnline()) return;
		
		Scoreboard scoreboard = getPlayer().getScoreboard();
		String c = "\u00a7" + ChatColor.charOf(getHighestRank().getRankColour());
		String rc = "\u00a7" + ChatColor.charOf(getHighestRank().getRankColour());

		if ((scoreboardFlag & 1 << ScoreboardFlag.TITLE.ordinal()) != 0) {
			List<String> flags = new ArrayList<>();
			if (isHidden()) flags.add("HIDE");
			if (isAFK()) flags.add("AFK");
			
			if (scoreboardObj == null) {
				scoreboardObj = scoreboard.getObjective("id"+getId()+"-side");
				if (scoreboardObj == null)
					scoreboardObj = scoreboard.registerNewObjective("id"+getId()+"-side", "dummy", getColouredName());
				if (isSettingEnabled(PlayerSetting.SHOW_SIDEBAR))
					showSidebar();

				setScoreboardLine(7, "\u00a78\u258E  ");
				setScoreboardLine(3, " ");
				setScoreboardLine(0, "\u00a78beansbeans.net");
			}

			setScoreboardLine(12, !flags.isEmpty() ? c + "\u258E \u00a77 " + flags : null);
			scoreboardObj.displayName(getColouredName()/*.append(!flags.isEmpty() ? Component.text("\u00a77 " + flags) : Component.empty())*/);
		}
		
		if ((scoreboardFlag & 1 << ScoreboardFlag.RANK.ordinal()) != 0) {
			setScoreboardLine(11, c + "\u258E \u00a7f\u2606 " + rc + getHighestRank().getNiceName());

			if (getDonorRank() != null)
				rc = "\u00a7" + ChatColor.charOf(getDonorRank().getRankColour());
			setScoreboardLine(10, getDonorRank() != null ? c + "\u258E \u00a7f\u2b50 " + rc + getDonorRank().getNiceName() : null);
		}
		
		if ((scoreboardFlag & 1 << ScoreboardFlag.COINS.ordinal()) != 0)
			setScoreboardLine(9, c + "\u258E\u00a76 \u20BF " + df.format(getBalance()));
		if ((scoreboardFlag & 1 << ScoreboardFlag.CRYSTALS.ordinal()) != 0)
			setScoreboardLine(8, c + "\u258E\u00a7d \u2756 " + df.format(getCrystals()));
		
		/*if (isInCivilization()) {
			scores.add(" ");
			Civilization civ = getCivilization();
			scores.add("\u00a72\u25D9 \u00a7fCiv:\u00a72 " + civ.getName());
			String jc = "\u00a7" + (hasJob() ? ChatColor.charOf(getJob().toComponent().color()) + getJob().getNiceName() : "2Citizen");
			scores.add("\u00a72\u25D9 \u00a7fJob: " + jc);
		}*/
		
		if ((scoreboardFlag & 1 << ScoreboardFlag.REGION.ordinal()) != 0) {
			Region r = getCurrentRegion();
			if (r != null && !r.isWorldRegion()) {
				String rName = r.getName();
				if (rName.length() > 9)
					rName = rName.substring(0, 10) + "..";
				
				setScoreboardLine(6, "\u00a7b\u258E \u00a7f\u06e9 \u00a79" + rName);
				MemberLevel level = r.getMember(getPlayer());
				setScoreboardLine(5, "\u00a7b\u258E \u00a7f\u272a \u00a7b" + level.toString());
			} else {
				String rName = getPlayer().getWorld().getName();
				if (rName.length() > 9)
					rName = rName.substring(0, 10) + "..";
				
				setScoreboardLine(6, "\u00a7b\u258E \u00a7f\u2742 \u00a72" + rName);
				setScoreboardLine(5, "\u00a7b\u258E \u00a7f\u272a \u00a7aWilderness");
			}
		}
		
		if ((scoreboardFlag & 1 << ScoreboardFlag.TIME.ordinal()) != 0) {
			int hour = (Calendar.getTime(getPlayer().getWorld())+6000) / 1000;
			boolean night = hour < 6 || hour > 18;
			
			String symbol = night ? "\u00a79\u263d" : "\u00a7e\u2600";
			if (getPlayer().getWorld().hasStorm()) {
				if (getPlayer().getWorld().isThundering())
					symbol = night ? "\u00a79\u26a1" : "\u00a76\u26a1";
				else
					symbol = night ? "\u00a79\u2602" : "\u00a7b\u2602";
			}
			setScoreboardLine(4, "\u00a7b\u258E " + symbol + "\u00a77 " + Calendar.getTimeString(Calendar.getTime(getPlayer().getWorld()), true));
		}

		if ((scoreboardFlag & 1 << ScoreboardFlag.REDSTONE.ordinal()) != 0) {
			float avg = redstoneManager.getAverageRedstoneActions(getPlayer().getChunk());

			// Hold redstone to show it
			if (avg < 1 || getPlayer().getInventory().getItemInMainHand().getType().getCreativeCategory() != CreativeCategory.REDSTONE) {
				setScoreboardLine(2, null);
			} else {
				float max = redstoneManager.getMaximumActions();
				float percentUsed = (avg / max) * 100;
				String col = percentUsed < 50 ? "\u00a7a" : percentUsed < 95 ? "\u00a7e" : "\u00a7c";

				setScoreboardLine(2, "\u00a78\u258E \u00a74\ud83d\udd25 \u00a7cWiring: " + col + (int)percentUsed + "%");
			}
		}

		if ((scoreboardFlag & 1 << ScoreboardFlag.TPS.ordinal()) != 0) {
			if (isRank(Rank.ADMINISTRATOR)) {
				setScoreboardLine(1, "\u00a78\u258E " + getScoreboardTPS());
			} else {
				setScoreboardLine(1, null);
			}
		}
	}
	
	private void setScoreboardLine(int line, String newContent) {
		String old = scoreboardScores.getOrDefault(line, null);
		if (old != null) {
			if (old.equals(newContent)) return;
			scoreboardObj.getScore(old).resetScore();
		}
		
		if (newContent != null) {
			scoreboardScores.put(line, newContent);
			scoreboardObj.getScore(newContent).setScore(line);
		} else {
			scoreboardScores.remove(line);
		}
	}

	@NotNull
	private String getScoreboardTPS() {
		double[] tps = Bukkit.getServer().getTPS();
		String tpString = (tps[0] < 19.9 ? (tps[0] < 15 ? "\u00a74" : "\u00a76") : "\u00a7a") + "\u26a0 ";
		tpString += "\u00a7f" + tpsf.format(tps[0]) + " \u00a77" + tpsf.format(tps[1]) + " \u00a78" + tpsf.format(tps[2]);
		return tpString;
	}

	private static RedstoneManager redstoneManager = Main.getInstance().getRedstoneManager();
	private static final DecimalFormat df = new DecimalFormat("#,###");
	private static final DecimalFormat tpsf = new DecimalFormat("#.#");

}
