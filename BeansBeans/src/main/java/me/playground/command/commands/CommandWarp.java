package me.playground.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.playerprofile.ProfileStore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.BeanGuiConfirm;
import me.playground.gui.BeanGuiWarps;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.TabCompleter;
import me.playground.warps.Warp;
import me.playground.warps.WarpManager;
import me.playground.warps.WarpType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public class CommandWarp extends BeanCommand {

	final WarpManager wm;
	
	public CommandWarp(Main plugin) {
		super(plugin, false, "warp");
		wm = plugin.warpManager();
	}
	
	final List<String> bannedWarpNames = new ArrayList<>(Arrays.asList("create", "delete", "rename", "reload", "remove", "settype", "public", "private", "lock", "relocate", "invite", "uninvite", "give", "list", "home"));
	
	final List<String> subCmds = Arrays.asList("create", "delete", "give", "home", "invite", "list", "private", "public", "reload", "relocate", "rename", "settype", "uninvite");
	final String[] warpTypeArgs = { "normal", "shop" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		
		if (args.length < 1) {
			new BeanGuiWarps(p).openInventory();
			return true;
		}
		
		final String subcmd = args[0].toLowerCase();
		final String warpname = args.length < 2 ? args[0] : args[1];
		final int creationCost = 75;
		final Warp warp = ("create".equals(subcmd) || (args.length == 1 && bannedWarpNames.contains(subcmd))) ? null : toWarp(p, warpname);
		
		if ("reload".equals(subcmd) && checkRank(sender, Rank.OWNER)) {
			wm.reload();
			sender.sendMessage("\u00a7aSaved and reloaded "+wm.countWarps()+" warps.");
		} else if ("create".equals(subcmd) && noGM(sender, GameMode.SPECTATOR)) { // CREATE WARP
			if (profile.getWarpCount() >= profile.getWarpLimit())
				throw new CommandException(sender, "\u00a7cYou have too many warps (\u00a7d"+profile.getWarpCount()+"\u00a7c/\u00a75"+profile.getWarpLimit()+"\u00a7c)!");
			if (args.length < 2) 
				throw new CommandException(sender, "Usage: \u00a7f/warp create \u00a77<warp name>");
			if (!profile.hasPermission("bean.cmd.warp.*") && profile.getBalance() < creationCost)
				throw new CommandException(sender, "\u00a7cYou need another \u00a76" + (creationCost-profile.getBalance()) + " Coins \u00a7cto do this!");
			if (wm.doesWarpExist(warpname))
				throw new CommandException(sender, Component.text("The warp ").append(toWarp(p, warpname).toComponent()).append(Component.text("\u00a7r already exists!")));
			if (!StringUtils.isAlphanumeric(warpname) || warpname.length() > 32 || bannedWarpNames.contains(warpname.toLowerCase()))
				throw new CommandException(sender, "\u00a7cCannot create a warp with the name '"+warpname+"'!");
			
			final Region r = getPlugin().regionManager().getRegion(p.getLocation());
			if (!isSafe(p.getLocation()))
				throw new CommandException(sender, "\u00a7cYou can't create a warp here!");
			if (!r.getEffectiveFlag(Flags.WARP_CREATION) && r.getMember(p).lowerThan(MemberLevel.MEMBER))
				throw new CommandException(sender, "\u00a7cYou don't have permission to create warps here!");
			
			Warp newWarp = null;
			try {
				newWarp = wm.createWarp(profile.getId(), warpname, p.getLocation());
				if (!profile.hasPermission("bean.cmd.warp.*"))
					profile.addToBalance(-creationCost, "Created a warp with the name '" + warpname + "'");
				profile.addOwnedWarp(warp);
			} catch (Throwable e) {
				p.sendMessage("\u00a7cThere was a problem creating your warp!");
			}
			p.sendMessage(Component.text("\u00a77Successfully created the warp: ").append(newWarp.toComponent()).append(Component.text("\u00a77 (\u00a7d"+profile.getWarpCount()+"\u00a77/"+"\u00a75"+profile.getWarpLimit()+"\u00a77)!")));
		} else if ("delete".equals(subcmd)) {
			if (args.length < 2) throw new CommandException(sender, "Usage: \u00a7f/warp delete \u00a77<warp>");
			canDo(sender, warp, "delete");
			
			final boolean isOwner = warp.isOwner(p);
				
			final BeanGuiConfirm confirmation = new BeanGuiConfirm(p,
					Arrays.asList(
							Component.text("\u00a77Confirming will delete the warp: ").append(warp.toComponent()),
							isOwner ? Component.text("\u00a78(Owned by You)") : Component.text("\u00a78(Owned by ").append(PlayerProfile.getDisplayName(warp.getOwnerId()).append(Component.text("\u00a78)"))),
							Component.empty(),
							Component.text("\u00a7cThis action cannot be undone!"))) {
				public void onAccept() {
					String name = warp.getName();
					int ownerId = warp.getOwnerId();
					if (warp.delete()) {
						if (ProfileStore.from(ownerId).isOnline())
							PlayerProfile.fromIfExists(ownerId).removeOwnedWarp(warp);
						p.sendMessage(Component.text("\u00a77Successfully deleted \u00a7d" + name));
					}
					p.closeInventory();
				}

				public void onDecline() {
					p.closeInventory();
				}
			};
				
			confirmation.openInventory();
		} else if ("public".equals(subcmd)) {
			if (args.length < 2) throw new CommandException(sender, "Usage: \u00a7f/warp public \u00a77<warp>");
			canDo(sender, warp, "publicize");
			
			warp.setPublic(true);
			sender.sendMessage(warp.toComponent().append(Component.text("\u00a77 is now \u00a7dpublic\u00a77.")));
		} else if ("private".equals(subcmd)) {
			if (args.length < 2) throw new CommandException(sender, "Usage: \u00a7f/warp private \u00a77<warp>");
			canDo(sender, warp, "privatize");
			
			warp.setPublic(false);
			sender.sendMessage(warp.toComponent().append(Component.text("\u00a77 is now \u00a75private\u00a77.")));
		} else if ("invite".equals(subcmd)) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/warp invite " + (warp!=null ? args[1] : "\u00a77<warp>") + "\u00a77 [players]");
			canDo(sender, warp, "invite people to");

			for (int x = 2; x < args.length; x++) {
				int id = toId(p, args[x]);
				if (warp.isInvited(id)) {
					p.sendMessage(PlayerProfile.getDisplayName(id).append(Component.text("\u00a7c is already invited to ")).append(warp.toComponent()));
				} else {
					warp.invitePlayer(id);
					p.sendMessage(Component.text("\u00a77Successfully invited ").append(PlayerProfile.getDisplayName(id)).append(Component.text("\u00a77 to ")).append(warp.toComponent()));
				}
			}
		} else if ("uninvite".equals(subcmd)) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/warp uninvite " + (warp!=null ? args[1] : "\u00a77<warp>") + "\u00a77 [players]");
			canDo(sender, warp, "uninvite people from");

			for (int x = 2; x < args.length; x++) {
				int id = toId(p, args[x]);

				if (warp.uninvitePlayer(id)) {
					p.sendMessage(Component.text("\u00a77Successfully uninvited ").append(PlayerProfile.getDisplayName(id)).append(Component.text("\u00a77 from ")).append(warp.toComponent()));
				} else {
					p.sendMessage(PlayerProfile.getDisplayName(id).append(Component.text("\u00a7c was never invited to ")).append(warp.toComponent()));
				}
			}
		} else if ("rename".equals(subcmd)) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/warp rename " + (warp!=null ? args[1] : "\u00a77<warp>") + "\u00a77 <new name>");
			canDo(sender, warp, "rename");
			
			if (!StringUtils.isAlphanumeric(args[2]) || args[2].length() > 32 || bannedWarpNames.contains(args[2].toLowerCase()))
				throw new CommandException(sender, "\u00a7cYou can't rename your warp to '"+args[2]+"'!");
			
			Warp existingWarp = wm.getWarp(args[2]);
			if (existingWarp != null)
				throw new CommandException(sender, Component.text("The warp ").append(existingWarp.toComponent()).append(Component.text("\u00a7r already exists!")));
			
			String oldName = warp.getName();
			
			warp.rename(args[2]);
			p.sendMessage(Component.text("\u00a77Renamed \u00a7d" + oldName + "\u00a77 to ").append(warp.toComponent()));
		} else if ("relocate".equals(subcmd) && noGM(sender, GameMode.SPECTATOR)) {
			if (args.length < 2) throw new CommandException(sender, "Usage: \u00a7f/warp relocate \u00a77<warp>");
			canDo(sender, warp, "relocate");
			
			if (!profile.hasPermission("bean.cmd.warp.*") && profile.getBalance() < creationCost)
				throw new CommandException(sender, "\u00a7cYou need another \u00a76" + (creationCost-profile.getBalance()) + " Coins \u00a7cto do this!");
			
			final Region r = getPlugin().regionManager().getRegion(p.getLocation());
			if (!isSafe(p.getLocation()) || (!r.getEffectiveFlag(Flags.WARP_CREATION) && r.getMember(p).lowerThan(MemberLevel.MEMBER)))
				throw new CommandException(sender, "\u00a7cYou can't redesignate your warp location to here.");
			
			warp.setLocation(p.getLocation());
			p.sendMessage(Component.text("\u00a77Relocated ").append(warp.toComponent()).append(Component.text("\u00a77 to your current location.")));
		} else if ("home".equals(subcmd)) {
			profile.clearCooldown("cmd");
			p.performCommand("home");
		} else if ("list".equals(subcmd)) {
			new BeanGuiWarps(p).openInventory();
			return true;
		} else if ("settype".equals(subcmd)) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/warp settype " + (warp!=null ? args[1] : "\u00a77<warp>") + "\u00a77 <type>");
			canDo(sender, warp, "set the type of");
			
			WarpType type = WarpType.PLAYER;
			if (args[2].equalsIgnoreCase("shop"))
				type = WarpType.SHOP;
			if (args[2].equalsIgnoreCase("server") && sender.hasPermission("bean.cmd.warp.*"))
				type = WarpType.SERVER;
			
			warp.setType(type);
			p.sendMessage(warp.toComponent().append(Component.text("\u00a77 is now a " + warp.getType().getName())));
		} else if ("give".equals(subcmd) && sender.hasPermission("bean.cmd.warp.*")) {
			if (args.length < 3) throw new CommandException(sender, "Usage: \u00a7f/warp give " + (warp!=null ? args[1] : "\u00a77<warp>") + "\u00a77 <player>");
			int id = toId(p, args[2]);
			warp.setOwnerId(id);
			p.sendMessage(Component.text("\u00a77Given ").append(warp.toComponent()).append(Component.text("\u00a77 to ")).append(PlayerProfile.getDisplayName(id)));
		} else { // find warp
			if (p.hasPermission("bean.cmd.warp.*") || p.hasPermission("bean.cmd.warp.others") || warp.isOwner(p) || warp.isPublic() || warp.isInvited(p))
				warp.warp(p);
			else
				throw new CommandException(sender, Component.text("\u00a7cYou can't warp to ").append(warp.toComponent()).append(Component.text("\u00a7c.")));
		}
		
		if (warp != null)
			profile.addToRecentWarps(warp.getName());
		
		return true;
	}
	
	private int toId(Player p, String name) {
		try {
			int id = PlayerProfile.getDBID(name);
			return id;
		} catch (Exception e) {
			throw new CommandException(p, "Couldn't find player '"+name+"'");
		}
	}
	
	private Warp toWarp(CommandSender sender, String s) {
		Warp w = wm.getWarp(s);
		if (w == null)
			throw new CommandException(sender, "Couldn't find warp '"+s+"'");
		return w;
	}
	
	private boolean canDo(CommandSender sender, Warp warp, String err) {
		boolean can = (!isPlayer(sender) || (warp.isOwner(((Player) sender)) || sender.hasPermission("bean.cmd.warp.*") || sender.hasPermission("bean.cmd.warp.others")));
		if (!can)
			throw new CommandException(sender, Component.text("\u00a7cYou can't " + err + " ").append(warp.toComponent()).append(Component.text("\u00a7c.")));
		return true;
	}

	private List<String> getWarpSuggestions(PlayerProfile profile) {
		List<String> warps = profile.recentWarps();
		profile.getOwnedWarps().keySet().forEach(name -> {
			if (!warps.contains(name)) warps.add(name);
		});
		return warps;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1) {
			List<String> ack = TabCompleter.completeString(args[0], this.subCmds);
			if (!sender.hasPermission("bean.cmd.warp.*")) {
				ack.remove("give");
				ack.remove("reload");
			}
			ack.addAll(TabCompleter.completeString(args[0], getWarpSuggestions(PlayerProfile.from((Player)sender))));
			return ack;
		}
		if (args.length == 2 && !args[0].equals("create") && subCmds.contains(args[0]))
			return TabCompleter.completeString(args[1], getWarpSuggestions(PlayerProfile.from((Player)sender)));
		if (args.length == 3 && args[0].equals("settype"))
			if (sender.hasPermission("bean.cmd.warp.*"))
				return TabCompleter.completeEnum(args[2], WarpType.class);
			else
				return TabCompleter.completeString(args[2], warpTypeArgs);
		if (args.length >= 3 && (args[0].equals("invite") || args[0].equals("uninvite") || args[0].equals("give")))
			return TabCompleter.completeOnlinePlayer(sender, args[args.length-1]);
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<action>").hoverEvent(HoverEvent.showText(Component.text("Action on the warp specified in the next argument."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str+" ").append(usageArguments[0]);
	}

}
