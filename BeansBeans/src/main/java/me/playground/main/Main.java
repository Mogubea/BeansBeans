package me.playground.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent.Cause;
import org.bukkit.plugin.java.JavaPlugin;

import me.playground.command.CommandManager;
import me.playground.command.commands.CommandCurse;
import me.playground.data.Datasource;
import me.playground.data.DatasourceCore;
import me.playground.discord.DiscordBot;
import me.playground.enchants.EnchantManager;
import me.playground.highscores.Highscores;
import me.playground.items.BeanItem;
import me.playground.listeners.ListenerManager;
import me.playground.listeners.protocol.ProtocolNPCListener;
import me.playground.loot.LootManager;
import me.playground.npc.NPCManager;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.recipes.RecipeManager;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.shop.ShopManager;
import me.playground.skills.Skill;
import me.playground.threads.StatusResponseThread;
import me.playground.threads.WhateverChat;
import me.playground.utils.Calendar;
import me.playground.utils.SignMenuFactory;
import me.playground.voting.VoteManager;
import me.playground.warps.WarpManager;
import me.playground.worlds.WorldManager;
import net.kyori.adventure.text.Component;

public class Main extends JavaPlugin {

	private static Main instance;
	public static long STARTUP_TIME;
	private final Random random = new Random();
	private boolean isDebug;
	private boolean fullyBooted;
	
	private Map<String, NamespacedKey> registeredKeys = new HashMap<String, NamespacedKey>();
	
	private EnchantManager enchantManager;
	private TeamManager teamManager;
	private CommandManager commandManager;
	public NPCManager npcManager;
	public Highscores highscores;
	private WorldManager worldManager;
	private RegionManager regionManager;
	private WarpManager warpManager;
	private ShopManager shopManager;
	private PermissionManager permissionManager;
	private RecipeManager recipeManager;
	private LootManager lootManager;
	private ListenerManager listenerManager;
	private SignMenuFactory signMenuFactory;
	
	private DiscordBot discordBot;
	private VoteManager voteManager;
	private WhateverChat webChatServer;
	private StatusResponseThread webStatusThread;
	
	private DatasourceCore datasource;

	public void onEnable() {
		isDebug = getConfig().getBoolean("debug", false);
		STARTUP_TIME = System.currentTimeMillis();
		instance = this;
		
		enchantManager = new EnchantManager(this);
		
		this.signMenuFactory = new SignMenuFactory(this);
		new ProtocolNPCListener(this);
		
		datasource = new DatasourceCore(this);
		Datasource.init(this);
		
		worldManager = new WorldManager(this); // Important to be before everything else due to a lot of things requiring the getWorld and getWorldId methods.
		regionManager = new RegionManager(this); // Should always be after WorldManager due to dependance.
		
		teamManager = new TeamManager(this);
		highscores = new Highscores();
		npcManager = new NPCManager(this); // Load all NPCs after TeamManager
		warpManager = new WarpManager(this);
		shopManager = new ShopManager(this);
		permissionManager = new PermissionManager(this);
		
		// Register Commands
		commandManager = new CommandManager(this);
		
		// Register Loot Manager
		lootManager = new LootManager(this);
		Datasource.loadAllLoot();
		
		// Register Listeners
		listenerManager = new ListenerManager(this);
		
		getSLF4JLogger().info("Loaded " + Skill.getRegisteredSkills().size() + " Skills"); // Before Civilizations as they and jobs depend on skills
		
		Datasource.loadAllCivilizations();
		
		getSLF4JLogger().info("Loaded " + BeanItem.values().length + " Custom Items");

		// UUID testId = UUID.randomUUID(); // creates a test profile, IT WORKS!!!!!
		// System.out.println("TEST 1: profile id for "+testId+" is: " +
		// PlayerProfile.from(testId).getId() + " loaded!");
		
		recipeManager = new RecipeManager(this);
		discordBot = new DiscordBot(this);
		
		//voteManager = new VoteManager(this);
		
		webChatServer = new WhateverChat(this, 8193);
		
		// TODO:
		try {
			webStatusThread = new StatusResponseThread(this, getServer().getIp(), 8191);
		} catch (Exception e) {
			getSLF4JLogger().warn("Failed to load Live Status Thread.");
			e.printStackTrace();
		}
		
		startMainServerLoop();
		fullyBooted = true;
	}
	
