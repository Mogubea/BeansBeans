package me.playground.command.commands.small;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.gui.BeanGuiNews;
import me.playground.gui.UpdateEntry;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandNews extends BeanCommand {
	
	public CommandNews(Main plugin) {
		super(plugin, true, "news", "updates", "announcements");
		description = "A command shortcut for viewing the update log!";
	}
	
	final String[] args = {"reload"};
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length > 0 && isRank(sender, Rank.ADMINISTRATOR) && args[0].equalsIgnoreCase("reload")) {
			UpdateEntry.reload();
			sender.sendMessage(Component.text("\u00a77The news has been updated."));
		} else if (isPlayer(sender)) {
			new BeanGuiNews((Player)sender).openInventory();
		}
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1 && isRank(sender, Rank.ADMINISTRATOR))
			return TabCompleter.completeString(args[0], this.args);
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
