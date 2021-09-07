package me.playground.discord;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.playground.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordCommandOnline extends DiscordCommand {

	public DiscordCommandOnline(Main plugin) {
		super(plugin, null, new CommandData("online", "Returns a list of the currently online players."));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		String playerList = "";
		int count = Bukkit.getOnlinePlayers().size();
		int x = 0;
		for (Player p : Bukkit.getOnlinePlayers())
			playerList += p.getName() + (++x < count ? ", " : "");
		
		final EmbedBuilder eb = embedBuilder(0x44ddff, "**There is currently "+count+"/"+Bukkit.getMaxPlayers()+" Players online**\n" + playerList);
		eb.setFooter("Responding to " + e.getMember().getUser().getAsTag() + "'s /online Command", e.getMember().getUser().getEffectiveAvatarUrl());
		e.replyEmbeds(eb.build()).queue();
	}
	
}