	public void onDisable() {
//		if (npcManager != null)
//			npcManager.hideAllNPCsFromAll();
		
		if (permissionManager != null) {
			for (UUID uuid : permissionManager.getRankPreviewers())
				permissionManager.stopPreviewingRank(Bukkit.getPlayer(uuid));
			
			for (Player p : Bukkit.getOnlinePlayers())
				permissionManager.clearPlayerPermissions(p);
		}
		
		for (Player p : Bukkit.getOnlinePlayers())
			p.kick(Component.text("\u00a7eBean's Beans is reloading."), Cause.RESTART_COMMAND);
		
		if (recipeManager != null)
			recipeManager.unregisterRecipes();
		
		if (fullyBooted) {
			Bukkit.getScheduler().cancelTask(mainLoop);
			Datasource.saveAll();
		}
		
		if (commandManager != null)
			commandManager.unregisterCommands();
		
		if (enchantManager != null)
			enchantManager.unregisterEnchantments();
		
		if (listenerManager != null)
			listenerManager.unregisterEvents();
		
		if (discordBot != null) 
			discordBot.shutdown();
		if (webChatServer != null) 
			webChatServer.shutdown();
		if (webStatusThread != null)
			webStatusThread.shutdown();
	}
	
	public DatasourceCore getDatasourceCore() {
		return datasource;
	}
	
