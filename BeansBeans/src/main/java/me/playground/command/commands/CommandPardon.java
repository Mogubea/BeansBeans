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

public class CommandPardon extends BeanCommand {

	public CommandPardon(Main plugin) {
		super(plugin, "bean.cmd.pardon", true, 1, "pardon", "unban");
		description = "Pardon a player.";
	}

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = toProfile(sender, args[0]);

		if (profile != null && target.getId() == profile.getId())
			throw new CommandException(sender, "You can't pardon yourself of your crimes.");

		if (getRank(sender).power() <= target.getHighestRank().power())
			throw new CommandException(sender, "You can't pardon players of equal or higher ranking!");

		if (!target.isBanned())
			throw new CommandException(sender, target.getComponentName().append(Component.text(" isn't banned.")));

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

		Utils.notifyAllStaff(Component.empty().append(toName(sender).append(Component.text("\u00a7r pardoned ")).append(target.getComponentName()).append(Component.text("\u00a7r's ban.")))
						.color(BeanColor.BAN).hoverEvent(HoverEvent.showText(target.getBan().toComponent())),
				"Player Ban Pardoned",
				dPunisher + " pardoned " + dTarget + "'s ban. They can now connect to Bean's Beans once again.", DiscordBot.getIconURL(target.getId()));

		target.getBan().pardon();
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);

		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be un-banned."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
