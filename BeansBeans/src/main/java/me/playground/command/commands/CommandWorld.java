package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.utils.TabCompleter;
import me.playground.worlds.WorldManager;
import net.kyori.adventure.text.Component;

public class CommandWorld extends BeanCommand {
	
	private final WorldManager wm;
	
	public CommandWorld(Main plugin) {
		super(plugin, true, "bean.cmd.world", "world");
		description = "General world command.";
		wm = plugin.getWorldManager();
	}
	
	final String[] subCmds = { "create", "close", "dbregister", "info", "list", "open", "tpto",  };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length > 0) {
			final String subCmd = args[0].toLowerCase();
			
			if (subCmd.equals("create") && checkSubPerm(sender, "create")) {
				if (args.length > 1) {
					final WorldCreator wc = new WorldCreator(args[1]);
					wc.type(args.length > 2 ? WorldType.valueOf(args[2].toUpperCase()) : WorldType.NORMAL);
					wc.environment(args.length > 3 ? Environment.valueOf(args[3].toUpperCase()) : Environment.NORMAL);
					//ChunkGenerator cg = wc.generator();
					final World w = wc.createWorld();
					w.setGameRule(GameRule.DISABLE_RAIDS, true);
					w.setGameRule(GameRule.KEEP_INVENTORY, true);
					w.setGameRule(GameRule.MOB_GRIEFING, true);
					w.getWorldBorder().setSize(args.length > 4 ? Integer.parseInt(args[4]) : 20000);
					
					wm.registerWorld(w);
					sender.sendMessage(Component.text("\u00a77Created and registered ").append(worldInfo(sender, w)).append(Component.text("\u00a77 to the database.")));
				}
			} else if (subCmd.equals("close") || subCmd.equals("open")) {
				boolean closeIt = subCmd.equals("close");
				World w = null;
				
				if (isPlayer(sender))
					w = args.length > 1 ? toWorld(sender, args[1]) : ((Player)sender).getWorld();
				else if (args.length > 1)
					w = toWorld(sender, args[1]);
				else
					throw new CommandException(sender, "Please specify a world.");
				
				final Region worldRegion = getPlugin().regionManager().getWorldRegion(w);
				worldRegion.setFlag(Flags.TELEPORT_IN, closeIt ? false : true);
				sender.sendMessage(worldInfo(sender,w).append(Component.text("\u00a77 is now " + (!closeIt ? "\u00a7aopen" : "\u00a7cclosed"))));
			} else if (subCmd.equals("info")) {
				World w = null;
				
				if (isPlayer(sender))
					w = args.length > 1 ? toWorld(sender, args[1]) : ((Player)sender).getWorld();
				else if (args.length > 1)
					w = toWorld(sender, args[1]);
				else
					throw new CommandException(sender, "Please specify a world.");
				
				sender.sendMessage(Component.text("Soon to be information about ").append(worldInfo(sender, w)));
			} else if (subCmd.equals("list")) {
				sender.sendMessage(Component.text("\u00a77Worlds: "));
				for (World w : Bukkit.getWorlds())
					sender.sendMessage(Component.text("\u00a77 - ").append(worldInfo(sender, w)));
			} else if ((subCmd.equals("tpto") || subCmd.equals("warpto") || subCmd.equals("goto") || subCmd.equals("tp")) && checkPlayer(sender)) {
				if (args.length > 1) {
					final Player p = (Player) sender;
					final World w = toWorld(p, args[1]);
					if (p.teleport(w.getSpawnLocation()))
						p.sendMessage(Component.text("\u00a77Successfully teleported to world: ").append(worldInfo(p, w)));
				}
			} else if (subCmd.equals("dbregister") && checkRank(sender, Rank.OWNER)) {
				if (args.length > 1) {
					final World w = toWorld(sender, args[1]);
					wm.registerWorld(w);
					sender.sendMessage(Component.text("\u00a77Registered ").append(worldInfo(sender, w)).append(Component.text("\u00a77 to the database.")));
				}
			}
		}
		
		
		return true;
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], this.subCmds);
		if (!(args[0].equals("create")) && args.length == 2)
			return TabCompleter.completeString(args[1], TabCompleter.completeObject(args[1], World::getName, Bukkit.getWorlds()));
		if (args[0].equals("create")) {
			if (args.length == 3)
				return TabCompleter.completeEnum(args[2], WorldType.class);
			if (args.length == 4)
				return TabCompleter.completeEnum(args[3], Environment.class);
			if (args.length == 5)
				return TabCompleter.completeIntegerBetween(args[4], 1, 100000);
		}
			
		
		return Collections.emptyList();
	}
}
