package me.playground.command.commands;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import me.playground.gui.BeanGuiAdminItemValues;
import me.playground.playerprofile.stats.DirtyInteger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;

import me.playground.command.BeanCommand;
import me.playground.command.CommandException;
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
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

public class CommandOp extends BeanCommand {

	public CommandOp(Main plugin) {
		super(plugin, "bean.cmd.op", true, "op");
		description = "Operator Command.";
	}

	final String[] subCmds = { "commands", "customitems", "debug", "itemvalues", "fixformatting", "formatchunks", "guiprofile", "lockserver", "openserver", "pissoff", "previewrank", "shops" };
	final String[] npcSubCmds = { "create", "list", "reload", "setskin", "tphere", "warpto", };
	final String[] shopSubCmds = { "enable", "disable", "reload" };
	
	@Override
	public boolean runCommand(PlayerProfile profile, @Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String str, @Nonnull String[] args) {
		if (args.length < 1)
			return false;
		
		final Player p = isPlayer(sender) ? (Player) sender : null;
		
		final String cmdStr = args[0].toLowerCase();
		final String subcmd = args.length>1 ? args[1].toLowerCase() : "";

		if ("itemvalues".equals(cmdStr)) {
			new BeanGuiAdminItemValues(p, getPlugin().getItemValueManager(), getPlugin().getItemTrackingManager()).openInventory();
			return true;
		}
		
		if ("debug".equals(cmdStr) && checkPlayer(sender)) {
			new BeanGuiDebug(p).openInventory();
			return true;
		}
		
		if ("horsegui".equals(cmdStr) && checkPlayer(sender)) {
			TraderLlama horse = (TraderLlama) p.getWorld().spawnEntity(p.getLocation(), EntityType.TRADER_LLAMA);
			horse.setCarryingChest(true);
			horse.setStrength(5); // 5 * 3 = inventory space lol
			horse.setAI(false);
			horse.setCollidable(false);
			horse.setInvisible(true);
			horse.setInvulnerable(true);
			horse.setSilent(true);
			horse.setAge(-99);
			horse.setAgeLock(true);
			horse.setOwner(p);
			horse.customName(Component.text("Pet Test GUI"));
			horse.setCustomNameVisible(true);
			
			AbstractHorseInventory inv = horse.getInventory();
			inv.setMaxStackSize(100);
			inv.setSaddle(BeanItem.PLAYER_MENU.getItemStack());
			
			horse.getInventory().setItem(5, BeanItem.PLAYER_MENU.getItemStack());
	        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> ((CraftPlayer)p).getHandle().connection.send(new ClientboundHorseScreenOpenPacket(2, 15, horse.getEntityId())), 2L);
			return true;
		}
		
		if ("formatchunks".equals(cmdStr) && checkPlayer(sender)) {
			int dist = args.length > 1 ? toIntDef(args[1], 0) : 0;
			if (dist < 1) dist = 1;
			int totalContainers = 0;
			int totalItems = 0;
			p.sendMessage(Component.text("\u00a77Formatting the contents of \u00a7e" + ((2*dist + 1)^2) + " Chunks\u00a77..."));
			int cX = p.getChunk().getX(); //p.getWorld().getChunkAt(cX).getTileEntities(b -> (b.getState() instanceof Container), true);
			int cZ = p.getChunk().getZ();
			
			for (int x = -1-dist; ++x < dist + 1;)
					for (int z = -1-dist; ++z < dist + 1;) {
						Chunk ch = p.getWorld().getChunkAt(cX + x, cZ + z);
						BlockState[] blockStates = ch.getTileEntities();
						int count = blockStates.length;
						totalContainers += count;
						for (int b = -1; ++b < count;) {
							if (!(blockStates[b] instanceof Container c)) continue;
							ItemStack[] newInv = c.getInventory().getContents();
							int invSize = newInv.length;
							for (byte co = -1; ++co < invSize;) {
								ItemStack i = newInv[co];
								if (i == null) continue;
								totalItems++;
								newInv[co] = BeanItem.formatItem(i);
							}
							c.getInventory().setContents(newInv);
						}
					}
			p.sendMessage(Component.text("\u00a77Done. Updated \u00a7a"+totalItems+"\u00a77 items in \u00a72" + totalContainers + "\u00a77 containers."));
			return true;
		}

