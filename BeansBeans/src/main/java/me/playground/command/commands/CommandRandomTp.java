package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandRandomTp extends BeanCommand {

	public CommandRandomTp(Main plugin) {
		super(plugin, "bean.cmd.randomtp", false, "randomtp", "rtp");
		this.description = "Teleport to a random location in the current world.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		final World w = p.getWorld();
		
		if (w.getEnvironment() != Environment.NORMAL)
			throw new CommandException(p, "Not very wise to do that here, chief.");
		
		if (!profile.isAdmin() && profile.getBalance() < 250)
			throw new CommandException(p, "You need 250 coins to use this command!");
		
		final Location oldLoc = p.getLocation();
			
		final int max = (int) w.getWorldBorder().getSize() / 7;
		
		Location randomLoc = new Location(w, -max + getRandom().nextInt(max*2) + 0.5, 60, -max + getRandom().nextInt(max*2) + 0.5);
		Block b = w.getHighestBlockAt(randomLoc);
			
		// For safety
		while (b.getType() == Material.LAVA || randomLoc.distance(oldLoc)<400) {
			randomLoc = new Location(w, -max + getRandom().nextInt(max*2) + 0.5, 60, -max + getRandom().nextInt(max*2) + 0.5);
			b = w.getHighestBlockAt(randomLoc);
		}
				
		int y = b.getY();
		randomLoc.setY(y);
		p.teleport(randomLoc, TeleportCause.COMMAND);
		p.sendMessage("\u00a77Randomly teleported!");
		profile.addToBalance(-250, "/rtp command");
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<item>").hoverEvent(HoverEvent.showText(Component.text("The item to be added to your inventory."))).color(NamedTextColor.GRAY),
			Component.text(" [amount]").hoverEvent(HoverEvent.showText(Component.text("Optional: The amount of the specified item."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
