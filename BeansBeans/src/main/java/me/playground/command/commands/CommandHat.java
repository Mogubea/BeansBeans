package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class CommandHat extends BeanCommand {
	
	public CommandHat(Main plugin) {
		super(plugin, "bean.cmd.hat", false, "hat");
		description = "Put an item on your head!";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player)sender;
		final ItemStack hand = p.getInventory().getItemInMainHand();
		
		if (hand.getType() == Material.AIR) throw new CommandException(sender, "You cannot equip air!");
		//if (hand.getItemMeta().hasAttributeModifiers() && !(hand.getType().name().endsWith("HELMET"))) throw new CommandException(sender, "You cannot equip that to your head!");
		
		final ItemStack head = p.getInventory().getHelmet();
		
		p.getInventory().setHelmet(hand);
		p.getInventory().setItemInMainHand(head);
		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.25F, 0.7F + getRandom().nextFloat()/2F);
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
