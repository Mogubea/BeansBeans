package me.playground.command.commands;

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
import me.playground.utils.BeanColor;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandSapphire extends BeanCommand {
	
	public CommandSapphire(Main plugin) {
		super(plugin, true, "sapphire", "tickets", "votepoints", "votecoins");
		description = "View your Sapphire Balance!";
	}
	
	final String[] adminCmds = { "give", "take", "set" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		PlayerProfile target = args.length > 0 && isRank(sender, Rank.MODERATOR) ? toProfile(sender, args[0]) : (isPlayer(sender) ? profile : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");
		
		if (args.length > 1 && isRank(sender, Rank.ADMINISTRATOR)) {
			final String subCmd = args[1].toLowerCase();
			if (args.length == 2)
				if (subCmd.equals("give")||subCmd.equals("take")||subCmd.equals("set"))
					throw new CommandException(sender, "Please specify an amount to " + subCmd + "!");
				else
					throw new CommandException(sender, "'"+args[1]+"' is not a valid monetary action!");
			
			final int amt = toIntMinMax(sender, args[2], 1, 100000000);
			if (subCmd.equals("give")) {
				target.addToSapphire(amt);
				sender.sendMessage(Component.text("\u00a77Given \u00a7r" + df.format(amt) + " Sapphire\u00a77 to ").colorIfAbsent(BeanColor.SAPPHIRE).append(target.getComponentName()).append(Component.text("\u00a77.")));
			} else if (subCmd.equals("take")) {
				target.addToSapphire(-amt);
				sender.sendMessage(Component.text("\u00a77Taken \u00a7r" + df.format(amt) + " Sapphire\u00a77 from ").colorIfAbsent(BeanColor.SAPPHIRE).append(target.getComponentName()).append(Component.text("\u00a77.")));
			} else if (subCmd.equals("set")) {
				target.setSapphire(amt);
				sender.sendMessage(Component.text("\u00a77Set ").append(target.getComponentName()).append(Component.text("\u00a77's sapphire to \u00a7r" + df.format(amt) + " Sapphire\u00a77.").colorIfAbsent(BeanColor.SAPPHIRE)));
			} else {
				throw new CommandException(sender, "'"+args[1]+"' is not a valid monetary action!");
			}
		} else {
			if (profile == null || profile.getId() == target.getId())
				sender.sendMessage(Component.text("\u00a77You have \u00a7r" + df.format(target.getSapphire()) + " Sapphire\u00a77.").colorIfAbsent(BeanColor.SAPPHIRE));
			else
				sender.sendMessage(target.getComponentName().append(Component.text("\u00a77has \u00a7r" + df.format(target.getSapphire()) + " Sapphire\u00a77.").colorIfAbsent(BeanColor.SAPPHIRE)));
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && isRank(sender, Rank.MODERATOR))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		if (args.length == 2 && isRank(sender, Rank.ADMINISTRATOR))
			return TabCompleter.completeString(args[1], adminCmds);
		if (args.length == 3 && isRank(sender, Rank.ADMINISTRATOR))
			return TabCompleter.completeInteger(args[2]);
		
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
