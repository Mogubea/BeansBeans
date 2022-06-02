package me.playground.discord;

import me.playground.main.Main;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordCommandStatusPost extends DiscordCommand {

	public DiscordCommandStatusPost(Main plugin) {
		super(plugin, Rank.OWNER, new CommandData("statuspost", "A command for Mogubean, it sets the location of the Server Status embed."));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		getBot().getTextChannelById(getDiscord().getStatusChatId()).deleteMessageById(getDiscord().getStatusMessageId()).queue();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription("Removed old Status Post, moving it to here..");
			
		e.getChannel().sendMessageEmbeds(eb.build()).queue((message) -> {
			plugin.getConfig().set("discord.statusChannel", getDiscord().setStatusChatId(e.getChannel().getIdLong()));
			plugin.getConfig().set("discord.statusMessage", getDiscord().setStatusMessageId(message.getIdLong()));
			plugin.saveConfig();
		});
		getDiscord().updateServerStatus(true);
		e.reply("Done.");
	}
	
}
