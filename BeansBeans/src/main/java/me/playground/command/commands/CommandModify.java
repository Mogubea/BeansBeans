package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.celestia.logging.Celestia;
import me.playground.command.BeanCommand;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * TODO: update this command to be neater
 * @author Beandon
 *
 */
public class CommandModify extends BeanCommand {

	public CommandModify(Main plugin) {
		super(plugin, "bean.cmd.modify", true, 2, "editplayer", "ep", "modify");
		description = "Modify the attributes of a player's profile.";
	}

	final String[] subCmds = { "name", "colour", "addgroup", "removegroup", "flipsetting" };
	final String[] hexSuggestions = { "afafaf", "0x7faf7f", "0x51ef51" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = isPlayer(sender) ? (Player) sender : null;
		
		PlayerProfile pp = toProfile(p, args[0]);
		
		byte modify = -1;
		final String cmdStr = args[1];
		
		if ("name".equals(cmdStr) || "nick".equals(cmdStr) || "nickname".equals(cmdStr) || "displayname".equals(cmdStr)) {
			modify = 0;
		} else
		if ("color".equals(cmdStr) || "colour".equals(cmdStr) || "rgb".equals(cmdStr) || "namecolour".equals(cmdStr) || "namecolor".equals(cmdStr)) {
			modify = 1;
		} else
		if ("addgroup".equals(cmdStr) || "ag".equals(cmdStr)) {
			modify = 2;
		} else
		if ("removegroup".equals(cmdStr) || "rg".equals(cmdStr)) {
			modify = 3;
		} else
		if ("flipsetting".equals(cmdStr)) {
			modify = 4;
		}
		
		if (modify < 0) {
			sender.sendMessage(
					"\u00a7c\""+cmdStr+"\" is not a valid attribute to modify.\n" +
					"\u00a7cAttributes: \u00a7fnickname, namecolour, addgroup, removegroup, flipsetting");
			return true;
		}
		
		if (args.length == 2) {
			sender.sendMessage("\u00a7cPlease specify a value for attribute \""+cmdStr+"\".");
			return true;
		}
		
		final String value = args[2];
		
		switch(modify) {
			case 0:
				String old0 = pp.getDisplayName();
				pp.setNickname(value);
				Celestia.logModify(pp.getId(), "Changed %ID"+pp.getId()+"'s Name to " + value);
				sender.sendMessage(pp.getColouredName().append(Component.text("\u00a77's nickname has been updated from ").append(Component.text(old0).color(pp.getNameColour()))));
				break;
			case 1:
				try {
					TextColor old1 = pp.getNameColour();
					pp.setNameColour((int)Long.parseLong(value, 16));
					sender.sendMessage(pp.getColouredName().append(Component.text("\u00a77's colour has been updated from ").append(Component.text(pp.getDisplayName()).color(old1))));
				} catch (Exception e) {
					sender.sendMessage(Component.text("'"+value+"' is an invalid hex colour!", NamedTextColor.RED));
				}
				break;
			case 2:
				Rank rank0 = null;
				try {
					rank0 = Rank.fromString(value);
					if (rank0 == Rank.OWNER || rank0 == Rank.ADMINISTRATOR) {
						if (p instanceof Player) {
							sender.sendMessage("\u00a7cYou don't have permission to add this rank.");
							return true;
						}
					}
					
					if (!pp.isRank(rank0)) {
						pp.addRank(rank0);
						sender.sendMessage(pp.getComponentName().append(Component.text("\u00a77 is now a ")).append(rank0.toComponent()).append(Component.text("\u00a77!")));
						
						Player t = pp.getPlayer();
						if (t != null)
							t.sendMessage(Component.text("\u00a77You are now a ").append(rank0.toComponent()).append(Component.text("\u00a77!")));
					} else {
						sender.sendMessage(pp.getComponentName().append(Component.text("\u00a7c is already a ")).append(rank0.toComponent()).append(Component.text("\u00a7c!")));
					}
				} catch(Exception e) {
					sender.sendMessage("\u00a7cThe rank '"+value+"' doesn't exist!");
					return true;
				}
				break;
			case 3:
				Rank rank1 = Rank.fromString(value);
				if (rank1 != null) {
					if (rank1 == Rank.OWNER || rank1 == Rank.ADMINISTRATOR || rank1 == Rank.NEWBEAN) {
						if (isPlayer(sender) || rank1 == Rank.NEWBEAN) {
							p.sendMessage("\u00a7cYou don't have permission to remove this rank.");
							return true;
						}
					}
					
					if (pp.isRank(rank1)) {
						pp.removeRank(rank1);
						sender.sendMessage(pp.getComponentName().append(Component.text("\u00a77 is \u00a7cno longer \u00a77a ")).append(rank1.toComponent()).append(Component.text("\u00a77!")));
						
						Player t = pp.getPlayer();
						if (t != null)
							t.sendMessage(Component.text("\u00a77You are \u00a7cno longer \u00a77a ").append(rank1.toComponent()).append(Component.text("\u00a77!")));
					} else {
						sender.sendMessage(pp.getComponentName().append(Component.text("\u00a7c was never a ")).append(rank1.toComponent()).append(Component.text("\u00a7c!")));
					}
				} else {
					sender.sendMessage("\u00a7cThe rank '"+value+"' doesn't exist!");
				}
				break;
			case 4:
				try {
					PlayerSetting st = PlayerSetting.valueOf(value.toUpperCase());
					pp.flipSetting(st);
					sender.sendMessage(pp.getDisplayName() + "\u00a77's " + st.toString() + " flag is now " + (pp.isSettingEnabled(st) ? "\u00a7aenabled" : "\u00a7cdisabled"));
				} catch (Exception e) {
					sender.sendMessage("\u00a7cSetting \"" + value + "\" not found! Available settings: \n\u00a7f" + PlayerSetting.values().toString());
					return true;
				}
				break;
		}
		sender.sendMessage("\u00a77Profile updated!");
		
		return false;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeOnlinePlayer(sender, args[0]);
		if (args.length == 2)
			return TabCompleter.completeString(args[1], this.subCmds);
		if (args.length == 3 && args[1].equals("addgroup") || args[1].equals("removegroup") || args[1].equals("ag") || args[1].equals("rg"))
			return TabCompleter.completeEnum(args[2], Rank.class);
		if (args.length == 3 && args[1].equals("colour"))
			return TabCompleter.completeString(args[2], this.hexSuggestions);
		if (args.length == 3 && args[1].equals("addcoins"))
			return TabCompleter.completeInteger(args[2]);
		if (args.length == 3 && args[1].equals("flipsetting"))
			return TabCompleter.completeEnum(args[2], PlayerSetting.class);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<player>").hoverEvent(HoverEvent.showText(Component.text("The player that is going to be modified."))).color(NamedTextColor.GRAY),
			Component.text(" <attribute>").hoverEvent(HoverEvent.showText(Component.text("The attribute to be modified. Options: name, colour, addgroup, removegroup, addcoins, flipsetting"))).color(NamedTextColor.GRAY),
			Component.text(" <value>").hoverEvent(HoverEvent.showText(Component.text("The future value of the player's specified attribute."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]).append(usageArguments[1]).append(usageArguments[2]);
	}

}
