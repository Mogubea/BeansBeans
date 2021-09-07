package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;

public class CommandPlugin extends BeanCommand {

	public CommandPlugin(Main plugin) {
		super(plugin, true, "plugins", "pl");
		description = "View the list of enabled plugins.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		String list = "";
		int x = 0;
		for (Plugin p : Bukkit.getServer().getPluginManager().getPlugins()) {
			x++;
			list += (p.isEnabled() ? "\u00a7a" : "\u00a7c") + p.getName() + "\u00a77, ";
		}
		
		list = "\u00a77Plugins ("+x+"): " + list;
		sender.sendMessage(Component.text(list));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return null;
	}

}
