package me.playground.discord;

import java.text.DecimalFormat;

import javax.annotation.Nonnull;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import me.playground.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class DiscordCommand {
	
	protected final CommandData commandData;
	protected final Main plugin;
	protected final Rank requiredRank;
	protected boolean enabled;
	
	public DiscordCommand(Main plugin, CommandData data) {
		this.requiredRank = null;
		this.plugin = plugin;
		
		this.commandData = data;
	}
	
	public DiscordCommand(Main plugin, Rank rank, CommandData data) {
		this.requiredRank = rank;
		this.plugin = plugin;
		
		this.commandData = data;
	}
	
	/**
	 * Fires if {@link #preSlashCommand(SlashCommandEvent)} doesn't have any issues with the person using the command. Handles the command.
	 * @param event SlashCommandEvent
	 */
	public abstract void onCommand(@Nonnull SlashCommandEvent event);
	
	/**
	 * This will handle permission based checks before the {@link #onCommand(SlashCommandEvent)} fires.
	 * 
	 * Users with the Administrator permission will bypass these checks.
	 * @param event SlashCommandEvent
	 */
	public void preSlashCommand(@Nonnull SlashCommandEvent event) throws ContextException {
		if (requiredRank != null) {
			if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !isRank(event.getMember(), requiredRank)) {
				event.replyEmbeds(embedBuilder(0xff4444, "You don't have permission to use this command.").build()).setEphemeral(true).queue();
				return;
			}
		}
		
		onCommand(event);
	}
	
	public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent e) {}
	
	/**
	 * @return Returns true if this command is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * @return Returns {@link #plugin}
	 */
	public Main getPlugin() {
		return plugin;
	}
	
	public DiscordBot getDiscord() {
		return getPlugin().getDiscord();
	}
	
	public JDA getBot() {
		return getDiscord().bot();
	}
	
	public CommandData getCommandData() {
		return commandData;
	}
	
	public boolean isLinked(long id) {
		return getDiscord().isLinked(id);
	}
	
	public boolean isLinked(Member member) {
		return isLinked(member.getUser().getIdLong());
	}
	
	public ProfileStore getLinkedStore(Member member) {
		return ProfileStore.from(getDiscord().getKey(getDiscord().linkedAccounts, member.getUser().getIdLong()), true);
	}
	
	public long getLinkedDiscord(int playerId) {
		return getDiscord().linkedAccounts.get(playerId);
	}
	
	protected EmbedBuilder embedBuilder(int colour, String description) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(colour);
		eb.appendDescription(description);
		return eb;
	}
	
	protected MessageEmbed rankEmbed(Rank rank) {
		final EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(rank.getRankHex());
		embed.setTitle(Utils.firstCharUpper(rank.lowerName()));
			
		String rankType = "Playtime";
		
		embed.setThumbnail("https://emoji.gg/assets/emoji/9286-writtenbook.gif");
		
		if (rank.isStaffRank()) {
			embed.setThumbnail("https://images-ext-1.discordapp.net/external/w0Ic-2tPONSmBuIeKW7QZp8N7pgnLcWERLC8qpinLNs/https/emoji.gg/assets/emoji/1201-modcheck.png");
			rankType = "Staff";
		} else if (rank.isDonorRank()) {
			embed.setThumbnail("https://images-ext-1.discordapp.net/external/buhY3rQciXfJICqn89S4EMB74NzzDOOAoaf3Iqfy6dI/https/emoji.gg/assets/emoji/8253_RaphtaliaPat.png");
			rankType = "Supporter";
		}
		
		embed.addField("Discord Role", "<@&" + rank.getDiscordId() + ">", true);
		embed.addField("Warp Bonus", "+ " + rank.getWarpBonus(), true);
		embed.addField("Rank Type", rankType, true);
		
		embed.addField("Information", rank.getDiscordInformation(), false);
		
		if (rank.isPlaytimeRank()) {
			if (rank != Rank.NEWBEAN)
				embed.addField("How to Obtain", "Play for **"+Utils.timeStringFromMillis((long)rank.getPlaytimeRequirement() * 1000L)+"**"
						+ (rank.getPlaytimeRequirement() > 60 * 60 * 23 ? " *("+(df.format(rank.getPlaytimeRequirement()/60/60))+" Hours)*": ""), false);
		} else if (rank.isDonorRank()) {
			if (rank == Rank.PLEBEIAN)
				embed.addField("How to Obtain", "Sapphire or Subscription Purchase", false);
			else
				embed.addField("How to Obtain", "Subscription Purchase Only", false);
		}
		return embed.build();
	}
	
	protected boolean isRank(Member member, Rank rank) {
		return getDiscord().isRank(member, rank);
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	
}
