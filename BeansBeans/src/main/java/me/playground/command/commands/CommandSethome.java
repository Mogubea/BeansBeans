package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.BeanGuiConfirm;
import me.playground.items.lore.Lore;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandSethome extends BeanCommand {
	
	public CommandSethome(Main plugin) {
		super(plugin, "sethome");
		description = "Set your /home position!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		noGM(sender, GameMode.SPECTATOR);
		final Player p = (Player)sender;
		final Location loc = p.getLocation();
		if (!isSafe(loc))
			throw new CommandException(p, "You can't set your home here!");

		new BeanGuiConfirm(p, Lore.getBuilder("Confirming will move your home location to your current position.").dontFormatColours().build().getLoree()) {
			@Override
			public void onAccept() {
				profile.setHome(p.getLocation());
				p.sendMessage(Component.text("\u00a77Successfully updated your ").append(commandInfo("home")).append(Component.text("\u00a77 location!")));
			}

			@Override
			public void onDecline() {}
		}.openInventory();
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
