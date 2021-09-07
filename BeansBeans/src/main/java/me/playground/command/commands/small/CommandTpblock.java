package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import net.kyori.adventure.text.Component;

public class CommandTpblock extends BeanCommand {
	
	public CommandTpblock(Main plugin) {
		super(plugin, "tpblock");
		description = "Toggle whether non-staff players can teleport to you.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		profile.flipSetting(PlayerSetting.ALLOW_TP);
		sender.sendMessage("\u00a77You are " + (profile.isSettingEnabled(PlayerSetting.ALLOW_TP) ? "\u00a7cno longer" : "\u00a7anow") + "\u00a77 blocking all teleport attempts.");
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
