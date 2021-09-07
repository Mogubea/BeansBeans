package me.playground.discord;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.playground.gui.BeanGuiConfirmDiscord;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DiscordCommandLink extends DiscordCommand {

	public DiscordCommandLink(Main plugin) {
		super(plugin, null, new CommandData("link", "Link your Minecraft and Discord accounts (Make sure you're logged into the server)!")
				.addOption(OptionType.STRING, "ign", "The name of your Minecraft Account.", true));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		
		if (isLinked(e.getMember())) {
			ProfileStore deeb = getLinkedStore(e.getMember());
			eb.setColor(0x44ddff);
			eb.appendDescription("You are currently linked to **" + deeb.getDisplayName() + "**");
			eb.setThumbnail("https://minotar.net/helm/"+deeb.getRealName()+"/100.jpeg");
		} else {
			final String playerName = e.getOption("ign").getAsString();
			final Player p = Utils.playerPartialMatch(playerName);
			final TextChannel channel = e.getTextChannel();
			final Member member = e.getMember();
			if (p == null) {
				eb.appendDescription("Sorry, I couldn't find any player with the name '"+playerName+"', please make sure that you are logged into the Bean's Beans server!");
			} else {
				PlayerProfile pp = PlayerProfile.from(p);
				if (!pp.isSettingEnabled(PlayerSetting.DISCORD)) {
					eb.appendDescription("**" + p.getName() + "** isn't accepting Discord Link requests, if this is your account, which it should be, please enable the **in-game** setting within the **Player Menu**.");
				} else if (isLinked(pp.getId())) {
					eb.appendDescription("**" + p.getName() + "** is already linked to a Discord Account.");
				} else {
					eb.setColor(0x44ddff);
					eb.appendDescription("A confirmation has been sent to **"+p.getName()+"**.");
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override
						public void run() {
							new BeanGuiConfirmDiscord(plugin.discord(), p, channel, member).openInventory();
						}
						
					}, 5L);
					
				}
			}
		}
		
		e.replyEmbeds(eb.build()).setEphemeral(true).queue();
	}
	
}
