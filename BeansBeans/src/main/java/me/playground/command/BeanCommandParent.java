package me.playground.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;

public abstract class BeanCommandParent extends BeanCommand {
	
	private final Map<String, Method> subCmds = new HashMap<>();
	private final int size;
	
	public BeanCommandParent(final Main plugin, String... aliases) {
		this(plugin, "", false, 1, aliases);
	}
	
	public BeanCommandParent(final Main plugin, boolean canConsoleRun, String... aliases) {
		this(plugin, "", canConsoleRun, 1, aliases);
	}
	
	public BeanCommandParent(final Main plugin, boolean canConsoleRun, int minArguments, String... aliases) {
		this(plugin, "", canConsoleRun, minArguments, aliases);
	}
	
	public BeanCommandParent(final Main plugin, String permissionString, boolean canConsoleRun, @Nonnull String... aliases) {
		this(plugin, permissionString, canConsoleRun, 1, aliases);
	}
	
	public BeanCommandParent(final Main plugin, String permissionString, boolean canConsoleRun, int minArguments, @Nonnull String... aliases) {
		super(plugin, permissionString, canConsoleRun, minArguments, aliases);
		
	    final List<Method> allMethods = new ArrayList<>(Arrays.asList(getClass().getDeclaredMethods()));
	    int size = allMethods.size();
	    for (int x = -1; ++x < size;) {
	    	final Method method = allMethods.get(x);
	    	if (method.isAnnotationPresent(SubCommand.class))
	    		subCmds.put(method.getName().toLowerCase(), method);
	    }

	    this.size = subCmds.size();
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 1) return false;
		
		String cmdName = args[0].toLowerCase();
		Method subCmd = subCmds.get(cmdName);
		if (subCmd == null) throw new CommandException(sender, "Sub-command /" + str + " " + cmdName + " does not exist.");

		String permissionString = subCmd.getAnnotation(SubCommand.class).permissionString();
		if (!permissionString.isEmpty() && !sender.hasPermission(permissionString))
			throw new CommandException(sender, "You don't have permission to use /" + str + " " + cmdName + ".");
		
		try {
			subCmd.invoke(this, profile, sender, Arrays.copyOfRange(args, 1, args.length));
		} catch (CommandException e) {
			e.notifySender();
			if (isPlayer(sender)) { // if an error during the command, remove the cooldown. TODO: make all of this more efficient.
				PlayerProfile pp = PlayerProfile.from(((Player)sender));
				if (pp.onCooldown("cmd." + cmd.getName()))
					pp.clearCooldown("cmd." + cmd.getName());
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			throw new CommandException(sender, "There was a problem executing this command.");
		}
		return true;
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (size < 1 || args.length != 1) return Collections.emptyList();
		final List<String> subs = new ArrayList<>();
		subCmds.forEach((string, method) -> {
			String permString = method.getAnnotation(SubCommand.class).permissionString();
			if (sender.hasPermission(permString)) 
				subs.add(string);
		});
		
		return subs;
	}
	
	public int getSize() {
		return size;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface SubCommand {
		String permissionString() default "";
	}
	
}
