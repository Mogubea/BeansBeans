package me.playground.listeners;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.playground.data.BanEntry;
import me.playground.data.Datasource;
import me.playground.gui.BeanGui;
import me.playground.main.Main;
import me.playground.main.PermissionManager;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileModifyRequest;
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
		if (e.getLoginResult() == Result.KICK_WHITELIST) {
			e.disallow(Result.KICK_WHITELIST, Component.text("Bean's Beans is currently whitelisted.", NamedTextColor.RED));
			return;
		}
		
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
		pp.updateShownNames(); // Done here due to requiring an existing player.
		getPlugin().teamManager().initScoreboard(p);
		getPlugin().npcManager().showAllNPCs(p);
		
		// Check for menu item
		ItemStack menuSlot = p.getInventory().getItem(9);
		if (menuSlot == null || !menuSlot.equals(BeanGui.menuItem))
			p.getInventory().setItem(9, BeanGui.menuItem);
		
		pp.getSkills().assignBarPlayer(p);
		e.joinMessage(Component.text("» ", NamedTextColor.GREEN).append(pp.getComponentName()).append(Component.text(" joined the server!", NamedTextColor.YELLOW)));
		
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
		pp.closeBeanGui(); // Just in case
		e.quitMessage(Component.text("« ", NamedTextColor.RED).append(pp.getComponentName()).append(Component.text(" left the server!", NamedTextColor.YELLOW)));
		//EmbedBuilder eb = getPlugin().discord().embedBuilder(0xff7876, "**"+pp.getDisplayName()+"** left the server!");
		//getPlugin().discord().chatChannel().sendMessageEmbeds(eb.build()).queue();
	}

}
