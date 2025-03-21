package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.utils.TabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandFly extends BeanCommand {
	
	public CommandFly(Main plugin) {
		super(plugin, "bean.cmd.fly", true, "fly");
		description = "Toggle flight";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = sender.hasPermission("bean.cmd.fly.others") && args.length > 0 ? toPlayer(sender, args[0]) : (isPlayer(sender) ? ((Player)sender) : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");
		
		// Check the rank of the target
		checkRankPower(sender, target, "You can't toggle flight of a player with equal or higher ranking!");
		
		target.setAllowFlight(!target.getAllowFlight());
		target.sendMessage("\u00a77You can " + (!target.getAllowFlight() ? "\u00a7cno longer" : "\u00a7anow") + "\u00a77 fly.");

		// Update their flag for reconnecting flight re-application if they have the flight permission.
		if (target.hasPermission("bean.cmd.fly"))
			PlayerProfile.from(target).setSettingEnabled(PlayerSetting.FLIGHT, target.getAllowFlight());

		if (sender != target)
			sender.sendMessage(PlayerProfile.from(target).getComponentName().append(Component.text(("\u00a77 can " + (!target.getAllowFlight() ? "\u00a7cno longer" : "\u00a7anow")) + "\u00a77 fly.")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.fly.others"))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
