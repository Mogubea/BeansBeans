package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandTeleport extends BeanCommand {

	public CommandTeleport(Main plugin) {
		super(plugin, "bean.cmd.teleport", true, 1, "teleport", "tp", "tpa");
		description = "Teleport to another player";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (!isPlayer(sender) && args.length < 2)
			throw new CommandException(sender, getUsage(sender, str, args));
		
		final Player teleporter = args.length>1 ? toPlayer(sender, args[0]) : ((isPlayer(sender) ? ((Player)sender) : null));
		final Player target = (sender.hasPermission("bean.cmd.teleport.others") && args.length>1) ? toPlayer(sender, args[1]) : toPlayer(sender, args[0]);
		
		final boolean isOther = teleporter != sender;
		
		if (isOther)
			this.checkRankPower(sender, teleporter, "You can't teleport players of equal or higher ranking!");
		
		if (teleporter == target)
			throw new CommandException(sender, "You can't teleport to yourself!");
		
		final PlayerProfile tepp = PlayerProfile.from(teleporter);
		final PlayerProfile tapp = PlayerProfile.from(target);
		
		if (!sender.hasPermission("bean.cmd.teleport.bypass") && !tapp.isSettingEnabled(PlayerSetting.ALLOW_TP))
			throw new CommandException(sender, Component.text("\u00a7cCouldn't teleport, ").append(tapp.getComponentName()).append(Component.text("\u00a7c is blocking attempts!")));
		
		if (target.getGameMode() == GameMode.SPECTATOR && teleporter.getGameMode() != GameMode.SPECTATOR)
			teleporter.setGameMode(GameMode.SPECTATOR);
		
		teleporter.teleport(target, TeleportCause.COMMAND);

		// Don't send a message if spectating or in hide
		if (teleporter.getGameMode() != GameMode.SPECTATOR && !PlayerProfile.from(teleporter).isHidden())
			target.sendMessage(tepp.getComponentName().append(Component.text("\u00a77 teleported to you!")));

		if (isOther)
			sender.sendMessage(Component.text("\u00a77Successfully teleported ").append(tepp.getComponentName()).append(Component.text("\u00a77 to ")).append(tapp.getComponentName()).append(Component.text("\u00a77!")));
		teleporter.sendMessage(Component.text("\u00a77Successfully teleported to ").append(tapp.getComponentName()).append(Component.text("\u00a77!")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		if (args.length == 2 && sender.hasPermission("bean.cmd.teleport.others"))
			return TabCompleter.completeOnlinePlayer(sender, args[1]);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player being teleported to."))).color(NamedTextColor.GRAY),
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player being teleported to, or the player that's being teleported."))).color(NamedTextColor.GRAY),
			Component.text(" [target]").hoverEvent(HoverEvent.showText(Component.text("Optional: The player being teleported to."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		if (sender.hasPermission("bean.cmd.teleport.others"))
			return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[1]).append(usageArguments[2]);
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
