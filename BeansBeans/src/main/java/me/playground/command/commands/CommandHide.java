package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandHide extends BeanCommand {

	public CommandHide(Main plugin) {
		super(plugin, "bean.cmd.hide", true, "hide");
		description = "Toggle online status.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = sender.hasPermission("bean.cmd.hide.others") && args.length > 0 ? toPlayer(sender, args[0]) : (isPlayer(sender) ? ((Player)sender) : null);
		if (target == null) throw new CommandException(sender, "Please specify a player!");
		
		// Check the rank of the target
		checkRankPower(sender, target, "You can't toggle visibility of a player with equal or higher ranking!");
		PlayerProfile tp = PlayerProfile.from(target);
		boolean toHide = tp.flipSetting(PlayerSetting.HIDE);
		
		// Update hidden player status for all players
		getPlugin().getServer().getOnlinePlayers().forEach(p -> {
			if (toHide) {
				if (PlayerProfile.from(p).isRank(Rank.MODERATOR)) return; // Don't hide from staff
				p.hidePlayer(getPlugin(), target);
			} else {
				p.showPlayer(getPlugin(), target);
			}
		});
		
		target.sendMessage("\u00a77You are " + (!toHide ? "\u00a7cno longer" : "\u00a7anow") + "\u00a77 hidden.");
		if (sender != target)
			sender.sendMessage(tp.getComponentName().append(Component.text(("\u00a77 is " + (!toHide ? "\u00a7cno longer" : "\u00a7anow")) + "\u00a77 hidden.")));
		return true;
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			if (sender.hasPermission("bean.cmd.hide.others"))
				return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ");
	}

}
