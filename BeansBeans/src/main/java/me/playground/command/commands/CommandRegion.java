package me.playground.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BlockVector;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;

import me.playground.celestia.logging.Celestia;
import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiConfirm;
import me.playground.gui.BeanGuiRegion;
import me.playground.gui.BeanGuiRegionMain;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.regions.flags.Flag;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * This needs to be drastically modified when the Settlement and Parent Systems comes into play,
 * as players will be able to modify regions within regions themselves.
 *
 */
public class CommandRegion extends BeanCommand {

	public CommandRegion(Main plugin) {
		super(plugin, "bean.cmd.region", false, "region", "rg");
		this.description = "The general region command.";
	}
	
	final List<String> subCmds = Arrays.asList("addmember", "define", "delete", "list", "info", "priority", "redefine", "removemember", "rename", "select", "setflag", "warpto");
	final String[] para2 = { "~" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		final Player p = (Player) sender;
		
		if (args.length < 1) {
			new BeanGuiRegionMain(p).openInventory();
			return false;
		}
		
		final String subcmd = args[0].toLowerCase();
		final RegionManager rm = getPlugin().regionManager();
		Region region = (hasPermission(sender, "search") && args.length > 1 && !args[1].equals("~") ? rm.getRegion(args[1].toLowerCase()) : rm.getRegion(p.getLocation()));
		if (!(subcmd.equals("define") || subcmd.equals("create") || subcmd.equals("def") || subcmd.equals("list")) && region == null)
			throw new CommandException(p, "The region '"+args[1]+"' does not exist.");
		
		if (subCmds.contains(subcmd)) {
			if (!hasPermission(sender, subcmd))
				throw new CommandException(p, "You don't have permission to do that.");
		} else {
			return false;
		}
		
		// REGION INFO <NAME>
		if (subcmd.equals("info")) {
			p.sendMessage(Component.text("\u00a77Information about region ").append(region.toComponent()));
			p.sendMessage("\u00a77Your level of power here: \u00a7f" +  region.getMember(p).toString());
			final StringBuilder sb = new StringBuilder();
			for (Flag<?> flag : Flags.getRegisteredFlags()) {
				final Object value = region.getEffectiveFlag(flag);
				if (value == null) {
					sb.append(flag.getName() + ": null, ");
					continue;
				}
				final boolean isInherit = region.getFlag(flag, true) == null;
				sb.append((isInherit ? "\u00a79" : "\u00a73") + flag.getName() + ": \u00a77"+ value + "\u00a78, ");
			}
			p.sendMessage(sb.toString());
			
		// REGION DEFINE <NEW NAME> [PLAYER NAMES]
		} else if (subcmd.equals("define")) {
			if (args.length < 2)
				throw new CommandException(p, "Please specify a name for the new region!");
			
			String regionName = args[1];
			
			if (regionName.length() < 3)
				throw new CommandException(p, "The region name must contain at least 3 characters.");
			
			if (!regionName.matches("^[a-zA-ZÀ-ÖØ-öø-ÿ0-9-_]+$"))
				throw new CommandException(p, "The region name '"+regionName+"' is invalid (a-z, À-Ö, 0-9, _, -)!");
			
			if (rm.getRegion(regionName) != null)
				throw new CommandException(p, "The region '"+regionName+"' already exists!");
			
			try {
				WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				com.sk89q.worldedit.regions.Region wer = we.getSession(p).getSelection();
				
				if (!wer.getWorld().getName().equals(p.getWorld().getName())) {
					p.sendMessage("\u00a7cPlease make sure your selection is within the same world as you.");
					return false;
				}
				
				ArrayList<Integer> ownerIds = new ArrayList<Integer>();
						
				if (args.length > 2) {
					for (int x = 2; x < args.length; x++) {
						int id = toId(p, args[x]);
						ownerIds.add(id);
					}
				}
				
				int priority = 0, parent = 0;
				
				region = rm.createRegion(profile.getId(), priority, parent, regionName, p.getWorld(), fromVector3(wer.getMinimumPoint()), fromVector3(wer.getMaximumPoint()));
				p.sendMessage(Component.text("\u00a7aYou Successfully defined the region ").append(region.toComponent()));
				for (int ownerId : ownerIds) {
					region.addMember(ownerId, MemberLevel.OWNER);
					p.sendMessage(PlayerProfile.getDisplayName(ownerId).append(Component.text("\u00a77 was added as an Owner.")));
				}
				
			} catch (IncompleteRegionException e) {
				throw new CommandException(p, "Please select an area using your wand/selection commands.");
			} catch (CommandException e) {
				e.notifySender();
			} catch (Exception e) {
				e.printStackTrace();
				throw new CommandException(p, "An error occured while trying to create the region.");
			}
		} else if (subcmd.equals("delete")) {
			if (args.length < 2)
				throw new CommandException(p, "Specify a region to delete!");
			if (region.isWorldRegion())
				throw new CommandException(p, "You cannot delete a world region.");
			
			final Region reg = region;
			
			new BeanGuiConfirm(p,Arrays.asList(Component.text("\u00a77Confirming will delete the region:"), region.toComponent(), Component.empty(), 
					Component.text("\u00a7cThis cannot be undone."))) {

				@Override
				public void onAccept() {
					reg.delete();
					Celestia.logRegionChange(p, "Deleted region #" + reg.getRegionId());
					p.sendMessage(Component.text("\u00a7cDeleted the region ").append(reg.toComponent()));
				}

				@Override
				public void onDecline() {
				}
			}.openInventory();
		} else if (subcmd.equals("redefine")) {
			if (args.length < 2)
				throw new CommandException(p, "Specify a region to redefine the boundaries of!");
			
			try {
				WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				com.sk89q.worldedit.regions.Region wer = we.getSession(p).getRegionSelector(BukkitAdapter.adapt(p.getWorld())).getRegion();
				
				if (!wer.getWorld().getName().equals(p.getWorld().getName())) {
					p.sendMessage("\u00a7cPlease make sure your selection is within the same world as you.");
					return false;
				}
				
				BlockVector3 min = wer.getMinimumPoint();
				BlockVector3 max = wer.getMaximumPoint();
				
				region.setMinimumPoint(min.getX(), min.getY(), min.getZ()).setMaximumPoint(max.getX(), max.getY(), max.getZ()).updateMapEntry();
				p.sendMessage(Component.text("\u00a77Successfully updated the boundaries of ").append(region.toComponent()));
			} catch (IncompleteRegionException e) {
				throw new CommandException(p, "Please select an area using your wand/selection commands.");
			} catch (CommandException e) {
				e.notifySender();
			} catch (Exception e) {
				e.printStackTrace();
				throw new CommandException(p, "An error occured while trying to create the region.");
			} 
		} else if (subcmd.equals("priority")) {
			if (args.length > 2) {
				int prio = toIntMinMax(p, args[2], 0, 20);
				region.setPriority(p, prio);
				p.sendMessage(region.toComponent().append(Component.text("\u00a77 now has a priority of \u00a7b" + region.getPriority())));
			} else {
				p.sendMessage(region.toComponent().append(Component.text("\u00a77 has a priority of \u00a7b" + region.getPriority())));
			}
		} else if (subcmd.equals("warpto")) {
			if (args.length < 2)
				throw new CommandException(p, "Please specify a region to warp to!");
			if (p.teleport(region.getRegionCenter(), TeleportCause.COMMAND))
				p.sendMessage(Component.text("\u00a77Successfully warped to the center of ").append(region.toComponent()));
		} else if (subcmd.equals("rename")) {
			if (args.length < 2)
				throw new CommandException(p, "Usage: \u00a7f/"+str+" addmember \u00a77 <region> <name>");
			
			String regionName = args[2];
			
			if (regionName.length() < 3)
				throw new CommandException(p, "The region name must contain at least 3 characters.");
			
			if (!regionName.matches("^[a-zA-ZÀ-ÖØ-öø-ÿ0-9-_]+$"))
				throw new CommandException(p, "The region name '"+regionName+"' is invalid (a-z, À-Ö, 0-9, _, -)!");
			
			if (rm.getRegion(regionName) != null)
				throw new CommandException(p, "The region '"+regionName+"' already exists!");
			
			String oldName = region.getName();
			
			region.setName(p, regionName);
			
			p.sendMessage(Component.text("\u00a77Renamed \u00a7f"+oldName+" \u00a77to ").append(region.toComponent()));
			refreshRegionViewers(region);
		} else if (subcmd.equals("addmember")) {
			if (args.length < 3)
				throw new CommandException(p, "Usage: \u00a7f/"+str+" addmember \u00a77 <region> <player> <member level>");
			
			MemberLevel level = MemberLevel.MEMBER;
			if (args.length > 3) {
				try {
					level = MemberLevel.valueOf(args[3].toUpperCase());
				} catch (Exception e) {
					throw new CommandException(p, "'"+args[3]+"' is not a valid member level.");
				}
			}
			
			int id = toId(p, args[2]);
			region.addMember(id, level);
			refreshRegionViewers(region);
			p.sendMessage(Component.text("\u00a77Added ").append(PlayerProfile.getDisplayName(id)).append(Component.text(" \u00a77to ").append(region.toComponent()).append(Component.text("\u00a77 as a \u00a7f" + level.name()))));	
		} else if (subcmd.equals("removemember")) {
			if (args.length < 3)
				throw new CommandException(p, "Usage: \u00a7f/"+str+" removemember \u00a77<region> <player>");
			
			int id = toId(p, args[2]);
			region.removeMember(id);
			refreshRegionViewers(region);
			p.sendMessage(Component.text("\u00a77Removed ").append(PlayerProfile.getDisplayName(id)).append(Component.text(" \u00a77from ").append(region.toComponent())));
		} else if (subcmd.equals("setflag")) {
			if (args.length < 3)
				throw new CommandException(p, "Usage: \u00a7f/"+str+" setflag \u00a77<region> <flag> <value or none>");
			
			Flag<?> f;
			try {
				f = Flags.getFlag(args[2]);
				region.setFlag(f, (args.length == 3) ? null : f.parseInput(args[3]));
				p.sendMessage(Component.text("\u00a77Set ").append(region.toComponent()).append(Component.text("\u00a77's \u00a79" + f.getName() + "\u00a77 to \u00a7f" + region.getFlag(f))));
				refreshRegionViewers(region);
			} catch (NullPointerException e) {
				throw new CommandException(p, "'"+args[2]+"' is not a valid flag.");
			} catch (CommandException e) {
				e.notifySender();
			}
		} else if (subcmd.equals("list")) {
			ArrayList<Region> regions = rm.getAllRegions();
			int page = args.length > 1 ? (toIntDef(args[1], 0)-1) : 0;
			if (page*50 > regions.size()) page = regions.size()/50 + 1;
			else if (page < 0) page = 0;
			
			Component text = Component.text("\u00a77Listing all Regions [Page: \u00a7f"+(page+1)+"\u00a77](\u00a7f"+(page*50+1)+"\u00a77-\u00a7f"+Math.min(regions.size(), 50 + page*50)+"\u00a77/\u00a7f"+regions.size()+"\u00a77):\n");
			for (int x = page*50; x < Math.min(regions.size(), 50 + page*50); x++)
				text = text.append(regions.get(x).toComponent()).append(Component.text("\u00a78, "));
			
			p.sendMessage(text);
		} else if (subcmd.equals("select")) {
			if (region.isWorldRegion())
				throw new CommandException(p, "There is no region here.");
			try {
				BlockVector min = region.getMinimumPoint();
				BlockVector max = region.getMaximumPoint();
			
				WorldEditPlugin we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
				LocalSession session = we.getSession(p);
				RegionSelector selector = session.getRegionSelector(BukkitAdapter.adapt(p.getWorld()));
				
				 if (selector.selectPrimary(BlockVector3.at(min.getX(), min.getY(), min.getZ()), ActorSelectorLimits.forActor(BukkitAdapter.adapt(p))))
					 selector.explainPrimarySelection(BukkitAdapter.adapt(p), session, BlockVector3.at(min.getX(), min.getY(), min.getZ()));
				selector.selectPrimary(BlockVector3.at(max.getX(), max.getY(), max.getZ()), ActorSelectorLimits.forActor(BukkitAdapter.adapt(p)));
					selector.explainSecondarySelection(BukkitAdapter.adapt(p), session, BlockVector3.at(max.getX(), max.getY(), max.getZ()));
				session.setRegionSelector(BukkitAdapter.adapt(p.getWorld()), selector);
				p.sendMessage(Component.text("\u00a77WorldEdit Selection has been mapped to ").append(region.toComponent()));
			} catch (Exception e) {
				throw new CommandException(p, "There was an error with WorldEdit.");
			}
		}
		return true;
	}
	
