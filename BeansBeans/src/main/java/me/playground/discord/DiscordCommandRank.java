package me.playground.discord;

import me.playground.main.Main;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordCommandRank extends DiscordCommand {

	public DiscordCommandRank(Main plugin) {
		super(plugin, null, new CommandData("rank", "View information about a Rank!").addOptions(Rank.retrieveDiscordOptionData()));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		if (e.getOption("name") != null) {
			Rank rank = null;
			try {
				rank = Rank.valueOf(e.getOption("name").getAsString().toUpperCase());
			} catch (Exception ex) { // Probably won't fire unless someone is using a modified version of Discord to ignore command warnings.
				final EmbedBuilder eb = embedBuilder(0xff4455, "Sorry, we couldn't find a rank by the name of **"+e.getOption("name").getAsString()+"**, please make sure you've spelt the rank name correctly!");
				e.replyEmbeds(eb.build()).setEphemeral(true).queue();
				return;
			}
			
			e.replyEmbeds(rankEmbed(rank)).setEphemeral(true).queue();
		}
	}
	
}