	public SignMenuFactory getSignMenuFactory() {
		return this.signMenuFactory;
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public static RegionManager getRegionManager() {
		return Main.instance.regionManager;
	}
	
	public static ShopManager getShopManager() {
		return Main.instance.shopManager;
	}
	
	public static TeamManager getTeamManager() {
		return Main.instance.teamManager;
	}
	
	public static PermissionManager getPermissionManager() {
		return Main.instance.permissionManager;
	}
	
	public CommandManager commandManager() {
		return commandManager;
	}
	
	public WorldManager getWorldManager() {
		return worldManager;
	}
	
	public RegionManager regionManager() {
		return regionManager;
	}
	
	public WarpManager warpManager() {
		return warpManager;
	}
	
	public ShopManager shopManager() {
		return shopManager;
	}
	
	public TeamManager teamManager() {
		return teamManager;
	}
	
	public PermissionManager permissionManager() {
		return permissionManager;
	}
	
	public DiscordBot getDiscord() {
		return discordBot;
	}
	
	public RecipeManager recipeManager() {
		return recipeManager;
	}
	
	public NPCManager npcManager() {
		return npcManager;
	}
	
	public LootManager lootManager() {
		return lootManager;
	}
	
	public VoteManager voteManager() {
		return voteManager;
	}

	public WhateverChat getWebChatServer() {
		return webChatServer;
	}
	
	/**
	 * Get an online player whether it's by nickname or username.
	 */
	public Player searchForPlayer(String name) {
		final Collection<? extends Player> online = getServer().getOnlinePlayers();
		List<Player> targets = new ArrayList<Player>();
		targets.addAll(online);
		int size = targets.size();

		for (int x = -1; ++x < size;) {
			Player p = targets.get(x);
			me.playground.playerprofile.PlayerProfile pp = me.playground.playerprofile.PlayerProfile.from(p);
			if ((pp.hasNickname() && pp.getDisplayName().equalsIgnoreCase(name)) || pp.getRealName().equalsIgnoreCase(name))
				return p;
		}

		String lowerName = name.toLowerCase();
		if (lowerName.length() >= 3) {
			for (int x = -1; ++x < size;) {
				Player p = targets.get(x);
				me.playground.playerprofile.PlayerProfile pp = me.playground.playerprofile.PlayerProfile.from(p);
				if ((pp.hasNickname() && pp.getDisplayName().toLowerCase().contains(lowerName)) || pp.getRealName().toLowerCase().equalsIgnoreCase(lowerName))
					return p;
			}
		}
		return null;
	}
	
	private long lastProfilePoke;
	private long lastPlaytimePoke;
	private long lastHighscoreUpdate;
	private long lastScoreboardUpdate;
	private long lastDiscordPoke;
	
	private int mainLoop;
	
	private void startMainServerLoop() {
		mainLoop = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				final long mili = System.currentTimeMillis();
				boolean doScoreboardUpdate = mili-lastScoreboardUpdate >= 1000 * 5;
				boolean doProfilePoke = mili-lastProfilePoke >= 1000 * 15;
				
				// Every Second
				if (mili-lastPlaytimePoke >= 1000) {
					lastPlaytimePoke = mili;
					
					Bukkit.getOnlinePlayers().forEach((p) -> {
						final PlayerProfile pp = PlayerProfile.from(p);
						pp.checkAFK();
						
						pp.getStats().addToStat(StatType.GENERIC, pp.isAFK() ? "afktime" : "playtime", 1);
						if (p.isInvulnerable())
							pp.getStats().addToStat(StatType.GENERIC, "invulnerableTime", 1);
						// Notify user of region
						final Region region = regionManager().getRegion(p.getLocation());
						final Region oldRegion = pp.getCurrentRegion();
						if (oldRegion == null || pp.getCurrentRegion().getRegionId() != region.getRegionId()) {
							if (!pp.updateCurrentRegion(region).isWorldRegion()) {
								p.sendActionBar(Component.text("\u00a77You have entered region \u00a79" + region.getName()));
							} else if (oldRegion != null) {
								p.sendActionBar(Component.text("\u00a77You have left region \u00a79" + oldRegion.getName()));
							}
							pp.flagScoreboardUpdate();
						}
						
						// Every 5 Seconds
						if (doScoreboardUpdate) {
							lastScoreboardUpdate = mili;
							if (pp.needsScoreboardUpdate())
								pp.updateScoreboard();
						}
						
						// Every 15 Seconds
						if (doProfilePoke) {
							lastProfilePoke = mili;
							// Playtime Check - Only checks for the next rank in line, this could be a non-playtime rank (eg. Exalted -> Moderator), that's why there's a check.
							if (!permissionManager().isPreviewing(p)) {
								Rank next = Rank.values()[pp.getPlaytimeRank().ordinal()+1];
								if (next.isPlaytimeRank() && pp.getPlaytime() >= next.getPlaytimeRequirement()) {
									pp.grantAdvancement("beansbeans:advancements/ranks/"+next.lowerName());
									pp.addRank(next);
								}
							}
							pp.getCheckDonorExpiration();
							pp.updateComponentName();
							p.sendPlayerListHeader(
									Component.text("\u00a77It is currently \u00a7b" + Calendar.getTimeString(Calendar.getTime(p.getWorld()), true)
									+ "\u00a77 on \u00a73Day " + Calendar.getDay(p.getWorld().getFullTime()) 
									+ "\n\n\u00a7fOnline Players:"));
						}
						
					});
				}
				
				if (mili-lastDiscordPoke > 1000 * 60) {
					getDiscord().updateServerStatus(true);
					lastDiscordPoke = mili;
				}
				
				if (mili-lastHighscoreUpdate > Highscores.UPDATE_INTERVAL) {
					lastHighscoreUpdate = mili;
					highscores.updateStoredHighscores();
					CommandCurse.performCurse();
				}
			}
		}, 10L, 10L); // every 500ms
	}

	public NamespacedKey getKey(String key) {
		String lcKey = key.toLowerCase();
		NamespacedKey nsk = registeredKeys.get(lcKey);
		if (nsk == null) {
			nsk = new NamespacedKey(this, lcKey);
			registeredKeys.put(lcKey, nsk);
		}
		
		return nsk;
	}
	
	public NamespacedKey keyRecipe(String key) {
		return new NamespacedKey(this, "recipe_"+key);
	}
	
	public Random getRandom() {
		return random;
	}
	
	public boolean isDebugMode() {
		return this.isDebug;
	}
	
}
