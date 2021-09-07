package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandI extends BeanCommand {

	public CommandI(Main plugin) {
		super(plugin, false, Rank.ADMINISTRATOR, 1, "i");
		description = "Give yourself an item.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		ItemStack i = toItemStack(sender, args[0], args.length > 1 ? toIntMinMax(sender, args[1], 1, 1024) : 1);
		
		((Player)sender).getInventory().addItem(i);
		sender.sendMessage(Component.text("\u00a77Added \u00a7f").append(toHover(i)).append(Component.text("\u00a77 to your inventory!")));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> mats = TabCompleter.completeItems(args[0]);
			mats.addAll(TabCompleter.completeObject(args[0], i -> ((BeanItem)i).getIdentifier(), BeanItem.values()));
			return mats;
		}
		if (args.length == 2)
			return TabCompleter.completeIntegerBetween(args[1], 1, 64);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<item>").hoverEvent(HoverEvent.showText(Component.text("The item to be added to your inventory."))).color(NamedTextColor.GRAY),
			Component.text(" [amount]").hoverEvent(HoverEvent.showText(Component.text("Optional: The amount of the specified item."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: /\u00a7f"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
