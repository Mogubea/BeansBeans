package me.playground.command.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandCurse extends BeanCommand {
	
	private static Set<Player> cursedPlayers = new HashSet<Player>();
	
	public CommandCurse(Main plugin) {
		super(plugin, "bean.cmd.curse", true, 1, "curse");
		description = "Impending doom from the curse...";
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		Player target = toPlayer(sender, args[0], true);
		cursedPlayers.add(target);
		target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
		target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 1));
		target.damage(0);
		target.playSound(target.getLocation(), Sound.ENTITY_BAT_DEATH, 1, 1);
		target.sendMessage(Component.text("\u00a77You've been cursed..."));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player about to die."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}
	
	public static void performCurse() {
		cursedPlayers.forEach(player -> {
			player.setHealth(0);
			player.sendMessage(Component.text("\u00a77Remember that curse? Yeah.. it got ya."));
		});
		cursedPlayers.clear();
	}
	
}