		// TODO: Modify after the Villager Rework and then remove once applied on live.
		if ("formatvillagers".equals(cmdStr) && checkPlayer(sender)) {
			int dist = args.length > 1 ? toIntDef(args[1], 0) : 0;
			if (dist < 1) dist = 1;
			DirtyInteger totalVillagers = new DirtyInteger(0);
			DirtyInteger totalTradesModified = new DirtyInteger(0);
			p.sendMessage(Component.text("\u00a77Formatting all villagers within a \u00a7e" + dist + " Chunk Radius\u00a77..."));

			p.getWorld().getNearbyEntitiesByType(Villager.class, p.getLocation(), dist * 16).forEach(villager -> {
				totalVillagers.addToValue(1);
				if (villager.getProfession() != null) {
					int count = villager.getRecipeCount();
					for (int x = count; --x > -1;) {
						MerchantRecipe recipe = villager.getRecipe(x);

						// Fix up mending
						if (recipe.getResult().getType() == Material.ENCHANTED_BOOK) {
							EnchantmentStorageMeta meta = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();
							if (meta.hasStoredEnchant(Enchantment.MENDING)) {
								recipe.setDemand(0);
								recipe.setMaxUses(0);
								recipe.setUses(0);
								recipe.setIgnoreDiscounts(true);
								villager.setRecipe(x, recipe);
							}
						}

						totalTradesModified.addToValue(1);

						// Replace recipe
						List<ItemStack> newIngredients = recipe.getIngredients();
						newIngredients.forEach(BeanItem::formatItem);
						MerchantRecipe newRecipe = new MerchantRecipe(BeanItem.formatItem(recipe.getResult()), recipe.getUses(), recipe.getMaxUses(),
								recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice(), recipe.shouldIgnoreDiscounts());
						newRecipe.setIngredients(newIngredients);
						villager.setRecipe(x, newRecipe);
					}
				}
			});
			p.sendMessage(Component.text("\u00a77Done. Updated \u00a7a"+totalTradesModified.getValue()+"\u00a77 trades among \u00a72" + totalVillagers.getValue() + "\u00a77 Villagers."));
			return true;
		}
		
		if ("previewrank".equals(cmdStr) && checkPlayer(sender)) {
			try {
				if (args.length < 2 || args[1].equalsIgnoreCase("stop") || args[1].equalsIgnoreCase("none")) {
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
					sb.append(ChatColor.DARK_GRAY + "" + npc.getEntityId() + ChatColor.GRAY + " - " + ChatColor.AQUA + npc.getEntity() .getBukkitEntity().getName() + "\n");
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
			} /*else if ("settitle".equals(subcmd)) {
				if (args.length<3) {
					sender.sendMessage("\u00a7cUsage: \u00a7f/op npc "+subcmd+" \u00a77<id> <title>");
					return true;
				}

				NPC<?> npc;
				try {
					npc = npcManager.getEntityNPC(Integer.parseInt(args[2]));
				} catch (Exception e) {
					sender.sendMessage("\u00a7cAn NPC with the ID of '"+args[2]+"' does not exist!");
					return true;
				}

				if (args.length > 3) {
					StringBuilder builder = new StringBuilder();
					int size = args.length;
					for (int x = 3; ++x < size;) {
						String word = args[x].replace("\n", "");
						builder.append(word);
						if (x+1 < size) builder.append(" ");
					}

					npc.setHologramText(Lore.getBuilder(builder.toString()).build().getLore());
					sender.sendMessage("\u00a77Hologram of NPC updated.");
				} else {
					npc.removeHologram();
					sender.sendMessage("\u00a77Hologram of NPC removed.");
				}

			}
			return true;*/
		}
		
		if ("shops".equals(cmdStr) || "shop".equals(cmdStr)) {
			if ("enable".equals(subcmd)) {
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
			} else if ("customitems".equals(cmdStr)) {
				new BeanGuiBeanItems(p).openInventory();
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
			if (args.length == 2)
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
	public Component getUsage(@Nonnull CommandSender sender, @NotNull String str, String @NotNull [] args) {
		return Component.text("\u00a7cUsage: \u00a7f"+str+" ").append(usageArguments[0]);
	}

}