	private int toId(Player p, String name) {
		try {
			int id = PlayerProfile.getDBID(name);
			if (id < 1)
				throw new CommandException(p, "Couldn't find player '"+name+"'");
			return id;
		} catch (Exception e) {
			throw new CommandException(p, "Couldn't find player '"+name+"'");
		}
	}
	
	private BlockVector fromVector3(BlockVector3 vector) {
		return new BlockVector(vector.getX(), vector.getY(), vector.getZ());
	}
	
	private boolean hasPermission(CommandSender sender, String subCmd) {
		return sender.hasPermission("bean.cmd.region.*") || sender.hasPermission("bean.cmd.region." + subCmd);
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], subCmds);
		if (args.length == 2)
			return TabCompleter.completeString(args[1], para2);
		
		if (args.length == 3 && args[0].equals("setflag") && hasPermission(sender, "bean.cmd.region.setflag"))
			return TabCompleter.completeObject(args[2], Flag::getName, Flags.getRegisteredFlags());
		if (args.length > 2 && args[0].equals("define"))
			return TabCompleter.completeOnlinePlayer(sender, args[args.length-1]);
		if (args.length == 3 && args[0].equals("priority"))
			return TabCompleter.completeIntegerBetween(args[2], 0, 20);
		if (args.length == 3 && args[0].equals("list"))
			return TabCompleter.completeInteger(args[2]);
		if (args.length == 3 && args[0].equals("addmember"))
			return TabCompleter.completeOnlinePlayer(sender, args[2]);
		if (args.length == 4 && args[0].equals("addmember"))
			return TabCompleter.completeEnum(args[3], MemberLevel.class);
		if (args.length == 4 && args[0].equals("setflag") && hasPermission(sender, "bean.cmd.region.setflag")) {
			Flag<?> flag = Flags.getFlag(args[2]);
			if (flag != null)
				return TabCompleter.completeFlagParse(args[3], flag);
			return Collections.emptyList();
		}
			
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f/"+str);
	}
	
	private void refreshRegionViewers(Region region) {
		BeanGui.getAllViewers(BeanGuiRegion.class).forEach((gui) -> {
			if (gui.getRegion() == region) {
				gui.refresh(); 
			}
		});
	}

}
