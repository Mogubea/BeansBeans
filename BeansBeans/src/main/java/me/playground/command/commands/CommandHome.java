package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandHome extends BeanCommand {
	
	public CommandHome(Main plugin) {
		super(plugin, "home");
		description = "Warp to your /sethome!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final PlayerProfile target = args.length > 0 && sender.hasPermission("bean.cmd.home.others") ? toProfile(sender, args[0]) : profile;
		
		final Player p = (Player)sender;
		final Location loc = target.getHome();
		if (loc == null)
			if (target.getId() != profile.getId())
				throw new CommandException(p, target.getComponentName().append(Component.text("\u00a7c doesn't have a home!")));
			else
				throw new CommandException(p, Component.text("\u00a7cYou don't have a home! Set one with ").append(commandInfo("sethome")).append(Component.text("\u00a7c.")));
		
		p.teleport(loc, TeleportCause.COMMAND);
		if (target.getId() != profile.getId())
			p.sendMessage(Component.text("\u00a77Successfully teleported to ").append(target.getComponentName()).append(Component.text("\u00a77's home.")));
		else
			p.sendMessage(Component.text("\u00a77Successfully teleported home."));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && isRank(sender, Rank.MODERATOR))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
