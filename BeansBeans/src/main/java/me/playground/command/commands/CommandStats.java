package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.PlayerStats;
import me.playground.playerprofile.stats.StatType;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class CommandStats extends BeanCommand {
	
	private final List<StatType> types;
	private final String categoryHelp;
	
	/*private final LoadingCache<Pair<PlayerProfile, StatType>, Set<String>> statCache = CacheBuilder.from("maximumSize=50,expireAfterAccess=30s")
			.build(new CacheLoader<Pair<PlayerProfile, StatType>, Set<String>>() {
				public Set<String> load(Pair<PlayerProfile, StatType> key) throws Exception {
					return key.getFirst().getStats().getMap().get(key.getSecond()).keySet();
				}
			});
	*/
	
	public CommandStats(Main plugin) {
		super(plugin, "bean.cmd.stats", true, "stats");
		description = "View your stats!";
		StatType[] types = StatType.values();
		this.types = List.of(types);
		
		String lol = types.toString();
		categoryHelp = lol.substring(1, lol.length() - 1);
	}
	
	final String[] adminCmds = { "set", "add", "take" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		StatType selected = isPlayer(sender) && args.length > 0 ? StatType.fromString(args[0]) : null;
		PlayerProfile target = selected == null && args.length > 0 && sender.hasPermission("bean.cmd.stats.other") ? toProfile(sender, args[0]) : (isPlayer(sender) ? profile : null);
		if (target == null) throw new CommandException(sender, "Please specify a player!");
		int baseArg = selected == null ? 1 : 0;
		
		if (args.length < baseArg+1) {
			sender.sendMessage(Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/" + str).append(Component.text(" <category> <stat>"))));
			sender.sendMessage(Component.text("Categories: " + categoryHelp, TextColor.color(0xff7777)));
			return true;
		}
		
		if (selected == null) selected = StatType.fromString(args[baseArg]);
		if (selected == null) throw new CommandException(sender, "'" + args[baseArg] + "' is an invalid stat category!");
		if (args.length == baseArg+1) {
			sender.sendMessage(Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/" + str + " " + args[baseArg].toLowerCase()).append(Component.text(" <stat>"))));
			return true;
		}
		
		PlayerStats stats = target.getStats();
		int value = stats.getStat(selected, args[baseArg + 1]);
		
		sender.sendMessage(target.getComponentName().append(Component.text("'s ", NamedTextColor.GRAY)).append(
				Component.text(args[baseArg + 1].toLowerCase(), TextColor.color(0x777777))).append(Component.text(" value: ", NamedTextColor.GRAY))
				.append(Component.text(value, TextColor.color(0xffffcc))));
		return true;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			if (!isPlayer(sender))
				return TabCompleter.completeOnlinePlayer(sender, args[0]);
			return TabCompleter.completeEnum(args[0], StatType.class);
		} else if (args.length == 2) {
			if (StatType.fromString(args[0]) == null)
				return TabCompleter.completeEnum(args[0], StatType.class);
		}
		
		
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}

}
