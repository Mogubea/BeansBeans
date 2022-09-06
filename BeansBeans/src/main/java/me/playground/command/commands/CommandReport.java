package me.playground.command.commands;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.discord.DiscordBot;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.utils.TabCompleter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandReport extends BeanCommand {
	
	public CommandReport(Main plugin) {
		super(plugin, false, 1, "report");
		description = "Report a bug or player.";
	}
	
	final String[] subCmds = {"bug", "player"};
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final String subCmd = args[0].toLowerCase();
		
		if (subCmd.equals("bug")) {
			if (args.length == 1)
				throw new CommandException(sender, "Please tell us about the in-game bug you found!");
			
			final StringBuilder sb = new StringBuilder();
			for (int x = 1; x < args.length; x++)
				sb.append(args[x] + " ");
			
			final EmbedBuilder report = new EmbedBuilder();
			report.setColor(0xff367a);
			report.setTitle("**Bug Report**");
			report.addField("Reporter", getReporterString(sender, profile), true);
			report.addField("Platform", "Minecraft Server", true);
			report.addField("Status", "New", true);
			report.addField("Last Updated", "<t:"+System.currentTimeMillis()/1000+":R>", false);
			
			report.addField("Bug Details", sb.toString(), false);
			
			if (isPlayer(sender)) {
				Player p = (Player) sender;
				Location l = p.getLocation();
				report.addField("Extra Information", "**World:** " + l.getWorld().getName() + ", **X:** " + l.getBlockX() + ", **Y:** " + l.getBlockY() + ", **Z:** " + l.getBlockZ(), false);
			}
			
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
			final Date date = new Date();
			
			if (!isPlayer(sender))
				report.setFooter("Posted at " + df.format(date));
			else
				report.setFooter("Posted at " + df.format(date), DiscordBot.getIconURL(profile.getId()));
			
			getDiscord().bot().getTextChannelById(getDiscord().getBugReportChatId()).sendMessageEmbeds(report.build()).queue((message) -> {
				message.addReaction("U+1F3C1").queue(); // Fixed on Live
				message.addReaction("U+1F3F3").queue(); // Fixed on Dev
				message.addReaction("U+2714").queue(); // Investigating
				message.addReaction("U+274C").queue(); // Not a Bug
			});
			
			sender.sendMessage(Component.text("\u00a77Your bug report has been submitted."));
		} else if (subCmd.equals("player")) {
			if (args.length == 1)
				throw new CommandException(sender, "You need to specify a player to report.");
			
			final ProfileStore target = ProfileStore.from(args[1], true);
			
			if (target == null)
				throw new CommandException(sender, "Couldn't find player '"+args[1]+"'");
			
			if (args.length == 2)
				throw new CommandException(sender, Component.text("Please describe why you are reporting ").append(target.getColouredName()).append(Component.text("\u00a7c.")));
			
			final StringBuilder sb = new StringBuilder();
			for (int x = 2; x < args.length; x++)
				sb.append(args[x] + " ");
			
			final EmbedBuilder report = new EmbedBuilder();
			report.setColor(0xff4222);
			report.setTitle("**Player Report for \"" + target.getRealName() + "\"**");
			report.addField("Reporter", getReporterString(sender, profile), true);
			report.addField("Platform", "Minecraft Server", true);
			report.addField("Status", "New", true);
			report.addField("Last Updated", "<t:"+System.currentTimeMillis()/1000+":R>", false);
			
			report.addField("Report Details", sb.toString(), false);
			
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
			final Date date = new Date();
			
			if (!isPlayer(sender))
				report.setFooter("Posted at " + df.format(date));
			else
				report.setFooter("Posted at " + df.format(date), DiscordBot.getIconURL(profile.getId()));
			
			report.setThumbnail(DiscordBot.getIconURL(target.getId()));
			
			getDiscord().bot().getTextChannelById(getDiscord().getPlayerReportChatId()).sendMessageEmbeds(report.build()).queue((message) -> {
				message.addReaction("U+1F3C1").queue(); // Solved
				message.addReaction("U+2714").queue(); // Investigating
			});
			
			sender.sendMessage(Component.text("\u00a77Your report regarding ").append(target.getColouredName()).append(Component.text("\u00a77 has been submitted.")));
		} else {
			throw new CommandException(sender, "Invalid report category (player / bug).");
		}
		return true;
	}
	
	private String getReporterString(CommandSender sender, PlayerProfile profile) {
		String reporter = sender.getName();
		if (isPlayer(sender) && getDiscord().isLinked(profile.getId())) {
			User user = getDiscord().bot().getUserById(getDiscord().linkedAccounts.get(profile.getId()));
			if (user == null)
				user = getDiscord().bot().retrieveUserById(getDiscord().linkedAccounts.get(profile.getId())).complete();
			if (user != null)
				reporter = user.getAsMention();
		}
		return reporter;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], subCmds);
		if (args[0].equals("player") && args.length == 2)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
