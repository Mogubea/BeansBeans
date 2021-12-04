package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;

public class CommandPlebeian extends BeanCommand {
	
	public CommandPlebeian(Main plugin) {
		super(plugin, true, Rank.PLEBEIAN.lowerName(), Rank.PATRICIAN.lowerName(), Rank.SENATOR.lowerName());
		description = "Check the remaining duration of your supporter rank.";
	}
	
//	final String[] args = {"reload"};
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		PlayerProfile target = args.length > 0 && sender.hasPermission("bean.cmd.plebeian.others") ? toProfile(sender, args[0]) : (isPlayer(sender) ? profile : null);
		if (target == null) throw new CommandException(sender, "Please specify a player!");
		
		long expiration = target.getCheckDonorExpiration(); // Check expiration before getting the rank just in-case they just expired.
		Rank rank = target.getDonorRank();
		
		if (target == profile) {
			if (rank == null) {
				if (expiration != 0L)
					sender.sendMessage(Component.text("\u00a77Your supporter rank\u00a77 expired \u00a7f" + Utils.timeStringFromNow(expiration) + " ago"));
				else
					sender.sendMessage(Component.text("\u00a77You don't have a supporter rank."));
			} else {
				sender.sendMessage(Component.text("\u00a77You are a ").append(rank.toComponent()).append(Component.text("\u00a77.")));
				if (expiration != 0L)
					sender.sendMessage(Component.text("\u00a77Your ").append(rank.toComponent()).append(Component.text("\u00a77 rank expires in ")).append(Component.text(Utils.timeStringFromNow(expiration), rank.getRankColour())));
			}
		} else {
			if (rank == null) {
				if (expiration != 0L)
					sender.sendMessage(target.getComponentName().append(Component.text("\u00a77's supporter rank expired \u00a7f" + Utils.timeStringFromNow(expiration) + " ago")));
				else
					sender.sendMessage(target.getComponentName().append(Component.text("\u00a77 doesn't have a supporter rank.")));
			} else {
				sender.sendMessage(target.getComponentName().append(Component.text("\u00a77 is a ")).append(rank.toComponent()).append(Component.text("\u00a77.")));
				if (expiration != 0L)
					sender.sendMessage(target.getComponentName().append(Component.text("\u00a77's ")).append(rank.toComponent()).append(Component.text("\u00a77 rank expires in ")).append(Component.text(Utils.timeStringFromNow(expiration), rank.getRankColour())));
			}
		}
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.plebeian.others"))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
//		if (args.length == 1 && sender.hasPermission(Permission.MODIFY_PERMISSIONS))
//			return TabCompleter.completeString(args[0], this.args);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}
	
}
