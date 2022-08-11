package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandEnderchest extends BeanCommand {
	
	public CommandEnderchest(Main plugin) {
		super(plugin, "bean.cmd.enderchest", false, "enderchest", "ec");
		description = "View your enderchest.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player)sender;
		final Player target = args.length > 0 && sender.hasPermission("bean.cmd.enderchest.others") ? toPlayer(sender, args[0]) : p;
		final Inventory inv = target.getEnderChest();
		
		p.openInventory(inv);
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.enderchest.others"))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
