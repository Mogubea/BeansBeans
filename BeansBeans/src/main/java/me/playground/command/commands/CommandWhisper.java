package me.playground.command.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.punishments.Type;
import me.playground.ranks.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

public class CommandWhisper extends BeanCommand { // TODO:

	private final HashMap<Integer, Integer> lastDMby = new HashMap<>();
	private final int ID_SERVER = 0;
	
	public CommandWhisper(Main plugin) {
		super(plugin, true, 1, "tell", "message", "msg", "whisper", "w", "pm", "dm", "reply", "r");
		description = "Privately message an online player!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final boolean isReply = str.equals("r") || str.equals("reply");
		
		final Player target = isReply ? toPlayerFromId(sender, lastDMby.getOrDefault(isPlayer(sender) ? profile.getId() : ID_SERVER, -2)) : toPlayer(sender, args[0], false);
		final PlayerProfile targetProfile = PlayerProfile.from(target);

		// If the player is muted
		if (profile != null && profile.isMuted()) {
			// Disallow full muted players entirely, but allow global mutes to talk to friends, party members and staff.
			if (profile.getMute().getType() == Type.MINECRAFT_FULL_MUTE || (!profile.isFriends(targetProfile.getId()) && !targetProfile.isRank(Rank.MODERATOR))) {
				sender.sendActionBar(Component.text("\u00a74\u26a0\u00a7c You're " + (profile.getMute().isPermanent() ? "permanently" : "currently") + " muted \u00a74\u26a0"));
				return true;
			}
		}

		// If the player is being ignored or the player is ignoring target
		if (profile != null && (profile.isIgnoring(targetProfile.getId()) || targetProfile.isIgnoring(profile.getId()))) {
			sender.sendActionBar(Component.text("\u00a7cYou can't message this player."));
			return true;
		}

		// If there is no message
		if (args.length < (isReply ? 1 : 2))
			throw new CommandException(sender, Component.text("You must type something to send to ").append(PlayerProfile.from(target).getComponentName()));
		
		Component from = profile != null ? profile.getComponentName() : Component.text("\u00a7d\u00a7oServer").hoverEvent(HoverEvent.showText(Component.text("\u00a77Server Console")));
		
		StringBuilder sb = new StringBuilder();
		for (int x = isReply ? 0 : 1; x < args.length; x++)
			sb.append(" ").append(args[x]);
		
		Component message = Component.text(sb.toString()).color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);
		target.sendMessage(Component.text("[").append(from).append(Component.text(" -> You]:")).append(message).colorIfAbsent(TextColor.color(0x6f6f6f)));
		sender.sendMessage(Component.text("[You -> ").append(PlayerProfile.from(target).getComponentName()).append(Component.text("]:")).append(message).colorIfAbsent(TextColor.color(0x6f6f6f)));
		lastDMby.put(PlayerProfile.from(target).getId(), isPlayer(sender) ? profile.getId() : ID_SERVER);
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final boolean isReply = str.equals("r") || str.equals("reply");
		if (!isReply && args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player you wish to send a private message to."))).color(NamedTextColor.GRAY),
			Component.text(" <message>").hoverEvent(HoverEvent.showText(Component.text("The private message that will be sent to the specified player."))).color(NamedTextColor.GRAY),
			Component.text("<message>").hoverEvent(HoverEvent.showText(Component.text("The private message that will be sent to the previous player that messaged you."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		if (str.equals("r") || str.equals("reply"))
			return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[2]);
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
