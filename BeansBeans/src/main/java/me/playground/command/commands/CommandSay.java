package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class CommandSay extends BeanCommand {
	
	public CommandSay(Main plugin) {
		super(plugin, "bean.cmd.say", true, 1, "say");
		description = "Broadcast something to the entire server.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		String msg = "["+sender.getName()+"] ";
		for (String s : args)
			msg += s + " ";
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(Component.text(msg).color(TextColor.color(0xff4298)));
			p.sendActionBar(Component.text(msg).color(TextColor.color(0xff4298)));
		}
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text(" <message>").hoverEvent(HoverEvent.showText(Component.text("The message to be announced to the entire server."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]);
	}

}
