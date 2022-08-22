package me.playground.command.commands;

import me.playground.command.BeanCommand;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandKick extends BeanCommand {

	public CommandKick(Main plugin) {
		super(plugin, "bean.cmd.kick", true, 1, "kick");
		description = "Kick a player from Bean's Beans.";
	}

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player target = toPlayer(sender, args[0]);

		// self kick, no logging.
		if (isPlayer(sender) && sender == target) {
			target.kick();
			return true;
		}

		checkRankPower(sender, target, "You can't kick players of equal or higher ranking!");

		String reason = null;

		// REASON
		if (args.length > 1) {
			StringBuilder sb = new StringBuilder();
			for (int x = 0; ++x < args.length;) {
				String word = args[x];

				sb.append(word);
				if (!(x+1 >= args.length))
					sb.append(" ");
			}

			reason = sb.toString();
		}

		PlayerProfile targetProfile = PlayerProfile.from(target);

		String dPunisher = profile != null ? profile.getDiscordMember() != null ? profile.getDiscordMember().getAsMention() : profile.getDisplayName() : "Server";
		String dTarget = targetProfile.getDiscordMember() != null ? targetProfile.getDiscordMember().getAsMention() : targetProfile.getDisplayName();

		PunishmentMinecraft punishment = getPlugin().getPunishmentManager().kickPlayer(target, profile != null ? profile.getId() : 0, reason);

		Utils.notifyAllStaff(Component.empty().append(toName(sender).append(Component.text("\u00a7r kicked ")).append(targetProfile.getComponentName()).append(Component.text("\u00a7r.\n"))
						.append(Component.text("\u00a74 • \u00a7rReason: \u00a7f" + (punishment.getReason() == null ? "No reason given" : punishment.getReason())))).color(BeanColor.BAN),
				"Player Kicked",
				dPunisher + " kicked " + dTarget + "\n" +
						"**Reason:** " + (punishment.getReason() == null ? "No reason given" : punishment.getReason()), DiscordBot.getIconURL(targetProfile.getId()));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);

		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be banned."))).color(NamedTextColor.GRAY),
			Component.text("[reason]").hoverEvent(HoverEvent.showText(Component.text("The ban reason shown the player."))),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
