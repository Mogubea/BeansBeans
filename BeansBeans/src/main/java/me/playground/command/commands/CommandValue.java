package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.stations.BeanGuiAnvil;
import me.playground.items.values.ItemValues;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandValue extends BeanCommand {

	public CommandValue(Main plugin) {
		super(plugin, "bean.cmd.value", false, "value", "itemvalue");
		description = "Check the sell value of your items!";
	}

	String[] args = {"hand", "offhand", "inventory", "inv"};

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player p = (Player) sender;

		boolean mainHand = args.length == 0 || args[0].equalsIgnoreCase("hand");
		if (mainHand || args[0].equalsIgnoreCase("offhand")) {
			ItemStack hand = mainHand ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();

			if (hand.getType().isAir())
				throw new CommandException(p, "Hold the item you wish to get the value of.");

			p.sendMessage(Component.text("\u00a77Your ").append(toHover(hand)).append(Component.text("\u00a77 can be sold for \u00a76" + dec.format(getPlugin().getItemValueManager().getTotalValue(hand, true)) + " Coins\u00a77.")));
		} else if (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("inv")) {
			Inventory inv = p.getInventory();
			float val = 0;

			for (ItemStack item : inv.getContents()) {
				if (item == null) continue;
				val += getPlugin().getItemValueManager().getTotalValue(item, true);
			}

			p.sendMessage(Component.text("\u00a77Your inventory can be sold for \u00a76" + dec.format(val) + " Coins\u00a77."));
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], this.args);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str);
	}
	
}
