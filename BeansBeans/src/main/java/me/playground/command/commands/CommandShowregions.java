package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.utils.BeanColor;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandShowregions extends BeanCommand {

	public CommandShowregions(Main plugin) {
		super(plugin, true, "showregions", "showregion");
		description = "Toggle Region Boundary visibility.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = sender.hasPermission("bean.cmd.showregions.others") && args.length > 0 ? toPlayer(sender, args[0]) : (isPlayer(sender) ? ((Player)sender) : null);
		if (target == null)
			throw new CommandException(sender, "Please specify a player!");

		// Check the rank of the target
		checkRankPower(sender, target, "You can't toggle the visuals of a player with equal or higher ranking!");

		PlayerProfile tpp = PlayerProfile.from(target);
		boolean enabled = tpp.flipSetting(PlayerSetting.REGION_BOUNDARIES);

		target.sendMessage(Component.text("You will ").append(enabled ? Component.text("now", NamedTextColor.GREEN) : Component.text("no longer", NamedTextColor.RED)).append(Component.text(" passively see ").append(Component.text("Region Boundaries", BeanColor.REGION_PLAYER).append(Component.text(".")))).colorIfAbsent(NamedTextColor.GRAY));

		if (sender != target)
			sender.sendMessage(tpp.getComponentName().append(Component.text(" will ").append(enabled ? Component.text("now", NamedTextColor.GREEN) : Component.text("no longer", NamedTextColor.RED)).append(Component.text(" passively see ").append(Component.text("Region Boundaries", BeanColor.REGION_PLAYER).append(Component.text("."))))).colorIfAbsent(NamedTextColor.GRAY));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && sender.hasPermission("bean.cmd.showregions.others"))
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
