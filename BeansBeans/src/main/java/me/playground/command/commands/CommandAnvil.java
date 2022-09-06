package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.gui.stations.BeanGuiAnvil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandAnvil extends BeanCommand {

	public CommandAnvil(Main plugin) {
		super(plugin, "bean.cmd.anvil", false, "anvil");
		description = "Open a virtual anvil!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		new BeanGuiAnvil((Player)sender, null).openInventory();
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str);
	}
	
}
