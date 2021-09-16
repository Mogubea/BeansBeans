package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.gui.BeanGuiBestiaryEntity;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

/**
 * Not the same as /bestiary, it also has a reload sub command and in future, will have more sub commands for those with permissions.
 */
public class CommandLoot extends BeanCommand {
	
	public CommandLoot(Main plugin) {
		super(plugin, true, "loot", "rewards");
		description = "A command shortcut for viewing the bestiary!";
	}
	
	final String[] args = {"reload"};
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length > 0 && sender.hasPermission("bean.loot") && args[0].equalsIgnoreCase("reload")) {
			getPlugin().lootManager().reload();
			sender.sendMessage("\u00a77Loaded " + getPlugin().lootManager().getAllEntries().size() + " Loot Entries.");
		} else if (isPlayer(sender)) {
			new BeanGuiBestiaryEntity((Player)sender).openInventory();
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.loot.reload"))
			return TabCompleter.completeString(args[0], this.args);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}
	
	/*if ("reload".equals(subcmd)) {
				getPlugin().lootManager().reload();
				sender.sendMessage("\u00a77Loaded " + getPlugin().lootManager().getAllEntries().size() + " Loot Entries.");
			}
			*/
}
