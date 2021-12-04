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
import org.bukkit.entity.Player;

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
	
	@SubCommand(canConsole = true, minArgs = 1, subCmds = {"list", "forceciv", "forcejob", "forcekick"})
	private void debug(PlayerProfile profile, CommandSender sender, String str, String[] args) {
		if (args[0].equalsIgnoreCase("list")) {
			for (Civilization c : Civilization.getCivilizations())
				sender.sendMessage(c.toComponent().append(Component.text(" ("+c.getCitizens().size()+" Members): " + c.getTreasury() + " Coins")));
		// Force into a Civilization with a specific role
		} else if (args[0].equalsIgnoreCase("forceciv")) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/civ "+args[0]+"\u00a77 <player> <civilization> [tier]");
			PlayerProfile target = toProfile(sender, args[1]);
			Civilization civ = toCivilization(sender, args[2]);
			if (target.isInCivilization()) target.getCivilization().kickCitizen(target.getId());
			CitizenTier tier = args.length > 3 ? CitizenTier.fromCmd(sender, args[3]) : CitizenTier.CITIZEN;
			civ.addCitizen(target.getId(), tier);
			sender.sendMessage(target.getComponentName().append(Component.text("\u00a77 is now part of ").append(civ.toComponent()).append(Component.text("\u00a77 as a "+tier.getNiceName()+"!"))));
		// Force a Job
		} else if (args[0].equalsIgnoreCase("forcejob")) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/civ "+args[0]+"\u00a77 <player> <job>");
			PlayerProfile target = toProfile(sender, args[1]);
			if (!target.isInCivilization()) throw new CommandException(sender, target.getComponentName().append(Component.text("\u00a7c is not in a Civilization!")));
			Job j = toJob(sender, args[2]);
			target.setJob(j, true);
			sender.sendMessage(target.getComponentName().append(Component.text("\u00a77's job has been set to ").append(j.toComponent()).append(Component.text("\u00a77!"))));
		// Force Kick
		} else if (args[0].equalsIgnoreCase("forcekick")) {
			if (args.length < 2) throw new CommandException(sender, "Usage: \u00a7f/civ "+args[0]+"\u00a77 <player>");
			PlayerProfile target = toProfile(sender, args[1]);
			if (!target.isInCivilization()) throw new CommandException(sender, target.getComponentName().append(Component.text("\u00a7c is not in a Civilization!")));
			Civilization civ = target.getCivilization();
			civ.kickCitizen(target.getId());
			sender.sendMessage(target.getComponentName().append(Component.text("\u00a77 was kicked from ").append(civ.toComponent()).append(Component.text("\u00a77!"))));
		}
	}
	
	@SubCommand(requiresCitizenship = true, citizenTier = CitizenTier.OFFICER)
	private void kick(PlayerProfile profile, CommandSender sender, String str, String[] args) {
		if (args.length < 1) throw new CommandException(sender, Component.text("\u00a7cSpecify a citizen to kick from ").append(profile.getCivilization().toComponent()).append(Component.text("\u00a7c.")));
		PlayerProfile target = toProfile(sender, args[0]);
		Civilization civ = profile.getCivilization();
		if (!target.isInCivilization() && target.getCivilization() != civ)
			throw new CommandException(sender, target.getComponentName().append(Component.text("\u00a7c is not a citizen of ").append(civ.toComponent()).append(Component.text("\u00a7c."))));
		if (civ.getCitizen(target.getId()).isOrAbove(civ.getCitizen(profile.getId())))
			throw new CommandException(sender, Component.text("\u00a7cYou cannot kick ").append(target.getComponentName()).append(Component.text("\u00a7c.")));
		civ.kickCitizen(target.getId());
	}
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 1) return false;
		final int size = methods.size();
		
		for (int x = -1; ++x < size;) {
			Method cmdMethod = methods.get(x);
			if (args[0].equalsIgnoreCase(cmdMethod.getName())) {
				SubCommand annotation = cmdMethod.getAnnotation(SubCommand.class);
				String name = cmdMethod.getName();
				
				if (annotation.requiresPermission() && !sender.hasPermission("bean.cmd.civilization."+name))
					throw new CommandException(sender, "You don't have permission to use /civ "+name+"!");
				if (!(sender instanceof Player) && !annotation.canConsole())
					throw new CommandException(sender, "You must be in-game to use /civ"+name+"!");
				if (annotation.requiresCitizenship()) {
					if ((!(sender instanceof Player) || !profile.isInCivilization()))
						throw new CommandException(sender, "You must be in a civilization to use /civ "+name+"!");
					if (profile.getCivilization().getCitizen(profile.getId()).isOrAbove(annotation.citizenTier()))
						throw new CommandException(sender, "You must be a "+annotation.citizenTier().name().toLowerCase()+" to use /civ "+name+"!");
				}
				if (annotation.minArgs() > args.length-1)
					throw new CommandException(sender, "Not enough arguments specified for /civ "+name+"!");
				
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
		if (args.length > 1) {
			int idx = subCmds.indexOf(args[0]);
			if (idx > -1) {
				SubCommand sc = methods.get(idx).getAnnotation(SubCommand.class);
				if (!sc.requiresPermission() || sender.hasPermission("bean.cmd.civilization."+methods.get(idx).getName())) {
					if (args.length == 2)
						return TabCompleter.completeString(args[1], sc.subCmds());
					if (args.length == 3) {
						if ("debug".equals(args[0]))
							if ("forceciv".equals(args[1]) || "forcejob".equals(args[1]) || "forcekick".equals(args[1]))
								return TabCompleter.completeOnlinePlayer(sender, args[2]);
					}
				}
			}
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
		String desc() default "";
		int minArgs() default 0;
		String[] subCmds() default {};
		boolean requiresPermission() default false;
		boolean canConsole() default false;
		boolean requiresCitizenship() default false;
		CitizenTier citizenTier() default CitizenTier.CITIZEN;
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
