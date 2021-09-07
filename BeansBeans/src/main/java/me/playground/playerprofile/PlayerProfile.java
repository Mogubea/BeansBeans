package me.playground.playerprofile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.playground.command.BeanCommand;
import me.playground.data.Datasource;
import me.playground.gui.BeanGui;
import me.playground.items.BeanItem;
import me.playground.listeners.ConnectionListener;
import me.playground.main.Main;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillType;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

public class PlayerProfile {
	
	public static LoadingCache<UUID, PlayerProfile> profileCache = CacheBuilder.from("maximumSize=500,expireAfterAccess=2m")
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
		return ProfileStore.from(name, false).getDBID();
	}
	
	public static int getDBID(OfflinePlayer p) {
		return ProfileStore.from(p.getUniqueId(), false).getDBID();
	}
	
	public static int getDBID(UUID uuid) {
		return ProfileStore.from(uuid, false).getDBID();
	}
	
	public static TextComponent getDisplayName(UUID uuid) {
		return ProfileStore.from(uuid, false).getColouredName();
	}
	
	public static TextComponent getDisplayName(int id) {
		return ProfileStore.from(id, false).getColouredName();
	}
	
	// XXX: Class Begins
	
	private final int 					playerId;
	
	private final ArrayList<Integer> 	ignoredPlayers = new ArrayList<Integer>();
	private final ArrayList<Rank> 		ranks = new ArrayList<Rank>();
	
	private final SkillData 			skillData;
	
	private String 						name;
	private String 						nickname;
	private TextComponent   			colouredName;
	private int 						nameColour = Rank.NEWBEAN.getRankColour();
	
	private long 						coins = 500;
	private short						warpCount;
	
	private UUID 						playerUUID;
	
	private Location					home;
	private ItemStack[] 				armourWardrobe;
	private final HeirloomInventory		heirloomInventory;
	private ArrayList<String> 			pickupBlacklist = new ArrayList<String>();
	
	private long 						booleanSettings = PlayerSetting.getDefaultSettings();
	
	// Not Saved
	private int							warpLimit;
	private ArrayList<String>			recentWarps = new ArrayList<String>(10);
	private HashMap<String, Long>  		cooldowns = new HashMap<String, Long>();
	private BeanGui			 			currentlyViewedGUI;
	private TextComponent 				chatLine;
	private Location[] 					lastLocation = new Location[2];
	private Region 						currentRegion;
	
	// Admin
	public UUID profileOverride;
	
	public PlayerProfile(int id, UUID uuid, ArrayList<Rank> ranks, int nameColour, String name, String nickname, long coins, long settings, short warpCount) {
		this.playerId = id;
		this.profileOverride = uuid; // XXX: TEMP
		this.playerUUID = uuid;
		this.nameColour = nameColour;
		
		this.name = name;
		if (!name.equals(nickname))
			this.nickname = nickname;
		
		this.coins = coins;
		this.warpCount = warpCount;
		
		this.booleanSettings = settings;
		
		this.home = Datasource.loadHome(id);
		this.skillData = Datasource.loadOrMakeBeanExperience(id);
		this.armourWardrobe = Datasource.loadArmourWardrobe(id);
		this.pickupBlacklist = Datasource.loadPickupBlacklist(id);
		this.heirloomInventory = new HeirloomInventory(this, Datasource.loadPlayerHeirlooms(id));
		
		setRanks(ranks);
		//Bukkit.getConsoleSender().sendMessage(Component.text("Profile was loaded for " + (hasNickname() ? getNickname() + " ("+getRealName()+")" : getRealName())));
	}
	
	public boolean isOnline() {
		return getPlayer()!=null;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(playerUUID);
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(playerUUID);
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
	}
	
	public void addToBalance(long amount, String log) {
		addToBalance(amount);
		Datasource.logTransaction(playerId, amount, log);
	}
	
	public void addToBalance(long amount) {
		coins+=amount;
		if (this.isOnline())
			getPlayer().sendActionBar(Component.text("\u00a76" + getBalance() + " Coins \u00a77( " + (amount>-1 ? "\u00a7a+" : "\u00a7c") + amount + "\u00a77 )"));
	}
	
	public boolean isMod() {
		return ranks.contains(Rank.MODERATOR)||isAdmin()||isOwner();
	}
	
	public boolean isAdmin() {
		return ranks.contains(Rank.ADMINISTRATOR)||isOwner();
	}
	
	public boolean isOwner() {
		return ranks.contains(Rank.OWNER);
	}
	
	public boolean isRank(Rank rank) {
		return ranks.contains(rank);
	}
	
	public Rank getHighestRank() {
		Rank r = Rank.NEWBEAN;
		for (Rank rr : ranks) {
			if (rr.power()>r.power())
				r = rr;
		}
		return r;
	}
	
	public Rank getDonorRank() {
		Rank r = null;
		for (Rank rr : ranks) {
			if (rr.isDonorRank() && (r != null ? rr.power()>r.power() : true))
				r = rr;
		}
		return r;
	}
	
	public Rank getPlaytimeRank() {
		Rank r = Rank.NEWBEAN;
		for (Rank rr : ranks) {
			if (rr.isDonorRank() || rr.isStaffRank())
				continue;
			
			if (rr.power() > r.power())
				r = rr;
		}
		return r;
	}
	
	public ArrayList<Rank> getRanks() {
		return ranks;
	}
	
	public Component getComponentRanks() {
		Component c = Component.text("");
		for (int x = 0; x < getRanks().size(); x++) {
			final Rank rank = getRanks().get(x);
			c = c.append(rank.toComponent());
			if (x+1 < getRanks().size())
				c = c.append(Component.text("\u00a7r, "));
		}
		return c;
	}
	
	public PlayerProfile addRank(Rank rank) {
		if (!ranks.contains(rank)) {
			boolean colourIsRank = this.nameColour == getHighestRank().getRankColour();
			ranks.add(rank);
			updateAll();
			if (colourIsRank)
				this.nameColour = getHighestRank().getRankColour();
			
			Main.getInstance().discord().updateRoles(this);
		}
		return this;
	}
	
	public PlayerProfile removeRank(Rank rank) {
		if (ranks.contains(rank)) {
			boolean colourIsRank = this.nameColour == getHighestRank().getRankColour();
			ranks.remove(rank);
			updateAll();
			if (colourIsRank)
				this.nameColour = getHighestRank().getRankColour();
			Main.getInstance().discord().updateRoles(this);
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
		updateAll();
	}
	
	/**
	 * Organise the current rank list into the correct order, calculate the current {@link #getWarpLimit()}, 
	 * update the player's names with {@link #updateShownNames()} and then update permissions based on current Ranks.
	 */
	private void updateAll() {
		// Order the Ranks and do Warp Limit
		List<Rank> rankz = new ArrayList<Rank>(ranks);
		this.warpLimit = 0;
		this.ranks.clear();
		for (Rank rank : Rank.values())
			if (rankz.contains(rank)) {
				ranks.add(rank);
				warpLimit += rank.getWarpBonus();
			}
		
		updateShownNames();
		
		// Update perms
		if (isOnline() && Main.getInstance().isEnabled())
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
		Main.getInstance().discord().updateNickname(this);
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
			Datasource.saveProfileColumn(this, "name", name);
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
		if (this.getPlayer() == null || !this.getPlayer().isOnline())
			return false;
		
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
		if (this.armourWardrobe == null)
			return;
		
		final Player p = this.getPlayer();
		final int offset = (id-1)*4;
		
		for (int x = 0; x < 4; x++) {
			if (this.armourWardrobe[offset + x] == null)
				continue;
			
			Bukkit.getServer().getWorld(p.getWorld().getName()).dropItem(p.getLocation(), this.armourWardrobe[offset + x]);
			this.armourWardrobe[offset + x] = null;
		}
		
	}
	
	/**
	 * Update the player's current names.
	 * Also fires {@link #updateComponentName()}.
	 */
	public void updateShownNames() {
		this.colouredName = Component.text(nickname==null?name:nickname).color(TextColor.color(getNameColour()));
			if (getPlayer() != null) {
				//getPlayer().getPlayerProfile().setName(getDisplayName()); // overhead and commands
				getPlayer().displayName(getColouredName()); // ??
				getPlayer().playerListName(getColouredName()); // player list
				Main.getTeamManager().updatePlayerTeam(this);
			}
			ProfileStore.updateStore(playerId, playerUUID, name, getColouredName());
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
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Rank: ").append(getHighestRank().toComponent()));
		
		int mins = getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60;
		int hours = Math.floorDiv(mins, 60);
		mins -= hours*60;
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- Playtime: \u00a7f" + (hours > 0 ? hours + " Hours and " : "") + mins + " Minutes"));
		
		hoverComponents = hoverComponents.append(Component.text("\n\u00a77- DBID: \u00a7a" + getId()));
		
		this.chatLine = Component.empty().append(chatLine.hoverEvent(HoverEvent.showText(hoverComponents)));
	}
	
	public TextComponent getComponentName() {
		if (chatLine == null)
			updateComponentName();
		return chatLine;
	}
	
	// XXX: Attributes
	public void updateAttributeBonuses() {
		if (this.isOnline()) {
			this.getHeirlooms().getModifiers().forEach((attribute, amount) -> {
				updateAttribute(attribute, amount);
			});
		}
	}
	
	public void updateAttribute(Attribute attribute, double amount) {
		if (this.isOnline()) {
			Player p = this.getPlayer();
			p.getAttribute(attribute).getModifiers().forEach((modifier) -> {
				p.getAttribute(attribute).removeModifier(modifier);
			});
			p.getAttribute(attribute).addModifier(new AttributeModifier(UUID.randomUUID(), attribute.translationKey(), amount, Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		}
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
		return this.isAdmin() && !this.profileOverride.equals(this.getUniqueId());
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
	
	public HeirloomInventory getHeirlooms() {
		return this.heirloomInventory;
	}
	
	public ArrayList<String> getPickupBlacklist() {
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
		boolean onCd = onCooldown(id);
		if (!onCd)
			addCooldown(id, mili);
		return onCd;
	}
	
	/**
	 * Pretty much only used in {@link BeanCommand#onCommand()}
	 * @param id Cooldown identifier
	 * @return the amount of milliseconds left in the cooldown
	 */
	public long getCooldown(String id) {
		return this.cooldowns.getOrDefault(id, 0L) - System.currentTimeMillis();
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
	
}
