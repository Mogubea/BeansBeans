package me.playground.discord;

import me.playground.celestia.logging.CelestiaAction;
import me.playground.data.Datasource;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.kyori.adventure.text.Component;

public class DiscordCommandLink extends DiscordCommand {

	public DiscordCommandLink(Main plugin) {
		super(plugin, null, new CommandData("link", "Link your Minecraft and Discord accounts!")
				.addOption(OptionType.INTEGER, "code", "The code given to you in-game.")
				.addOption(OptionType.STRING, "break", "Break a Discord Link, Admin Command."));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		if (e.getMember() == null) return; // Shouldn't happen.

		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);

		if (e.getOption("break") != null && getDiscord().isRank(e.getMember(), Rank.ADMINISTRATOR)) {
			String targetName = e.getOption("break").getAsString();
			ProfileStore target = ProfileStore.from(targetName, true);
			if (target == null) {
				eb.appendDescription("Sorry, I couldn't find any player with the name **"+targetName+"**.");
			} else if (getDiscord().isLinked(target.getId())) {
				eb.setColor(0x44ddff);
				User targetUser = getBot().retrieveUserById(getDiscord().linkedAccounts.get(target.getId())).complete();
				eb.appendDescription("**" + targetName +"** is no longer linked to " + targetUser.getAsMention());
				eb.setThumbnail(DiscordBot.getIconURL(target.getId()));
				getDiscord().breakLink(target.getId());
			} else {
				eb.appendDescription("**" + targetName + "** isn't linked to an account.");
			}
		} else if (isLinked(e.getMember())) {
			ProfileStore deeb = getLinkedStore(e.getMember());
			eb.setColor(0x44ddff);
			eb.appendDescription("You are currently linked to **" + deeb.getDisplayName() + "**");
			eb.setThumbnail(DiscordBot.getIconURL(deeb.getId()));
		} else if (e.getOption("code") != null) {
			final long code = e.getOption("code").getAsLong();
			final Member member = e.getMember();
			
			int linkedId = getDiscord().createLink(member.getIdLong(), code);
			
			if (linkedId <= 0) {
				eb.appendDescription("Sorry, but **" + code + "** is an invalid Link Code. Please make sure you've entered it correctly!\n\n"
						+ "If you still need a code, use `/link` for instructions on how to get one.");
			} else {
				PlayerProfile pp = PlayerProfile.fromIfExists(linkedId);
				
				if (pp.isOnline())
					pp.getPlayer().sendMessage(Component.text("\u00a7aYou are now successfully linked to \u00a79" + member.getUser().getAsTag()));
				
				if (!member.hasPermission(Permission.ADMINISTRATOR)) {
					getDiscord().updateNickname(pp);
					getDiscord().updateRoles(pp);
				} else if (pp.isOnline()) {
					pp.getPlayer().sendMessage(Component.text("\u00a77However, due to having Administrator perms, your nickname and roles cannot be updated by the bot."));
				}
				
				Datasource.logCelestia(CelestiaAction.LINK_DISCORD, pp.getId(), null, "Linked to Discord ID: "+member.getIdLong());
				//pp.addToBalance(2500, "Linking Discord");
				pp.grantAdvancement("beansbeans:advancements/discord");
				eb.setColor(0x44ddff);
				eb.appendDescription("You are now linked to **" + pp.getDisplayName() + "**");
				eb.setThumbnail(DiscordBot.getIconURL(pp.getId()));
			}
		} else {
			eb.setColor(0x44ddff);
			eb.setTitle("How to link your Minecraft Account");
			eb.appendDescription("In order to link your accounts, you need to provide this `/link` command with your **Discord Link Code**.");
			eb.addField("How to obtain a Discord Link Code",
					"• Log into the Bean's Beans Minecraft Server\n"
					+ "• Access your Player Menu by using **/menu**\n"
					+ "• Access your Profile Settings by clicking your skull\n"
					+ "• Obtain a **Discord Link Code** by clicking the Discord Icon", false);
			eb.setImage("https://i.imgur.com/ntAtPVk.gif");
		}
		
		e.replyEmbeds(eb.build()).setEphemeral(true).queue();
	}
	
}
