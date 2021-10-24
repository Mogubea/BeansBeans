package me.playground.discord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class DiscordCommandReport extends DiscordCommand {

	public DiscordCommandReport(Main plugin) {
		super(plugin, null, new CommandData("report", "Submit a bug or player report.")
				.addSubcommands(
						new SubcommandData("bug", "Submit a bug report.")
						.addOptions(
								new OptionData(OptionType.STRING, "origin", "The platform that this bug originates from.", true)
								.addChoice("In-Game", "Minecraft Server")
								.addChoice("Discord", "Discord Server")
								.addChoice("Website", "Website"))
						.addOption(OptionType.STRING, "details", "Give us as much information as you can about the bug.", true)
						.addOption(OptionType.STRING, "replicate", "If possible, tell us how one would recreate the bug."),
						new SubcommandData("player", "Submit a report about an in-game Player.")
						.addOption(OptionType.STRING, "ign", "The name of the in-game Player.", true)
						.addOption(OptionType.STRING, "details", "Please specify what this player has done to make you file a report.", true)));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		e.deferReply(true).queue();
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		if (e.getSubcommandName() == null) {
			eb.appendDescription("Please enter a subcommand.");
		} else if (e.getSubcommandName().equalsIgnoreCase("bug")) {
			if (e.getOption("origin") != null) {
				if (e.getOption("details") != null) {
					final EmbedBuilder report = new EmbedBuilder();
					report.setColor(0xff367a);
					report.setTitle("**Bug Report**");
					report.addField("Reporter", e.getMember().getAsMention(), true);
					report.addField("Platform", e.getOption("origin").getAsString(), true);
					report.addField("Status", "New", true);
					report.addField("Last Updated", "<t:"+System.currentTimeMillis()/1000+":R>", false);
					
					report.addField("Bug Details", e.getOption("details").getAsString(), false);
					
					if (e.getOption("replicate") != null)
						report.addField("How to Replicate", e.getOption("replicate").getAsString(), false);
					
					final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
					final Date date = new Date();
					
					if (isLinked(e.getMember().getIdLong())) {
						final ProfileStore deeb = getLinkedStore(e.getMember());
						report.setFooter("Posted at " + df.format(date), DiscordBot.getIconURL(deeb.getId()));
					} else {
						report.setFooter("Posted at " + df.format(date), e.getMember().getUser().getAvatarUrl());
					}
					
					getBot().getTextChannelById(getDiscord().getBugReportChatId()).sendMessageEmbeds(report.build()).queue((message) -> {
						message.addReaction("U+1F3C1").queue(); // Fixed on Live
						message.addReaction("U+1F3F3").queue(); // Fixed on Dev
						message.addReaction("U+2714").queue(); // Investigating
						message.addReaction("U+274C").queue(); // Not a Bug
					});
					
					eb.appendDescription("Successfully submitted the bug report.");
					eb.setColor(0x44ddff);
				} else {
					eb.appendDescription("Please give us some details about the bug, otherwise we won't be able to fix it!");
				}
			} else {
				eb.appendDescription("Please specify the platform that you found this bug on.");
			}
		} else if (e.getSubcommandName().equalsIgnoreCase("player")) {
			if (e.getOption("ign") != null) {
				final ProfileStore target = ProfileStore.from(e.getOption("ign").getAsString(), true);
				
				if (target != null) {
					if (e.getOption("details") != null) {
						final EmbedBuilder report = new EmbedBuilder();
						report.setColor(0xff4222);
						report.setTitle("**Player Report for \"" + target.getRealName() + "\"**");
						report.addField("Reporter", e.getMember().getAsMention(), true);
						report.addField("Platform", "Minecraft Server", true);
						report.addField("Status", "New", true);
						report.addField("Last Updated", "<t:"+System.currentTimeMillis()/1000+":R>", false);
						
						report.addField("Report Details", e.getOption("details").getAsString(), false);
						
						final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
						final Date date = new Date();
						
						if (isLinked(e.getMember().getIdLong())) {
							final ProfileStore deeb = getLinkedStore(e.getMember());
							report.setFooter("Posted at " + df.format(date), DiscordBot.getIconURL(deeb.getId()));
						} else {
							report.setFooter("Posted at " + df.format(date), e.getMember().getUser().getAvatarUrl());
						}
						
						report.setThumbnail(DiscordBot.getIconURL(target.getId()));
						
						getBot().getTextChannelById(getDiscord().getPlayerReportChatId()).sendMessageEmbeds(report.build()).queue((message) -> {
							message.addReaction("U+1F3C1").queue(); // Solved
							message.addReaction("U+2714").queue(); // Investigating
						});
						
						eb.appendDescription("Successfully submitted a player report against **"+target.getRealName()+"**.");
						eb.setColor(0x44ddff);
					} else {
						eb.appendDescription("Please give us some information on why you're reporting this player.");
					}
				} else {
					eb.appendDescription("Sorry, we couldn't find a player by the name of **"+e.getOption("ign").getAsString()+"**, please make sure you've spelt their name correctly!");
				}
			} else {
				eb.appendDescription("Please specify the name of the player you wish to report.");
			}
		} else {
			eb.appendDescription("Invalid subcommand.");
		}
		e.getHook().sendMessageEmbeds(eb.build()).queue();
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		Message message = e.retrieveMessage().complete();
		if (e.getReactionEmote().isEmoji()) {
			List<MessageEmbed> embeds = message.getEmbeds();
			if (embeds.size() > 0) {
				boolean isBug = e.getChannel().getIdLong() == getDiscord().getBugReportChatId();
				
				EmbedBuilder eb = new EmbedBuilder(embeds.get(0));
				switch(e.getReactionEmote().getAsCodepoints()) {
				case "U+1f3c1": // Fixed on Live
					if (isBug) {
						eb.setColor(0x54c5ff);
						eb.getFields().set(2, new Field("Status", "Fixed on Live", true));
					} else {
						eb.setColor(0x54c5ff);
						eb.getFields().set(2, new Field("Status", "Solved", true));
					}
					break;
				case "U+1f3f3": // Fixed on Dev
					if (isBug) {
						eb.setColor(0x44ff89);
						eb.getFields().set(2, new Field("Status", "Fixed on Dev", true));
					} else {
						eb.setColor(0xff9922);
						eb.getFields().set(2, new Field("Status", "Investigating", true));
					}
					break;
				case "U+2714": // Acknowledged
					eb.setColor(0xff9922);
					eb.getFields().set(2, new Field("Status", "Investigating", true));
					break;
				case "U+274c": // Not a Bug
					eb.setColor(0x585858);
					eb.getFields().set(2, new Field("Status", "Not a Bug", true));
					break;
				default:
					return;
				}
				eb.getFields().set(3, new Field("Last Updated", "<t:"+System.currentTimeMillis()/1000+":R> by " + e.getMember().getAsMention(), false));
				message.editMessageEmbeds(eb.build()).queue();
			}
		}
	}
	
}
