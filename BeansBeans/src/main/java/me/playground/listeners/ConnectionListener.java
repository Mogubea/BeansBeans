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
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileModifyRequest;
import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;

public class ConnectionListener extends EventListener {

	public ConnectionListener(Main plugin) {
		super(plugin);
	}
	
	/*@EventHandler // TODO: Make a dynamic motd!
	public void ping(ServerListPingEvent e) {
		e.motd(Component.text("Bean's Beans").color(TextColor.color(0xFD1742)));
	}*/
	
	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
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
		e.getPlayerProfile().setName(pp.getDisplayName());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		getPlugin().permissionManager().updatePlayerPermissions(p);
		
		PlayerProfile pp = PlayerProfile.from(p);
		pp.updateShownNames(); // Done here due to requiring an existing player.

		getPlugin().npcManager().showAllNPCs(p);
		
		// Check for menu item
		ItemStack menuSlot = p.getInventory().getItem(9);
		if (menuSlot == null || !menuSlot.equals(BeanGui.menuItem))
			p.getInventory().setItem(9, BeanGui.menuItem);
		
		pp.getSkills().assignBarPlayer(p);
		e.joinMessage(Component.text("\u00a7a» ").append(pp.getComponentName()).append(Component.text("\u00a7e joined the server!")));
		
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
		if (!e.getPlayer().hasPermission("bean.gm." + e.getPlayer().getGameMode().name().toLowerCase()))
			e.getPlayer().setGameMode(GameMode.SURVIVAL);
		
		getPlugin().permissionManager().clearPlayerPermissions(e.getPlayer());
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		pp.closeBeanGui(); // Just in case
		e.quitMessage(Component.text("\u00a7c« ").append(pp.getComponentName()).append(Component.text("\u00a7e left the server!")));
		//EmbedBuilder eb = getPlugin().discord().embedBuilder(0xff7876, "**"+pp.getDisplayName()+"** left the server!");
		//getPlugin().discord().chatChannel().sendMessageEmbeds(eb.build()).queue();
	}

}
