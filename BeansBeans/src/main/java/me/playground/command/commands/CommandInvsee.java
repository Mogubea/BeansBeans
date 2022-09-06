package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.gui.BeanGuiShulker;
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

public class CommandInvsee extends BeanCommand {
	
	public CommandInvsee(Main plugin) {
		super(plugin, "bean.cmd.invsee", false, 1, "invsee", "showinv");
		description = "View another player's inventory.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player target = toPlayer(sender, args[0], false);

		// TODO: Create a custom GUI where we can view a player's inventory safely.
		if (PlayerProfile.from(target).getBeanGui() instanceof BeanGuiShulker) return false;

		((Player)sender).openInventory(target.getInventory());
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text(" <player>").hoverEvent(HoverEvent.showText(Component.text("The player's inventory to view"))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]);
	}

}
