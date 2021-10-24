package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.BeanColor;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandEnchant extends BeanCommand {

	public CommandEnchant(Main plugin) {
		super(plugin, "bean.cmd.enchant", false, 1, "enchant");
		description = "Alter the enchantments of the item in your hand.";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		final ItemStack i = p.getInventory().getItemInMainHand();
		
		if (i.getType() == Material.AIR)
			throw new CommandException(sender, "You cannot enchant your hand!");
		
		final boolean canUnsafeEnchant = profile.isOwner();
		Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(args[0]));
		
		if (e == null)
			e = Enchantment.getByKey(Main.key(args[0]));
		if (e == null)
			throw new CommandException(sender, "Unknown enchantment '"+args[0]+"'");
		
		final int level = args.length > 1 ? toIntMinMax(sender, args[1] , 0, canUnsafeEnchant ? 127 : e.getMaxLevel()) : 1;
		
		if (level < 1) {
			i.removeEnchantment(e);
			BeanItem.formatItem(i);
			p.sendMessage(e.displayName(level).color(BeanColor.ENCHANT).append(Component.text("\u00a77 has been \u00a7cremoved\u00a77 from \u00a7f")).append(toHover(i)));
		} else {
			if (canUnsafeEnchant) {
				i.addUnsafeEnchantment(e, level);
			} else try {
				i.addEnchantment(e, level);
			} catch (Exception ee) {
				throw new CommandException(sender, e.displayName(level).color(BeanColor.ENCHANT).append(Component.text("\u00a7c cannot be added to \u00a7f")).append(toHover(i)));
			}
			BeanItem.formatItem(i);
			p.sendMessage(e.displayName(level).color(BeanColor.ENCHANT).append(Component.text("\u00a77 has been \u00a7aadded \u00a77to \u00a7f")).append(toHover(i)));
		}
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeObject(args[0], e -> ((Enchantment)e).getKey().getKey(), Enchantment.values());
		if (args.length == 2) {
			Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(args[0]));
			if (e == null || e.getMaxLevel() < 2)
				return Collections.emptyList();
			return TabCompleter.completeIntegerBetween(args[1], 1, e.getMaxLevel());
		}
		
		return Collections.emptyList();
	}

	final Component[] usageArguments = {
			Component.text("<enchantment>").hoverEvent(HoverEvent.showText(Component.text("The enchantment to be added to or removed from the item in your hand."))).color(NamedTextColor.GRAY),
			Component.text(" [level]").hoverEvent(HoverEvent.showText(Component.text("Optional: The level of the enchantment. Levels below 1 will remove the enchantment completely."))).color(NamedTextColor.GRAY)
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
	}

}
