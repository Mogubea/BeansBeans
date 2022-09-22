package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
		super(plugin, "bean.cmd.value", true, "value", "itemvalue");
		description = "Check the sell value of your items!";
	}

	String[] args = {"hand", "offhand", "inventory", "inv"};

	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		boolean isPlayer = isPlayer(sender);

		if (!isPlayer && args.length < 1)
			throw new CommandException(sender, Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/value <item>", NamedTextColor.WHITE)));

		boolean mainHand = args.length == 0 || args[0].equalsIgnoreCase("hand");
		if (isPlayer && (mainHand || args[0].equalsIgnoreCase("offhand"))) {
			ItemStack hand = mainHand ? ((Player)sender).getInventory().getItemInMainHand() : ((Player)sender).getInventory().getItemInOffHand();

			if (hand.getType().isAir())
				throw new CommandException(sender, "Either hold the item you wish to get the value of, or specify the item in the command.");

			sender.sendMessage(Component.text("\u00a77Your ").append(toHover(hand)).append(Component.text("\u00a77 can be sold for \u00a76" + dec.format(getPlugin().getItemValueManager().getTotalValue(hand, true)) + " Coins\u00a77.")));
		} else if (isPlayer && (args[0].equalsIgnoreCase("inventory") || args[0].equalsIgnoreCase("inv"))) {
			Inventory inv = ((Player)sender).getInventory();
			float val = 0;

			for (ItemStack item : inv.getContents()) {
				if (item == null) continue;
				val += getPlugin().getItemValueManager().getTotalValue(item, true);
			}

			sender.sendMessage(Component.text("\u00a77Your inventory can be sold for \u00a76" + dec.format(val) + " Coins\u00a77."));
		} else {
			ItemStack itemToValue = toItemStack(sender, args[0], 1);
			double value = getPlugin().getItemValueManager().getValue(itemToValue);
			if (value > 0) {
				sender.sendMessage(Component.empty().append(toHover(itemToValue)).append(Component.text(" can typically be sold to Beansfolk for ", NamedTextColor.GRAY)).append(Component.text(dec.format(value) + " Coins", NamedTextColor.GOLD).append(Component.text(" each.", NamedTextColor.GRAY))));
			} else {
				sender.sendMessage(Component.text("Beansfolk aren't currently willing to purchase ").append(toHover(itemToValue)).append(Component.text(".", NamedTextColor.GRAY)));
			}
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> params = isPlayer(sender) ? TabCompleter.completeString(args[0], this.args) : TabCompleter.completeItems(args[0]);
			if (isPlayer(sender)) params.addAll(TabCompleter.completeItems(args[0]));
			return params;
		}
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str);
	}
	
}
