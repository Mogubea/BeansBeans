package me.playground.command.commands.small;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.BeanGuiSkills;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.skills.Skill;
import me.playground.skills.Skills;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandSkills extends BeanCommand {
	
	private final DecimalFormat df = new DecimalFormat("#,###");
	
	public CommandSkills(Main plugin) {
		super(plugin, true, "skills");
		description = "A command shortcut for accessing your skills.";
	}
	
	final String[] args = {"givexp", "removexp", "gxp", "rxp", "setlevel", "sl"};
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		PlayerProfile prof = (args.length > 0 || !isPlayer(sender)) && sender.hasPermission("bean.cmd.skills.edit") ? toProfile(sender, args[0]) : null;
		if (prof != null) {
			if (args.length < 4)
				throw new CommandException(sender, "Usage:\u00a7f /"+str+" " + prof.getDisplayName() + "\u00a77<gxp/rxp/sl> <skill> <value>");
			
			String action = args[1].toLowerCase();
			Skill skill = Skill.getByName(args[2].toLowerCase());
			Skills skills = prof.getSkills();
			if (skill == null)
				throw new CommandException(sender, "Couldn't find skill '" + args[2] + "'");
			
			int value = toIntMinMax(sender, args[3], 1, Integer.MAX_VALUE);
			
			Component details = Component.text(skill.getName() + " XP", skill.getColour());
			Component msg;
			String oldGrade = skills.getSkillInfo(skill).getGrade();
			long oldXp = skills.getTotalExperience(skill);

			switch (action) {
				case "givexp", "gxp" -> {
					prof.getSkills().addExperience(skill, value);
					msg = Component.text("Given ", NamedTextColor.GRAY).append(details)
							.append(Component.text(" to ", NamedTextColor.GRAY)).append(prof.getComponentName()).append(Component.text(".", NamedTextColor.GRAY));
				}
				case "removexp", "rxp" -> {
					prof.getSkills().addExperience(skill, -value);
					msg = Component.text("Taken ", NamedTextColor.GRAY).append(details)
							.append(Component.text(" from ", NamedTextColor.GRAY)).append(prof.getComponentName()).append(Component.text(".", NamedTextColor.GRAY));
				}
				case "setlevel", "sl" -> {
					prof.getSkills().setLevel(skill, value);
					msg = Component.text("Set ", NamedTextColor.GRAY).append(prof.getComponentName()).append(Component.text("'s ", NamedTextColor.GRAY)).append(details)
							.append(Component.text(" to ", NamedTextColor.GRAY)).append(Component.text(skills.getSkillInfo(skill).getGrade(), NamedTextColor.WHITE))
							.append(Component.text(".", NamedTextColor.GRAY));
				}
				default -> {
					return true;
				}
			}
			
			sender.sendMessage(msg.hoverEvent(HoverEvent.showText(Component.text(skill.getName() + " Changes", skill.getColour())
					.append(Component.text("\n\u00a77Grade: \u00a77\u00a7l" + oldGrade + " \u00a78\u00a7l\u2192\u00a7f\u00a7l " + skills.getSkillInfo(skill).getGrade()))
					.append(Component.text("\n\u00a77XP: \u00a73" + df.format(oldXp) + " \u00a78\u00a7l\u2192\u00a7b " + df.format(skills.getTotalExperience(skill)))))));
			
			return true;
		}
		
		new BeanGuiSkills((Player)sender).openInventory();
		return true;
	}
	
	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (sender.hasPermission("bean.cmd.skills.edit")) {
			if (args.length == 1)
				return TabCompleter.completeOnlinePlayer(sender, args[0]);
			if (args.length == 2)
				return TabCompleter.completeString(args[1], this.args);
			if (args.length == 3)
				return TabCompleter.completeObject(args[2], (Skill::getName), Skill.getRegisteredSkills());
			if (args.length == 4)
				return TabCompleter.completeIntegerBetween(args[3], 1, Integer.MAX_VALUE);
		}
		
		return Collections.emptyList();
	}
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}
	
}
