package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.enchants.BeanEnchantment;
import me.playground.gui.BeanGui;
import me.playground.gui.BeanGuiBeanItems;
import me.playground.gui.BeanGuiShop;
import me.playground.gui.debug.BeanGuiDebug;
import me.playground.items.BeanItem;
import me.playground.main.Main;
import me.playground.npc.NPC;
import me.playground.npc.NPCHuman;
import me.playground.npc.NPCManager;
import me.playground.npc.NPCType;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandOp extends BeanCommand {

	public CommandOp(Main plugin) {
		super(plugin, "bean.cmd.op", true, "op");
		description = "Operator Command.";
	}

	final String[] subCmds = { "commands", "customitems", "debug", "fixformatting", "guiprofile", "lockserver", "menuitem", "moltentouch", "openserver", "pissoff", "previewrank", "shops" };
	final String[] npcSubCmds = { "create", "list", "reload", "setskin", "tphere", "warpto", };
	final String[] shopSubCmds = { "enable", "disable", "reload" };
	final String[] shopReloadCmds = { "-f" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 1)
			return false;
		
		final Player p = isPlayer(sender) ? (Player) sender : null;
		
		final String cmdStr = args[0].toLowerCase();
		final String subcmd = args.length>1 ? args[1].toLowerCase() : "";
		
		if ("debug".equals(cmdStr) && checkPlayer(sender)) {
			new BeanGuiDebug(p).openInventory();
			return true;
		}
		
		if ("previewrank".equals(cmdStr) && checkPlayer(sender)) {
			try {
				if (args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("none")) {
					if (getPlugin().permissionManager().stopPreviewingRank(p))
						p.sendMessage(Component.text("\u00a77You are no longer previewing a rank."));
					else
						p.sendMessage(Component.text("\u00a7cYou weren't previewing a rank."));
				} else {
					Rank r = Rank.fromString(args[1]);
					getPlugin().permissionManager().previewRankFor(p, r);
					p.sendMessage(Component.text("\u00a77You are now previewing the rank: ").append(r.toComponent()));
				}
				return true;
			} catch (Exception e) {
				throw new CommandException(sender, "Please enter a valid rank!");
			}
		}
		
		if ("pissoff".equals(cmdStr) && checkPlayer(sender)) {
			int dist = args.length > 1 ? Integer.parseInt(args[1]) : 15;
			
			for (Entity e : ((Player)sender).getNearbyEntities(dist, dist, dist)) {
				if (e instanceof ArmorStand) {
					ArmorStand as = (ArmorStand) e;
					if (as.isMarker() || as.isInvulnerable())
						as.remove();
				}
			}
			return true;
		}
		
		if ("base64".equals(cmdStr) && checkPlayer(sender)) {
			getPlugin().getLogger().info("Base64 ItemStack Request: " + Utils.toBase64(p.getInventory().getItemInMainHand()));
			return true;
		}
		
		if (isPlayer(sender) && "npc".equals(cmdStr)) {
			if (args.length==1) {
				sender.sendMessage("\u00a7cSub-Commands: \u00a7f/op npc \u00a77create, list, warpto, reload, tphere, settitle");
				return true;
			}
			
			final NPCManager npcManager = getPlugin().npcManager();
			
			if ("reload".equals(subcmd)) {
				sender.sendMessage("\u00a77Reloading all \u00a7aNPCs\u00a77...");
				npcManager.reload();
				sender.sendMessage("\u00a77Reloaded!");
			}
			else if ("create".equals(subcmd)) {
				if (args.length==2) {
					sender.sendMessage("\u00a7cUsage: \u00a7f/op npc create \u00a77<name>");
					return true;
				}
				npcManager.createNPC(profile.getId(), p.getLocation(), NPCType.HUMAN, args[2]);
			}
			else if ("list".equals(subcmd)) {
				StringBuilder sb = new StringBuilder();
				sb.append(ChatColor.GRAY + "There's a total of " + ChatColor.GREEN + npcManager.getAllNPCs().size() + " NPCs" + ChatColor.GRAY + " currently loaded:\n");
				for (NPC<?> npc : npcManager.getAllNPCs())
					sb.append(ChatColor.DARK_GRAY + "" + npc.getEntityId() + ChatColor.GRAY + " - " + ChatColor.AQUA + npc.getEntity().getBukkitEntity().getName() + "\n");
				sender.sendMessage(sb.toString());
			}
			else if ("setskin".equals(subcmd)) {
				if (args.length > 3) {
					NPC<?> npc = npcManager.getEntityNPC(Integer.parseInt(args[2]));
					if (npc instanceof NPCHuman)
						((NPCHuman)npc).setSkin(args[2], args[3]);
					sender.sendMessage(Component.text("\u00a77Updated " + npc.getEntityId() + "'s skin."));
				}
			} else if ("warpto".equals(subcmd)||"tpto".equals(subcmd)) {
				if (args.length==2) {
					sender.sendMessage("\u00a7cUsage: \u00a7f/op npc "+subcmd+" \u00a77<id>");
					return true;
				}
				try {
					p.teleport(npcManager.getEntityNPC(Integer.parseInt(args[2])).getLocation());
					sender.sendMessage("\u00a77Warped to NPC!");
				} catch (Exception e) { // NullPointerException and NumberFormatException
					sender.sendMessage("\u00a7cAn NPC with the ID of '"+args[2]+"' does not exist!");
				}
			}
			else if ("tphere".equals(subcmd)) {
				if (args.length==2) {
					sender.sendMessage("\u00a7cUsage: \u00a7f/op npc "+subcmd+" \u00a77<id>");
					return true;
				}
				try {
					npcManager.getEntityNPC(Integer.parseInt(args[2])).teleport(p.getLocation(), true);
					sender.sendMessage("\u00a77Warped NPC to you!");
				} catch (Exception e) { // NullPointerException and NumberFormatException
					sender.sendMessage("\u00a7cAn NPC with the ID of '"+args[2]+"' does not exist!");
				}
			} else if ("settitle".equals(subcmd)) {
				if (args.length<3) {
					sender.sendMessage("\u00a7cUsage: \u00a7f/op npc "+subcmd+" \u00a77<id> <title>");
					return true;
				}
				try {
					Component title = args.length < 4 ? null : Component.text(args[3]);
					NPC<?> npc = npcManager.getEntityNPC(Integer.parseInt(args[2]));
					if (title != null)
						npc.setTitle(title, true);
					else
						npc.removeTitle(true);
					sender.sendMessage("\u00a77Title of NPC updated.");
				} catch (Exception e) { // NullPointerException and NumberFormatException
					sender.sendMessage("\u00a7cAn NPC with the ID of '"+args[2]+"' does not exist!");
				}
			}
			return true;
		}
		
		if ("shops".equals(cmdStr) || "shop".equals(cmdStr)) {
			if ("reload".equals(subcmd)) {
				for (Player pp : Bukkit.getOnlinePlayers()) {
					PlayerProfile prof = PlayerProfile.from(pp);
					if (prof.getBeanGui() != null && prof.getBeanGui() instanceof BeanGuiShop)
						pp.closeInventory(Reason.UNLOADED);
				}
				
				boolean forceEntityReload = args.length > 2 && args[2].equalsIgnoreCase("-f");
				
				getPlugin().shopManager().reload(forceEntityReload);
				sender.sendMessage("\u00a7eShop \u00a77entries" + (forceEntityReload ? " and entities" : "") + " have been reloaded.");
			} else if ("enable".equals(subcmd)) {
				getPlugin().shopManager().enable();
				sender.sendMessage("\u00a7eShops \u00a77are now \u00a7aenabled\u00a77!");
			} else if ("disable".equals(subcmd)) {
				for (Player pp : Bukkit.getOnlinePlayers()) {
					PlayerProfile prof = PlayerProfile.from(pp);
					if (prof.getBeanGui() != null && prof.getBeanGui() instanceof BeanGuiShop)
						pp.closeInventory(Reason.UNLOADED);
				}
				getPlugin().shopManager().disable();
				sender.sendMessage("\u00a7eShops \u00a77are now \u00a7cdisabled\u00a77!");
			}
			return true;
		}
		
		
		if (isPlayer(sender)) {
			if ("fixformatting".equals(cmdStr)) {
				Player t = args.length > 1 ? toPlayer(sender, args[1]) : p;
				for (ItemStack item : t.getInventory().getContents())
					if (item != null && item.getType() != Material.AIR)
						BeanItem.resetItemFormatting(item);
				for (ItemStack item : t.getInventory().getArmorContents())
					if (item != null && item.getType() != Material.AIR)
						BeanItem.resetItemFormatting(item);
				for (ItemStack item : t.getEnderChest().getContents())
					if (item != null && item.getType() != Material.AIR)
						BeanItem.resetItemFormatting(item);
				PlayerProfile tp = PlayerProfile.from(t);
				for (ItemStack item : tp.getArmourWardrobe())
					if (item != null && item.getType() != Material.AIR)
						BeanItem.resetItemFormatting(item);
				sender.sendMessage(PlayerProfile.from(t).getComponentName().append(Component.text("\u00a77's Containers have been updated.")));
			} else if ("menuitem".equals(cmdStr)) {
				p.getInventory().addItem(BeanGui.menuItem);
			} else if ("customitems".equals(cmdStr)) {
				new BeanGuiBeanItems(p).openInventory();
			} else if ("moltentouch".equals(cmdStr)) {
				ItemStack i = p.getEquipment().getItemInMainHand();
				if (i.getType() != Material.AIR) {
					i.addUnsafeEnchantment(BeanEnchantment.MOLTEN_TOUCH, 1);
					BeanItem.formatItem(i);
				}
			} else if ("guiprofile".equals(cmdStr)) {
				if (args.length == 1) {
					profile.profileOverride = profile.getUniqueId();
					sender.sendMessage("Reset GUI profile viewing override.");
				} else {
					PlayerProfile tpp = PlayerProfile.fromIfExists(args[1]);
					if (tpp != null) {
						sender.sendMessage("Set profile viewing override to " + tpp.getColouredName());
						profile.profileOverride = tpp.getUniqueId();
					}
				}
			}
			return true;
		}
		
		return false;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], this.subCmds);
		if (args.length == 2 && args[0].equalsIgnoreCase("npc"))
			return TabCompleter.completeString(args[1], this.npcSubCmds);
		if (args.length >= 2 && args[0].equals("shops") || args[0].equals("shop")) {
			if (args.length == 3 && args[1].equals("reload"))
				return TabCompleter.completeString(args[2], this.shopReloadCmds);
			else if (args.length == 2)
				return TabCompleter.completeString(args[1], this.shopSubCmds);
			return Collections.emptyList();
		}
		if (args.length == 2 && args[0].equals("fixformatting"))
			return TabCompleter.completeOnlinePlayer(sender, args[1]);
		if (args.length == 2 && args[0].equals("guiprofile"))
			return TabCompleter.completeOnlinePlayer(sender, args[1]);
		if (args.length == 2 && args[0].equals("previewrank")) {
			List<String> l = TabCompleter.completeEnum(args[1], Rank.class);
			l.add("stop");
			l.add("none");
			return l;
		}
			
		
		return Collections.emptyList();
	}
	
	final Component[] usageArguments = {
			Component.text("<command>").hoverEvent(HoverEvent.showText(Component.text("Command."))).color(NamedTextColor.GRAY),
	};
	
	@Override
	public Component getUsage(@Nonnull CommandSender sender, String str, String[] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str+" ").append(usageArguments[0]);
	}

}
