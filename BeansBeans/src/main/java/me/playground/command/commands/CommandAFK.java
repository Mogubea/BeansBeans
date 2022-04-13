package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandAFK extends BeanCommand {

	public CommandAFK(Main plugin) {
		super(plugin, false, "afk");
		description = "Toggle AFK Status.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (profile.isAFK()) {
			profile.pokeAFK();
		} else {
			String reason = "Away";
			if (args.length > 0) {
				StringBuilder sb = new StringBuilder();
				for (int x = -1; ++x < args.length;) {
					sb.append(args[x]);
					if (x+1 < args.length)
						sb.append(" ");
				}
				reason = sb.toString();
			}
			profile.setAFK(reason);
		}
		return true;
	}

	final Component[] usageArguments = {
			Component.text("[reason]").hoverEvent(HoverEvent.showText(Component.text("Reason for being away."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

	@Override
	public List<String> runTabComplete(CommandSender sender, Command cmd, String str, String[] args) {
		return Collections.emptyList();
	}

}
