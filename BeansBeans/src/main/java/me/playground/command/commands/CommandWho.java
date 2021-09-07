package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandWho extends BeanCommand {
	
	public CommandWho(Main plugin) {
		super(plugin, true, "who");
		description = "View information about a player.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		PlayerProfile target = args.length > 0 ? toProfile(sender, args[0]) : (isPlayer(sender) ? profile : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");
		
		sender.sendMessage(Component.text("\u00a77Information about ").append(target.getComponentName()));
		int mins = target.getOfflinePlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60;
		int hours = Math.floorDiv(mins, 60);
		mins -= hours*60;
		
		sender.sendMessage(Component.text("\u00a77Playtime: \u00a7f" + (hours > 0 ? hours + " Hours and " : "") + mins + " Minutes"));
		sender.sendMessage(Component.text("\u00a77Ranks: ").append(target.getComponentRanks()));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
