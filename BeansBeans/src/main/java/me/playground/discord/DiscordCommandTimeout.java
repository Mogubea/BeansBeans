package me.playground.discord;

import java.util.concurrent.TimeUnit;

import me.playground.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DiscordCommandTimeout extends DiscordCommand {

	public DiscordCommandTimeout(Main plugin) {
		super(plugin, null, new CommandData("timeout", "General time out command.")
				.addSubcommands(
					new SubcommandData("give", "Give the user a time out. This stacks.")
						.addOption(OptionType.USER, "user", "The user going to be timed out.", true)
						//.addOption(OptionType.STRING, "reason", "The reason for timing out this user.", true)
						.addOption(OptionType.INTEGER, "days", "The amount of days to time out for.")
						.addOption(OptionType.INTEGER, "minutes", "The amount of minutes to time out for.")
						.addOption(OptionType.INTEGER, "seconds", "The amount of seconds to time out for."),
					new SubcommandData("check", "Check a user's time out")
						.addOption(OptionType.USER, "user", "The user's time out to check", true),
					new SubcommandData("clear", "Clear the user's time out.")
						.addOption(OptionType.USER, "user", "The user who's having their time out cleared.", true))
				);
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		e.deferReply(true).queue();	
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		
		if (e.getSubcommandName() == null || e.getOption("user") == null) {
			eb.appendDescription("There was an error performing this command.");
		} else if (e.getSubcommandName().equalsIgnoreCase("give")) {
			//String reason = e.getOption("reason") != null ? e.getOption("reason").getAsString() : "No reason.";
			long secs = e.getOption("seconds") != null ? e.getOption("seconds").getAsLong() : 0;
			long mins = e.getOption("minutes") != null ? e.getOption("minutes").getAsLong() : 0;
			long hrs = e.getOption("hours") != null ? e.getOption("hours").getAsLong() : 0;
			long days = e.getOption("days") != null ? e.getOption("days").getAsLong() : 0;
			Member toTimeout = e.getOption("user").getAsMember();
			
			try {
				toTimeout.timeoutFor(secs + mins*60 + hrs*60*60 + days*24*60*60, TimeUnit.SECONDS).queue();
				eb.setColor(0x44ddff);
				eb.appendDescription("Successfully timed out " + toTimeout.getAsMention());
			} catch (HierarchyException ex) {
				eb.appendDescription(toTimeout.getAsMention() + " is too powerful to be timed out!");
			} catch (NullPointerException ex) {
				eb.appendDescription("The user you attempted to timeout isn't in this Discord!");
			}
		} else if (e.getSubcommandName().equalsIgnoreCase("clear")) {
			e.getMember().removeTimeout().queue();
		} else if (e.getSubcommandName().equalsIgnoreCase("check")) {
			if (e.getMember().isTimedOut()) {
				eb.appendDescription(e.getMember().getAsMention() + " is currently timed out until " + e.getMember().getTimeOutEnd());
			} else {
				eb.appendDescription(e.getMember().getAsMention() + " is not currently timed out.");
				eb.setColor(0x44ddff);
			}
		} else {
			eb.appendDescription("Invalid subcommand.");
		}
		e.getHook().sendMessageEmbeds(eb.build()).queue();
	}
	
}
