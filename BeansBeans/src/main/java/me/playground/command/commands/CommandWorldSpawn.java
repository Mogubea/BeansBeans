package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandWorldSpawn extends BeanCommand {
	
	public CommandWorldSpawn(Main plugin) {
		super(plugin, false, "wspawn", "worldspawn");
		description = "Warp to the world's spawnpoint!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		final World w = ((p.hasPermission("bean.cmd.world") && args.length > 0 ? toWorld(p, args[0]) : p.getWorld()));
		
		if (p.teleport(p.getWorld().getSpawnLocation()))
			p.sendMessage(Component.text("\u00a77Successfully teleported to ").append(worldInfo(p, w)).append(Component.text("\u00a77's spawn.")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.world"))
			return TabCompleter.completeString(args[0], TabCompleter.completeObject(args[0], w -> ((World)w).getName(), Bukkit.getWorlds()));
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
