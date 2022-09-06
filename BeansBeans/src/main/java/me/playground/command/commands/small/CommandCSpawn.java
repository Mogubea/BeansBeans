package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandCSpawn extends BeanCommand {
	
	public CommandCSpawn(Main plugin) {
		super(plugin, false, "cspawn");
		description = "A command to warp to your Civilization's spawn!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (!profile.isInCivilization()) throw new CommandException(sender, "You're not part of a Civilization!");
		if (!profile.getCivilization().hasSpawn()) throw new CommandException(sender, profile.getCivilization().toComponent().append(Component.text("\u00a7c doesn't have a spawn!")));
		
		if (((Player)sender).teleport(profile.getCivilization().getSpawn()))
			sender.sendMessage(Component.text("\u00a77Warped to ").append(profile.getCivilization().toComponent()).append(Component.text("\u00a77's spawn!")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
