package me.playground.main;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.playground.command.CommandManager;
import me.playground.currency.Currency;
import me.playground.data.Datasource;
import me.playground.discord.DiscordBot;
import me.playground.enchants.BeanEnchantment;
import me.playground.highscores.Highscores;
import me.playground.items.BeanItem;
import me.playground.listeners.ListenerManager;
import me.playground.listeners.protocol.ProtocolNPCListener;
import me.playground.loot.LootManager;
import me.playground.npc.NPCManager;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.recipes.RecipeManager;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.shop.ShopManager;
import me.playground.utils.BeanColor;
import me.playground.utils.Calendar;
import me.playground.utils.SignMenuFactory;
import me.playground.utils.Utils;
import me.playground.warps.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class Main extends JavaPlugin {

	private static Main instance;
	private final Random random = new Random();
	private byte serverState = 0;
	
	private TeamManager teamManager;
	private CommandManager commandManager;
	public NPCManager npcManager;
	public Highscores highscores;
	private RegionManager regionManager;
	private WarpManager warpManager;
	private ShopManager shopManager;
	private PermissionManager permissionManager;
	private RecipeManager recipeManager;
	private LootManager lootManager;
	private SignMenuFactory signMenuFactory;
	private DiscordBot discordBot;

	public void onEnable() {
		instance = this;
		registerEnchantments();
		
		this.signMenuFactory = new SignMenuFactory(this);
		new ProtocolNPCListener(this);
		
		Datasource.init(this);
		
		Datasource.loadWorlds();
		
		regionManager = new RegionManager();
		Datasource.loadAllRegions();
		
		teamManager = new TeamManager();
		highscores = new Highscores();
		npcManager = new NPCManager(this);
		Datasource.loadAllNPCs();
		warpManager = new WarpManager();
		shopManager = new ShopManager();
		permissionManager = new PermissionManager(this);
		
		// Register Commands
		commandManager = new CommandManager(this);
		
		// Register Loot Manager
		lootManager = new LootManager(this);
		Datasource.loadAllLoot();
		
		// Register Listeners
		new ListenerManager(this);
		
		Datasource.loadAllCivilizations();
		
		// Load rando magic	
		registerProtocol();
		
		getLogger().fine("Loaded " + BeanItem.values().length + " Custom Items");

		// UUID testId = UUID.randomUUID(); // creates a test profile, IT WORKS!!!!!
		// System.out.println("TEST 1: profile id for "+testId+" is: " +
		// PlayerProfile.from(testId).getId() + " loaded!");
		
		recipeManager = new RecipeManager(this);
		discordBot = new DiscordBot(this);
		
		startMainServerLoop();
	}

	public void onDisable() {
		npcManager.hideAllNPCsFromAll();
		
		for (UUID uuid : permissionManager.getRankPreviewers())
			permissionManager.stopPreviewingRank(Bukkit.getPlayer(uuid));
		
		for (Player p : Bukkit.getOnlinePlayers()) { // wtf is this title method lol
			p.showTitle(Title.title(Component.text("\u00a7cServer is Restarting..."), Component.empty(), Times.of(Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(1))));
			p.closeInventory();
			PlayerProfile.from(p).getSkills().forceHideBar();
			permissionManager.clearPlayerPermissions(p);
		}
		
		Datasource.saveAll();
		unregisterEnchantments();
		discordBot.shutdown();
	}
	
	public SignMenuFactory getSignMenuFactory() {
		return this.signMenuFactory;
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public static CommandManager getCommandManager() {
		return Main.instance.commandManager;
	}
	
	public static RegionManager getRegionManager() {
		return Main.instance.regionManager;
	}
	
	public static WarpManager getWarpManager() {
		return Main.instance.warpManager;
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
	public DiscordBot discord() {
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

	private void registerProtocol() {
		//ProtocolLibrary.getProtocolManager().addPacketListener(new EquipmentHider(this));
		/*ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            /*@Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                	//event.getPlayer().sendMessage(event.getPacket().getEntityTypeModifier().getValues().get(0) + "");
                	// TODO:
                }
            }
            @Override
            public void onPacketSending(PacketEvent event) {
            	
            }
        });*/
	}
	
	private void registerEnchantments() {
		try {
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
            for (BeanEnchantment ench : BeanEnchantment.getCustomEnchants())
            	Enchantment.registerEnchantment(ench);
            getLogger().fine("Registered custom enchantments.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	@SuppressWarnings("unchecked")
	private void unregisterEnchantments() {
		try {
           Field keyField = Enchantment.class.getDeclaredField("byKey");
           Field nameField = Enchantment.class.getDeclaredField("byName");
           keyField.setAccessible(true);
           nameField.setAccessible(true);
           HashMap<NamespacedKey, Enchantment> byKey = (HashMap<NamespacedKey, Enchantment>) keyField.get(null);
           HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) nameField.get(null);
           for (BeanEnchantment ench : BeanEnchantment.getCustomEnchants()) {
        	   byKey.remove(ench.getKey());
        	   byName.remove(ench.getName());
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	/**
	 * 0 = Open for all. 1 = Closed for all except Mods+ 2 = Closed for all except
	 * Admins+
	 * 
	 * @return the current open state of the server.
	 */
	public static byte getServerOpenState() {
		return Main.getInstance().serverState;
	}

	public static void setServerOpenState(int state) {
		switch (state) {
		case 1:
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerProfile pp = PlayerProfile.from(p);
				if (!pp.isMod())
					p.kick(Component.text("Server is going into maintenance."));
			}
			break;
		case 2:
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerProfile pp = PlayerProfile.from(p);
				if (!pp.isAdmin())
					p.kick(Component.text("Server is going into maintenance."));
			}
			break;
		default:
			state = 0;
			break;
		}
		Main.getInstance().serverState = (byte) state;
	}
	
	private long lastProfilePoke;
	private long lastHighscoreUpdate;
	private long lastDiscordPoke;
	private long lastPreviewPoke;
	
	private void startMainServerLoop() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				final long mili = System.currentTimeMillis();
				//ticksDone += 20;
				
				if (mili-lastPreviewPoke > 1500) {
					lastPreviewPoke = mili;
					
					// Rank preview notification.
					permissionManager().getRankPreviewers().forEach((uuid) -> {
						PlayerProfile pp = PlayerProfile.from(uuid);
						if (pp.isOnline())
							pp.getPlayer().sendActionBar(Component.text("You are currently previewing ").append(pp.getHighestRank().toComponent()));
						else
							permissionManager().stopPreviewingRank(pp);
					});
					
					Bukkit.getOnlinePlayers().forEach((p) -> {
						final PlayerProfile pp = PlayerProfile.from(p);
						
						// Notify user of region
						final Region region = regionManager().getRegion(p.getLocation());
						final Region oldRegion = pp.getCurrentRegion();
						if (oldRegion == null || pp.getCurrentRegion().getRegionId() != region.getRegionId()) {
							if (!pp.updateCurrentRegion(region).isWorldRegion()) {
								p.sendActionBar(Component.text("\u00a77You have entered region \u00a79" + region.getName()));
							} else if (oldRegion != null) {
								p.sendActionBar(Component.text("\u00a77You have left region \u00a79" + oldRegion.getName()));
							}
						}
						
						if (mili-lastProfilePoke > 1500 * 8) {
							lastProfilePoke = mili;
							// Playtime Check - Only checks for the next rank in line, this could be a non-playtime rank (eg. Exalted -> Moderator), that's why there's a check.
							if (!permissionManager().isPreviewing(p)) {
								Rank next = Rank.values()[pp.getPlaytimeRank().ordinal()+1];
								if (next.isPlaytimeRank() && (p.getStatistic(Statistic.PLAY_ONE_MINUTE)/20) >= next.getPlaytimeRequirement()) {
									pp.grantAdvancement("beansbeans:advancements/ranks/"+next.lowerName());
									pp.addRank(next);
								}
							}
							
							pp.updateComponentName();
							p.sendPlayerListHeaderAndFooter(
									Component.text("\u00a77It is currently \u00a7b" + Calendar.getTimeString(Calendar.getTime(p.getWorld()), true)
									+ "\u00a77 on \u00a73Day " + Calendar.getDay(p.getWorld().getFullTime()) 
									+ "\n\n\u00a7fOnline Players:"),
									
									pp.getHighestRank().toComponent().append(Component.text("\u00a7f --- " + 
									Utils.currencyString(Currency.COINS, pp.getBalance()) + "\u00a7f --- \u00a7r" 
									+ pp.getHeirlooms().size() + " Heirlooms").colorIfAbsent(BeanColor.HEIRLOOM)));
						}
						
					});
				}
				
				if (mili-lastDiscordPoke > 1000 * 60) {
					discord().updateServerStatus(true);
					lastDiscordPoke = mili;
				}
				
				if (mili-lastHighscoreUpdate > Highscores.UPDATE_INTERVAL) {
					lastHighscoreUpdate = mili;
					highscores.updateStoredHighscores();
				}
			}
		}, 10L, 10L); // every 500ms
	}

	public static NamespacedKey key(String key) {
		return new NamespacedKey(getInstance(), key);
	}
	
	public NamespacedKey keyRecipe(String key) {
		return new NamespacedKey(this, "recipe_"+key);
	}
	
	public Random getRandom() {
		return random;
	}
	
}
