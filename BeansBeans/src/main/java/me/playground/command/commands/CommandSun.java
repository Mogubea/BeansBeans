package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;

public class CommandSun extends BeanCommand {
	
	public CommandSun(Main plugin) {
		super(plugin, "bean.cmd.sun", false, "sun");
		description = "Change the weather to sunny!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		final World w = p.getWorld();
		
		if (w.getEnvironment() != Environment.NORMAL)
			throw new CommandException(sender, "There is no weather system here!");
		
		if (!(w.isThundering() || w.hasStorm()))
			throw new CommandException(sender, "It's not raining!");
		
		w.setThundering(false);
		w.setStorm(false);
		w.setWeatherDuration(20000 + getRandom().nextInt(16000));
		p.sendMessage(Component.text("\u00a77It is no longer raining!"));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
