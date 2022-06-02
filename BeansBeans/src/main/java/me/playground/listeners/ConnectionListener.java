package me.playground.listeners;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.playground.data.BanEntry;
import me.playground.data.Datasource;
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

public class ConnectionListener extends EventListener {

	private final PermissionManager permManager;
	
	public ConnectionListener(Main plugin) {
		super(plugin);
		permManager = getPlugin().permissionManager();
	}
	
	/*@EventHandler // TODO: Make a dynamic motd!
	public void ping(ServerListPingEvent e) {
		e.motd(Component.text("Bean's Beans").color(TextColor.color(0xFD1742)));
	}*/
	
	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
		if (e.getLoginResult() != Result.ALLOWED) return;
		
		BanEntry be = Datasource.getBanEntry(e.getUniqueId());
		
		if (be != null) {
			final boolean perma = be.getBanEnd() <= 0;
			
			e.disallow(Result.KICK_BANNED, Component.text("\u00a7cYou're " + (perma ? "\u00a74permanently\u00a7c" : "currently") + " banned from Bean's Beans!\n"
					+ "\u00a7cReason: \u00a7f\""+be.getBanReason()+"\""
					+ (perma ? "" : "\n\n\u00a7cTime Remaining: \u00a7b" + be.getTimeRemaining()) + "\n\n\n"
					+ "\u00a77If you believe this ban was a mistake or is unwarranted, please\n\u00a77contact a member of Staff via the Server's Discord."));
			return;
		}
		
		// If a player profile doesn't exist, this is where it's created.
		PlayerProfile pp = PlayerProfile.from(e.getUniqueId());
		if (pp == null) { // If there was a problem loading it, just kick them.
			e.disallow(Result.KICK_OTHER, Component.text("\u00a7cThere was a problem loading your profile.\n\u00a77Please contact a member of Staff via the Server's Discord."));
			return;
		}
		
		pp.updateRealName(e.getName());
		e.getPlayerProfile().setName(pp.getDisplayName()); // TODO: figure it out idk?
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		permManager.updatePlayerPermissions(p);
		
		p.sendPlayerListHeader(
				Component.text("\u00a77It is currently \u00a7b" + Calendar.getTimeString(Calendar.getTime(p.getWorld()), true)
				+ "\u00a77 on \u00a73Day " + Calendar.getDay(p.getWorld().getFullTime()) 
				+ "\n\n\u00a7fOnline Players:"));
		
		PlayerProfile pp = PlayerProfile.from(p);
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
		
		e.joinMessage(!pp.isHidden() ? Component.text("� ", NamedTextColor.GREEN).append(pp.getComponentName()).append(Component.text(" joined the server!", NamedTextColor.YELLOW)) : null);
		
		// Check for Donor Rank expiriry
		pp.getCheckDonorExpiration();
		
		// Staff Messages
		if (pp.isRank(Rank.MODERATOR)) {
			ArrayList<ProfileModifyRequest> reqs = ProfileModifyRequest.getPendingRequests();
			if (reqs.size() > 0)
				p.sendMessage(Component.text("There are \u00a7b" + reqs.size() + " \u00a7rpending nickname requests.", BeanColor.STAFF));
		}
		
		//EmbedBuilder eb = getPlugin().discord().embedBuilder(0x44ff89, "**"+pp.getDisplayName()+"** joined the server!");
		//getPlugin().discord().chatChannel().sendMessageEmbeds(eb.build()).queue();
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
		e.quitMessage(!pp.isHidden() ? Component.text("� ", NamedTextColor.RED).append(pp.getComponentName()).append(Component.text(" left the server!", NamedTextColor.YELLOW)) : null);
		//EmbedBuilder eb = getPlugin().discord().embedBuilder(0xff7876, "**"+pp.getDisplayName()+"** left the server!");
		//getPlugin().discord().chatChannel().sendMessageEmbeds(eb.build()).queue();
	}

}
