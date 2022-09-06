package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;

public class CommandHug extends BeanCommand {
	
	public CommandHug(Main plugin) {
		super(plugin, true, 1, "hug");
		description = "Hugs";
		cooldown = 1000 * 300;
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("@a")) {
			Bukkit.broadcast(Component.text("\u00a7d» ").append(profile.getComponentName()).append(Component.text("\u00a7a hugged everybody!")));
		} else {
			final Player target = toPlayer(sender, args[0], false);
			Bukkit.broadcast(Component.text("\u00a7d» ").append(toName(sender)).append(Component.text("\u00a7a hugged ").append(toName(target))));
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text(" <player>").hoverEvent(HoverEvent.showText(Component.text("The player being hugged!"))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]);
	}

}
