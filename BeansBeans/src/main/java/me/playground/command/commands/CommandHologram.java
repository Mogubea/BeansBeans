package me.playground.command.commands;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
import me.playground.entity.CustomEntityType;
import me.playground.entity.EntityHologram;
import me.playground.entity.EntityHologramLine;
import me.playground.gui.BeanGuiConfirm;
import me.playground.items.lore.Lore;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.utils.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.Entity;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Make this command class better in general, it's pretty meh
 */
public class CommandHologram extends BeanCommand {

	private static final int NON_STAFF_MAX_LINES = 2;
	private static final int NON_STAFF_LENGTH_MAX = 50;

	public CommandHologram(Main plugin) {
		super(plugin, "bean.cmd.hologram", false, 1, "hologram", "hg");
		description = "Holograms";
	}

	/**/

	private static final String[] subCmds = {"create", "remove", "modifyrank", "addline", "removeline", "editline", "resetlines", "linespacing"};

	@Override
	public boolean runCommand(PlayerProfile profile, @NotNull CommandSender sender, @NotNull Command cmd, @NotNull String str, @NotNull String[] args) {
		final Player p = (Player) sender;
		final String subCmd = args[0].toLowerCase();

		// Create
		if (subCmd.equals(subCmds[0]) && checkSubPerm(p, "create")) {
			CustomEntityType.HOLOGRAM.spawn(p.getLocation());
		}

		// Remove
		else if (subCmd.equals(subCmds[1])) {
			EntityHologram hologram = getHologram(profile, p, true);

			new BeanGuiConfirm(p, Lore.getBuilder("Confirm to remove this hologram from existence.").build().getLoree()) {
				@Override
				public void onAccept() {
					hologram.remove(Entity.RemovalReason.DISCARDED);
					p.sendMessage(Component.text("Successfully removed the Hologram.", NamedTextColor.GRAY));
				}

				@Override
				public void onDecline() {
				}
			}.openInventory();
		}

		// Modify the modification rank requirement if not owned
		else if (subCmd.equals(subCmds[2]) && checkSubPerm(p, "create")) {
			EntityHologram hologram = getHologram(profile, p, true);
			if (args.length < 2)
				throw new CommandException(p, "You must specify the rank that can modify this Hologram!");

			try {
				Rank rank = Rank.fromString(args[1].toUpperCase());
				hologram.setOverridePower(rank.power());
				p.sendMessage(Component.text("Successfully set the minimum rank required to modify this Hologram to ", NamedTextColor.GRAY).append(rank.toComponent()));
			} catch (Exception e) {
				throw new CommandException(p, "Couldn't find rank '" + args[1] + "'");
			}
		}

		// Add Line
		else if (subCmd.equals(subCmds[3])) {
			EntityHologram hologram = getHologram(profile, p, true);

			if (!profile.hasPermission(Permission.HOLOGRAM_EXTRA_LINES) && hologram.getSize() >= NON_STAFF_MAX_LINES)
				throw new CommandException(p, "You cannot create holograms with more than 2 lines of text.");

			if (args.length < 2)
				throw new CommandException(p, "You must specify what you wish to have on line #" + (hologram.getSize() + 1) + " of the hologram!");

			hologram.addComponent(createComponent(profile, p, Arrays.copyOfRange(args, 1, args.length)));
			p.sendMessage(Component.text("Successfully added line #" + hologram.getSize() + " to Hologram.", NamedTextColor.GRAY));
		}

		// Remove Line
		else if (subCmd.equals(subCmds[4])) {
			EntityHologram hologram = getHologram(profile, p, true);
			int lineToDelete = toIntMinMax(p, args[1], 1, hologram.getSize());

			p.sendMessage(Component.text("Successfully removed line #" + lineToDelete + " from the Hologram.", NamedTextColor.GRAY));
			hologram.removeComponent(lineToDelete - 1);
		}

		// Edit Line
		else if (subCmd.equals(subCmds[5])) {
			EntityHologram hologram = getHologram(profile, p, true);
			int lineToEdit = toIntMinMax(p, args[1], 1, hologram.getSize());

			hologram.setComponent(lineToEdit - 1, createComponent(profile, p, Arrays.copyOfRange(args, 2, args.length - 1)));
			p.sendMessage(Component.text("Successfully modified line #" + lineToEdit + " of the Hologram."));
		}

		// Reset Components
		else if (subCmd.equals(subCmds[6])) {
			EntityHologram hologram = getHologram(profile, p, true);
			hologram.setComponents(null);
			p.sendMessage(Component.text("Successfully reset the Hologram's lines."));
		}

		else if (subCmd.equals(subCmds[7])) {
			EntityHologram hologram = getHologram(profile, p, true);
			hologram.setSpaceBetweenComponents(toFloat(p, args[1]));
			p.sendMessage(Component.text("Successfully updated the Hologram's line spacing."));
		}

		return false;
	}

