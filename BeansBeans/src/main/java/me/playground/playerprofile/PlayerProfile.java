package me.playground.playerprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.playground.civilizations.Civilization;
import me.playground.civilizations.jobs.Job;
import me.playground.command.BeanCommand;
import me.playground.data.Datasource;
import me.playground.discord.DiscordBot;
import me.playground.gui.BeanGui;
import me.playground.items.BeanItem;
import me.playground.listeners.ConnectionListener;
import me.playground.main.Main;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillType;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.network.PlayerConnection;

public class PlayerProfile {
	
	public static LoadingCache<UUID, PlayerProfile> profileCache = CacheBuilder.from("maximumSize=500,expireAfterAccess=15m")
			.build(
					new CacheLoader<UUID, PlayerProfile>() {
						public PlayerProfile load(UUID playerUUID) throws Exception { // if the key doesn't exist, request it via this method
							PlayerProfile prof = Datasource.getOrMakeProfile(playerUUID);
							if (prof.getPlayer()!=null) // assign bar player if online
								prof.getSkills().assignBarPlayer(prof.getPlayer());
							return prof;
						}
					});
	
	public static PlayerProfile from(Player p) {
		if (p == null) return null;
		try {
			return profileCache.get(p.getUniqueId());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PlayerProfile from(HumanEntity p) {
		if (p == null) return null;
		try {
			return profileCache.get(p.getUniqueId());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PlayerProfile from(UUID playerUUID) {
		try {
			return profileCache.get(playerUUID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PlayerProfile fromIfExists(String name) {
		try {
			ProfileStore ps = ProfileStore.from(name, true);
			if (ps == null) return null;
			return profileCache.get(ps.getUniqueId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PlayerProfile fromIfExists(int playerId) {
		try {
			ProfileStore ps = ProfileStore.from(playerId, true);
			if (ps == null) return null;
			return profileCache.get(ps.getUniqueId());
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
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
	
	private final int 					playerId;
	private OfflinePlayer				player;
	
	private final ArrayList<Integer> 	ignoredPlayers = new ArrayList<Integer>();
	private final ArrayList<Rank> 		ranks = new ArrayList<Rank>();
	private final Set<String>			privatePermissions;
	private long 						donorExpirationTime = 0L;
	
	private final SkillData 			skillData;
	
	private String 						name;
	private String 						nickname;
	private TextComponent   			colouredName;
	private int 						nameColour = Rank.NEWBEAN.getRankHex();
	
	private long 						coins = 500;
	private short						warpCount;
	
	private UUID 						playerUUID;
	
	private Location					home;
	private ItemStack[] 				armourWardrobe;
	private final HeirloomInventory		heirloomInventory;
	private final PlayerStats			stats;
	private List<String> 				pickupBlacklist = new ArrayList<String>();
	private List<Delivery>				inbox = new ArrayList<Delivery>();
	
	private long 						booleanSettings = PlayerSetting.getDefaultSettings();
	
	// Saved for performance
	private Civilization				civilization;
	private Job							job;
	
	// Not Saved
	private final long					loadTime; // Time when the profile was loaded.
	private int							warpLimit;
	/**
	 * This should always be exactly the same as the online player's permission set.
	 * This exists to minimise or eradicate the use of {@link #isRank(Rank)}. As a 
	 * permission based system is far more reliable.
	 */
	private Set<String>					permissions = new HashSet<String>(); 
	private ArrayList<String>			recentWarps = new ArrayList<String>(10);
	private HashMap<String, Long>  		cooldowns = new HashMap<String, Long>();
	private BeanGui			 			currentlyViewedGUI;
	private TextComponent 				chatLine;
	private Location[] 					lastLocation = new Location[2];
	private Region 						currentRegion;
	
	// Admin
	public UUID profileOverride;
	
	public PlayerProfile(int id, UUID uuid, ArrayList<Rank> ranks, Set<String> perms, int nameColour, String name, String nickname, long coins, long settings, short warpCount) {
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
		
		this.home = Datasource.loadHome(id);
		this.skillData = Datasource.loadOrMakeBeanExperience(id);
		this.armourWardrobe = Datasource.loadArmourWardrobe(id);
		this.pickupBlacklist = Datasource.loadPickupBlacklist(id);
		this.heirloomInventory = new HeirloomInventory(this, Datasource.loadPlayerHeirlooms(id));
		this.stats = Datasource.loadPlayerStats(this);
		
		setRanks(ranks);
		refreshInbox();
		//Bukkit.getConsoleSender().sendMessage(Component.text("Profile was loaded for " + (hasNickname() ? getNickname() + " ("+getRealName()+")" : getRealName())));
	}
	
	public boolean isOnline() {
		return getPlayer()!=null;
	}
	
	public Player getPlayer() {
		return player.getPlayer();
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return player;
	}
	
	public UUID getUniqueId() {
		return playerUUID;
	}
	
	public int getId() {
		return playerId;
	}
	
	public long getBalance() {
		return coins;
	}
	
	public void setBalance(long amount, String log) {
		coins = amount;
		Datasource.logTransaction(playerId, amount, log);
		flagScoreboardUpdate();
	}
	
	public void addToBalance(long amount, String log) {
		addToBalance(amount);
		Datasource.logTransaction(playerId, amount, log);
	}
	
	public void addToBalance(long amount) {
		coins+=amount;
		if (this.isOnline())
			getPlayer().sendActionBar(Component.text("\u00a76" + getBalance() + " Coins \u00a77( " + (amount>-1 ? "\u00a7a+" : "\u00a7c") + amount + "\u00a77 )"));
		flagScoreboardUpdate();
	}
	
	/**
	 * A player's Sapphire count is stored within the voting {@link #StatType}.
	 * @return the player's Sapphire
	 */
	public int getSapphire() {
		return getStat(StatType.VOTING, "sapphire");
	}
	
	/**
	 * A player's Sapphire count is stored within the voting {@link #StatType}.
	 * <br>Add to a player's sapphire count.
	 */
	public void addToSapphire(int amount) {
		stats.addToStat(StatType.VOTING, "sapphire", amount);
		flagScoreboardUpdate();
	}
	
	public void setSapphire(int amount) {
		stats.setStat(StatType.VOTING, "sapphire", amount);
		flagScoreboardUpdate();
	}
	
	public boolean hasPermission(String permissionString) {
		return this.permissions.contains("*") || this.permissions.contains(permissionString);
	}
	
	public Set<String> getPrivatePermissions() {
		return this.privatePermissions;
	}
	
	public Set<String> getPermissions() {
		return permissions;
	}
	
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
	
	public PlayerProfile addRank(Rank rank) {
		if (!ranks.contains(rank)) {
			boolean colourIsRank = this.nameColour == getHighestRank().getRankHex();
			ranks.add(rank);
			updateRanksPerms();
			if (colourIsRank)
				this.nameColour = getHighestRank().getRankHex();
			
			Main.getInstance().getDiscord().updateRoles(this);
		}
		return this;
	}
	
	public PlayerProfile removeRank(Rank... rankz) {
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
		
		return this;
	}
	
	/**
	 * Sets the players ranks. This method does not call the {@link DiscordBot#updateRoles()} method 
	 * as it's not usually used in situations where ranks are updated, but rather previewed or loaded.
	 * @param ranks The new ranks of the player.
	 */
	public void setRanks(List<Rank> ranks) {
		this.ranks.clear();
		ranks.forEach((rank) -> { this.ranks.add(rank); });
		updateRanksPerms();
	}
	
	/**
	 * Organise the current rank list into the correct order, stores the highest of each rank category,
	 * calculates the current {@link #getWarpLimit()}, update the player's names with {@link #updateShownNames()} 
	 * and then update permissions based on current Ranks.
	 */
	private void updateRanksPerms() {
		// Order the Ranks and do Warp Limit
		List<Rank> rankz = new ArrayList<Rank>(ranks);
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
		
		updateShownNames();
		
		// Update perms
		if (isOnline() && Main.getInstance().isEnabled()) // TODO: possibly find a less static method of handling this..?
			Main.getPermissionManager().updatePlayerPermissions(getPlayer());
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
	
	public String setNickname(String nickname) {
		this.nickname = nickname;
		Main.getInstance().getDiscord().updateNickname(this);
		updateShownNames();
		return nickname;
	}
	
	public void setNameColour(int colour) {
		this.nameColour = colour;
		updateShownNames();
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public boolean hasNickname() {
		return nickname != null;
	}
	
	/**
	 * This method is only called in {@link ConnectionListener#onPlayerPreLogin()} to update this player's {@link #name} upon login.
	 */
	public void updateRealName(String name) {
		if (this.name != name) {
			this.name = name;
			Datasource.saveProfileColumn(getId(), "name", name);
		}
	}
	
	public TextColor getNameColour() {
		return TextColor.color(nameColour);
	}
	
	public String getDisplayName() {
		return (nickname==null?name:nickname);
	}
	
	public String getRealName() {
		return name;
	}
	
	public TextComponent getColouredName() {
		return this.colouredName;
	}
	
	public SkillData getSkills() {
		return skillData;
	}
	
	public Location getHome() {
		return this.home;
	}
	
	public void setHome(Location location) {
		this.home = location;
	}
	
	public ItemStack[] getArmourWardrobe() {
		return this.armourWardrobe;
	}
	
	public boolean performWardrobeSwap(int id) {
		if (!isOnline()) return false;
		
		final Player p = this.getPlayer();
		final int offset = (id-1)*4;
		final ItemStack[] armor = p.getInventory().getArmorContents().clone();
		final ItemStack[] oldStored = new ItemStack[]
				{
				this.armourWardrobe[offset + 0],
				this.armourWardrobe[offset + 1],
				this.armourWardrobe[offset + 2],
				this.armourWardrobe[offset + 3],
				};
		
		// Set armor
		p.getInventory().setArmorContents(oldStored);
		
		// Save stored armor
		for (int x = 0; x < 4; x++)
			this.armourWardrobe[offset + x] = armor[x];
		
		//Datasource.saveArmourWardrobe(this); saved with full profile
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 3, 1);
		return true;
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
	public void updateShownNames() {
		this.colouredName = Component.text(nickname==null?name:nickname).color(TextColor.color(getNameColour()));
			if (isOnline()) {
				Bukkit.getOnlinePlayers().forEach((p) -> { 
					PlayerConnection connection = ((CraftPlayer)p).getHandle().b;
					//connection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.d, ((CraftPlayer)p).getHandle())); // d updates player's display name
					connection.a(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.e, ((CraftPlayer)p).getHandle()));
					connection.a(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, ((CraftPlayer)p).getHandle()));
				});
				getPlayer().displayName(getColouredName()); // Display Name
				Main.getTeamManager().updatePlayerTeam(getPlayer()); // Team
				flagScoreboardUpdate();
				getPlayer().playerListName(getPlayer().teamDisplayName()); // Player List, done after the Team Update
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
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Rank: ").append(getHighestRank().toComponent()));
		
		int mins = getStat(StatType.GENERIC, "playtime") / 60;
		int hours = mins / 60;
		mins -= hours*60;
		
		if (isInCivilization() && hasJob())
			hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Job: ").append(getJob().toComponent()));
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Playtime: \u00a7f" + (hours > 0 ? hours + " Hours and " : "") + mins + " Minutes"));
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Id: \u00a7a" + getId()));
		
		this.chatLine = Component.empty().append(chatLine.hoverEvent(HoverEvent.showText(hoverComponents)));
	}
	
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
	
	public int getSkillLevel(SkillType skill) {
		return skillData.getSkillInfo(skill).getLevel();
	}
	
	public boolean isSettingEnabled(PlayerSetting setting) {
		return (this.booleanSettings & 1<<setting.ordinal()) != 0;
	}
	
	public void flipSetting(PlayerSetting setting) {
		this.booleanSettings ^= 1 << setting.ordinal();
		doSettingEffect(setting);
	}
	
	public void setSettingEnabled(PlayerSetting setting, boolean enabled) {
		if (enabled)
			this.booleanSettings |= 1 << setting.ordinal();
		else
			this.booleanSettings &= ~(1 << setting.ordinal());
		doSettingEffect(setting);
	}
	
	private void doSettingEffect(PlayerSetting setting) {
		//final boolean enabled = isSettingEnabled(setting);
		switch(setting) {
		//case HIDE_ARMOR:
		//	if (enabled)
		//		EquipmentHider.fakeRemoveArmor(playerUUID);
		//	else
		//		EquipmentHider.sendActualArmor(playerUUID);
		//	break;
		default:
			break;
		}
	}
	
	public long getSettings() {
		return this.booleanSettings;
	}
	
	public ItemStack getSkull() {
		return Utils.getSkullFromPlayer(Bukkit.getOfflinePlayer(this.getUniqueId()));
	}
	
	public BeanGui getBeanGui() {
		return this.currentlyViewedGUI;
	}
	
	public BeanGui setBeanGui(BeanGui gui) {
		if (!this.isOnline()) // Shouldn't happen.
			return null;
		return this.currentlyViewedGUI = gui;
	}
	
	public void closeBeanGui() {
		this.currentlyViewedGUI = null;
	}
	
	public boolean isOverridingProfile() {
		return hasPermission(Permission.PROFILE_OVERRIDE) && !this.profileOverride.equals(this.getUniqueId());
	}
	
	/**
	 * While in a BeanGUI
	 * @param i
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
		AdvancementProgress progress = getPlayer().getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString(advancementKey)));
		progress.getRemainingCriteria().forEach((c) -> progress.awardCriteria(c));
		
	//	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + getDisplayName() + " only " + advancementKey); // cheeky
	}
	
	/**
	 * If the player is online, revoke them of the specified advancement.
	 * @param advancementKey - The exact directory and key.
	 */
	public void revokeAdvancement(@Nonnull String advancementKey) {
		if (!isOnline()) return;
		AdvancementProgress progress = getPlayer().getAdvancementProgress(Bukkit.getAdvancement(NamespacedKey.fromString(advancementKey)));
		progress.getAwardedCriteria().forEach((c) -> progress.revokeCriteria(c));
	//	Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + getDisplayName() + " only " + advancementKey); // cheeky
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
	
	public boolean onCdElseAdd(String id, int mili) {
		return onCdElseAdd(id, mili, false);
	}
	
	public boolean onCdElseAdd(String id, int mili, boolean force) {
		if (force && hasPermission(Permission.BYPASS_COOLDOWNS))
			return false;
		boolean onCd = onCooldown(id);
		if (!onCd)
			addCooldown(id, mili);
		return onCd;
	}
	
	public float getLuck() {
		if (isOnline())
			return (float) getPlayer().getAttribute(Attribute.GENERIC_LUCK).getValue();
		return 0.0F;
	}
	
	/**
	 * Pretty much only used in {@link BeanCommand#onCommand()}
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
	
	/**
	 * Updates every 1.5 seconds in {@link Main#startMainServerLoop()}
	 * @return last region entered
	 */
	public Region getCurrentRegion() {
		return currentRegion;
	}
	
	public Region updateCurrentRegion(Region region) {
		return this.currentRegion = region;
	}
	
	public int getCivilizationId() {
		return civilization == null ? 0 : civilization.getId();
	}
	
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
	
	public List<Delivery> getInbox() {
		return inbox;
	}
	
	private long lastInboxUpdate;
	public void refreshInbox() {
		Datasource.refreshPlayerInbox(this);
		lastInboxUpdate = System.currentTimeMillis();
	}
	
	public long getLastInboxUpdate() {
		return lastInboxUpdate;
	}
	
	/**
	 * Inefficient ban check, make better in future.
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
		Main.getTeamManager().updatePlayerTeam(getPlayer()); // Team
		flagScoreboardUpdate();
		getPlayer().playerListName(getPlayer().teamDisplayName()); // Player List, done after the Team Update
		getPlayer().sendMessage(Component.text("\u00a77You are now AFK."));
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
		Main.getTeamManager().updatePlayerTeam(getPlayer()); // Team
		flagScoreboardUpdate();
		getPlayer().playerListName(getPlayer().teamDisplayName()); // Player List, done after the Team Update
		getPlayer().sendMessage(Component.text("\u00a77You are no longer AFK."));
	}
	
	/**
	 * Checks if the player is currently AFK. If so, mark them as AFK.
	 */
	public void checkAFK() {
		if (!isOnline() || isAFK()) return;
		
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
	 * @return time in millis when this profile instance was loaded.
	 */
	public long getLoadTime() {
		return loadTime;
	}
	
	public void updateScoreboard() {
		if (!isOnline()) return;
		Main.getTeamManager().updatePlayerScoreboard(getPlayer());
		this.scoreboardFlag = false;
	}
	
	private boolean scoreboardFlag;
	public void flagScoreboardUpdate() {
		this.scoreboardFlag = true;
	}
	
	public boolean needsScoreboardUpdate() {
		return this.scoreboardFlag;
	}
	
}
