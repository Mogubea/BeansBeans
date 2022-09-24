package me.playground.command.commands.small;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;

public class CommandLoot extends BeanCommand {
	
	public CommandLoot(Main plugin) {
		super(plugin, "loot");
		description = "A command shortcut for viewing the bestiary!";
	}

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		//new BeanGuiBestiaryEntity((Player)sender).openInventory();
		return true;
	}

}
