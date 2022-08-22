package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.discord.DiscordBot;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
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

public class CommandUnmute extends BeanCommand {

	public CommandUnmute(Main plugin) {
		super(plugin, "bean.cmd.unmute", true, 1, "unmute");
		description = "Unmute a player.";
	}

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = toProfile(sender, args[0]);

		if (profile != null && target.getId() == profile.getId())
			throw new CommandException(sender, "You can't un-mute yourself.");

		if (getRank(sender).power() <= target.getHighestRank().power())
			throw new CommandException(sender, "You can't un-mute players of equal or higher ranking!");

		if (!target.isMuted())
			throw new CommandException(sender, target.getComponentName().append(Component.text(" isn't muted.")));

		/*String reason = null;

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
		}*/

		String dPunisher = profile != null ? profile.getDiscordMember() != null ? profile.getDiscordMember().getAsMention() : profile.getDisplayName() : "Server";
		String dTarget = target.getDiscordMember() != null ? target.getDiscordMember().getAsMention() : target.getDisplayName();

		Utils.notifyAllStaff(Component.empty().append(toName(sender).append(Component.text("\u00a7r un-muted ")).append(target.getComponentName()).append(Component.text("\u00a7r.")))
						.color(BeanColor.BAN).hoverEvent(HoverEvent.showText(target.getMute().toComponent())),
				"Player Un-muted",
				dPunisher + " un-muted " + dTarget + "'s. They can now speak on Bean's Beans once again.", DiscordBot.getIconURL(target.getId()));

		target.getMute().pardon();
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);

		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be un-muted."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
