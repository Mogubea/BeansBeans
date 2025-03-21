package me.playground.discord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

/**
 * TODO: improve this entire command class. It's crap.
 */
public class DiscordCommandEmbed extends DiscordCommand {

	public DiscordCommandEmbed(Main plugin) {
		super(plugin, Rank.ADMINISTRATOR, new CommandData("embed", "General embed command.")
				.addSubcommands(
						new SubcommandData("create", "Create a new embed")
						.addOption(OptionType.STRING, "content", "The content of this embed.", true)
						.addOption(OptionType.INTEGER, "colour", "The colour of the embed.")
						.addOption(OptionType.STRING, "title", "The title of the embed.")
						.addOption(OptionType.STRING, "titleurl", "Header url.")
						.addOption(OptionType.STRING, "image", "Direct url for image.")
						.addOption(OptionType.STRING, "thumbnail", "Direct url for thumbnail image.")
						.addOption(OptionType.STRING, "author", "Shows above the title")
						.addOption(OptionType.STRING, "authorurl", "URL of the author tag, requires author")
						.addOption(OptionType.STRING, "authorimg", "Top left mini image, requires author")
						.addOption(OptionType.STRING, "footer", "Footer content.")
						.addOption(OptionType.STRING, "footerurl", "Direct url for the footer image.")
						.addOption(OptionType.BOOLEAN, "footerdate", "Use custom date footer format.")
						,
						new SubcommandData("rank", "Create a rank embed")
						.addOptions(Rank.retrieveDiscordOptionData()),
						new SubcommandData("ranks", "Create a list of rank embeds")
								.addOptions(new OptionData(OptionType.STRING, "category", "Rank Category", true)
										.addChoice("Staff", "Staff").addChoice("Supporter", "Supporter").addChoice("Playtime", "Playtime"))));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);

		if (e.getSubcommandName().equalsIgnoreCase("ranks")) {
			List<MessageEmbed> embeds = new ArrayList<>(10);
			String category = e.getOption("category").getAsString();

			for (Rank rank : Rank.values()) {
				if (category.equalsIgnoreCase("Staff") && !rank.isStaffRank()) continue;
				if (category.equalsIgnoreCase("Supporter") && !rank.isDonorRank()) continue;
				if (category.equalsIgnoreCase("Playtime") && !rank.isPlaytimeRank()) continue;

				embeds.add(rankEmbed(rank));
			}

			Collections.reverse(embeds); // xd

			e.getChannel().sendMessageEmbeds(embeds).queue();
			eb.appendDescription("Rank Embeds posted.");
			eb.setColor(0x44ddff);
		} else if (e.getSubcommandName().equalsIgnoreCase("rank")) {
			try {
				Rank rank = Rank.valueOf(e.getOption("name").getAsString().toUpperCase());
				e.getChannel().sendMessageEmbeds(rankEmbed(rank)).queue();
				eb.appendDescription("Rank Embed posted.");
				eb.setColor(0x44ddff);
			} catch (Exception ex) { // Probably won't fire unless someone is using a modified version of Discord to ignore command warnings.
				eb = embedBuilder(0xff4455, "Sorry, we couldn't find a rank by the name of **"+e.getOption("name").getAsString()+"**, please make sure you've spelt the rank name correctly!");
			}
		} else {
			if (e.getOption("content") != null) {
				final EmbedBuilder suggestion = embedBuilder(0x54c5ff, e.getOption("content").getAsString().replaceAll("/nl", "\n"));
				if (e.getOption("colour") != null)
					suggestion.setColor((int) e.getOption("colour").getAsLong());
				if (e.getOption("title") != null)
					suggestion.setTitle(e.getOption("title").getAsString(), (e.getOption("titleurl") != null ? e.getOption("titleurl").getAsString() : null));
				if (e.getOption("image") != null)
					suggestion.setImage(e.getOption("image").getAsString());
				if (e.getOption("thumbnail") != null)
					suggestion.setThumbnail(e.getOption("thumbnail").getAsString());
				
				if (e.getOption("author") != null) {
					String url = e.getOption("authorurl") != null ? e.getOption("authorurl").getAsString() : null;
					String img = e.getOption("authorimg") != null ? e.getOption("authorimg").getAsString() : null;
					suggestion.setAuthor(e.getOption("author").getAsString(), url, img);
				}
				
				if (e.getOption("footerdate") != null && e.getOption("footerdate").getAsBoolean()) {
					suggestion.setTimestamp(Instant.now());
					
					if (isLinked(e.getMember().getIdLong())) {
						final ProfileStore deeb = getLinkedStore(e.getMember());
						suggestion.setFooter("Posted by " + e.getMember().getEffectiveName(), DiscordBot.getIconURL(deeb.getId()));
					} else {
						suggestion.setFooter("Posted by " + e.getMember().getEffectiveName(), e.getMember().getUser().getAvatarUrl());
					}
				} else {
					if (e.getOption("footer") != null)
						suggestion.setFooter(e.getOption("footer").getAsString(), (e.getOption("footerurl") != null ? e.getOption("footerurl").getAsString() : null));
				}
				
				e.getChannel().sendMessageEmbeds(suggestion.build()).queue();
				
				eb.appendDescription("Embed posted.");
				eb.setColor(0x44ddff);
			} else {
				eb.appendDescription("At the very least, specify the content of this embed.");
			}
		}

		e.replyEmbeds(eb.build()).setEphemeral(true).queue();
	}
	
}
