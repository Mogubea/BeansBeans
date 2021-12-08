package me.playground.command;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import me.playground.command.commands.CommandAnvil;
import me.playground.command.commands.CommandCivilization;
import me.playground.command.commands.CommandEnchant;
import me.playground.command.commands.CommandEnderchest;
import me.playground.command.commands.CommandGamemode;
import me.playground.command.commands.CommandHome;
import me.playground.command.commands.CommandHug;
import me.playground.command.commands.CommandI;
import me.playground.command.commands.CommandModify;
import me.playground.command.commands.CommandMoney;
import me.playground.command.commands.CommandOp;
import me.playground.command.commands.CommandPerform;
import me.playground.command.commands.CommandPickupfilter;
import me.playground.command.commands.CommandPlugin;
import me.playground.command.commands.CommandRandomTp;
import me.playground.command.commands.CommandRegion;
import me.playground.command.commands.CommandReport;
import me.playground.command.commands.CommandReturn;
import me.playground.command.commands.CommandSapphire;
import me.playground.command.commands.CommandSay;
import me.playground.command.commands.CommandSethome;
import me.playground.command.commands.CommandSun;
import me.playground.command.commands.CommandTeleport;
import me.playground.command.commands.CommandToCoord;
import me.playground.command.commands.CommandWarp;
import me.playground.command.commands.CommandWhisper;
import me.playground.command.commands.CommandWho;
import me.playground.command.commands.CommandWorkbench;
import me.playground.command.commands.CommandWorld;
import me.playground.command.commands.CommandWorldSpawn;
import me.playground.command.commands.small.CommandBestiary;
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

public class CommandManager {
	
	public List<String> disabledCommands = new ArrayList<String>();
	private List<Command> myCommands = new ArrayList<Command>();
	
	private final Main plugin;
	
	public Main getPlugin() {
		return plugin;
	}
	
	public CommandManager(Main plugin) {
		this.plugin = plugin;
		long mili = System.currentTimeMillis();
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
		registerCommand(new CommandBestiary(plugin));
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
		plugin.getLogger().info("Registered " + myCommands.size() + " commands in " + (System.currentTimeMillis()-mili) + "ms");
	}
	
	@SuppressWarnings("deprecation")
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
		command.setPermissionMessage("\u00a7cYou don't have permission to use /"+aliases[0]+".");
		command.setDescription(cmd.getDescription());
		command.setExecutor(cmd);
		Bukkit.getCommandMap().register(plugin.getDescription().getName(), command);
		myCommands.add(command);
	}
	
	public List<Command> getMyCommands() {
		return myCommands;
	}
	
	/*public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
	    final List<Method> methods = new ArrayList<Method>();
	    Class<?> klass = type;
	    while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
	        // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
	        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));       
	        for (final Method method : allMethods) {
	            if (method.isAnnotationPresent(annotation)) {
	                // TODO process annotInstance
	                methods.add(method);
	            }
	        }
	        // move to the upper class in the hierarchy in search for more methods
	        klass = klass.getSuperclass();
	    }
	    return methods;
	}*/
	
	/*private CommandMap getCommandMap() {
		CommandMap commandMap = null;
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	 
		return commandMap;
	}*/
	
}
