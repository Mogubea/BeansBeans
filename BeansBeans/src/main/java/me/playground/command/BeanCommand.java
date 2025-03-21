package me.playground.command;

import java.text.DecimalFormat;
import java.util.*;

import javax.annotation.Nullable;

import net.kyori.adventure.text.format.NamedTextColor;
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
import me.playground.playerprofile.ProfileStore;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Rank;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;

public abstract class BeanCommand implements TabExecutor, IPluginRef {
	
	private final Main plugin;
	protected final Random random = new Random();
	protected final String permissionString;
	protected final boolean canConsoleRun;
	protected final String[] aliases;

	protected final List<Component> usageHelp = new ArrayList<>();
	
	protected final int minArgs;

	private Component usageComponent;
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
	
	public BeanCommand(final Main plugin, String permissionString, boolean canConsoleRun, @NotNull String... aliases) {
		this(plugin, permissionString, canConsoleRun, 0, aliases);
	}
	
	public BeanCommand(final Main plugin, String permissionString, boolean canConsoleRun, int minArguments, @NotNull String... aliases) {
		this.plugin = plugin;
		this.permissionString = permissionString;
		this.canConsoleRun = canConsoleRun;
		this.aliases = aliases;
		this.minArgs = minArguments;
	}
	
	public abstract boolean runCommand(@Nullable PlayerProfile profile, @NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args);
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		try {
			if (str.startsWith("beansbeans:"))
				str = str.substring(11);
			
			if (!this.isEnabled())
				throw new CommandException(sender, "/"+str + " is currently disabled.");
		
			if (!this.canConsoleRun && !isPlayer(sender))
				throw new CommandException(sender, "You must be in-game to use /"+str+".");

			PlayerProfile pp = isPlayer(sender) ? PlayerProfile.from((Player) sender) : null;

			// Check for cool-downs if they are a player
			if (pp != null) {
				if (pp.onCooldown("cmd." + cmd.getName())) {
					sender.sendActionBar(Component.text("\u00a7cPlease wait " + Utils.timeStringFromNow(pp.getCooldown("cmd."+cmd.getName())) + " before using /"+str+" again."));
					return false;
				}

				if (pp.onCdElseAdd("cmd", 600)) {
					sender.sendActionBar(Component.text("\u00a7cYou are sending commands too fast!"));
					return false;
				}

				pp.getStats().addToStat(StatType.GENERIC, "commandsRun", 1);
			}

			if (args.length < minArgs)
				throw new CommandException(sender, getUsage(sender, str.toLowerCase(), args));

			boolean successful = runCommand(isPlayer(sender) ? PlayerProfile.from((Player)sender) : null, sender, cmd, str.toLowerCase(), args);

			// Add command cooldown after a successful run.
			if (successful && pp != null && cooldown > 0)
				pp.addCooldown("cmd." + cmd.getName(), cooldown);

			return successful;
		} catch (CommandException e) {
			e.notifySender();
		}