	@Override
	public @Nullable List<String> runTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length == 1)
			return TabCompleter.completeString(args[0], subCmds);
		return Collections.emptyList();
	}

	private TextComponent createComponent(PlayerProfile profile, Player p, String[] args) {
		StringBuilder builder = new StringBuilder();
		int size = args.length;
		for (int x = -1; ++x < size;) {
			String word = args[x].replace("\n", "");
			builder.append(word);
			if (x+1 < size) builder.append(" ");
		}

		if (!profile.hasPermission(Permission.HOLOGRAM_EXTRA_LINES) && builder.length() > NON_STAFF_LENGTH_MAX)
			throw new CommandException(p, "Each line of a Hologram can only hold up to 50 characters (The line you provided was " + builder.length() + " characters long)!");

		TextComponent component = Lore.fastBuild(true, 100, builder.toString()).get(0);
		if (component.content().length() > 100)
			throw new CommandException(p, "Each line of a Hologram can only display up to 100 characters (The line you provided was " + component.content().length() + " characters long)!");

		return Lore.fastBuild(true, 100, builder.toString()).get(0);
	}

	private EntityHologram getHologram(PlayerProfile profile, Player p, boolean permissionCheck) {
		EntityHologram hologram = getEntityInLineOfSightBlockIterator(p, 6);

		if (hologram == null)
			throw new CommandException(p, "You must be looking at a Hologram in order to use this command.");

		if (permissionCheck && profile.getId() != hologram.getCreatorId() && profile.getHighestRank().power() < hologram.getOverridePower())
			throw new CommandException(p, "You don't have permission to modify this Hologram.");
		return hologram;
	}

	private EntityHologram getEntityInLineOfSightBlockIterator(Player p, int range) {
		@NotNull List<org.bukkit.entity.Entity> targetList = p.getNearbyEntities(range, range, range);
		new ArrayList<>(targetList).forEach(entity -> {
			CraftEntity craft = ((CraftEntity)entity);
			if (craft.getHandle() instanceof EntityHologram || craft.getHandle() instanceof EntityHologramLine) return;
			targetList.remove(entity);
		});

		p.sendMessage("Iterating entities: " + targetList.size());

		BlockIterator bi = new BlockIterator(p, range);
		org.bukkit.entity.Entity target = null;
		while(bi.hasNext() && target == null) {
			Block b = bi.next();
			if (b.getType().isSolid()) {
				p.sendMessage("broke free");
				break;
			} else {
				for (org.bukkit.entity.Entity e : targetList) {
					p.sendMessage("checking if block equal to block " + ((CraftEntity)e).getHandle().getClass().toString());
					if (!b.equals(e.getLocation().getBlock())) continue;
					target = e;
					p.sendMessage("found target");
				}
			}
		}
		if (target == null) return null;
		if (((CraftEntity)target).getHandle() instanceof EntityHologramLine line) return line.getHologramBase();
		return (EntityHologram) ((CraftEntity)target).getHandle();
	}

}
