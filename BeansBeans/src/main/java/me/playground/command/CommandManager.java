package me.playground.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.playground.command.commands.CommandAFK;
import me.playground.command.commands.CommandAnvil;
import me.playground.command.commands.CommandCivilization;
import me.playground.command.commands.CommandCurse;
import me.playground.command.commands.CommandEnchant;
import me.playground.command.commands.CommandEnderchest;
import me.playground.command.commands.CommandGamemode;
import me.playground.command.commands.CommandHat;
import me.playground.command.commands.CommandHeal;
import me.playground.command.commands.CommandHide;
import me.playground.command.commands.CommandHome;
import me.playground.command.commands.CommandHug;
import me.playground.command.commands.CommandI;
import me.playground.command.commands.CommandInvsee;
import me.playground.command.commands.CommandModify;
import me.playground.command.commands.CommandMoney;
import me.playground.command.commands.CommandOp;
import me.playground.command.commands.CommandPerform;
import me.playground.command.commands.CommandPickupfilter;
import me.playground.command.commands.CommandPlugin;
import me.playground.command.commands.CommandRandomTp;
import me.playground.command.commands.CommandRegion;
import me.playground.command.commands.CommandReload;
import me.playground.command.commands.CommandReport;
import me.playground.command.commands.CommandReturn;
import me.playground.command.commands.CommandSapphire;
import me.playground.command.commands.CommandSay;
import me.playground.command.commands.CommandSendItem;
import me.playground.command.commands.CommandSethome;
import me.playground.command.commands.CommandSkull;
import me.playground.command.commands.CommandStats;
import me.playground.command.commands.CommandSun;
import me.playground.command.commands.CommandTeleport;
import me.playground.command.commands.CommandToCoord;
import me.playground.command.commands.CommandWarp;
import me.playground.command.commands.CommandWhisper;
import me.playground.command.commands.CommandWho;
import me.playground.command.commands.CommandWorkbench;
import me.playground.command.commands.CommandWorld;
import me.playground.command.commands.CommandWorldSpawn;
import me.playground.command.commands.small.CommandCSpawn;
import me.playground.command.commands.small.CommandFly;
import me.playground.command.commands.small.CommandGod;
import me.playground.command.commands.small.CommandHeirloom;
import me.playground.command.commands.small.CommandLoot;
import me.playground.command.commands.small.CommandMenu;
import me.playground.command.commands.small.CommandNews;
import me.playground.command.commands.small.CommandPlebeian;
import me.playground.command.commands.small.CommandSkills;
import me.playground.command.commands.small.CommandSpawn;
import me.playground.command.commands.small.CommandTpblock;
import me.playground.command.commands.small.CommandVote;
import me.playground.command.commands.small.CommandWardrobe;
import me.playground.command.commands.small.CommandWarps;
import me.playground.main.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandManager {
	
	public List<String> disabledCommands = new ArrayList<String>();
	private List<Command> myCommands = new ArrayList<Command>();
	
	private final Main plugin;
	private final Commodore commodore;
	
	public Main getPlugin() {
		return plugin;
	}
	
	public CommandManager(Main plugin) {
		this.plugin = plugin;
		this.commodore = CommodoreProvider.getCommodore(plugin);
		long mili = System.currentTimeMillis();
		unregisterAnnoyingBukkit();
		
		// Warping Commands
		registerCommand(new CommandHome(plugin));
		registerCommand(new CommandSethome(plugin));
		registerCommand(new CommandReturn(plugin));
		registerCommand(new CommandWarp(plugin));
		registerCommand(new CommandTeleport(plugin));
		registerCommand(new CommandRandomTp(plugin));
		registerCommand(new CommandToCoord(plugin));
		// Shortcut Commands
		registerCommand(new CommandSkills(plugin));
		registerCommand(new CommandTpblock(plugin));
		registerCommand(new CommandPickupfilter(plugin));
		registerCommand(new CommandWardrobe(plugin));
		registerCommand(new CommandNews(plugin));
		registerCommand(new CommandWarps(plugin));
		registerCommand(new CommandFly(plugin));
		registerCommand(new CommandGod(plugin));
		registerCommand(new CommandSpawn(plugin));
		registerCommand(new CommandCSpawn(plugin));
		registerCommand(new CommandMenu(plugin));
		registerCommand(new CommandEnderchest(plugin));
		registerCommand(new CommandHeirloom(plugin));
//		registerCommand(new CommandBestiary(plugin));
		registerCommand(new CommandLoot(plugin));
		registerCommand(new CommandVote(plugin));
		
		// Other Commands
		registerCommand(new CommandWho(plugin));
		registerCommand(new CommandRegion(plugin));
		registerCommand(new CommandMoney(plugin));
		registerCommand(new CommandSapphire(plugin));
		registerCommand(new CommandOp(plugin));
		registerCommand(new CommandModify(plugin));
		registerCommand(new CommandEnchant(plugin));
		registerCommand(new CommandI(plugin));
		registerCommand(new CommandWhisper(plugin));
		registerCommand(new CommandPlugin(plugin));
		registerCommand(new CommandWorld(plugin));
		registerCommand(new CommandSay(plugin));
		registerCommand(new CommandGamemode(plugin));
		registerCommand(new CommandPerform(plugin));
		registerCommand(new CommandSun(plugin));
		registerCommand(new CommandHug(plugin));
		registerCommand(new CommandReport(plugin));
		registerCommand(new CommandWorldSpawn(plugin));
		registerCommand(new CommandCivilization(plugin));
		registerCommand(new CommandPlebeian(plugin));
		registerCommand(new CommandWorkbench(plugin));
		registerCommand(new CommandAnvil(plugin));
		registerCommand(new CommandHeal(plugin));
		registerCommand(new CommandCurse(plugin));
		registerCommand(new CommandAFK(plugin));
		registerCommand(new CommandSendItem(plugin));
		registerCommand(new CommandHat(plugin));
		registerCommand(new CommandSkull(plugin));
		registerCommand(new CommandReload(plugin));
		registerCommand(new CommandStats(plugin));
		registerCommand(new CommandInvsee(plugin));
		registerCommand(new CommandHide(plugin));
		
		plugin.getLogger().info("Registered " + myCommands.size() + " commands in " + (System.currentTimeMillis()-mili) + "ms");
	}
	
	/**
	 * Unregisters /reload, /rl, /pl and /plugins as they get in the way of my own commands.
	 */
	private void unregisterAnnoyingBukkit() {
		SimplePluginManager spm = (SimplePluginManager) getPlugin().getServer().getPluginManager();
		Field commandMapField = null;
		try {
			commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			
			SimpleCommandMap scm = (SimpleCommandMap) commandMapField.get(spm);
			scm.getKnownCommands().remove("reload");
			scm.getKnownCommands().remove("rl");
			scm.getKnownCommands().remove("pl");
			scm.getKnownCommands().remove("plugins");
			commandMapField.setAccessible(false); 
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void registerCommand(BeanCommand cmd) {
		String[] aliases = cmd.getAliases();
		PluginCommand command = null;
		
		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
			command = c.newInstance(aliases[0], plugin);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		command.setAliases(Arrays.asList(aliases));
		command.setPermission(cmd.getPermissionString());
		command.permissionMessage(Component.text("You don't have permission to use ", NamedTextColor.RED).append(cmd.toComponent()).append(Component.text(".", NamedTextColor.RED)));
		command.setDescription(cmd.getDescription());
		command.setExecutor(cmd);
		Bukkit.getCommandMap().register(plugin.getDescription().getName(), command);
		
		if (cmd instanceof ICommodore)
			commodore.register(((ICommodore)cmd).getCommodore());
		
		myCommands.add(command);
	}
	
	public void unregisterCommands() {
		int size = getMyCommands().size();
		for (int x = -1; ++x < size;)
			myCommands.get(x).unregister(Bukkit.getCommandMap());
	}
	
	public List<Command> getMyCommands() {
		return myCommands;
	}
	
}
