package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandPerform extends BeanCommand {
	
	public CommandPerform(Main plugin) {
		super(plugin, "bean.cmd.perform", true, 2, "perform");
		description = "Send a command or chat message as another user.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player target = toPlayer(sender, args[0]);
		
		// Can only perform as someone below your rank.
		checkRankPower(sender, target, "You can't perform actions as someone with equal or higher ranking!");
		
		String performance = "";
		for (int x = 1; x < args.length; x++)
			performance += args[x] + " ";
		
		target.chat(performance);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text(" <player>").hoverEvent(HoverEvent.showText(Component.text("The player that is going to be performing the following message."))).color(NamedTextColor.GRAY),
			Component.text(" <performance>").hoverEvent(HoverEvent.showText(Component.text("The performance, this can be a chat message or command."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]).append(usageArguments[1]);
	}

}
