package me.playground.command;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.discord.DiscordBot;
import me.playground.items.BeanItem;
import me.playground.main.IPluginRef;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public abstract class BeanCommand implements TabExecutor, IPluginRef {
	
	private final Main plugin;
	protected final Random random = new Random();
	protected final String permissionString;
	protected final boolean canConsoleRun;
	protected final String[] aliases;
	
	protected final int minArgs;
	
	protected String description = "";
	protected int cooldown;
	
	private boolean enabled = true;
	
	public BeanCommand(final Main plugin, String... aliases) {
		this(plugin, "", false, 0, aliases);
	}
	
	public BeanCommand(final Main plugin, boolean canConsoleRun, String... aliases) {
		this(plugin, "", canConsoleRun, 0, aliases);
	}
	
	public BeanCommand(final Main plugin, boolean canConsoleRun, int minArguments, String... aliases) {
		this(plugin, "", canConsoleRun, minArguments, aliases);
	}
	
	public BeanCommand(final Main plugin, String permissionString, boolean canConsoleRun, @Nonnull String... aliases) {
		this(plugin, permissionString, canConsoleRun, 0, aliases);
	}
	
	public BeanCommand(final Main plugin, String permissionString, boolean canConsoleRun, int minArguments, @Nonnull String... aliases) {
		this.plugin = plugin;
		this.permissionString = permissionString;
		this.canConsoleRun = canConsoleRun;
		this.aliases = aliases;
		this.minArgs = minArguments;
	}
	
	public BeanCommand(final Main plugin, boolean canConsoleRun, Rank permissionRank, @Nonnull String... aliases) {
		this(plugin, permissionRank == null ? "" : "bean.rank." + permissionRank.lowerName(), canConsoleRun, aliases);
	}
	
	public BeanCommand(final Main plugin, boolean canConsoleRun, Rank permissionRank, int minArguments, @Nonnull String... aliases) {
		this(plugin, permissionRank == null ? "" : "bean.rank." + permissionRank.lowerName(), canConsoleRun, minArguments, aliases);
	}
	
	public abstract boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args);
	public abstract @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args);
	public abstract Component getUsage(@Nonnull CommandSender sender, @Nonnull String str, @Nonnull String[] args);
	
	@Override
	public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		try {
			if (!this.isEnabled())
				throw new CommandException(sender, "/"+str + " is currently disabled.");
		
			if (!this.canConsoleRun && !isPlayer(sender))
				throw new CommandException(sender, "You must be in-game to use /"+str+".");
			
			if (isPlayer(sender)) {
				PlayerProfile pp = PlayerProfile.from(((Player)sender));
				if (cooldown > 0 && !sender.isOp() && pp.onCdElseAdd("cmd." + cmd.getName(), cooldown)) {
					((Player)sender).sendActionBar(Component.text("\u00a7cPlease wait " + pp.getCooldown("cmd."+cmd.getName())/1000 + " seconds before using /"+str+" again."));
					return false;
				}
				
				if (pp.onCdElseAdd("cmd", 600)) {
					((Player)sender).sendActionBar(Component.text("\u00a7cYou are sending commands too fast!"));
					return false;
				}
			}
			
			if (args.length < minArgs)
				throw new CommandException(sender, getUsage(sender, str.toLowerCase(), args));
			
			return runCommand(isPlayer(sender) ? PlayerProfile.from((Player)sender) : null, sender, cmd, str.toLowerCase(), args);
		} catch (CommandException e) {
			e.notifySender();
			return false;
		}
	}
	
	@Override
	public @Nullable List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (!this.canConsoleRun && !(sender instanceof Player))
			return new ArrayList<>();
		
		String[] newArgs = new String[args.length];
		for (int x = 0; x < args.length; x++)
			newArgs[x] = args[x].toLowerCase();
		
		return runTabComplete(sender, cmd, str, newArgs);
	}
	
	public String getPermissionString() {
		return permissionString;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void enable() {
		this.enabled = true;
	}
	
	public void disable() {
		this.enabled = false;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public String[] getAliases() {
		return aliases;
	}
	
	@Override
	public Main getPlugin() {
		return plugin;
	}
	
	protected Random getRandom() {
		return random;
	}
	
	protected boolean isPlayer(CommandSender sender) {
		return sender instanceof Player;
	}
	
	protected ItemStack toItemStack(CommandSender sender, String str, int amount) {
		final BeanItem bi = BeanItem.from(str);
		
		ItemStack i = null;
		if (bi == null) {
			try {
				Material m = Material.valueOf(str.toUpperCase());
				if (m == null)
					return null;
				i = BeanItem.formatItem(new ItemStack(m));
			} catch (IllegalArgumentException e) {
				throw new CommandException(sender, "Unknown item '"+str+"'");
			}
		} else {
			i = bi.getItemStack();
		}
		
		i.setAmount(amount); 
		return i;
	}
	
	protected Component toHover(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return Component.empty();
		
		final ItemMeta meta = item.getItemMeta();
		final Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.text(item.getI18NDisplayName());
		
		return displayName.hoverEvent(item.asHoverEvent());
	}
	
	protected int toIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException numberformatexception) {
            return def;
        }
    }
	
	protected int toInt(CommandSender sender, String s, String err) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException numberformatexception) {
            throw new CommandException(sender, err);
        }
    }
	
	protected int toIntMinMax(CommandSender sender, String s, int i, int j) {
        int k = toInt(sender, s, "'" + s + "' is not a valid number!");

        if (k < i) {
            throw new CommandException(sender, "Please specify a larger number (between " + i + " and " + j + ")!");
        } else if (k > j) {
            throw new CommandException(sender, "Please specify a smaller number (between " + i + " and " + j + ")!");
        } else {
            return k;
        }
    }
	
	protected Player toPlayer(CommandSender sender, String s) {
		return toPlayer(sender, s, true);
	}
	
	protected Player toPlayer(CommandSender sender, String s, boolean self) {
		Player p = Utils.playerPartialMatch(s);
		if (p == null)
			throw new CommandException(sender, "Couldn't find player '"+s+"'");
		if (!self && p == sender)
			throw new CommandException(sender, "You can't target yourself!");
		
		return p;
	}
	
	protected PlayerProfile toProfile(CommandSender sender, String s) {
		PlayerProfile pp = PlayerProfile.fromIfExists(s);
		if (pp == null)
			throw new CommandException(sender, "Couldn't find player '"+s+"'");
		return pp;
	}
	
	protected Component toName(CommandSender sender) {
		if (sender instanceof Player)
			return PlayerProfile.from(((Player)sender)).getComponentName();
		else
			return Component.text("\u00a7dServer");
	}
	
	protected World toWorld(CommandSender sender, String s) {
		World w = Bukkit.getWorld(s);
		if (w == null)
			throw new CommandException(sender, "Couldn't find world '"+s+"'");
		return w;
	}
	
	protected Player toPlayerFromId(CommandSender sender, int id) {
		if (id <= 0)
			throw new CommandException(sender, "The specified player has never played here!");
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (PlayerProfile.from(p).getId() == id)
				return p;
		}
		
		throw new CommandException(sender, "Couldn't find player '"+PlayerProfile.getDisplayName(id).content()+"'");
	}
	
	protected Collection<? extends Player> onlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}
	
	protected Collection<? extends Player> onlineRank(Rank rank) {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player p : onlinePlayers())
			if (PlayerProfile.from(p).isRank(rank))
				players.add(p);
		return players;
	}
	
	protected boolean isRank(CommandSender sender, Rank rank) {
		return isPlayer(sender) ? PlayerProfile.from((Player)sender).isRank(rank) : true;
	}
	
	protected Rank getRank(CommandSender sender) {
		return isPlayer(sender) ? PlayerProfile.from((Player)sender).getHighestRank() : Rank.OWNER;
	}
	
	protected boolean checkRankPower(CommandSender sender, Player target, String err) {
		if (sender == target) 
			return true;
		if (getPlugin().permissionManager().isPreviewing(target)) // Prevent bs
			return false;
		final boolean ra = isPlayer(sender) ? getRank(sender).power() > getRank(target).power() : true;
		if (!ra)
			throw new CommandException(sender, err);
		return ra;
	}
	
	protected boolean checkRank(CommandSender sender, Rank rank) {
		final boolean ra = isRank(sender, rank);
		if (!ra)
			throw new CommandException(sender, "You don't have permission to do that.");
		return ra;
	}
	
	protected boolean checkPlayer(CommandSender sender) {
		final boolean ra = isPlayer(sender);
		if (!ra)
			throw new CommandException(sender, "You must be in-game to do that.");
		return ra;
	}
	
	protected boolean isSafe(Location loc) {
		if (loc.getBlock().getType() == Material.LAVA || loc.subtract(0,1,0).getBlock().isEmpty())
			return false;
		return true;
	}
	
	protected boolean noGM(CommandSender sender, GameMode mode) {
		if (sender instanceof Player && ((Player)sender).getGameMode() == mode)
			throw new CommandException(sender, "This command is disabled in " + mode.name().toLowerCase() + " mode.");
		return true;
	}
	
	protected Component commandInfo(String cmd) {
		final PluginCommand pc = Bukkit.getServer().getPluginCommand(cmd);
		return Component.text("/"+cmd.toLowerCase()).hoverEvent(HoverEvent.showText(Component.text(pc.getDescription()))).clickEvent(ClickEvent.suggestCommand("/"+cmd)).color(BeanColor.WORLD);
	}
	
	protected Component worldInfo(CommandSender sender, World world) {
		Component text;
		if (isRank(sender, Rank.ADMINISTRATOR)) {
			Component playerList = Component.text("");
			for (int x = 0; x < world.getPlayerCount(); x++) {
				final Player p = world.getPlayers().get(x);
				PlayerProfile pp = PlayerProfile.from(p);
				playerList = playerList.append(pp.getColouredName());
				if (x+1 < world.getPlayerCount())
					playerList = playerList.append(Component.text("\u00a7a, "));
			}
			
			
			text = Component.text(
					"\u00a72UUID: \u00a7a" + world.getUID() +
					"\n\u00a72Type: \u00a7a" + world.getEnvironment() + 
					"\n\u00a72Entities: \u00a7a" + world.getEntityCount() + 
					"\n\u00a72Players: \u00a7a").append(playerList);
			return Component.text(world.getName()).color(BeanColor.WORLD).hoverEvent(HoverEvent.showText(text)).clickEvent(ClickEvent.suggestCommand("/world tpto " + world.getName()));
		} else {
			text = Component.text(
					"\u00a72Type: \u00a7a" + world.getEnvironment() + 
					"\n\u00a72Players: \u00a7a" + world.getPlayerCount());
			return Component.text(world.getName()).color(BeanColor.WORLD).hoverEvent(HoverEvent.showText(text));
		}
		
	}
	
	protected Component regionInfo(CommandSender sender, Region region) {
		Component text;
		if (!(sender instanceof Player))
			return Component.text(region.getName()).color(BeanColor.REGION);
		
		final Player p = (Player) sender;
		
		if (region.isWorldRegion()) {
			text = Component.text(
					"\u00a7rWorld Region" +
					"\n\u00a77Can you build here: " + (region.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(region.getMember(p)) ? "\u00a7aYes" : "\u00a7cNo"));
		} else {
			text = Component.text(
					"\u00a7rPlayer Region" +
					"\n\u00a7e(\u00a78" + region.getMinimumPoint().toString() + "\u00a77 - \u00a78" + region.getMaximumPoint() + "\u00a7e)" +
					"\n\u00a77Can you build here: " + (region.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(region.getMember(p)) ? "\u00a7aYes" : "\u00a7cNo") +
					"\n\u00a77Priority: \u00a7b" + region.getPriority()
					
					).append(region.getComponentMembers());
		}
		text = text.colorIfAbsent(region.isWorldRegion() ? BeanColor.REGION_WORLD : BeanColor.REGION);
		Component done = region.getColouredName().hoverEvent(HoverEvent.showText(text));
		
		if (sender.hasPermission("bean.cmd.region.warpto") || sender.hasPermission("bean.cmd.region.*"))
			return Component.empty().append(done.clickEvent(ClickEvent.suggestCommand("/region warpto " + region.getName())));
		else
			return Component.empty().append(done);
	}
	
	protected DiscordBot getDiscord() {
		return getPlugin().discord();
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	
}
