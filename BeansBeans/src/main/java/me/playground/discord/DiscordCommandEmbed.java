package me.playground.discord;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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
						.addOption(OptionType.STRING, "footer", "Footer content.")
						.addOption(OptionType.STRING, "footerurl", "Direct url for the footer image.")
						.addOption(OptionType.BOOLEAN, "footerdate", "Use custom date footer format.")
						));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		e.deferReply(true).queue();
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		
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
			
			if (e.getOption("footerdate") != null && e.getOption("footerdate").getAsBoolean()) {
				final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
				final Date date = new Date();
				
				if (isLinked(e.getMember().getIdLong())) {
					final ProfileStore deeb = getLinkedStore(e.getMember());
					suggestion.setFooter("Posted at " + df.format(date), "https://minotar.net/helm/"+deeb.getRealName()+"/100.png");
				} else {
					suggestion.setFooter("Posted at " + df.format(date), e.getMember().getUser().getAvatarUrl());
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
		
		e.getHook().sendMessageEmbeds(eb.build()).queue();
	}
	
}
