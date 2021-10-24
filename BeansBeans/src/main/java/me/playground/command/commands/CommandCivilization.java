package me.playground.command.commands;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.playground.civilizations.CitizenTier;
import me.playground.civilizations.Civilization;
import me.playground.civilizations.jobs.Job;
import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;

public class CommandCivilization extends BeanCommand {
	final private List<String> subCmds = new ArrayList<String>();
	private List<Method> methods = getMethodsAnnotatedWith(CommandCivilization.class, SubCommand.class);
	
	public CommandCivilization(Main plugin) {
		super(plugin, "bean.cmd.civilization", false, "civilization", "civ");
		this.description = "The general civilization command.";
	}
	
	@SubCommand(aliases = "debug", permission = "bean.cmd.civilization.debug", canConsole = true, minArgs = 1, subCmds = {"list", "forceciv", "forcejob"})
	private void debug(PlayerProfile profile, CommandSender sender, String str, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			for (Civilization c : Civilization.getCivilizations())
				sender.sendMessage(c.toComponent().append(Component.text(" ("+c.getCitizens().size()+" Members): " + c.getTreasury() + " Coins")));
		} else if (args[0].equalsIgnoreCase("forceciv")) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/"+str+" "+args[0]+"\u00a77 <player> <civilization> [tier]");
			PlayerProfile pp = toProfile(sender, args[1]);
			Civilization civ = toCivilization(sender, args[2]);
			if (pp.isInCivilization()) pp.getCivilization().kickCitizen(pp.getId());
			civ.addCitizen(pp.getId());
			sender.sendMessage(pp.getComponentName().append(Component.text("\u00a77's is now part of ").append(civ.toComponent()).append(Component.text("\u00a77!"))));
		} else if (args[0].equalsIgnoreCase("forcejob")) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/"+str+" "+args[0]+"\u00a77 <player> <job>");
			PlayerProfile pp = toProfile(sender, args[1]);
			if (!pp.isInCivilization()) throw new CommandException(sender, pp.getComponentName().append(Component.text("\u00a7c is not in a Civilization!")));
			Job j = toJob(sender, args[2]);
			pp.setJob(j, true);
			sender.sendMessage(pp.getComponentName().append(Component.text("\u00a77's job has been set to ").append(j.toComponent()).append(Component.text("\u00a77!"))));
		}
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 1) return false;
		final int size = methods.size();
		
		for (int x = -1; ++x < size;) {
			Method cmdMethod = methods.get(x);
			if (args[0].equalsIgnoreCase(cmdMethod.getName())) {
				SubCommand annotation = cmdMethod.getAnnotation(SubCommand.class);
				
				final String permission = annotation.permission();
				if (!permission.isEmpty() && !sender.hasPermission(permission))
					throw new CommandException(sender, "You don't have permission to use /"+str+" "+cmdMethod.getName()+"!");
				if (annotation.minArgs() > args.length-1)
					throw new CommandException(sender, "Not enough arguments specified for /"+str+" "+cmdMethod.getName()+"!");
				
				
				String[] subArgs = new String[args.length-1];
				int ysize = subArgs.length;
				for (int y = -1; ++y < ysize;)
					subArgs[y] = args[y+1];
				
				Object[] methodArgs = new Object[] { profile, sender, args[0], subArgs };
				try {
					cmdMethod.invoke(this, methodArgs);
					return true;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					if (e.getCause() instanceof CommandException)
						((CommandException) e.getCause()).notifySender();
					else
						e.printStackTrace();
				} 
			}
		}
		return false;
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], subCmds);
		if (args.length == 2) {
			int idx = subCmds.indexOf(args[0]);
			if (idx > -1)
				return TabCompleter.completeString(args[1], methods.get(idx).getAnnotation(SubCommand.class).subCmds());
		}
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}
	
	protected Job toJob(CommandSender sender, String s) {
		Job job = Job.getByName(s);
		if (job == null)
			throw new CommandException(sender, "Couldn't find job '"+s+"'");
		return job;
	}
	
	protected Civilization toCivilization(CommandSender sender, String s) {
		Civilization civ = Civilization.getByName(s);
		if (civ == null)
			throw new CommandException(sender, "Couldn't find civilization '"+s+"'");
		return civ;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	private @interface SubCommand {
		String[] aliases();
		String desc() default "";
		String permission() default "";
		int minArgs() default 0;
		String[] subCmds() default {};
		boolean canConsole() default false;
		CitizenTier tier() default CitizenTier.CITIZEN;
	}
	
	private List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
	    final List<Method> methods = new ArrayList<Method>();
	    Class<?> klass = type;
	    while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
	        // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
	        final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));       
	        for (final Method method : allMethods) {
	            if (method.isAnnotationPresent(annotation)) {
	                methods.add(method);
	                subCmds.add(method.getName());
	            }
	        }
	        // move to the upper class in the hierarchy in search for more methods
	        klass = klass.getSuperclass();
	    }
	    return methods;
	}
	
}
