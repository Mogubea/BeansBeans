package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandReturn extends BeanCommand {
	
	public CommandReturn(Main plugin) {
		super(plugin, "bean.cmd.return", false, "return", "back");
		description = "Return to your previous location before warping or dying.";
		cooldown = 8;
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = ((Player)sender);
		
		if (profile.getLastLocation(0) == null)
			throw new CommandException(p, "You don't have a position to return to!");
			
		p.teleport(profile.getLastLocation(0), TeleportCause.COMMAND);
		p.sendMessage("\u00a77Successfully returned to your previous location!");
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
