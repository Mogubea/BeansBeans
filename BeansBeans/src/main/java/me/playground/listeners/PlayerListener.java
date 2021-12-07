package me.playground.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;

import club.minnced.discord.webhook.WebhookClient;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.playground.celestia.logging.CelestiaAction;
import me.playground.data.Datasource;
import me.playground.enchants.BeanEnchantment;
import me.playground.gui.BeanGui;
import me.playground.items.BItemFishingRod;
import me.playground.items.BeanItem;
import me.playground.listeners.events.PlayerInteractNPCEvent;
import me.playground.listeners.events.PlayerRightClickHarvestEvent;
import me.playground.loot.LootRetriever;
import me.playground.loot.LootTable;
import me.playground.loot.RetrieveMethod;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.skills.BxpValues;
import me.playground.playerprofile.skills.SkillData;
import me.playground.playerprofile.skills.SkillType;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

public class PlayerListener extends EventListener {
	
	public PlayerListener(Main plugin) {
		super(plugin);
		
		advancementCoins.put(Main.key("deepslateemeraldore"), 500);
		advancementCoins.put(Main.key("minerscollection"), 500);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncChatEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		PlayerProfile pp = PlayerProfile.from(p);
		
		if (PlayerProfile.from(p).onCdElseAdd("chat", 400)) {
			p.sendActionBar(Component.text("\u00a7cYou are sending messages too fast!"));
			return;
		}
		
		TextComponent chat = pp.isRank(Rank.MODERATOR) ? Component.empty().append(Component.text("\u24E2").color(Rank.MODERATOR.getRankColour()))
				.hoverEvent(HoverEvent.showText(Component.text("Staff Member").color(Rank.MODERATOR.getRankColour())))
				.append(Component.text(" "))
				.append(pp.getComponentName())
				: pp.getComponentName();
		
		final String content = ((TextComponent)e.message()).content();
		final ArrayList<UUID> pinged = new ArrayList<UUID>();
		
		// @Name and /Command formatting.
		if (content.contains("@") || content.contains("/")) {
			final String[] spaceSplit = ((TextComponent)e.message()).content().split(" ");
			Component newMessage = Component.text("");
			for (String word : spaceSplit)
				// Handle looking for pings within the message.
				if (word.startsWith("@")) {
					try {
						Player ping = Utils.playerPartialMatch(word.substring(1));
						if (pinged.contains(ping.getUniqueId()))
							throw new RuntimeException();
						
						PlayerProfile prof = PlayerProfile.from(ping);
						pinged.add(ping.getUniqueId()); // Add player to the pinged list so we can colour the message a bit more too.
						if (prof.isSettingEnabled(PlayerSetting.PING_SOUNDS))
							ping.playSound(ping.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.3F, 0.7F);
						newMessage = newMessage.append(Component.text("@").color(prof.getNameColour()).append(prof.getComponentName()).append(Component.text(" ")));
					} catch (Exception ex) {
						newMessage = newMessage.append(Component.text(word + " "));
					}
				}
				else if (word.startsWith("/"))
					newMessage = newMessage.append(toCommand(word.substring(1)));
				else
					newMessage = newMessage.append(Component.text(word + " "));
			e.message(newMessage);
		}
		
		chat = chat.append(Component.text("\u00a78 » \u00a7r").append(e.message()));
		
		for (Player pl : Bukkit.getOnlinePlayers()) {
			PlayerProfile ppl = PlayerProfile.from(pl);
			if (pp.isRank(Rank.MODERATOR) || !ppl.getIgnoredPlayers().contains(pp.getId()))
				pl.sendMessage(chat.colorIfAbsent(TextColor.color(pinged.contains(pl.getUniqueId()) ? 0xffffff : 0xe8e8e8)));
		}
		
		getPlugin().getLogger().info("[CHAT] " + pp.getDisplayName() + ": " + content);
		Datasource.logCelestia(CelestiaAction.CHAT, e.getPlayer(), e.getPlayer().getLocation(), content);
		
		WebhookClient client = getPlugin().discord().getWebhookClient(pp.getId());
		client.send(content);
	}
	
