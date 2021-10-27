package me.playground.discord;

import org.bukkit.Statistic;

import me.playground.civilizations.CitizenTier;
import me.playground.civilizations.jobs.Job;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.skills.SkillType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class DiscordCommandWho extends DiscordCommand {

	public DiscordCommandWho(Main plugin) {
		super(plugin, null, new CommandData("who", "View information about a player on the server!")
				.addOption(OptionType.STRING, "ign", "The content of this embed.", true));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		if (e.getOption("ign") != null) {
			PlayerProfile pp = PlayerProfile.fromIfExists(e.getOption("ign").getAsString());
			if (pp != null) {
				final EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(pp.getHighestRank().getRankColour());
				
				embed.setTitle(MarkdownSanitizer.escape(pp.getDisplayName()));
				embed.setThumbnail(DiscordBot.getIconURL(pp.getId()));
				embed.addField("Rank", "<@&" + pp.getHighestRank().getDiscordId() + ">", true);
				embed.addField("Status", pp.isOnline() ? "Online" : "Offline", true);
				embed.addField("Linked", !isLinked(pp.getId()) ? "No" : "<@" + getLinkedDiscord(pp.getId()) + ">", true);
				if (pp.isMod()) // Here since Staff Rank overrides Rank, why not show it
					embed.addField("Playtime Rank", "<@&" + pp.getPlaytimeRank().getDiscordId() + ">", true);
				if (pp.getDonorRank() != null) // Here since it's cool to show
					embed.addField("Supporter Rank", "<@&" + pp.getDonorRank().getDiscordId() + ">", true);
				if (pp.hasNickname())
					embed.addField("Real Name", MarkdownSanitizer.escape(pp.getRealName()), true);
				if (pp.isInCivilization()) {
					CitizenTier tier = pp.getCivilization().getCitizen(pp.getId());
					Job job = pp.getJob();
					embed.addField("Civilization", tier.getNiceName() + " of " + pp.getCivilization().getName() + " (" + (job != null ? job.getNiceName() : " Unemployed") +")", false);
				}
				
				int mins = pp.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60;
				int hours = Math.floorDiv(mins, 60);
				int days = Math.floorDiv(hours, 24);
				mins -= hours*60;
				hours -= days*24;
				
				String d = (days > 0 ? days + " Day" + (days == 1 ? "" : "s") + ", " : "");
				String h = hours + " Hour" + (hours == 1 ? "" : "s") + " and ";
				String m = mins + " Minute" + (mins == 1 ? "" : "s");
				
				embed.addField("Playtime", d + h + m, false);
				embed.addField("Skills", 
						"<:mining:879514404868222976>" + pp.getSkillLevel(SkillType.MINING) +
						"**,** <:logcutting:879514418306744441>" + pp.getSkillLevel(SkillType.LOGCUTTING) +
						"**,** <:agriculture:879514439890657340>" + pp.getSkillLevel(SkillType.AGRICULTURE) +
						"**,** <:fishing:879514461495525446>" + pp.getSkillLevel(SkillType.FISHING) +
						"**,** <:combat:879518113144651816>" + pp.getSkillLevel(SkillType.COMBAT), false);
				e.replyEmbeds(embed.build()).queue();
			} else {
				final EmbedBuilder eb = embedBuilder(0xff4455, "Sorry, we couldn't find a player by the name of **"+e.getOption("ign").getAsString()+"**, please make sure you've spelt their name correctly!");
				e.replyEmbeds(eb.build()).setEphemeral(true).queue();
			}
		}
	}
	
}
