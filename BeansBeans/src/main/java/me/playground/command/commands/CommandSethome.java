package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.data.Datasource;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;

public class CommandSethome extends BeanCommand {
	
	public CommandSethome(Main plugin) {
		super(plugin, "sethome");
		description = "Set your /home position!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		noGM(sender, GameMode.SPECTATOR);
		final Player p = (Player)sender;
		final Location loc = p.getLocation();
		if (!isSafe(loc))
			throw new CommandException(p, "You can't set your home here!");

		if (!Datasource.setHome(p, p.getLocation()))
			throw new CommandException(p, "There was a problem setting your home.");

		profile.setHome(p.getLocation());
		p.sendMessage(Component.text("\u00a77Successfully updated your ").append(commandInfo("home")).append(Component.text("\u00a77 location!")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
