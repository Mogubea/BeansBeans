package me.playground.discord;

import javax.annotation.Nonnull;

import me.playground.main.Main;
import me.playground.playerprofile.ProfileStore;
import me.playground.ranks.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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
	public void preSlashCommand(@Nonnull SlashCommandEvent event) {
		if (requiredRank != null) {
			if (!event.getMember().hasPermission(Permission.ADMINISTRATOR) && !isRank(event.getMember(), requiredRank)) {
				event.replyEmbeds(embedBuilder(0xff4444, "You don't have permission to use this command.").build()).setEphemeral(true).queue();
				return;
			}
		}
		
		try {
			onCommand(event);
		} catch (ErrorResponseException e) {
			getPlugin().getLogger().warning("Issue firing /"+event.getName() + " in Discord.");
		}
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
		return getPlugin().discord();
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
	
	protected boolean isRank(Member member, Rank rank) {
		return getDiscord().isRank(member, rank);
	}
	
}