		return false;
	}
	
	@Override
	public final @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		if (!this.canConsoleRun && !(sender instanceof Player))
			return new ArrayList<>();
		
		String[] newArgs = new String[args.length];
		for (int x = 0; x < args.length; x++)
			newArgs[x] = args[x].toLowerCase();
		
		return runTabComplete(sender, cmd, str, newArgs);
	}

	public @Nullable List<String> runTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		return Collections.emptyList();
	}

	protected Component getUsage(@NotNull CommandSender sender, @NotNull String str, @NotNull String[] args) {
		if (usageComponent != null) return usageComponent;

		Component usage = Component.text("Usage: ", NamedTextColor.RED).append(Component.text("/" + str, NamedTextColor.WHITE).hoverEvent(Component.text(getDescription())));
		int length = args.length;
		for (int x = -1; ++x < length;)
			usage = usage.append(Component.text(" " + args[x], NamedTextColor.WHITE).hoverEvent(usageHelp.get(x).hoverEvent()));

		int helpLength = usageHelp.size();

		for (int x = length-1; ++x < helpLength;)
			usage = usage.append(usageHelp.get(x));

		return usageComponent = usage;
	}
	
	public String getPermissionString() {
		return permissionString;
	}

	@NotNull
	public String getDescription() {
		if (description == null)
			description = "A Bean's Beans command.";
		return description;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public String[] getAliases() {
		return aliases;
	}

	@NotNull
	public String getName() {
		return aliases[0];
	}
	
	@Override
	@NotNull
	public Main getPlugin() {
		return plugin;
	}

	@NotNull
	protected Random getRandom() {
		return random;
	}
	
	protected boolean isPlayer(@NotNull CommandSender sender) {
		return sender instanceof Player;
	}
	
	protected ItemStack toItemStack(@NotNull CommandSender sender, String str, int amount) {
		final BeanItem bi = BeanItem.from(str);
		
		ItemStack i;
		if (bi == null) {
			try {
				Material m = Material.valueOf(str.toUpperCase());
				if (!m.isItem())
					throw new CommandException(sender, "Invalid item '"+str+"'");
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
		final Component displayName = meta.hasDisplayName() ? meta.displayName() : Component.translatable(item);
		
		return displayName.hoverEvent(item.asHoverEvent());
	}
	
	protected int toIntDef(String s, int def) {
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

	protected float toFloat(CommandSender sender, String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException numberformatexception) {
			throw new CommandException(sender, "'" + s + "' is not a valid floating number!");
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
		Player p = getPlugin().searchForPlayer(s);
		if (p == null)
			throw new CommandException(sender, "Couldn't find player '"+s+"'");
		if (!self && p == sender)
			throw new CommandException(sender, "You can't target yourself!");
		
		return p;
	}
	
	protected PlayerProfile toProfile(CommandSender sender, String s) {
		PlayerProfile pp = PlayerProfile.fromIfExists(s);
		if (pp == null) {
			Player p = toPlayer(sender, s);
			if (p != null)
				pp = PlayerProfile.from(p);
		}
			
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
		
		throw new CommandException(sender, "Couldn't find player '"+ ProfileStore.from(id).getDisplayName()+"'");
	}
	
	protected Collection<? extends Player> onlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}
	
	protected Collection<? extends Player> onlineRank(Rank rank) {
		ArrayList<Player> players = new ArrayList<>();
		for (Player p : onlinePlayers())
			if (PlayerProfile.from(p).isRank(rank))
				players.add(p);
		return players;
	}
	
	protected boolean isRank(CommandSender sender, Rank rank) {
		return !isPlayer(sender) || PlayerProfile.from((Player) sender).isRank(rank);
	}

	@NotNull
	protected Rank getRank(CommandSender sender) {
		return isPlayer(sender) ? PlayerProfile.from((Player)sender).getHighestRank() : Rank.OWNER;
	}
	
	protected boolean checkRankPower(CommandSender sender, Player target, String err) {
		if (sender == target) 
			return true;
		if (getPlugin().permissionManager().isPreviewing(target)) // Prevent bs
			return false;
		final boolean ra = !isPlayer(sender) || getRank(sender).power() > getRank(target).power();
		if (!ra)
			throw new CommandException(sender, err);
		return true;
	}
	
	protected boolean checkRank(CommandSender sender, Rank rank) {
		if (!isRank(sender, rank))
			throw new CommandException(sender, "You don't have permission to do that.");
		return true;
	}
	
	protected boolean checkPlayer(CommandSender sender) {
		if (!isPlayer(sender))
			throw new CommandException(sender, "You must be in-game to do that.");
		return true;
	}
	
	protected boolean checkSubPerm(CommandSender sender, String subCmd) {
		if (getPermissionString() == null || getPermissionString().isEmpty()) return true;
		final boolean ra = sender.hasPermission("*") || sender.hasPermission(getPermissionString() + "." + subCmd) || sender.hasPermission(getPermissionString() + ".*");
		if (!ra) 
			throw new CommandException(sender, "You don't have permission to use /" + aliases[0] + " " + subCmd + ".");
		return true;
	}
	
	protected boolean isSafe(Location loc) {
		return loc.getBlock().getType() != Material.LAVA && (!loc.subtract(0, 0.2, 0).getBlock().isEmpty());
	}
	
	protected boolean noGM(CommandSender sender, GameMode mode) {
		if (sender instanceof Player && ((Player)sender).getGameMode() == mode)
			throw new CommandException(sender, "This command is disabled in " + mode.name().toLowerCase() + " mode.");
		return true;
	}
	
	public Component toComponent() {
		return Component.text("/"+aliases[0]).hoverEvent(HoverEvent.showText(Component.text(this.description))).clickEvent(ClickEvent.suggestCommand("/"+aliases[0])).color(BeanColor.COMMAND);
	}
	
	protected Component commandInfo(String cmd) {
		final PluginCommand pc = Bukkit.getServer().getPluginCommand(cmd);
		return Component.text("/"+cmd.toLowerCase()).hoverEvent(HoverEvent.showText(Component.text(pc.getDescription()))).clickEvent(ClickEvent.suggestCommand("/"+cmd)).color(BeanColor.COMMAND);
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
	
	protected DiscordBot getDiscord() {
		return getPlugin().getDiscord();
	}
	
	protected final DecimalFormat df = new DecimalFormat("#,###");
	protected final DecimalFormat dec = new DecimalFormat("#,###.##");
	
}
