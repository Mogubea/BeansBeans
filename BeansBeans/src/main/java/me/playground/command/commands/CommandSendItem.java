package me.playground.command.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.BeanGuiInbox;
import me.playground.main.Main;
import me.playground.playerprofile.Delivery;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandSendItem extends BeanCommand {
	
	public CommandSendItem(Main plugin) {
		super(plugin, "bean.cmd.senditem", false, 1, "senditem");
		description = "Send an item to a player.";
		cooldown = 30;
	}
	
	private final String[] options = { "hotbar", "toprow", "middlerow", "bottomrow", "inventory" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player p = (Player) sender;
		ItemStack i = p.getEquipment().getItemInMainHand();
		
		if (/*!sender.hasPermission(this.getPermissionString() + ".*") && */i.getType() == Material.AIR)
			throw new CommandException(p, "You cannot send someone air!");
		
		PlayerProfile target = toProfile(sender, args[0]);
		if (target.isOnline()) {
			HashMap<Integer, ItemStack> remStacks = target.getPlayer().getInventory().addItem(i);
			if (remStacks.size() < 1) return true;
			Delivery.createItemDelivery(target.getId(), profile.getId(), "Item Package", "", remStacks.values().toArray(new ItemStack[remStacks.size()]));
			if (target.getBeanGui() instanceof BeanGuiInbox)
				target.getBeanGui().refresh();
		} else {
			Delivery.createItemDelivery(target.getId(), profile.getId(), "Item Package", "", i);
		}
		p.sendMessage(Component.text("\u00a77Successfully sent \u00a7f").append(toHover(i)).append(Component.text("\u00a77 to ")).append(target.getComponentName()).append(Component.text("\u00a77.")));
		p.getEquipment().setItemInMainHand(null, true);
		
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		if (args.length >= 2 && sender.hasPermission(this.getPermissionString() + ".*"))
			return TabCompleter.completeString(args[args.length - 1], options);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that's going to receive the item in your main hand."))).color(NamedTextColor.GRAY),
			Component.text("[slots]").hoverEvent(HoverEvent.showText(Component.text("If you wish to send multiple items, please specify the rows here."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		if (sender.hasPermission(this.getPermissionString() + ".*"))
			return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]);
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}
	
}
