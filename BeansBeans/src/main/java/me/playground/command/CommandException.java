package me.playground.command;

import org.bukkit.command.CommandSender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandException extends RuntimeException {
	private static final long serialVersionUID = 7387538523632048868L;
	private final CommandSender sender;
	private final Component c;
	
	public CommandException(CommandSender sender, String s) {
		super(s);
		this.sender = sender;
		this.c = Component.text(s).colorIfAbsent(NamedTextColor.RED);
	}
	
	public CommandException(CommandSender sender, Component c) {
		super("Component");
		this.sender = sender;
		this.c = c.colorIfAbsent(NamedTextColor.RED);
	}
	
	public CommandSender getCommandSender() {
		return sender;
	}
	
	public Component getComponentMessage() {
		return c;
	}
	
	public void notifySender() {
		sender.sendMessage(c);
	}
	
}
