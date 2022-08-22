package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.discord.DiscordBot;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.punishments.PunishmentMinecraft;
import me.playground.utils.BeanColor;
import me.playground.utils.TabCompleter;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandMute extends BeanCommand {

	public CommandMute(Main plugin) {
		super(plugin, "bean.cmd.mute", true, 1, "mute");
		description = "Mute a player from speaking on Bean's Beans.";
	}

	private final String FLAG_NO_APPEAL = "-noappeal"; // Don't show appeal message and disallow the creation of appeals.
	private final String FLAG_FULL_MUTE = "-full"; // Mute in private channels and DMs as well as global chat.
	private final String FLAG_OVERRIDE = "-override"; // Override their current relevant mute.

	private final List<String> possibleFlags = List.of(FLAG_NO_APPEAL, FLAG_FULL_MUTE, FLAG_OVERRIDE);
	private final List<String> exampleTimes = List.of("~", "1y", "90d", "4w", "1w", "3d", "1d", "12h", "1h", "6h30m", "3d12h10s");

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = toProfile(sender, args[0]);
		if (profile != null && target.getId() == profile.getId())
			throw new CommandException(sender, "It would be a bit silly to mute yourself, no?");

		if (getRank(sender).power() <= target.getHighestRank().power())
			throw new CommandException(sender, "You can't mute players of equal or higher ranking!");

		boolean appeal = true;
		boolean fullMute = false;
		boolean override = false;
		long duration = -1;
		String reason = null;

		// DURATION
		if (args.length > 1) {
			String muteTime = args[1].toLowerCase();
			if (!muteTime.contains("~")) {
				int charLength = muteTime.length();
				StringBuilder builder = new StringBuilder();
				int currentNumber = 0;

				for (int i = -1; ++i < charLength; ) {
					char c = muteTime.charAt(i);
					switch (c) {
						case 'y', 'Y' -> duration += currentNumber * (60 * 60 * 24 * 365);
						case 'w', 'W' -> duration += currentNumber * (60 * 60 * 24 * 7);
						case 'd', 'D' -> duration += currentNumber * (60 * 60 * 24);
						case 'h', 'H' -> duration += currentNumber * (60 * 60);
						case 'm', 'M' -> duration += currentNumber * 60L;
						case 's', 'S' -> duration += currentNumber;
						default -> {
							try {
								Integer.parseInt(c + "");
								builder.append(c);
							} catch (NumberFormatException e) {
								throw new CommandException(sender, "\"" + args[1] + "\" is an invalid time format.");
							}

							try {
								currentNumber = Integer.parseInt(builder.toString());
							} catch (NumberFormatException e) {
								throw new CommandException(sender, "\"" + builder + "\" is an invalid number.");
							}

							continue;
						}
					}

					builder = new StringBuilder();
				}

				duration *= 1000L;
			}
		}

		// REASON
		if (args.length > 2) {
			StringBuilder sb = new StringBuilder();
			for (int x = 1; ++x < args.length;) {
				String word = args[x];
				// FLAGS
				if (possibleFlags.contains(word)) {
					switch (word) {
						case FLAG_NO_APPEAL -> appeal = false;
						case FLAG_FULL_MUTE -> fullMute = true;
						case FLAG_OVERRIDE -> override = true;
					}
					continue;
				}
				sb.append(word);
				if (!(x + 1 >= args.length))
					sb.append(" ");
			}
			reason = sb.toString();
		}

		// CHECK IF MUTED ALREADY
		if (target.isMuted()) {
			if (!override) {
				sender.sendMessage(target.getComponentName().append(Component.text(" is already muted. If you wish to override their current mute, use the -override flag.", NamedTextColor.RED))
						.hoverEvent(HoverEvent.showText(target.getMute().toComponent())));
				return true;
			}
			target.getMute().setEnabled(false);
		}

		String dPunisher = profile != null ? profile.getDiscordMember() != null ? profile.getDiscordMember().getAsMention() : profile.getDisplayName() : "Server";
		String dTarget = target.getDiscordMember() != null ? target.getDiscordMember().getAsMention() : target.getDisplayName();

		PunishmentMinecraft punishment = getPlugin().getPunishmentManager().mutePlayer(target.getUniqueId(), profile != null ? profile.getId() : 0, duration, reason, true, appeal, fullMute);

		Utils.notifyAllStaff(Component.empty().append(toName(sender).append(Component.text("\u00a7r has " + (fullMute ? "full " : "") + "muted ")).append(target.getComponentName()).append(Component.text("\u00a7r.\n"))
				.append(Component.text("\u00a74 • \u00a7rDuration: \u00a7f" + punishment.getTotalString() + "\n"))
						.append(Component.text("\u00a74 • \u00a7rReason: \u00a7f" + (punishment.getReason() == null ? "No reason given" : punishment.getReason())))).color(BeanColor.BAN),
				"Player " + (fullMute ? "Full" : "") + " Muted",
				dPunisher + " has muted " + dTarget + "\n" +
						"**Mute Time:** <t:" + punishment.getPunishmentStart().getEpochSecond() + ":R>\n" +
						"**Expiry Time:** " + (punishment.getPunishmentEnd() == null ? "Never\n" : "<t:" + punishment.getPunishmentEnd().getEpochSecond() + ":R>\n") +
						"**Reason:** " + punishment.getNonnullReason(), DiscordBot.getIconURL(target.getId()));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		if (args.length == 2)
			return TabCompleter.completeString(args[1], exampleTimes);

		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be muted."))).color(NamedTextColor.GRAY),
			Component.text(" [time]").hoverEvent(HoverEvent.showText(Component.text("""
					Mute Duration
					\u00a77The amount of time the player will be muted for.
					\u00a77Example Format: \u00a7f1y2w3d12h30m30s
					\u00a78 • \u00a7f1y\u00a77 mutes for 1 year.
					\u00a78 • \u00a7f2w\u00a77 mutes for 2 weeks.
					\u00a78 • \u00a7f3d\u00a77 mutes for 3 days.
					\u00a78 • \u00a7f12h\u00a77 mutes for 12 hours.
					\u00a78 • \u00a7f30m\u00a77 mutes for 30 minutes.
					\u00a78 • \u00a7f30s\u00a77 mutes for 30 seconds.
					
					\u00a77Using Tilda (~) instead will mute permanently."""))).color(NamedTextColor.GRAY),
			Component.text(" [category]").hoverEvent(HoverEvent.showText(Component.text("The category of this mute."))).color(NamedTextColor.GRAY),
			Component.text(" [reason]").hoverEvent(HoverEvent.showText(Component.text("The mute reason shown the player."))).color(NamedTextColor.GRAY),
			Component.text(" [flags]").hoverEvent(HoverEvent.showText(Component.text("""
					Mute Flags
					\u00a77Appending any of these flags will change mute attributes.
					\u00a78 • \u00a7f-noappeal\u00a77 prevents appealing this mute.
					\u00a78 • \u00a7f-full\u00a77 also enforces private channel muting.
					\u00a78 • \u00a7f-override\u00a77 overrides the player's current mute."""))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]).append(usageArguments[2]).append(usageArguments[3]).append(usageArguments[4]);
	}

}
