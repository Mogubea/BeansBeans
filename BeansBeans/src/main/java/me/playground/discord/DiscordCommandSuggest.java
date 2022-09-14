package me.playground.discord;

import me.playground.main.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class DiscordCommandSuggest extends DiscordCommand {
	public DiscordCommandSuggest(Main plugin, CommandData data) {
		super(plugin, data);
	}

	@Override
	public void onCommand(@NotNull SlashCommandEvent event) {

	}
/*
	public DiscordCommandSuggest(Main plugin) {
		super(plugin, null, new CommandData("suggest", "Suggest a feature to be added to Bean's Beans!")
				.addOptions(
						new OptionData(OptionType.STRING, "platform", "The platform your suggestion is for!", true)
						.addChoice("In-Game", "Minecraft Server")
						.addChoice("Discord", "Discord Server")
						.addChoice("Website", "Website"))
				.addOption(OptionType.STRING, "title", "The title of your suggestion!", true)
				.addOption(OptionType.STRING, "suggestion", "Tell us what you think should be added!", true));
//				.addOption(OptionType.BOOLEAN, "anonymous", "Would you like this suggestion to be anonymous?"));
	}

	@Override
	public void onCommand(SlashCommandEvent e) {
		e.deferReply(true).queue();
		final EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(0xff4455);
		
		if (e.getOption("platform") != null && e.getOption("suggestion") != null) {
			final EmbedBuilder suggestion = new EmbedBuilder();
			suggestion.setColor(0x36cf5a);
			suggestion.setTitle("Suggestion - " + e.getOption("title").getAsString());
			suggestion.addField("Suggested By", e.getMember().getAsMention(), true);
			suggestion.addField("Platform", e.getOption("platform").getAsString(), true);
			suggestion.addField("Status", "Open", true);
			suggestion.addField("Posted", "<t:"+System.currentTimeMillis()/1000+":R>", false);
			suggestion.addField("Suggestion", e.getOption("suggestion").getAsString(), false);
			
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
			final Date date = new Date();
			
//			if (e.getOption("anonymous") == null || !e.getOption("anonymous").getAsBoolean()) {
				if (isLinked(e.getMember().getIdLong())) {
					final ProfileStore deeb = getLinkedStore(e.getMember());
					suggestion.setFooter("This suggestion was submitted at " + df.format(date), DiscordBot.getIconURL(deeb.getId()));
				} else {
					suggestion.setFooter("This suggestion was submitted at " + df.format(date), e.getMember().getUser().getAvatarUrl());
				}
//			} else {
//				report.setFooter("This suggestion was submitted at " + df.format(date));
//			}
			
			getBot().getTextChannelById(getDiscord().getSuggestionChatId()).sendMessageEmbeds(suggestion.build()).queue((message) -> {
				message.addReaction("nodders:879450101465100408").queue();
				message.addReaction("help:873540607925178378").queue();
				message.addReaction("nopers:879447544462200873").queue();
			});
			
			eb.appendDescription("You have successfully submitted your suggestion! You, and everyone else, can view it in the <#"+getDiscord().getSuggestionChatId()+"> channel!");
			eb.setColor(0x44ddff);
		} else {
			eb.appendDescription("Please make sure you specify the platform, title and suggestion arguments!");
		}
		
		e.getHook().sendMessageEmbeds(eb.build()).queue();
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if (!isRank(e.getMember(), Rank.ADMINISTRATOR))
			return;
		
		if (e.getReactionEmote().isEmoji()) {
			Message msg = e.retrieveMessage().complete();
			List<MessageEmbed> embeds = msg.getEmbeds();
			if (embeds.size() > 0) {
				EmbedBuilder eb = new EmbedBuilder(embeds.get(0));
				
				switch(e.getReactionEmote().getAsCodepoints()) {
				case "U+1f3c1": // Live
					eb.setColor(0x54c5ff);
					eb.getFields().set(2, new Field("Status", "Implemented ("+getPlugin().getDescription().getVersion()+")", true));
					break;
				case "U+1f3f3": case "U+1f3f3U+fe0f": // Dev
					eb.setColor(0x36efca);
					eb.getFields().set(2, new Field("Status", "In Development", true));
					break;
				case "U+2714": case "U+2705": // Yah
					eb.setColor(0x30ef8a);
					eb.getFields().set(2, new Field("Status", "Approved", true));
					break;
				case "U+274c": case "U+274e": // Nah
					eb.setColor(0x585858);
					eb.getFields().set(2, new Field("Status", "Unlikely", true));
					break;
				default:
					return;
				}
				eb.getFields().set(3, new Field("Last Reviewed", "<t:"+System.currentTimeMillis()/1000+":R> by " + e.getMember().getAsMention(), false));
				msg.editMessageEmbeds(eb.build()).queue((message) -> {
					message.clearReactions(e.getReactionEmote().getAsCodepoints()).queue();
				});
			}
		}
	}
	*/
}
