package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandCelestia extends BeanCommand {

	public CommandCelestia(Main plugin) {
		super(plugin, false, Rank.MODERATOR, 1, "celestia", "cel");
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<item>").hoverEvent(HoverEvent.showText(Component.text("The item to be added to your inventory."))).color(NamedTextColor.GRAY),
			Component.text(" [amount]").hoverEvent(HoverEvent.showText(Component.text("Optional: The amount of the specified item."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
