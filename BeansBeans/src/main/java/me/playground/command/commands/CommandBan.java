package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

public class CommandBan extends BeanCommand {
	
	public CommandBan(Main plugin) {
		super(plugin, "bean.cmd.ban", true, 1, "ban");
		description = "Ban a rule breaker";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = toProfile(sender, args[0]);
		if (profile != null && target.getId() == profile.getId())
			throw new CommandException(sender, "You can't ban yourself, silly!");
		sender.sendMessage(Component.text("Will have a gui for this in the future."));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to be banned."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
