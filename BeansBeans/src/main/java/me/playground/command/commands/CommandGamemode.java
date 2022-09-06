package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandGamemode extends BeanCommand {

	public CommandGamemode(Main plugin) {
		super(plugin, "bean.cmd.gamemode", true, 1, "gamemode", "gm");
		description = "Update your, or another players, gamemode.";
	}
	
	final String[] gamemodes = { "survival", "creative", "adventure", "spectator" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = (sender.hasPermission("bean.cmd.gamemode.others") && args.length > 1) ? toPlayer(sender, args[1]) : (isPlayer(sender) ? ((Player)sender) : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");
		
		// Check the rank of the target
		checkRankPower(sender, target, "You can't change the gamemode of a player with equal or higher ranking!");
		
		// Allow the usage of /gm <number> instead of the traditional /gamemode <gamemode>.
		String gm = args[0].toLowerCase();
		final int intAlt = toIntDef(args[0], -1);
		if (intAlt > -1 && intAlt < gamemodes.length)
			gm = gamemodes[intAlt];
		
		// Allow the usage of shortened gamemode identifiers (s, c, a).
		for (int x = 0; x < gamemodes.length; x++) {
			if (gm.equals(gamemodes[x]) || gamemodes[x].startsWith(gm)) {
				final PlayerProfile tp = PlayerProfile.from(target);
				target.setGameMode(GameMode.valueOf(gamemodes[x].toUpperCase()));
				if (target != sender)
					sender.sendMessage(tp.getComponentName().append(Component.text("\u00a77 is now in \u00a7f" + Utils.firstCharUpper(target.getGameMode().name()) + " Mode\u00a77.")));
				return true;
			}
		}
		
		// Gamemode doesn't exist
		throw new CommandException(sender, "'"+args[0]+"' is not a valid gamemode.");
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> gms = TabCompleter.completeString(args[0], gamemodes);
			if (!sender.hasPermission("bean.gm.creative"))
				gms.remove("creative");
			if (!sender.hasPermission("bean.gm.adventure"))
				gms.remove("adventure");
			return gms;
		}
		if (args.length == 2)
			return TabCompleter.completeOnlinePlayer(sender, args[1]);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<gamemode>").hoverEvent(HoverEvent.showText(Component.text("The gamemode being swapped to."))).color(NamedTextColor.GRAY),
			Component.text(" [player]").hoverEvent(HoverEvent.showText(Component.text("Optional: The player who's gamemode is being swapped."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
