package me.playground.listeners;

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
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Lightable;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.playground.celestia.logging.CelestiaAction;
import me.playground.data.Datasource;
import me.playground.enchants.BeanEnchantment;
import me.playground.items.BItemFishingRod;
import me.playground.items.BeanItem;
import me.playground.listeners.events.PlayerInteractNPCEvent;
import me.playground.listeners.events.PlayerRightClickHarvestEvent;
import me.playground.listeners.events.SkillLevelUpEvent;
import me.playground.loot.LootRetriever;
import me.playground.loot.LootTable;
import me.playground.loot.RetrieveMethod;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.settings.PlayerSetting;
import me.playground.playerprofile.stats.StatType;
import me.playground.ranks.Permission;
import me.playground.ranks.Rank;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.regions.flags.MemberLevel;
import me.playground.skills.Skill;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class PlayerListener extends EventListener {
	
	public PlayerListener(Main plugin) {
		super(plugin);
		
		advancementCoins.put(plugin.getKey("deepslateemeraldore"), 500);
		advancementCoins.put(plugin.getKey("minerscollection"), 500);
	}
	
	@EventHandler
	public void onPlayerChat(AsyncChatEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		PlayerProfile pp = PlayerProfile.from(p);
		
		if (pp.onCdElseAdd("chat", 400)) {
			p.sendActionBar(Component.text("\u00a7cYou are sending messages too fast!"));
			return;
		}
		
		pp.getStats().addToStat(StatType.GENERIC, "chatMessages", 1, true);
		
		TextComponent chat = pp.isRank(Rank.MODERATOR) ? Component.empty().append(Component.text("\u24E2", BeanColor.STAFF)
				.hoverEvent(HoverEvent.showText(Component.text("Staff Member", BeanColor.STAFF))))
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
						Player ping = getPlugin().searchForPlayer(word.substring(1));
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
		
		chat = chat.append(Component.text("\u00a78 � \u00a7r").append(e.message()));
		
		for (Player pl : Bukkit.getOnlinePlayers()) {
			PlayerProfile ppl = PlayerProfile.from(pl);
			if (pp.isRank(Rank.MODERATOR) || !ppl.getIgnoredPlayers().contains(pp.getId()))
				pl.sendMessage(chat.colorIfAbsent(TextColor.color(pinged.contains(pl.getUniqueId()) ? 0xffffff : 0xe8e8e8)));
		}
		
		Datasource.logCelestia(CelestiaAction.CHAT, e.getPlayer(), e.getPlayer().getLocation(), content); // Logs
		getPlugin().getLogger().info("[CHAT] " + pp.getDisplayName() + ": " + content); // Console
		getPlugin().getDiscord().sendWebhookMessage(pp.getId(), content); // Discord Chat
		getPlugin().getWebChatServer().sendWebMessage(pp.getId(), content); // Web Chat
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
		
		if (pp.isSettingEnabled(PlayerSetting.MENU_ITEM))
			e.getDrops().remove(BeanItem.PLAYER_MENU.getOriginalStack());
		
		TextReplacementConfig c = TextReplacementConfig.builder().match("\u24E2 ").replacement("").build();
		TextReplacementConfig c2 = TextReplacementConfig.builder().match(p.getName()).replacement(pp.getComponentName()).build();
		
		e.deathMessage(Component.text("\u00a77� ").append(e.deathMessage().color(TextColor.color(0xff9999))).replaceText(c).replaceText(c2));
		
		getPlugin().getServer().getOnlinePlayers().forEach(player -> { if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_DEATH_MESSAGES)) { player.sendMessage(e.deathMessage()); }});
		
		e.deathMessage(null);
	}
	
	@EventHandler
	public void onBedEnter(PlayerBedEnterEvent e) {
		Player p = e.getPlayer();
		p.setStatistic(Statistic.TIME_SINCE_REST, 0);
		
		enactRegionPermission(getRegionAt(e.getBed().getLocation()), e, p, Flags.WARP_CREATION, null);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onClickEntity(PlayerInteractEntityEvent e) {
		Entity ent = e.getRightClicked();
		
		if (ent.getType() == EntityType.VILLAGER) { // Villager
			if (!enactRegionPermission(getRegionAt(ent.getLocation()), e, e.getPlayer(), Flags.VILLAGER_ACCESS, "trade"))
				e.getPlayer().playSound(ent.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6F, 1F);
		} else if (ent.getType() == EntityType.GLOW_ITEM_FRAME || ent.getType() == EntityType.ITEM_FRAME || ent.getType() == EntityType.LEASH_HITCH) { // Frames and such
			enactRegionPermission(getRegionAt(ent.getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "build");
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onArmourStand(PlayerArmorStandManipulateEvent e) {
		enactRegionPermission(getRegionAt(e.getRightClicked().getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "use stands");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketFillEvent e) {
		enactRegionPermission(getRegionAt(e.getBlockClicked().getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "use buckets");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketEmptyEvent e) {
		enactRegionPermission(getRegionAt(e.getBlockClicked().getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "use buckets");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBucket(PlayerBucketEntityEvent e) {
		enactRegionPermission(getRegionAt(e.getEntity().getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "use buckets");
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onInteractDumb(PlayerInteractEvent e) {
		final Block block = e.getClickedBlock();
		
		// Cancel mob destruction of farmland.
		if (e.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND) {
			e.setCancelled(true);
			return;
		}
		
		// Stop here if unnecessary to check
		if (e.getHand() != EquipmentSlot.HAND || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;
		
		final Player p = e.getPlayer();
		
		// Check Spawner spawncount
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.SPAWNER && e.getItem() == null && !PlayerProfile.from(p).onCdElseAdd("spawnerCheck", 500, true)) {
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			int spawns = spawner.getPersistentDataContainer().getOrDefault(getPlugin().getKey("spawnerspawns"), PersistentDataType.INTEGER, 0);
			Component creature = Component.translatable(spawner.getSpawnedType().translationKey());
			
			p.sendActionBar(Component.text("\u00a78(" + (block.isBlockPowered() ? "\u00a7cDisabled" : "\u00a7aActive") + "\u00a78)\u00a7r This ").append(
					creature.color(NamedTextColor.WHITE).append(Component.text(" ").append(Component.translatable(block.translationKey()))
							.append(Component.text("\u00a7r has spawned \u00a7f" + spawns + " \u00a7r")))
							.append(creature)).append(Component.text("(s)")).colorIfAbsent(TextColor.color(0xeeeeee)));
			p.playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.2F, 0.8F);
			doArmSwing(e.getPlayer());
			return;
		}
		
		// Cancel the cleansing of custom leather items
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.WATER_CAULDRON && e.getItem() != null && 
				e.getItem().getItemMeta() instanceof LeatherArmorMeta && BeanItem.from(e.getItem()) != null) {
			e.setCancelled(true);
			return;
		}
		
		final Region region = getRegionAt(e.getClickedBlock().getLocation());
		final boolean canBuild = checkRegionPermission(region, e, p, Flags.BUILD_ACCESS);
		final Material blockMat = block.getType();
		final ItemStack item = e.getItem();
		
		// Region Permission Checks - Regardless of items
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !p.isBlocking()) {
			if (block.getState() instanceof Container) { // Using Containers
				enactRegionPermission(region, e, p, Flags.CONTAINER_ACCESS, "use containers"); return;
			} else if (blockMat.name().endsWith("DOOR") || blockMat.name().endsWith("GATE")) { // Using Doors
				enactRegionPermission(region, e, p, Flags.DOOR_ACCESS, "open door"); return;
			} else if (blockMat.name().endsWith("ANVIL")) { // Using Anvils
				if (!enactRegionPermission(region, e, p, Flags.ANVIL_ACCESS, "use anvils")) return;
				if (!region.getEffectiveFlag(Flags.ANVIL_DEGRADATION)) { // Handle anvil unbreakable flag by giving the Player a fake Anvil GUI.
					e.setCancelled(true);
					p.openAnvil(p.getLocation(), true);
				}
				return;
			} else if (blockMat == Material.FLOWER_POT || blockMat.name().startsWith("POTTED")) { // Flower Pots
				enactRegionPermission(canBuild, e, p, "plant flowers"); return;
			} else if (blockMat == Material.NOTE_BLOCK) { // Tune Note Blocks
				enactRegionPermission(canBuild, e, p, "tune notes"); return;
			} else if (blockMat == Material.JUKEBOX) { // Mess with Music Discs
				enactRegionPermission(canBuild, e, p, "play music"); return;
			} else if (blockMat == Material.REDSTONE_WIRE) { // Mess with Redstone wires
				enactRegionPermission(canBuild, e, p, "alter redstone"); return;
			}
		}
		
		if (item == null) return;
		final Material itemMat = item.getType();
		
		if (itemMat.name().endsWith("_SPAWN_EGG")) { // Spawn Mobs
			enactRegionPermission(canBuild, e, p, "spawn mobs"); return;
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (itemMat == Material.BONE_MEAL) { // Bone Meal
				enactRegionPermission(canBuild || (block.getBlockData() instanceof Ageable ? checkRegionPermission(region, e, p, Flags.CROP_ACCESS) : canBuild), e, p, "apply Bone Meal"); return;
			} else if (blockMat.name().endsWith("SIGN") && (itemMat.name().endsWith("DYE") || itemMat == Material.GLOW_INK_SAC)) { // Dye Signs
				enactRegionPermission(canBuild, e, p, "dye signs"); return;
			} else if (itemMat == Material.SHEARS && blockMat == Material.PUMPKIN) { // Carve Pumpkins 
				enactRegionPermission(canBuild, e, p, "carve pumpkins"); return;
			} else if (itemMat.name().endsWith("BOAT") || itemMat.name().endsWith("MINECART")) { // Placing Vehicles
				enactRegionPermission(canBuild, e, p, "place vehicles"); return;
			} else if (itemMat == Material.END_CRYSTAL || itemMat == Material.ARMOR_STAND || itemMat == Material.GLOW_ITEM_FRAME || itemMat == Material.ITEM_FRAME || itemMat == Material.PAINTING || itemMat == Material.LEAD) {
				enactRegionPermission(canBuild, e, p, "build"); return;
			} else if (itemMat.name().endsWith("_DYE") && (blockMat.name().endsWith("_WOOL") || (blockMat.name().endsWith("_CARPET") && blockMat != Material.MOSS_CARPET))) { // Dye Wool and Carpets
				PlayerProfile pp = PlayerProfile.from(p);
				if (!(pp.isSettingEnabled(PlayerSetting.QUICK_WOOL_DYE) && pp.hasPermission(Permission.QUICK_WOOL_DYE))) return;
				String itemMatName = itemMat.name();
				String starter = itemMatName.substring(0, itemMatName.length() - 4);
				if (blockMat.name().startsWith(starter)) return; // Stop it if they're the same colour
				if (!enactRegionPermission(canBuild, e, p, "dye wool")) return;
				
				String ending = blockMat.name().endsWith("T") ? "_CARPET" : "_WOOL";
				block.setType(Material.valueOf(starter + ending), false); // <dye name> + <_block name>
				block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation().add(e.getBlockFace().getModX(), e.getBlockFace().getModY(), e.getBlockFace().getModZ()), 3, block.getBlockData());
				block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.BLOCK_SLIME_BLOCK_PLACE, 0.2F, 0.7F + getPlugin().getRandom().nextFloat()/4F);
				if (p.getGameMode() != GameMode.CREATIVE)
					item.subtract(1);
				doArmSwing(p);
			} else if (item.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT) || (itemMat == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta)item.getItemMeta()).hasEnchant(Enchantment.FIRE_ASPECT))) {
				if (blockMat.name().endsWith("CANDLE") || blockMat.name().endsWith("CANDLE_CAKE") || blockMat.name().endsWith("CAMPFIRE")) {
					Lightable data = (Lightable) block.getBlockData();
					if (data.isLit() || !enactRegionPermission(canBuild, e, p, "ignite fires")) return;
					
					data.setLit(true);
					block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.ITEM_FIRECHARGE_USE, 0.2F, 0.7F + getPlugin().getRandom().nextFloat()/2F);
					block.setBlockData(data, false); // no need for physics check
					doArmSwing(p);
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
					
					// Bonemealable Sugar Cane
					if (m == Material.SUGAR_CANE && e.getItem() != null && e.getItem().getType() == Material.BONE_MEAL) {
						Location loc = b.getLocation().clone();
						int height = 0;
						while(height < 3 && loc.subtract(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) height++;
						if (height > 2) return; else height = 0;
						while(height < 3 && loc.add(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) height++;
						if (height > 2 || loc.add(0, 1, 0).getBlock().getType() != Material.AIR) return;
						
						doArmSwing(p);
						
						Block highestCane = loc.subtract(0, 2, 0).getBlock();
						crop = (Ageable) highestCane.getBlockData();
						crop.setAge(0);
						p.getEquipment().getItemInMainHand().subtract(1);
						b.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, b.getLocation().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5);
						loc.add(0, 1, 0).getBlock().setType(Material.SUGAR_CANE);
						return;
					}
					
					if (crop.getAge() < crop.getMaximumAge()) return;
					if (m == Material.SWEET_BERRY_BUSH || m == Material.BAMBOO || m.name().endsWith("STEM") || m == Material.CAVE_VINES_PLANT || m == Material.SUGAR_CANE) return;
					
					final boolean canHarvest = r.getEffectiveFlag(Flags.BUILD_ACCESS).lowerOrEqTo(r.getMember(p)) || r.getEffectiveFlag(Flags.CROP_ACCESS).lowerOrEqTo(r.getMember(p));
					
					if (!canHarvest) {
						p.sendActionBar(Component.text("\u00a74\u2716 \u00a7cYou don't have permission to harvest crops here. \u00a74\u2716"));
						return;
					}
					
					PlayerRightClickHarvestEvent event = new PlayerRightClickHarvestEvent(e.getPlayer(), e.getItem(), e.getClickedBlock(), e.getBlockFace());
					Bukkit.getServer().getPluginManager().callEvent(event);
					
					if (event.isCancelled()) return;
					PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(event, Skill.AGRICULTURE);
					
					for (ItemStack i : b.getDrops()) {
						i.setAmount(Math.max(1, i.getAmount() - 1));
						e.getPlayer().getWorld().dropItemNaturally(b.getLocation(), i);
					}
					
					doArmSwing(p);
					
					e.setCancelled(true);
					crop.setAge(0);
					b.setBlockData(crop);
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
			PlayerProfile.from(p).getStats().addToStat(StatType.GENERIC, "steps", 1, true);
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
							Bukkit.getPluginManager().callEvent(new PlayerItemDamageEvent(p, p.getInventory().getBoots(), 1, 1));
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
			e.getPlayer().sendActionBar(Component.text("\u00a74\u2716 \u00a7cYou don't have permission to harvest crops here. \u00a74\u2716"));
			e.setCancelled(true);
			return;
		} else {
			PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(e, Skill.AGRICULTURE);
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
				PlayerProfile.from(e.getPlayer()).getStats().addToStat(StatType.GENERIC, "respawn", 1, true);
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
				PlayerProfile pp = PlayerProfile.from(e.getPlayer());
				pp.updateLastLocation(e.getFrom(), 0);
				pp.getStats().addToStat(StatType.GENERIC, "teleport", 1, true);
			}
		}
		
	}
	
	@EventHandler
	public void onItemMend(PlayerItemMendEvent e) {
		e.setCancelled(true);
		BeanItem.addDurability(e.getItem(), e.getRepairAmount());
		PlayerProfile.from(e.getPlayer()).getStats().addToStat(StatType.GENERIC, "mending", e.getRepairAmount());
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
		if (toGm != fromGm) {
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
		pp.pokeAFK();
		
		if (advancementCoins.containsKey(e.getAdvancement().getKey()))
			pp.addToBalance(advancementCoins.get(e.getAdvancement().getKey()), "Advancement: " + e.getAdvancement().getKey().asString());
		
		if (e.message() != null) {
			TranslatableComponent ack = (TranslatableComponent) e.message();
			Component msg = Component.text("\u00a7e� ").append(pp.getComponentName()).append(Component.text(" has achieved ").append(ack.args().get(ack.args().size()-1))).color(TextColor.color(0xfff7a9));
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
			break;
		default:
			break;
		}
		
		PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(e, Skill.FISHING);
	}
	
	@EventHandler
	public void onNPCInteract(PlayerInteractNPCEvent e) {
		e.getPlayer().sendMessage("That's an NPC (DBID: "+e.getNPC().getDatabaseId()+")!");
	}
	
	@EventHandler
	public void onSkillLevel(SkillLevelUpEvent e) {
		e.getPlayer().playSound(e.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
	}
	
	private boolean hasGmPerm(Player p, GameMode gm) {
		boolean ye = gm == GameMode.SURVIVAL || p.hasPermission("bean.gm." + gm.name().toLowerCase());
		if (!ye && gm == GameMode.SPECTATOR && p.hasPermission("bean.gm.moderator"))
			ye = true;
		
		return ye;
	}
	
}
