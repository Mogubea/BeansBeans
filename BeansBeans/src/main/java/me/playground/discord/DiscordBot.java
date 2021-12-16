package me.playground.discord;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import club.minnced.discord.webhook.WebhookClient;
import me.playground.data.Datasource;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import me.playground.utils.Calendar;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

public class DiscordBot extends ListenerAdapter {
	
	private final Webhook hook;
	private final WebhookClient chatClient;
	private int lastId = -1;
	
	public final HashMap<Integer, Long> linkedAccounts = Datasource.grabLinkedDiscordAccounts();
	
	// Cache Icons for 30 minutes to reduce risk of rate limiting from source website.
	// This may lead to skins not updating for a while, but that's not a big enough deal to worry about.
	private static LoadingCache<Integer, Icon> iconCache = CacheBuilder.from("maximumSize=100,expireAfterAccess=30m")
			.build(
					new CacheLoader<Integer, Icon>() {
						public Icon load(Integer playerId) throws Exception { // if the key doesn't exist, request it via this method
							try {
								InputStream image = new URL(getIconURL(playerId)).openStream();
								return Icon.from(image);
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}
					});
	
	/**
	 * Grab the current {@link Icon} for the specified player from the cache.
	 */
	public Icon getHeadIcon(int playerId) {
		try {
			return iconCache.get(playerId);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getIconURL(int playerId) {
		return "https://cravatar.eu/helmavatar/" + ProfileStore.from(playerId, false).getRealName()+"/64.png";
	}
	
	public void sendWebhookMessage(int playerId, String message) {
		try {
			if (lastId != playerId) {
				final String name = ((TextComponent)ProfileStore.from(playerId, false).getColouredName()).content();
				Icon icon = getHeadIcon(playerId);
				hook.getManager().setName(name).setAvatar(icon).queue(woo -> {
					chatClient.send(message);
				});
				lastId = playerId;
			} else {
				chatClient.send(message);
			}
		} catch (ErrorResponseException e) { // Just in-case there's a situation where discord doesn't respond
		}
	}
	
	public WebhookClient getWebhookClient(int playerId) {
		
		
		return chatClient;
	}
	
	public void shutdown() {
		if (!isOnline()) return;
		chatClient.close();
		
		updateServerStatus(false);
		chatChannel().putPermissionOverride(chatChannel().getGuild().getRoleById(Rank.NEWBEAN.getDiscordId())).setDeny(Permission.MESSAGE_WRITE).queue();
		
		for (Webhook hook : chatChannel().retrieveWebhooks().complete())
			hook.delete().queue();
		
		try {
			Thread.sleep(1000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			discordBot.shutdown();
		}
	}
	
	private final String token;
	private final String statusMessageTitle;
	private final boolean isDebug;
	
	private long ingameChatId;
	private long statusChatId;
	private long playerReportChatId;
	private long statusMessageId;
	private long suggestionChatId;
	private long bugReportChatId;
	
	private final JDA discordBot;
	private final Main plugin;
	private final TextChannel ingameChat;
	
	private final long bootTime = System.currentTimeMillis()/1000;
	
	public DiscordBot(Main plugin) {
		this.plugin = plugin;
		this.token = plugin.getConfig().getString("discord.token");
		this.ingameChatId = plugin.getConfig().getLong("discord.ingameChannel");
		this.statusChatId = plugin.getConfig().getLong("discord.statusChannel");
		this.playerReportChatId = plugin.getConfig().getLong("discord.playerReportChannel");
		this.bugReportChatId = plugin.getConfig().getLong("discord.bugReportChannel");
		this.suggestionChatId = plugin.getConfig().getLong("discord.suggestionChannel");
		this.statusMessageTitle = plugin.getConfig().getString("discord.statusTitle");
		this.statusMessageId = plugin.getConfig().getLong("discord.statusMessage");
		this.isDebug = plugin.getConfig().getBoolean("debug", false);
		this.discordBot = buildBot();
		if (discordBot != null) {
			this.ingameChat = discordBot.getTextChannelById(ingameChatId);
			discordBot.addEventListener(this);
			chatChannel().putPermissionOverride(chatChannel().getGuild().getRoleById(Rank.NEWBEAN.getDiscordId())).setAllow(Permission.MESSAGE_WRITE).queue();
			registerCommands();
			
			this.hook = chatChannel().createWebhook("Chat Webhook").complete();
			this.chatClient = WebhookClient.withId(hook.getIdLong(), hook.getToken());
		} else {
			this.ingameChat = null;
			this.chatClient = null;
			this.hook = null;
		}
	}
	
	private JDA buildBot() {
		JDABuilder builder = JDABuilder.createDefault(token);
		JDA bot = null;
		try {
			bot = builder.build();
			bot.awaitReady();
		} catch (Exception e) {
			e.printStackTrace();
			getPlugin().getLog4JLogger().error("There was a problem connecting to Discord. Continuing on without the Discord Bot...");
		}
		return bot;
	}
	
	public boolean isOnline() {
		return discordBot != null;
	}
	
	public JDA bot() {
		return discordBot;
	}
	
	public long channelId() {
		return ingameChatId;
	}
	
	public TextChannel chatChannel() {
		return ingameChat;
	}
	
	public EmbedBuilder embedBuilder(int colour, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(colour);
		eb.appendDescription(description);
		return eb;
	}
	
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if (e.getMember().getUser().isBot())
			return;
		
		if (e.getChannel().getIdLong() == getPlayerReportChatId() || e.getChannel().getIdLong() == getBugReportChatId())
			this.getCommand("report").onMessageReactionAdd(e);
		else if (e.getChannel().getIdLong() == getSuggestionChatId())
			this.getCommand("suggest").onMessageReactionAdd(e);
	}
	
	public boolean isDebug() {
		return this.isDebug;
	}
	
	//
	public long getIngameChatId() {
		return this.ingameChatId;
	}
	
	public long getStatusChatId() {
		return this.statusChatId;
	}
	
	public long setStatusChatId(long newId) {
		return this.statusChatId = newId;
	}
	
	public long getStatusMessageId() {
		return this.statusMessageId;
	}
	
	public long setStatusMessageId(long newId) {
		return this.statusMessageId = newId;
	}
	
	public long getPlayerReportChatId() {
		return this.playerReportChatId;
	}
	
	public long getBugReportChatId() {
		return this.bugReportChatId;
	}
	
	public long getSuggestionChatId() {
		return this.suggestionChatId;
	}
	
	// XXX: Commands
	private final HashMap<String, DiscordCommand> discordCommands = new HashMap<String, DiscordCommand>();
	
	private void registerCommands() {
		List<CommandData> cmds = new ArrayList<CommandData>();
		
		cmds.add(registerCommand(new DiscordCommandOnline(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandStatusPost(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandLink(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandReport(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandSuggest(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandEmbed(getPlugin())).getCommandData());
		cmds.add(registerCommand(new DiscordCommandWho(getPlugin())).getCommandData());
		discordBot.updateCommands().addCommands(cmds).queue();
	}
	
	private DiscordCommand registerCommand(DiscordCommand cmd) {
		discordCommands.put(cmd.getCommandData().getName(), cmd);
		return cmd;
	}
	
	public DiscordCommand getCommand(String cmd) {
		return discordCommands.get(cmd.toLowerCase());
	}
	
	public void onSlashCommand(SlashCommandEvent e) {
		if (e.getMember().getUser().isBot()) 
			return;
		
		final DiscordCommand cmd = discordCommands.get(e.getName().toLowerCase());
		if (cmd != null) {
			cmd.preSlashCommand(e);
			return;
		}
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
		if (!e.getAuthor().isBot()) {
			if (e.getChannel().getIdLong() == ingameChatId) {
				String msg = MarkdownSanitizer.sanitize(e.getMessage().getContentStripped());
				if (msg.isEmpty())
					return;
				if (msg.length() > 160)
					msg = msg.substring(0, 160) + "...";
				
				ProfileStore ps = isLinked(e.getMember().getIdLong()) ? ProfileStore.from(getKey(linkedAccounts, e.getMember().getIdLong()), false) : null;
				final String name = ps == null ? e.getMember().getEffectiveName() : ps.getDisplayName();
				TextComponent chat = isRank(e.getMember(), Rank.MODERATOR) ? Component.empty().append(Component.text("\u24E2").color(TextColor.color(Rank.MODERATOR.getRankHex())).hoverEvent(HoverEvent.showText(Component.text("Staff Member").color(TextColor.color(Rank.MODERATOR.getRankHex()))))).append(Component.text(" ")) : Component.empty();
				chat = chat.append(Component.text(name).color(TextColor.color(0x7789ff)).hoverEvent(HoverEvent.showText(Component.text("Discord Client\n\u00a77- Tag: " + e.getAuthor().getAsTag()).color(TextColor.color(0x6779ff)))));
				chat = chat.append(Component.text("\u00a79 » \u00a7r").append(Component.text(msg)));
				
				for (Player pl : Bukkit.getOnlinePlayers()) {
					PlayerProfile ppl = PlayerProfile.from(pl);
					if (isRank(e.getMember(), Rank.MODERATOR) || !ppl.getIgnoredPlayers().contains(ps.getId()))
						pl.sendMessage(chat.colorIfAbsent(TextColor.color(0xabc5fa)));
				}
				getPlugin().getLogger().info("[DISCORD] " + name + ": " + msg);
			}
		}
	}
	
	public void updateServerStatus(boolean online) {
		if (!isOnline()) return;
		try {
			int count = Bukkit.getOnlinePlayers().size();
			String playerList = "no one";
			
			EmbedBuilder test = new EmbedBuilder();
			
			test.setTitle(statusMessageTitle, "http://beansbeans.net:8015");
			test.setColor(online ? 0x44ffaa : 0xee3567);
			test.addField("Status", online ? "Online" : "Offline", true);
			if (online)
				test.addField("Last Boot", "<t:"+bootTime+":R>", true);
			else
				test.addField("Shutdown Time", "<t:"+System.currentTimeMillis()/1000+":R>", true);
			
			int igtime = Calendar.getTime(Bukkit.getWorlds().get(0));
			
			test.addField("Server Plugin Version", plugin.getDescription().getVersion(), true);
			test.addField("Overworld Time", Calendar.getTimeString(igtime, true), true);
			test.addField("Minecraft Version", Bukkit.getBukkitVersion(), true);
			if (online) {
				playerList = "";
				int x = 0;
				for (Player p : Bukkit.getOnlinePlayers())
					playerList += p.getName() + (++x < count ? ", " : "");
				test.addField("Online Players ("+count+"/"+Bukkit.getServer().getMaxPlayers()+")", playerList, false);
				
				int hour = Calendar.getHour(igtime);
				
				if (hour > 18 || hour < 6) { // Night
					test.setImage("https://i.imgur.com/f4Z0gGV.png");
				} else if (hour >= 6 && hour <= 8) { // Dawn
					test.setImage("https://i.imgur.com/dggMO9T.png");
				} else if (hour > 8 && hour < 17) { // Day
					test.setImage("https://i.imgur.com/ROCQYbe.png");
				} else { // Dusk
					test.setImage("https://i.imgur.com/DwOruz2.png");
				}
			} else {
				test.setImage("https://i.imgur.com/CPMkKYg.png");
			}
			test.addField("Random Statistics", "There are **" + plugin.shopManager().countShops() + "** Shops\n"
					+ "There are **"+plugin.regionManager().countRegions()+"** Regions\n"
					+ "There are **"+plugin.warpManager().countWarps()+"** Warps", false);
			test.setFooter("Last updated ");
			test.setTimestamp(Instant.now());
			
			discordBot.getTextChannelById(statusChatId).retrieveMessageById(statusMessageId).queue((message) -> {
				message.editMessageEmbeds(test.build()).queue();
			});
			
			String pString1 = (count > 0 ? count + (count > 1 ? " Players" : " Player") : "no one");
			discordBot.getPresence().setActivity(Activity.playing("Bean's Beans (" + count + "/50 Players)"));
			
			if (online)
				chatChannel().getManager().setTopic("Have a chat with the players currently on the server!\n\nThere is currently **"+pString1+"** online;\n" + playerList).queue();
			else
				chatChannel().getManager().setTopic("Have a chat with the players that would be on the server.. If it was open!!").queue();
		} catch (ErrorResponseException e) {}
	}
	
	/**
	 * Update the player's nickname on Discord.
	 * @param pp The player's profile
	 */
	public void updateNickname(PlayerProfile pp) {
		if (isDebug() || !isOnline()) return;
		Guild g = chatChannel().getGuild();
		try {
			Member member = g.retrieveMemberById(linkedAccounts.getOrDefault(pp.getId(), 0L)).complete();
			g.modifyNickname(member, pp.getDisplayName()).queue();
		} catch (ErrorResponseException e) {}
	}
	
	/**
	 * Update the player's roles on Discord, this excludes any Staff ranks.
	 * @param pp The player's profile
	 */
	public void updateRoles(PlayerProfile pp) {
		if (isDebug() || !isOnline()) return;
		Guild g = chatChannel().getGuild();
		try {
			Member member = g.retrieveMemberById(linkedAccounts.getOrDefault(pp.getId(), 0L)).complete();
			ArrayList<Role> addRoles = new ArrayList<Role>();
			ArrayList<Role> remRoles = new ArrayList<Role>();
			
			// Remove any roles they don't have
			member.getRoles().forEach(role -> {
				try {
					Rank rank = Rank.fromString(role.getName());
					if (!rank.isStaffRank() && !pp.isRank(rank))
						remRoles.add(role);
				} catch (RuntimeException e) {}
			});
			
			// Add any roles, excluding staff roles, they have
			pp.getRanks().forEach(rank -> {
				if (!rank.isStaffRank())
					addRoles.add(g.getRoleById(rank.getDiscordId())); 
			});
			
			g.modifyMemberRoles(member, addRoles, remRoles).queue();
		} catch (ErrorResponseException e) {}
	}
	
	public <K, V> K getKey(Map<K, V> map, V value) {
	    for (Entry<K, V> entry : map.entrySet()) {
	        if (entry.getValue().equals(value)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	public Main getPlugin() {
		return this.plugin;
	}
	
	public boolean isLinked(long id) {
		return linkedAccounts.containsKey((int)id) || linkedAccounts.containsValue(id);
	}
	
	public boolean isRank(Member member, Rank rank) {
		List<Role> roles = member.getRoles();
	    return (roles.stream()
	                .filter(role -> (role.getIdLong()==rank.getDiscordId())) // filter by role name
	                .findFirst() // take first result
	                .orElse(null)) != null; // else return null
	}
}
