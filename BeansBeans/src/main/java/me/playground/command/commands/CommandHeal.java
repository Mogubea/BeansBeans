package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandHeal extends BeanCommand {

	public CommandHeal(Main plugin) {
		super(plugin, "bean.cmd.heal", true, "heal");
		description = "General healing command.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		boolean isPlayer = isPlayer(sender);
		int healValue = args.length > 0 ? toIntDef(args[args.length > 1 ? 1 : 0], -1) : -1;
		Player target = args.length > 0 && sender.hasPermission("bean.cmd.heal.others") && healValue == -1 ? toPlayer(sender, args[0], true) : (isPlayer ? ((Player)sender) : null);
		if (target == null) throw new CommandException(sender, "Please specify a player!");
		
		double maxHeal = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - target.getHealth();
		boolean healHunger = false;
		
		if (healValue < 1 || healValue > maxHeal) {
			healValue = (int) maxHeal;
			healHunger = true;
			target.setFoodLevel(20);
			target.setSaturation(4F);
		}
		
		String suffix = (healHunger ? "." : " for \u00a7c"+healValue+" \u2764");
		
		target.setHealth(target.getHealth() + healValue);
		if (target == sender) {
			target.sendMessage(Component.text("\u00a77You healed yourself" + suffix));
		} else {
			target.sendMessage(Component.text("\u00a77You were healed" + suffix));
			sender.sendMessage(Component.text("\u00a77You healed ").append(PlayerProfile.from(target).getComponentName()).append(Component.text("\u00a77" + suffix)));
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("bean.cmd.heal.others"))
				return TabCompleter.completeOnlinePlayer(sender, args[0]);
			else
				return TabCompleter.completeInteger(args[0]);
		}
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str);
	}

}
