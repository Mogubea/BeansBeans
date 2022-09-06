package me.playground.command.commands;

import javax.annotation.Nonnull;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommandParent;
import me.playground.command.CommandException;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Central reloading command
 */
public class CommandReload extends BeanCommandParent {
	
	public CommandReload(Main plugin) {
		super(plugin, "bean.cmd.reload", true, 1, "reload", "rl");
		description = "Reload the server or specific systems within the server.";
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.commands")
	public void commands(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		if (getPlugin().getServer().reloadCommandAliases())
			sender.sendMessage(Component.text("Reloaded commands.yml.", NamedTextColor.GREEN));
		else
			sender.sendMessage(Component.text("There was a problem reloading commands.yml.", NamedTextColor.RED));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.permissions")
	public void permissions(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		getPlugin().getServer().reloadPermissions();
		for (Player p : getPlugin().getServer().getOnlinePlayers())
			getPlugin().permissionManager().updatePlayerPermissions(p);
		sender.sendMessage(Component.text("Reloaded permissions.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.whitelist")
	public void whitelist(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		getPlugin().getServer().reloadWhitelist();
		sender.sendMessage(Component.text("Reloaded whitelist.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.regions")
	public void regions(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		getPlugin().regionManager().reload();
		sender.sendMessage(Component.text("Reloaded " + getPlugin().regionManager().countRegions() + " regions.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.shops")
	public void shops(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		boolean forceChunks = args.length > 0 && args[0].equalsIgnoreCase("-f");
		getPlugin().shopManager().reload(forceChunks);
		sender.sendMessage(Component.text((forceChunks ? "Force loaded chunks and reloaded " : "Reloaded ") + getPlugin().shopManager().countShops() + " shops.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.menushops")
	public void menushops(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		getPlugin().menuShopManager().reload();
		sender.sendMessage(Component.text("Reloaded " + getPlugin().menuShopManager().getPurchaseOptions().size() + " PurchaseOptions in Menu Shops.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.npcs")
	public void npcs(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		getPlugin().npcManager().reload();
		sender.sendMessage(Component.text("Reloaded " + getPlugin().npcManager().getAllNPCs().size() + " NPCs.", NamedTextColor.GREEN));
	}
	
	@SubCommand(permissionString = "bean.cmd.reload.all")
	public void all(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull String[] args) {
		if (!getPlugin().isDebugMode()) throw new CommandException(sender, "Full reloads are disabled on the live server.");
		sender.sendMessage(Component.text("Attempting to reload the server...", NamedTextColor.GRAY));
		getPlugin().getServer().reload();
		sender.sendMessage(Component.text("The server has been reloaded.", NamedTextColor.GREEN));
	}
	
	final Component[] usageArguments = {
			Component.text(" [system]").hoverEvent(HoverEvent.showText(Component.text("The desired system to reload."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str).append(usageArguments[0]);
	}
	
}
