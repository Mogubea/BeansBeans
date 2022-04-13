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
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.TabCompleter;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

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
		
		Component statusPrefix = Component.empty();
		if (!target.isOnline())
			if (target.isBanned())
				statusPrefix = statusPrefix.append(Component.text("\u00a7f[\u00a77Offline\u00a7f]").hoverEvent(HoverEvent.showText(Component.text("\u00a77This player is offline."))));
			else
				statusPrefix = statusPrefix.append(Component.text("\u00a7f[\u00a7cBanned\u00a7f]").hoverEvent(HoverEvent.showText(Component.text("\u00a77This player is banned."))));
		else if (target.isAFK())
			statusPrefix = statusPrefix.append(Component.text("\u00a7f[\u00a77AFK\u00a7f]").hoverEvent(HoverEvent.showText(Component.text("\u00a77AFK For: \u00a7f" + Utils.timeStringFromNow(target.getLastAFK())))));
		
		sender.sendMessage(Component.text("\u00a77Information about ").append(target.getComponentName()).append(Component.text(" ")).append(statusPrefix));
		int mins = target.getStat(StatType.GENERIC, "playtime") / 60;
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
