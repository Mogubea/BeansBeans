package me.playground.discord;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordCommandForceLink extends DiscordCommand {

	public DiscordCommandForceLink(Main plugin) {
		super(plugin, Rank.ADMINISTRATOR, new CommandData("forcelink", "Force a link between Discord and Minecraft accounts.")
				.addOption(OptionType.MENTIONABLE, "discord", "Discord Tag", true)
				.addOption(OptionType.STRING, "ign", "The In-Game Name", true));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		if (e.getMember() == null) return; // Shouldn't happen.

		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);

		// Force a link
		String targetName = e.getOption("ign").getAsString();
		ProfileStore target = ProfileStore.from(targetName, true);
		Member targetUser = e.getOption("discord").getAsMember();
		if (target == null) {
			eb.appendDescription("Sorry, I couldn't find any player with the name **"+targetName+"**.");
		} else if (targetUser == null) {
			eb.appendDescription("Sorry, I couldn't find the specified Discord Member.");
		} else {
			eb.setColor(0x44ddff);
			if (isLinked(targetUser.getIdLong())) {
				int oldId = getDiscord().getLinkedIdFromDiscordId(targetUser.getIdLong());
				try {
					User oldUser = getBot().retrieveUserById(getLinkedDiscord(oldId)).complete();
					if (oldUser != null)
						eb.appendDescription("**" + targetName +"** is no longer linked to " + oldUser.getAsMention());
				} catch (RuntimeException ex) {
					ProfileStore ps = ProfileStore.from(oldId);
					eb.appendDescription("**" + targetName +"** is no longer linked to **" + ps.getDisplayName() + "**.");
				}
			}
			eb.appendDescription("**" + targetName +"** is now linked to " + targetUser.getAsMention());
			eb.setThumbnail(DiscordBot.getIconURL(target.getId()));
			getDiscord().createLink(target.getId(), targetUser.getIdLong());
		}

		e.replyEmbeds(eb.build()).setEphemeral(true).queue();
	}
	
}
