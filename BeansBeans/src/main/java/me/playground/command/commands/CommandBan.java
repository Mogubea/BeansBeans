package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.discord.DiscordBot;
import me.playground.punishments.PunishmentMinecraft;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandBan extends BeanCommand {
	
	public CommandBan(Main plugin) {
		super(plugin, "bean.cmd.ban", true, 1, "ban", "ban-ip");
		description = "Ban a player from Bean's Beans.";
	}

	private final String FLAG_NO_APPEAL = "-noappeal"; // Don't show appeal message and disallow the creation of appeals.
	private final String FLAG_BAN_IP = "-ip"; // Ban their IP as well.
	private final String FLAG_OVERRIDE = "-override"; // Override their current relevant ban.

	private final List<String> possibleFlags = List.of(FLAG_NO_APPEAL, FLAG_BAN_IP, FLAG_OVERRIDE);
	private final List<String> exampleTimes = List.of("~", "1y", "90d", "4w", "1w", "3d", "1d", "12h", "1h", "6h30m", "3d12h10s");

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = toProfile(sender, args[0]);
		if (profile != null && target.getId() == profile.getId())
			throw new CommandException(sender, "It would be a bit silly to ban yourself, no?");

		if (isPlayer(sender) && getRank(sender).power() <= target.getHighestRank().power())
			throw new CommandException(sender, "You can't ban players of equal or higher ranking!");

		boolean appeal = true;
		boolean banIp = cmd.getName().equalsIgnoreCase("ban-ip");
		boolean override = false;
		long duration = -1;
		String reason = null;

		// DURATION
		if (args.length > 1) {
			String banTime = args[1].toLowerCase();
			if (!banTime.contains("~")) {
				int charLength = banTime.length();
				StringBuilder builder = new StringBuilder();
				int currentNumber = 0;

				for (int i = -1; ++i < charLength; ) {
					char c = banTime.charAt(i);
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
						case FLAG_BAN_IP -> banIp = true;
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

		// CHECK IF BANNED ALREADY
		if (target.isBanned()) {
			if (!override) {
				sender.sendMessage(target.getComponentName().append(Component.text(" is already banned. If you wish to override their current ban, use the -override flag.", NamedTextColor.RED))
						.hoverEvent(HoverEvent.showText(target.getBan().toComponent())));
				return true;
			}
			target.getBan().setEnabled(false);
		}

		String dPunisher = profile != null ? profile.getDiscordMember() != null ? profile.getDiscordMember().getAsMention() : profile.getDisplayName() : "Server";
		String dTarget = target.getDiscordMember() != null ? target.getDiscordMember().getAsMention() : target.getDisplayName();

		PunishmentMinecraft punishment = getPlugin().getPunishmentManager().banPlayer(target.getUniqueId(), profile != null ? profile.getId() : 0, duration, reason, true, appeal, banIp);

		Utils.notifyAllStaff(Component.empty().append(toName(sender).append(Component.text("\u00a7r has " + (banIp ? "IP " : "") + "banned ")).append(target.getComponentName()).append(Component.text("\u00a7r.\n"))
				.append(Component.text("\u00a74 • \u00a7rDuration: \u00a7d" + punishment.getTotalString() + "\n"))
						.append(Component.text("\u00a74 • \u00a7rReason: \u00a7f" + (punishment.getReason() == null ? "No reason given" : punishment.getReason())))).color(BeanColor.BAN),
				"Player " + (banIp ? "IP" : "") + " Banned",
				dPunisher + " has banned " + dTarget + "\n" +
						"**Ban Time:** <t:" + punishment.getPunishmentStart().getEpochSecond() + ":R>\n" +
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
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be banned."))).color(NamedTextColor.GRAY),
			Component.text(" [time]").hoverEvent(HoverEvent.showText(Component.text("""
					Ban Duration
					\u00a77The amount of time the player will be banned for.
					\u00a77Example Format: \u00a7f1y2w3d12h30m30s
					\u00a78 • \u00a7f1y\u00a77 bans for 1 year.
					\u00a78 • \u00a7f2w\u00a77 bans for 2 weeks.
					\u00a78 • \u00a7f3d\u00a77 bans for 3 days.
					\u00a78 • \u00a7f12h\u00a77 bans for 12 hours.
					\u00a78 • \u00a7f30m\u00a77 bans for 30 minutes.
					\u00a78 • \u00a7f30s\u00a77 bans for 30 seconds.
					
					\u00a77Using Tilda (~) instead will ban permanently."""))).color(NamedTextColor.GRAY),
			Component.text(" [category]").hoverEvent(HoverEvent.showText(Component.text("The category of this ban."))).color(NamedTextColor.GRAY),
			Component.text(" [reason]").hoverEvent(HoverEvent.showText(Component.text("The ban reason shown the player."))).color(NamedTextColor.GRAY),
			Component.text(" [flags]").hoverEvent(HoverEvent.showText(Component.text("""
					Ban Flags
					\u00a77Appending any of these flags will change ban attributes.
					\u00a78 • \u00a7f-noappeal\u00a77 prevents appealing this ban.
					\u00a78 • \u00a7f-ip\u00a77 enforces an IP ban on top."""))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]).append(usageArguments[2]).append(usageArguments[3]).append(usageArguments[4]);
	}

}
