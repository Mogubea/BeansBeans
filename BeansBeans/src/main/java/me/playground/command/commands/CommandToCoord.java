package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class CommandToCoord extends BeanCommand {

	public CommandToCoord(Main plugin) {
		super(plugin, "bean.cmd.tocoord", false, 3, "tocoord", "tpcoord", "tpc");
		description = "Teleport to the specified co-ordinates";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		final World w = args.length < 4 ? p.getWorld() : toWorld(p, args[3]);
			
		final Location toLoc = new Location(
				w,
				toIntDef(args[0], p.getLocation().getBlockX()),
				toIntDef(args[1], p.getLocation().getBlockY()),
				toIntDef(args[2], p.getLocation().getBlockZ()));
		if (!w.getWorldBorder().isInside(toLoc))
			throw new CommandException(p, Component.text("\u00a7cX: "+toLoc.getX()+", Y: "+toLoc.getY()+", Z: "+toLoc.getZ()+" is outside of ").append(worldInfo(p, w)).append(Component.text("\u00a7c's borders!")));
		
		p.teleport(toLoc, TeleportCause.COMMAND);
		p.sendMessage(Component.text("\u00a77Successfully teleported to \u00a7r" + toLoc.getX()+"\u00a77, \u00a7r"+toLoc.getY()+"\u00a77, \u00a7r"+toLoc.getZ()+"\u00a77 in ").colorIfAbsent(TextColor.color(0x30cb5a)).append(worldInfo(p, w)).append(Component.text("\u00a77.")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 4)
			return TabCompleter.completeIntegerBetween(args[args.length-1], -10000, 10000);
		if (args.length == 4)
			return TabCompleter.completeString(args[3], TabCompleter.completeObject(args[1], w -> ((World)w).getName(), Bukkit.getWorlds()));
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text(" <x>").hoverEvent(HoverEvent.showText(Component.text("The desired X co-ordinate."))).color(NamedTextColor.GRAY),
			Component.text(" <y>").hoverEvent(HoverEvent.showText(Component.text("The desired Y co-ordinate."))).color(NamedTextColor.GRAY),
			Component.text(" <z>").hoverEvent(HoverEvent.showText(Component.text("The desired Z co-ordinate."))).color(NamedTextColor.GRAY),
			Component.text(" [world]").hoverEvent(HoverEvent.showText(Component.text("Optional: The desired world."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]).append(usageArguments[1]).append(usageArguments[2]).append(usageArguments[3]);
	}

}
