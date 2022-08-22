package me.playground.listeners;

import java.util.ArrayList;

import me.playground.ranks.Permission;
import me.playground.regions.Region;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.main.PermissionManager;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import me.playground.utils.Calendar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.server.ServerListPingEvent;

public class ConnectionListener extends EventListener {

	private final PermissionManager permManager;
	private final int MAX_PLAYERS = 100; // excess just in case some staff members need to get in
	private final int MAX_DONOR_LIMIT = 50; // +20 donors
	private final int NON_DONOR_LIMIT = 30; // 30 normal users

	private final Component nonDonorFullMsg = Component.text("\u00a7cBean's Beans is currently full!\n\n\n\u00a77Did you know that ")
			.append(Rank.PATRICIAN.toComponent()).append(Component.text("\u00a77s bypass the player limit, up to a \n\u00a77cap of 50 players rather than the usual 30!"));

	private final Component donorFullMsg = Component.text("\u00a7cBean's Beans is currently full!");

	public ConnectionListener(Main plugin) {
		super(plugin);
		permManager = getPlugin().permissionManager();
		getPlugin().getServer().setMaxPlayers(MAX_PLAYERS);
	}
	
	@EventHandler
	public void ping(ServerListPingEvent e) {
		e.setMaxPlayers(NON_DONOR_LIMIT);
	}
	
	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
		if (e.getLoginResult() != Result.ALLOWED) return;

		try {
			PlayerProfile pp = PlayerProfile.from(e.getUniqueId());

			// Ban Check
			if (pp.isBanned()) {
				e.disallow(Result.KICK_BANNED, getPlugin().getPunishmentManager().getBanMessage(pp.getBan()));
				return;
			}

			// Player capacity
			int playerCount = getPlugin().getServer().getOnlinePlayers().size();
			if (playerCount >= NON_DONOR_LIMIT && !pp.hasPermission(Permission.BYPASS_MAX_PLAYERCOUNT)) {
				e.disallow(Result.KICK_FULL, nonDonorFullMsg);
			} else if (playerCount >= MAX_DONOR_LIMIT && !pp.hasPermission(Permission.BYPASS_MAX_DONORCOUNT)) {
				e.disallow(Result.KICK_FULL, donorFullMsg);
			} else {
				pp.updateRealName(e.getName());
				e.getPlayerProfile().setName(pp.getDisplayName()); // TODO: figure it out idk?
			}
		} catch (Exception ex) {
			e.disallow(Result.KICK_OTHER, Component.text("\u00a7cThere was a problem loading your profile.\n\u00a77Please contact a member of Staff via the Server's Discord."));
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		final PlayerProfile pp = PlayerProfile.from(p);
		final Region region = getRegionAt(p.getLocation());
		pp.updateCurrentRegion(region);

		permManager.updatePlayerPermissions(p);
		
		p.sendPlayerListHeader(
				Component.text("\u00a77It is currently \u00a7b" + Calendar.getTimeString(Calendar.getTime(p.getWorld()), true)
				+ "\u00a77 on \u00a73Day " + Calendar.getDay(p.getWorld().getFullTime()) 
				+ "\n\n\u00a7fOnline Players:"));
		

		pp.updateShownNames(false); // Done here due to requiring an existing player.
		pp.pokeAFK();
		getPlugin().teamManager().initScoreboard(p);
		getPlugin().npcManager().showAllNPCs(p);
		
		// Place menu item
		if (pp.isSettingEnabled(PlayerSetting.MENU_ITEM))
			p.getInventory().setItem(9, BeanItem.PLAYER_MENU.getOriginalStack());
		
		pp.getSkills().setBarPlayer();
		pp.getStats().setStat(StatType.GENERIC, "lastLogin", (int)(System.currentTimeMillis()/60000L));
		
		// Do hide stuff
		getPlugin().getServer().getOnlinePlayers().forEach(player -> {
			PlayerProfile profile = PlayerProfile.from(player);
			if (profile.isHidden() && !pp.isRank(Rank.MODERATOR))
				p.hidePlayer(getPlugin(), player);
			
			if (pp.isHidden() && !profile.isRank(Rank.MODERATOR))
				player.hidePlayer(getPlugin(), p);
		});

		// New user
		if (!p.hasPlayedBefore()) {
			e.joinMessage(!pp.isHidden() ? Component.text("» ", NamedTextColor.GREEN).append(pp.getComponentName()).append(Component.text(" joined the server for the first time!", NamedTextColor.YELLOW)) : null);
		} else {
			e.joinMessage(!pp.isHidden() ? Component.text("» ", NamedTextColor.GREEN).append(pp.getComponentName()).append(Component.text(" joined the server!", NamedTextColor.YELLOW)) : null);
		}

		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
			// Notify of mute
			if (pp.isMuted())
				p.sendMessage(getPlugin().getPunishmentManager().getMuteMessage(pp.getMute()));

			// Check for Donor Rank expiry
			pp.getCheckDonorExpiration();

			// Staff Messages
			if (pp.isRank(Rank.MODERATOR)) {
				ArrayList<ProfileModifyRequest> reqs = ProfileModifyRequest.getPendingRequests();
				if (reqs.size() > 0)
					p.sendMessage(Component.text("There are \u00a7b" + reqs.size() + " \u00a7rpending nickname requests.", BeanColor.STAFF));
			}
		});
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		final Player p = e.getPlayer();
		if (!p.hasPermission("bean.gm." + p.getGameMode().name().toLowerCase()))
			p.setGameMode(GameMode.SURVIVAL);
		
		permManager.clearPlayerPermissions(p);
		permManager.stopPreviewingRank(p);
		PlayerProfile pp = PlayerProfile.from(p);
		pp.getStats().setStat(StatType.GENERIC, "lastLogout", (int)(System.currentTimeMillis()/60000L));
		pp.closeBeanGui(); // Just in case
		e.quitMessage(!pp.isHidden() ? Component.text("« ", NamedTextColor.RED).append(pp.getComponentName()).append(Component.text(" left the server!", NamedTextColor.YELLOW)) : null);
	}

}