	private Component toCommand(String cmd) {
		final PluginCommand pc = Bukkit.getServer().getPluginCommand(cmd);
		if (pc != null)
			return Component.text("/"+cmd+" ").hoverEvent(HoverEvent.showText(Component.text(pc.getDescription()))).clickEvent(ClickEvent.suggestCommand("/"+cmd.toLowerCase())).color(BeanColor.COMMAND);
		else
			return Component.text("/"+cmd+" ");
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		PlayerProfile pp = PlayerProfile.from(p);
		
		Location old = p.getLocation();
		final Location loc = new Location(p.getWorld(), old.getX(), old.getY(), old.getZ(), old.getYaw(), old.getPitch());
		pp.updateLastLocation(loc, 0);
		
		if (!e.getKeepInventory()) // Just to be safe.
			e.getDrops().remove(BeanGui.menuItem);
		
		TextReplacementConfig c = TextReplacementConfig.builder().match("\u24E2 ").replacement("").build();
		TextReplacementConfig c2 = TextReplacementConfig.builder().match(p.getName()).replacement(pp.getComponentName()).build();
		
		e.deathMessage(Component.text("\u00a77» ").append(e.deathMessage().color(TextColor.color(0xff9999))).replaceText(c).replaceText(c2));
		
		Bukkit.getOnlinePlayers().forEach(player -> { if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_DEATH_MESSAGES)) { player.sendMessage(e.deathMessage()); }});
		
		e.deathMessage(null);
	}
	
	@EventHandler
	public void onBedEnter(PlayerBedEnterEvent e) {
		final Region r = getRegionAt(e.getBed().getLocation());
		Player p = e.getPlayer();
		p.setStatistic(Statistic.TIME_SINCE_REST, 0);
		
		if (!r.getEffectiveFlag(Flags.WARP_CREATION) && !(r.getMember(p).higherThan(MemberLevel.VISITOR)))
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onClickEntity(PlayerInteractEntityEvent e) {
		Entity ent = e.getRightClicked();
		// Villager
		if (ent.getType() == EntityType.VILLAGER) {
			final Region r = getRegionAt(ent.getLocation());
			if (r.getEffectiveFlag(Flags.VILLAGER_ACCESS).higherThan(r.getMember(e.getPlayer()))) {
				e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to trade here."));
				e.getPlayer().playSound(ent.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7F, 1F);
				e.setCancelled(true);
			}
		} else 
		// Frames and such
		if (ent.getType() == EntityType.GLOW_ITEM_FRAME || ent.getType() == EntityType.ITEM_FRAME || ent.getType() == EntityType.LEASH_HITCH) {
			final Region r = getRegionAt(ent.getLocation());
			if (r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(e.getPlayer()))) {
				e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to build here."));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onArmourStand(PlayerArmorStandManipulateEvent e) {
		final Region r = getRegionAt(e.getRightClicked().getLocation());
		if (r.getEffectiveFlag(Flags.BUILD_ACCESS).higherThan(r.getMember(e.getPlayer()))) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to build here."));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketFillEvent e) {
		Region r = getRegionAt(e.getBlockClicked().getLocation());
		final boolean canBuild = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(e.getPlayer()));
		if (!canBuild) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to use buckets here."));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketEmptyEvent e) {
		Region r = getRegionAt(e.getBlockClicked().getLocation());
		final boolean canBuild = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(e.getPlayer()));
		if (!canBuild) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to use buckets here."));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketEntityEvent e) {
		Region r = getRegionAt(e.getEntity().getLocation());
		final boolean canBuild = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(e.getPlayer()));
		if (!canBuild) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to use buckets here."));
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onInteractDumb(PlayerInteractEvent e) {
		// Cancel mob destruction of farmland.
		if (e.getAction() == Action.PHYSICAL && e.getClickedBlock().getType() == Material.FARMLAND) {
			e.setCancelled(true);
			return;
		}
		
		// Stop here if unnecessary to check
		if (e.getHand() != EquipmentSlot.HAND || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR) return;
		
		// Cancel the cleansing of custom leather items
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.WATER_CAULDRON && e.getItem() != null && 
				e.getItem().getItemMeta() instanceof LeatherArmorMeta && BeanItem.from(e.getItem()) != null) {
			e.setCancelled(true);
			return;
		}
		
		// Perform region permission checklist
		Region r = getRegionAt(e.getClickedBlock().getLocation());
		final Player p = e.getPlayer();
		
		final boolean canBuild = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(p));
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!p.isBlocking()) {
				if (!canBuild) {
					final Material bm = e.getClickedBlock().getType();
					final BlockState state = e.getClickedBlock().getState();
					String error = null;
					
					if (state instanceof Container) {
						if (r.getEffectiveFlag(Flags.CONTAINER_ACCESS).higherThan(r.getMember(p)))
							error = "You don't have permission to open containers here.";
					} else if (bm.name().endsWith("DOOR") || bm.name().endsWith("GATE")) {
						if (r.getEffectiveFlag(Flags.DOOR_ACCESS).higherThan(r.getMember(p)))
							error = "You don't have permission to open doors here.";
					} else if (bm.name().endsWith("ANVIL")) {
						if (r.getEffectiveFlag(Flags.ANVIL_ACCESS).higherThan(r.getMember(p))) {
							error = "You don't have permission to use anvils here.";
						// Handle anvil unbreakable flag by giving the Player a fake Anvil GUI.
						} else if (!r.getEffectiveFlag(Flags.ANVIL_DEGRADATION)) {
							e.setCancelled(true);
							p.openAnvil(p.getLocation(), true);
							return;
						}
					} else if (bm == Material.FLOWER_POT || bm.name().startsWith("POTTED")) {
						error = "You don't have permission to decorate here.";
					} else if (bm == Material.NOTE_BLOCK) {
						error = "You don't have permission to tune notes here.";
					} else if (bm == Material.JUKEBOX) {
						error = "You don't have permission to use music disks here.";
					} else if (bm == Material.REDSTONE_WIRE) {
						error = "You don't have permission to alter redstone here.";
					}
					
					if (error != null) {
						p.sendActionBar(Component.text("\u00a7c" + error));
						e.setCancelled(true);
						return;
					}
				}
			}
		}
		
		final ItemStack i = e.getItem();
		if (i == null) return;
		final Material m = i.getType();
		
		if (!canBuild) {
			String error = null;

			if (m.name().endsWith("_SPAWN_EGG")) {
				error = "You don't have permission to spawn mobs here.";
			} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				final Material bm = e.getClickedBlock().getType();
				if (m == Material.BONE_MEAL) {
					if (e.getClickedBlock().getBlockData() instanceof Ageable ? !r.getEffectiveFlag(Flags.CROP_ACCESS).lowerOrEqTo(r.getMember(p)) : true)
						error = "You don't have permission to use bonemeal here.";
				} else if (bm.name().endsWith("SIGN") && (m.name().endsWith("DYE") || m == Material.GLOW_INK_SAC)) {
					error = "You don't have permission to dye signs here.";
				} else if (m == Material.SHEARS && bm == Material.PUMPKIN) {
					error = "You don't have permission to carve pumpkins here.";
				} else if (m.name().endsWith("BOAT") || m.name().endsWith("MINECART")) {
					error = "You don't have permission to place vehicles here.";
				} else if (m == Material.END_CRYSTAL || m == Material.ARMOR_STAND || m == Material.GLOW_ITEM_FRAME || m == Material.ITEM_FRAME || m == Material.PAINTING || m == Material.LEAD) {
					error = "You don't have permission to build here.";
				}
			}
			
			if (error != null) {
				p.sendActionBar(Component.text("\u00a7c" + error));
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClick(PlayerInteractEvent e) {
		ItemStack item = e.getItem();
		
		if (e.useInteractedBlock() == Result.DENY) return;
		
		if (item != null) {
			// Prevent being able to change spawner types with Spawn Eggs.
			if (item.getType().name().endsWith("_SPAWN_EGG") && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.SPAWNER) {
				e.setCancelled(true);
				return;
			}
			
			BeanItem custom = BeanItem.from(item);
			if (custom != null)
				custom.onInteract(e);
		}
		
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Block b = e.getClickedBlock();
			final Player p = e.getPlayer();
			final Region r = getRegionAt(e.getClickedBlock().getLocation());
			//final boolean canBuildHere = r.getEffectiveFlag(Flags.BLOCK_PLACE).lowerOrEqTo(r.getMember(p));
			
			if (b != null) {
				Material m = b.getType();
				
				// Sign
				/*if (item == null && m.toString().endsWith("_SIGN")) {
					if (canBuildHere) {
						Sign sign = (Sign) b.getState();
						if (!sign.isEditable()) { // remove in future if a new world is made.
							sign.setEditable(true);
							sign.update();
						}
						PacketPlayOutOpenSignEditor packet = new PacketPlayOutOpenSignEditor(new BlockPosition(b.getX(), b.getY(), b.getZ()));
						((CraftPlayer) p).getHandle().b.sendPacket(packet);
						return;
					} else {
						p.sendActionBar(Component.text("\u00a7cYou don't have permission to edit signs here."));
					}
				}*/
				
				
				// Farm shit
				
				if (b.getBlockData() instanceof Ageable) {
					Ageable crop = (Ageable) b.getBlockData();
					
					if (crop.getAge() < crop.getMaximumAge())
						return;
					if (m == Material.SWEET_BERRY_BUSH || m == Material.BAMBOO || m.name().endsWith("STEM"))
						return;
					
					final boolean canHarvest = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(p)) || r.getEffectiveFlag(Flags.CROP_ACCESS).lowerOrEqTo(r.getMember(p));
					
					if (!canHarvest) {
						p.sendActionBar(Component.text("\u00a7cYou don't have permission to harvest crops here."));
						return;
					}
					
					PlayerRightClickHarvestEvent event = new PlayerRightClickHarvestEvent(e.getPlayer(), e.getItem(), e.getClickedBlock(), e.getBlockFace());
					Bukkit.getServer().getPluginManager().callEvent(event);
					
					if (event.isCancelled())
						return;
					
					for (ItemStack i : b.getDrops()) {
						i.setAmount(Math.max(1, i.getAmount()-1));
						e.getPlayer().getWorld().dropItemNaturally(b.getLocation(), i);
					}
					
					try {
						PacketContainer arm = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ANIMATION);
			            arm.getEntityModifier(p.getWorld()).write(0, p);
			            ProtocolLibrary.getProtocolManager().sendServerPacket(p, arm);
					} catch (InvocationTargetException e1) {
						e1.printStackTrace();
					}
					
					e.setCancelled(true);
					crop.setAge(0);
					b.setBlockData(crop);
					
					PlayerProfile.from(e.getPlayer()).getSkills().addXp(SkillType.AGRICULTURE, BxpValues.getFarmingValue(m));
				}
			}
		}
	}
	
	final float DEFAULT_MOVESPEED = 0.2F;
	final ArrayList<UUID> talarianUsers = new ArrayList<UUID>();
	
	@EventHandler
	public void onChangeArmour(PlayerArmorChangeEvent e) {
		if (e.getSlotType() == SlotType.FEET) {
			boolean ack = talarianUsers.contains(e.getPlayer().getUniqueId());
			if (BeanItem.is(e.getNewItem(), BeanItem.TALARIANS)) {
				if (!ack)
					talarianUsers.add(e.getPlayer().getUniqueId());
			} else if (ack) {
				talarianUsers.remove(e.getPlayer().getUniqueId());
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (BeanItem.is(e.getPlayer().getInventory().getBoots(), BeanItem.TALARIANS))
			if (!talarianUsers.contains(e.getPlayer().getUniqueId()))
				talarianUsers.add(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		final Player p = e.getPlayer();
		if (e.hasExplicitlyChangedBlock()) {
			if (talarianUsers.contains(p.getUniqueId())) {
				if (p.isSprinting() && !p.isSwimming()) {
					boolean fast = p.getWalkSpeed() > 0.24F;
					if (((LivingEntity)p).isOnGround()) {
						if (p.getWalkSpeed() < 0.25F)
							p.setWalkSpeed(p.getWalkSpeed()+0.0018F);
						if (fast) {
							p.playSound(p.getLocation(), Sound.BLOCK_GRASS_STEP, 0.2F, 0.1F);
							p.spawnParticle(Particle.CLOUD, p.getLocation(), 2, 0D, 0.4D, 0D, 0.01D);
						}
					}
					if (fast)
						if (getPlugin().getRandom().nextInt(65)==1)
							Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent(p, p.getInventory().getBoots(), 1));
				} else if (p.getWalkSpeed() != DEFAULT_MOVESPEED) {
					p.setWalkSpeed(DEFAULT_MOVESPEED);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockHarvest(PlayerHarvestBlockEvent e) {
		final Material m = e.getHarvestedBlock().getType();
		
		if (m == Material.COMPOSTER) // No need to block that
			return;
		
		final Region r = getRegionAt(e.getHarvestedBlock().getLocation());
		if (!(r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(e.getPlayer())) || r.getEffectiveFlag(Flags.CROP_ACCESS).lowerOrEqTo(r.getMember(e.getPlayer())))) {
			e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to harvest crops here."));
			e.setCancelled(true);
			return;
		} else {
			PlayerProfile.from(e.getPlayer()).getSkills().addXp(SkillType.AGRICULTURE, BxpValues.getFarmingValue(m));
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				Player p = e.getPlayer();
				p.setHealth(10);
				p.setSaturation(2.0F);
				p.setFoodLevel(15);
			}
		});
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.CHORUS_FRUIT || e.getCause() == TeleportCause.ENDER_PEARL) {
			final Region r = getPlugin().regionManager().getRegion(e.getTo());
			if (!r.getEffectiveFlag(Flags.ENDERPEARLS) && r.getMember(e.getPlayer()).lowerThan(MemberLevel.MEMBER)) {
				e.setCancelled(true);
				e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to ender warp into here."));
			}
			return;
		}
		
		if (e.getCause() == TeleportCause.COMMAND || e.getCause() == TeleportCause.PLUGIN || e.getCause() == TeleportCause.NETHER_PORTAL) {
			if (e.getTo().getWorld() != e.getFrom().getWorld()) {
				final Region r = getPlugin().regionManager().getWorldRegion(e.getTo().getWorld());
				final boolean canWarpInto = r.getEffectiveFlag(Flags.TELEPORT_IN) || r.getMember(e.getPlayer()).equals(MemberLevel.MASTER);
				if (!canWarpInto) {
					e.setCancelled(true);
					e.getPlayer().sendActionBar(Component.text("\u00a7cYou don't have permission to teleport into \u00a7r"+e.getTo().getWorld().getName()).color(BeanColor.WORLD));
					return;
				}
			}
			if (e.getCause() != TeleportCause.NETHER_PORTAL) {
				e.getPlayer().setFallDistance(0F);
				PlayerProfile.from(e.getPlayer()).updateLastLocation(e.getFrom(), 0);
			}
		}
		
	}
	
	@EventHandler
	public void onItemMend(PlayerItemMendEvent e) {
		e.setCancelled(true);
		BeanItem.addDurability(e.getItem(), e.getRepairAmount());
	}
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		BeanItem.reduceItemDurabilityBy(e.getItem(), e.getDamage());
	}
	
	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		GameMode fromGm = p.getGameMode();
		GameMode toGm = e.getNewGameMode();
		if (toGm != p.getGameMode()) {
			if (hasGmPerm(p, toGm)) {
				if (!p.hasPermission("bean.gm.spectator")) {
					if (toGm == GameMode.SPECTATOR)
						PlayerProfile.from(p).updateLastLocation(p.getLocation(), 1);
					else if (fromGm == GameMode.SPECTATOR)
						p.teleport(PlayerProfile.from(p).getLastLocation(1), TeleportCause.END_GATEWAY);
				}
				p.sendMessage(Component.text("\u00a77You are now in \u00a7f" + Utils.firstCharUpper(toGm.name()) + " Mode\u00a77."));
			} else {
				e.setCancelled(true);
			}
		}
	}
	
	private final HashMap<NamespacedKey, Integer> advancementCoins = new HashMap<NamespacedKey, Integer>(2);
	
	@EventHandler
	public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
		Player p = e.getPlayer();
		PlayerProfile pp = PlayerProfile.from(p);
		
		if (advancementCoins.containsKey(e.getAdvancement().getKey()))
			pp.addToBalance(advancementCoins.get(e.getAdvancement().getKey()), "Advancement: " + e.getAdvancement().getKey().asString());
		
		if (e.message() != null) {
			TranslatableComponent ack = (TranslatableComponent) e.message();
			Component msg = Component.text("\u00a7e» ").append(pp.getComponentName()).append(Component.text(" has achieved ").append(ack.args().get(ack.args().size()-1))).color(TextColor.color(0xfff7a9));
			Bukkit.getOnlinePlayers().forEach(player -> { if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_ACHIEVEMENTS)) { player.sendMessage(msg); }});
			e.message(null);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onConsume(PlayerItemConsumeEvent e) {
		PlayerProfile.from(e.getPlayer()).getHeirlooms().doPlayerItemConsumeEvent(e);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerFish(PlayerFishEvent e) {
		final Player p = e.getPlayer();
		
		switch(e.getState()) {
		case CAUGHT_ENTITY: // Protect animals and villagers from being reeled
			if (!(e.getCaught() instanceof Animals || e.getCaught() instanceof Villager)) return;
			
			final Region r = getRegionAt(e.getCaught().getLocation());
			if (r.getEffectiveFlag(Flags.PROTECT_ANIMALS) && r.getMember(p).lowerThan(MemberLevel.MEMBER)) {
				p.sendActionBar(Component.text("\u00a7cYou don't have permission to fish animals here."));
				e.getHook().remove();
				e.setCancelled(true);
			}
			break;
		default:
			break;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerFishing(PlayerFishEvent e) {
		final Player p = e.getPlayer();
		
		BeanItem bi = BeanItem.from(p.getInventory().getItemInMainHand());
		if (bi instanceof BItemFishingRod)
			((BItemFishingRod)bi).onFish(e);
		
		if (e.isCancelled()) return;
		
		switch(e.getState()) {
		case CAUGHT_FISH: // Fishing
			Item caught = (Item) e.getCaught();
			final LootTable table = getPlugin().lootManager().getLootTable("fishing");
			ItemStack rod = p.getInventory().getItemInMainHand();
			if (rod.getType() != Material.FISHING_ROD)
				rod = p.getInventory().getItemInOffHand();
			
			if (table != null)
				caught.setItemStack(LootRetriever.from(table, RetrieveMethod.CUMULATIVE_CHANCE, p)
						.luck(PlayerProfile.from(p).getLuck() + rod.getEnchantmentLevel(Enchantment.LUCK))
						.biome(e.getHook().getLocation().getBlock().getBiome())
						.burn(rod.getEnchantmentLevel(BeanEnchantment.SEARING) > 0)
						.getLoot().get(0));
			
			SkillData sd = PlayerProfile.from(e.getPlayer()).getSkills();
			sd.addXp(SkillType.FISHING, 43 * e.getExpToDrop());
			break;
		default:
			break;
		}
	}
	
	@EventHandler
	public void onNPCInteract(PlayerInteractNPCEvent e) {
		e.getPlayer().sendMessage("That's an NPC (DBID: "+e.getNPC().getDatabaseId()+")!");
	}
	
	private boolean hasGmPerm(Player p, GameMode gm) {
		boolean ye = gm == GameMode.SURVIVAL || p.hasPermission("bean.gm." + gm.name().toLowerCase());
		if (!ye && gm == GameMode.SPECTATOR && p.hasPermission("bean.gm.moderator"))
			ye = true;
		
		return ye;
	}
	
}
