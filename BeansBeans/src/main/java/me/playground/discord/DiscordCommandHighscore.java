package me.playground.discord;

import me.playground.highscores.Highscore;
import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class DiscordCommandHighscore extends DiscordCommand {
	
	private final String[] posPfx = { ":trophy:", ":second_place:", ":third_place:" };
	
	public DiscordCommandHighscore(Main plugin) {
		super(plugin, null, new CommandData("highscore", "View highscore information!").addOptions(plugin.highscores.retrieveDiscordOptionData()));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		if (e.getOption("category") != null) {
			Highscore highscore = plugin.highscores.getHighscore(e.getOption("category").getAsString());
			
			if (highscore != null) {
				final EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(0x44ddff);
				embed.setTitle(MarkdownSanitizer.escape(e.getOption("category").getAsString()) + " Leaderboard");
				
				final int totalEntries = highscore.getSize();
				final int toDisplay = totalEntries < 10 ? totalEntries : 10;
				final ProfileStore cmdStore = getLinkedStore(e.getMember());
				
				String embedInfo = "";
				for (int x = -1; ++x < toDisplay;) {
					embedInfo += "**" + ((x < posPfx.length) ? posPfx[x] : " " + (x + 1)) + ".** ";
					ProfileStore posStore = ProfileStore.from(highscore.getOrder().get(x));
					embedInfo += posStore.getDisplayName() + " - **"+df.format(highscore.getScoreOf(posStore.getId()))+"** "+highscore.getSuffix()+"\n";
				}
				
				if (cmdStore != null)
					if (highscore.getPositionOf(cmdStore.getId()) > toDisplay)
						embedInfo += "...\n**"+(highscore.getOrder().get(cmdStore.getId())+1)+". "+cmdStore.getDisplayName()+"** - "+highscore.getScoreOf(cmdStore.getId())+" "+highscore.getSuffix();
				
				embed.setDescription(embedInfo);
				embed.setThumbnail("https://img.icons8.com/nolan/344/trophy.png");
				
				e.replyEmbeds(embed.build()).queue();
			} else {
				final EmbedBuilder eb = embedBuilder(0xff4455, "Sorry, we couldn't find a category by the name of **"+e.getOption("ign").getAsString()+"**, please make sure you've spelt the name correctly!");
				e.replyEmbeds(eb.build()).setEphemeral(true).queue();
			}
		}
	}
	
}
