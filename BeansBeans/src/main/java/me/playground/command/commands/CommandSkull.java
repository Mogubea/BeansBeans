package me.playground.command.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandSkull extends BeanCommand {
	
	public CommandSkull(Main plugin) {
		super(plugin, "bean.cmd.skull", false, 1, "skull");
		description = "Spawn the skull of a player.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player)sender;
		final UUID uuid = getPlugin().getServer().getPlayerUniqueId(args[0]);
		if (uuid == null) throw new CommandException(sender, "The player '"+args[0]+"' does not exist.");
		
		p.getInventory().addItem(Utils.getSkullFromPlayer(uuid));
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
