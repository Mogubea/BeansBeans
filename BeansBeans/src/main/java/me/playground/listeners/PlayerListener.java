package me.playground.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.playground.gui.BeanGuiMainMenu;
import me.playground.gui.stations.BeanGuiAnvil;
import me.playground.items.tracking.DemanifestationReason;
import me.playground.listeners.events.PlayerSpawnCreatureEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.SpawnEggItem;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Lightable;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.playground.celestia.logging.CelestiaAction;
import me.playground.enchants.BEnchantment;
import me.playground.gui.stations.BeanGuiEnchantingTable;
import me.playground.items.BeanBlock;
import me.playground.items.BItemFishingRod;
import me.playground.items.BeanItem;
import me.playground.items.ItemRarity;
import me.playground.listeners.events.PlayerInteractNPCEvent;
import me.playground.listeners.events.PlayerRightClickHarvestEvent;
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
import me.playground.skills.Skill;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

public class PlayerListener extends EventListener {
	
	public PlayerListener(Main plugin) {
		super(plugin);
		
		advancementCoins.put(plugin.getKey("deepslateemeraldore"), 500);
		advancementCoins.put(plugin.getKey("minerscollection"), 500);

		getPlugin().getProtocolManager().addPacketListener(new PacketAdapter(getPlugin(), PacketType.Play.Server.SPAWN_ENTITY) {
			@Override
			public void onPacketSending(PacketEvent event) {
				Player p = event.getPlayer();
				Entity e = p.getWorld().getEntity(event.getPacket().getUUIDs().read(0));
				if (!(e instanceof Item item)) return;
				if (!item.hasMetadata("rarity")) return; // Goes bye bye on restarts but whatever

				String rarityName = ItemRarity.valueOf(item.getMetadata("rarity").get(0).asString()).name();
				Team team = p.getScoreboard().getTeam("itemRarity_" + rarityName);
				if (team != null)
					team.addEntity(e);
			}
		});

	}
	
	@EventHandler
	public void onPlayerChat(AsyncChatEvent e) {
		e.setCancelled(true);
		Player p = e.getPlayer();
		PlayerProfile pp = PlayerProfile.from(p);

		if (pp.isMuted()) {
			p.sendActionBar(Component.text("\u00a74\u26a0\u00a7c You're " + (pp.getMute().isPermanent() ? "permanently" : "currently") + " muted \u00a74\u26a0"));
			return;
		}

		if (pp.onCdElseAdd("chat", 400)) {
			p.sendActionBar(Component.text("\u00a7cYou're sending messages too fast!"));
			return;
		}

		pp.getStats().addToStat(StatType.GENERIC, "chatMessages", 1, true);

		TextComponent chat = Component.empty();

		if (pp.getDonorRank() != null)
			chat = Component.empty().append(Component.text('\uE020'));

		chat = chat.append(Component.text(pp.getHighestRank().getUnicode()).hoverEvent(pp.getHighestRank().toComponent()))
				.append(Component.text(" "))
				.append(pp.getComponentName());

		final String content = ((TextComponent)e.message()).content();
		final ArrayList<UUID> pinged = new ArrayList<>();

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
						pinged.add(ping.getUniqueId()); // Add player to the pinged list, so we can colour the message a bit more too.
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

		getPlugin().getCelestiaManager().log(pp.getId(), CelestiaAction.CHAT, e.getPlayer().getLocation(), content);
		getPlugin().getLogger().info("[CHAT] " + pp.getDisplayName() + ": " + content); // Console
		getPlugin().getDiscord().sendChatMessage(pp.getId(), content); // Discord Chat
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

		// To remove
		e.getDrops().remove(BeanItem.PLAYER_MENU.getOriginalStack());

		// gross
		TextReplacementConfig c = TextReplacementConfig.builder().match("\u24E2 ").replacement("").build();
		TextReplacementConfig c2 = TextReplacementConfig.builder().match(p.getName()).replacement(pp.getComponentName()).build();
		TextReplacementConfig c3 = TextReplacementConfig.builder().match("\u2b50 ").replacement("").build();

		e.deathMessage(Component.text("\u2620 ", NamedTextColor.RED).append(e.deathMessage().color(TextColor.color(0xff7a7a))).replaceText(c).replaceText(c3).replaceText(c2));
		
		getPlugin().getServer().getOnlinePlayers().forEach(player -> { if (PlayerProfile.from(player).isSettingEnabled(PlayerSetting.SHOW_DEATH_MESSAGES)) { player.sendMessage(e.deathMessage()); }});
		
		e.deathMessage(null);

		// Hostile entities recover 25% of their maximum hp when killing a player.
		// TODO: Create a helper class to obtain the maximum health of a monster rather than going through all this attribute shit every time.
		if (p.getKiller() instanceof Monster monster) {
			AttributeInstance maxHpAttr = monster.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (maxHpAttr == null) return;
			double hp = monster.getHealth(), maxHp = maxHpAttr.getValue();
			monster.setHealth(Math.min(maxHp, hp + maxHp / 4));
			monster.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, monster.getLocation(), 10);
		}
	}
	
	@EventHandler
	public void onBedEnter(PlayerBedEnterEvent e) {
		Player p = e.getPlayer();
		p.setStatistic(Statistic.TIME_SINCE_REST, 0);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onClickEntity(PlayerInteractEntityEvent e) {
		Entity ent = e.getRightClicked();
		ItemStack item = e.getPlayer().getInventory().getItem(e.getHand());

		// Pre-Region Check Entity Interaction method
		if (BeanItem.func(item, custom -> custom.onEntityInteract(e))) return;

		if (item.getType() == Material.NAME_TAG && ent instanceof ArmorStand && ent.isInvulnerable())
			e.setCancelled(true); // Cancel but continue.

		// Only bother if matching entities
		if (item.getType().name().endsWith("SPAWN_EGG") && item.getType().name().startsWith(e.getRightClicked().getType().name())) {
			if (!enactRegionPermission(getRegionAt(ent.getLocation()), e, e.getPlayer(), Flags.BUILD_ACCESS, "spawn mobs")) return;
			e.setCancelled(true); // Always cancel

			// We take spawning these Entities into our own hands so that we may customise things and spawn custom entities etc. etc.
			try {
				net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(new ItemStack(item));
				net.minecraft.world.entity.EntityType<?> entity = ((SpawnEggItem) nmsItem.getItem()).getType(nmsItem.getTag());
				Location l = ent.getLocation();

				LivingEntity newent = (LivingEntity) entity.spawn((((CraftWorld)ent.getWorld()).getHandle()), nmsItem, ((CraftPlayer)e.getPlayer()).getHandle(), new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ()),
						MobSpawnType.SPAWN_EGG, true, false, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG).getBukkitEntity();
				if (new PlayerSpawnCreatureEvent(newent, e.getPlayer(), item).callEvent()) {
					newent.spawnAt(newent.getLocation());
					item.subtract(1);
				}
			} catch (Exception ex) {
				e.getPlayer().sendActionBar(Component.text("\u00a7cThere was a problem summoning this entity."));
			}
			return;
		}

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
	public void onSwap(PlayerSwapHandItemsEvent e) {
		PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		if (!pp.isSettingEnabled(PlayerSetting.SWAP_TO_OPEN_MENU)) return;

		e.setCancelled(true);
		new BeanGuiMainMenu(e.getPlayer()).openInventory();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInteractDumb(PlayerInteractEvent e) {
		final Block block = e.getClickedBlock();
		
		// Cancel mob destruction of farmland.
		if (e.getAction() == Action.PHYSICAL && block != null && block.getType() == Material.FARMLAND) {
			e.setCancelled(true);
			return;
		}

		final ItemStack item = e.getItem();
		final Player p = e.getPlayer();

		// Stop here if unnecessary to check
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || block == null) return;

		// Re-flatten pathing
		if (item != null && item.getType().name().endsWith("SHOVEL")) {
			if (block.getType() == Material.DIRT_PATH) {
				if (!enactRegionPermission(getRegionAt(block.getLocation()), e, p, Flags.BUILD_ACCESS, "flatten dirt paths")) return;
				swingHand(p, e.getHand());
				new PlayerItemDamageEvent(p, item, 1, 1).callEvent();
				block.setType(Material.DIRT);
				block.getWorld().playSound(block.getLocation().add(0.5, 1, 0.5), Sound.ITEM_SHOVEL_FLATTEN, 0.55f, 0.9f + rand.nextFloat(0.2f));
				e.setCancelled(true);
				return;
			}
		}

		// Check Spawner spawn count
		if (block.getType() == Material.SPAWNER && e.getItem() == null && !PlayerProfile.from(p).onCdElseAdd("spawnerCheck", 500, true)) {
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			int spawns = spawner.getPersistentDataContainer().getOrDefault(getPlugin().getKey("spawnerspawns"), PersistentDataType.INTEGER, 0);
			Component creature = Component.translatable(spawner.getSpawnedType().translationKey());
			
			p.sendActionBar(Component.text("\u00a78(" + (block.isBlockPowered() ? "\u00a7cDisabled" : "\u00a7aActive") + "\u00a78)\u00a7r This ").append(
					creature.color(NamedTextColor.WHITE).append(Component.text(" ").append(Component.translatable(block.translationKey()))
							.append(Component.text("\u00a7r has spawned \u00a7f" + spawns + " \u00a7r")))
							.append(creature)).append(Component.text("(s)")).colorIfAbsent(TextColor.color(0xeeeeee)));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.2F, 0.8F);
			swingHand(p, e.getHand());
			return;
		}

		// Cancel the cleansing of custom leather items
		if (block.getType() == Material.WATER_CAULDRON && e.getItem() != null && e.getItem().getItemMeta() instanceof LeatherArmorMeta && BeanItem.from(e.getItem()) != null) {
			e.setCancelled(true);
			return;
		}
		
		final Region region = getRegionAt(block.getLocation());
		final boolean canBuild = checkRegionPermission(region, e, p, Flags.BUILD_ACCESS);
		final Material blockMat = block.getType();

		// Stick to quickly check protection bounds
		if (e.getItem() != null && e.getItem().getType() == Material.STICK) {
			p.sendActionBar(region.isWorldRegion() ?
					Component.text("This block is not protected by any player regions.", NamedTextColor.GRAY) :
					Component.text("This block is protected by ", NamedTextColor.GRAY).append(region.getColouredName()).append(Component.text(".", NamedTextColor.GRAY)));
			swingHand(p, e.getHand());
		}

		// Region Permission Checks - Regardless of items
		if (!p.isBlocking()) {
			boolean stop = false;

			if (blockMat.name().endsWith("_BED")) { // Use Beds to set home
				stop = !enactRegionPermission(region, e, p, Flags.WARP_CREATION, "sleep");
			} else if (block.getState() instanceof Container) { // Using Containers
				stop = !enactRegionPermission(region, e, p, Flags.CONTAINER_ACCESS, "use containers");
			} else if (blockMat.name().endsWith("DOOR") || blockMat.name().endsWith("GATE")) { // Using Doors
				stop = !enactRegionPermission(region, e, p, Flags.DOOR_ACCESS, "open door");
			} else if (blockMat == Material.ENCHANTING_TABLE) {
				stop = !enactRegionPermission(region, e, p, Flags.CONTAINER_ACCESS, "use enchanting table");
			} else if (blockMat.name().endsWith("ANVIL")) { // Using Anvils
				stop = !enactRegionPermission(region, e, p, Flags.ANVIL_ACCESS, "use anvils");
				if (stop) return;
				if (!region.getEffectiveFlag(Flags.ANVIL_DEGRADATION)) { // Handle anvil unbreakable flag by giving the Player a fake Anvil GUI.
					e.setCancelled(true);
					new BeanGuiAnvil(p, e.getClickedBlock()).openInventory();
					return;
				}
			} else if (blockMat == Material.FLOWER_POT || blockMat.name().startsWith("POTTED")) { // Flower Pots
				stop = !enactRegionPermission(canBuild, e, p, "plant flowers");
			} else if (blockMat == Material.NOTE_BLOCK) { // Tune Note Blocks
				stop = !enactRegionPermission(canBuild, e, p, "tune notes");
			} else if (blockMat == Material.JUKEBOX) { // Mess with Music Discs
				stop = !enactRegionPermission(canBuild, e, p, "play music");
			} else if (blockMat == Material.REDSTONE_WIRE) { // Mess with Redstone wires
				stop = !enactRegionPermission(canBuild, e, p, "alter redstone");
			} else if (blockMat == Material.BEACON) { // Alter beacons
				stop = !enactRegionPermission(region, e, p, Flags.CONTAINER_ACCESS, "edit beacons");
			}
			
			if (stop) return;
			
			// Post-Region Check Custom Blocks method
			if (BeanBlock.from(block, (custom) -> custom.onBlockInteract(e)) != null) return;
		}

		if (BeanItem.func(item, (custom) -> custom.onInteract(e))) return;

		if (item == null || e.useItemInHand() == Result.DENY) return;
		final Material itemMat = item.getType();

		if (itemMat.name().endsWith("_SPAWN_EGG")) {
			if (!block.getType().isInteractable()) {
				if (!enactRegionPermission(canBuild, e, p, "spawn mobs")) return;
				e.setCancelled(true); // Always cancel

				// We take spawning these Entities into our own hands so that we may customise things and spawn custom entities etc. etc.
				try {
					net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(new ItemStack(item));
					net.minecraft.world.entity.EntityType<?> entity = ((SpawnEggItem) nmsItem.getItem()).getType(nmsItem.getTag());
					BlockFace face = e.getBlockFace();
					Location l = block.getLocation().add(face.getModX(), face.getModY(), face.getModZ());
					LivingEntity newent = (LivingEntity) entity.spawn((((CraftWorld)p.getWorld()).getHandle()), nmsItem, ((CraftPlayer)e.getPlayer()).getHandle(),
							new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ()), MobSpawnType.SPAWN_EGG, e.getBlockFace() == BlockFace.UP, false, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG).getBukkitEntity();
					if (new PlayerSpawnCreatureEvent(newent, e.getPlayer(), item).callEvent()) {
						newent.spawnAt(newent.getLocation());
						item.subtract(1);
					}
				} catch (Exception ex) {
					p.sendActionBar(Component.text("\u00a7cThere was a problem summoning this entity."));
				}
			}
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (itemMat == Material.BONE_MEAL) { // Bone Meal
				enactRegionPermission(canBuild || (block.getBlockData() instanceof Ageable && checkRegionPermission(region, e, p, Flags.CROP_ACCESS)), e, p, "apply Bone Meal");
			} else if (blockMat.name().endsWith("SIGN") && (itemMat.name().endsWith("DYE") || itemMat == Material.GLOW_INK_SAC)) { // Dye Signs
				enactRegionPermission(canBuild, e, p, "dye signs");
			} else if (itemMat == Material.SHEARS && blockMat == Material.PUMPKIN) { // Carve Pumpkins 
				enactRegionPermission(canBuild, e, p, "carve pumpkins");
			} else if (itemMat.name().endsWith("BOAT") || itemMat.name().endsWith("MINECART")) { // Placing Vehicles
				enactRegionPermission(canBuild, e, p, "place vehicles");
			} else if (itemMat == Material.END_CRYSTAL || itemMat == Material.ARMOR_STAND || itemMat == Material.GLOW_ITEM_FRAME || itemMat == Material.ITEM_FRAME || itemMat == Material.PAINTING || itemMat == Material.LEAD) {
				enactRegionPermission(canBuild, e, p, "build");
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
				block.getWorld().playSound(block.getLocation().add(0.5, 0, 0.5), Sound.BLOCK_SLIME_BLOCK_PLACE, 0.2F, 0.8F + getPlugin().getRandom().nextFloat()/4F);
				if (p.getGameMode() != GameMode.CREATIVE)
					item.subtract(1);
				swingHand(p, e.getHand());
			} else if (item.getItemMeta().hasEnchant(Enchantment.FIRE_ASPECT) || (itemMat == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta)item.getItemMeta()).hasStoredEnchant(Enchantment.FIRE_ASPECT))) {
				if (blockMat.name().endsWith("CANDLE") || blockMat.name().endsWith("CANDLE_CAKE") || blockMat.name().endsWith("CAMPFIRE")) {
					Lightable data = (Lightable) block.getBlockData();
					if (data.isLit() || !enactRegionPermission(canBuild, e, p, "ignite fires")) return;
					
					data.setLit(true);
					block.getWorld().playSound(block.getLocation().toCenterLocation(), Sound.ITEM_FIRECHARGE_USE, 0.2F, 0.7F + getPlugin().getRandom().nextFloat()/2F);
					block.setBlockData(data, false); // no need for physics check
					swingHand(p, e.getHand());
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent e) {
		if (e.useInteractedBlock() == Result.DENY) return;
		ItemStack item = e.getItem();
		BeanItem custom = BeanItem.from(item);

		if (e.getHand() == EquipmentSlot.HAND) {
			final Block b = e.getClickedBlock();
			final Player p = e.getPlayer();

			// "Custom" Anvil
			if (e.getClickedBlock() != null && e.getClickedBlock().getType().name().endsWith("ANVIL")) {
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (!(p.isSneaking() && item != null) && !p.isBlocking()) {
						e.setCancelled(true);
						new BeanGuiAnvil(p, e.getClickedBlock()).openInventory();
						return;
					}
				}
			}

			// Custom Enchanting Table
			else if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
				EnchantingTable table = (EnchantingTable) b.getState();
				PersistentDataContainer pdc = table.getPersistentDataContainer();
				
				// Right Click Interactions
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (!(p.isSneaking() && item != null) && !p.isBlocking()) {
						e.setCancelled(true);
						
						// Add lapis to the Enchanting Table lapis pocket
						if (custom == null && (item != null && (item.getType() == Material.LAPIS_LAZULI || e.getItem().getType() == Material.LAPIS_BLOCK))) {
							boolean block = item.getType() != Material.LAPIS_LAZULI;

							int oldLapis = pdc.getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)0);
							int lapisLevel = pdc.getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS_LEVEL, PersistentDataType.BYTE, (byte)0);
							int maxLapis = 128 + (lapisLevel*lapisLevel) * 64;
							
							if (oldLapis >= maxLapis) {
								b.getWorld().spawnParticle(Particle.CRIT, b.getLocation().add(0.5, 0.5, 0.5), 7, 0.35, 0.35, 0.35, 0.15);
								p.sendActionBar(Component.text("The Lazuli Storage Compartment™ is full!", NamedTextColor.RED));
								return;
							}

							int handLapisValue = e.getItem().getAmount() * (block ? 9 : 1);
							int remainingSpace = oldLapis+handLapisValue - maxLapis;
							if (remainingSpace < 0) remainingSpace = 0;
							int toTakeAway = remainingSpace;

							if (block && remainingSpace > 0) {
								p.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, toTakeAway)).forEach((idx, itemm) -> p.getWorld().dropItem(p.getLocation(), itemm));
								remainingSpace = Math.floorDiv(remainingSpace, 9);
							}

							p.playSound(b.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6F, 0.9F);
							b.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, b.getLocation().add(0.5, 0.5, 0.5), 8, 0.45, 0.45, 0.45, new DustTransition(Color.BLUE, Color.AQUA, 1.5F));
							pdc.set(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)(oldLapis + handLapisValue - toTakeAway));
							BeanGuiEnchantingTable.forceUpdateLapis(table);
							e.getItem().setAmount(remainingSpace);
							return;
						}
						
						// Otherwise, open the GUI
						new BeanGuiEnchantingTable(p, (EnchantingTable)b.getState()).openInventory();
						return;
					}
				} else if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getItem() == null) {
					int amt = pdc.getOrDefault(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)0);
					if (amt <= 0) return;
					
					int toDrop = Math.min(amt, 64);
					pdc.set(BeanGuiEnchantingTable.KEY_LAPIS, PersistentDataType.SHORT, (short)(amt - toDrop));
					BeanGuiEnchantingTable.forceUpdateLapis(table);
					
					p.playSound(b.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.6F, 0.9F);
					b.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, b.getLocation().add(0.5, 0.5, 0.5), 7, 0.55, 0.55, 0.55, new DustTransition(Color.BLUE, Color.AQUA, 1.5F));
					b.getWorld().dropItem(b.getLocation().add(0.5, 1.2, 0.5), new ItemStack(Material.LAPIS_LAZULI, toDrop));
				}
			}
		}
		
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Block b = e.getClickedBlock();
			final Player p = e.getPlayer();
			final Region r = getRegionAt(b.getLocation());

			Material m = b.getType();

			// Edit Existing Sign
			/*if (item == null && m.toString().endsWith("_SIGN")) {
				if (!(p.isSneaking() && e.getItem() != null) && !p.isBlocking()) {
					if (!enactRegionPermission(r, e, p, Flags.BUILD_ACCESS, "edit signs")) return;
					e.setCancelled(true);

					Sign sign = (Sign) b.getState();
					if (!sign.isEditable()) {
						sign.setEditable(true);
						sign.update(true);
					}

					// Delay it to allow the sign to update
					getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
						ClientboundOpenSignEditorPacket packet = new ClientboundOpenSignEditorPacket(new BlockPos(b.getX(), b.getY(), b.getZ()));
						((CraftPlayer) p).getHandle().connection.send(packet);
					}, 1L);

					p.swingMainHand();
					return;
				}
			}*/

			// Farm shit
			if (b.getBlockData() instanceof Ageable crop) {

				// Bonemealable Sugar Cane
				if (m == Material.SUGAR_CANE && e.getItem() != null && custom == null && e.getItem().getType() == Material.BONE_MEAL) {
					Location loc = b.getLocation().clone();
					int height = 0;
					while(height < 3 && loc.subtract(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) height++;
					if (height > 2) return; else height = 0;
					while(height < 3 && loc.add(0, 1, 0).getBlock().getType() == Material.SUGAR_CANE) height++;
					if (height > 2 || loc.add(0, 1, 0).getBlock().getType() != Material.AIR) return;

					p.swingMainHand();

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

				p.swingMainHand();

				e.setCancelled(true);
				crop.setAge(0);
				b.setBlockData(crop);
			}
		}
	}
	
	final float DEFAULT_MOVESPEED = 0.2F;
	final ArrayList<UUID> talarianUsers = new ArrayList<>();
	
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
		} else {
			PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(e, Skill.AGRICULTURE);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
			Player p = e.getPlayer();
			p.setHealth(10);
			p.setSaturation(2.0F);
			p.setFoodLevel(12);
			PlayerProfile.from(e.getPlayer()).getStats().addToStat(StatType.GENERIC, "respawn", 1, true);
		});
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		final Region r = getRegionAt(e.getTo());

		switch(e.getCause()) {
			case ENDER_PEARL, CHORUS_FRUIT -> enactRegionPermission(r, e, e.getPlayer(), Flags.ENDERPEARLS, "ender warp into");
			case COMMAND, PLUGIN, NETHER_PORTAL, END_PORTAL -> {
				if (!enactRegionPermission(getRegionAt(e.getFrom()), e, e.getPlayer(), Flags.TELEPORT_OUT, "teleport out of")) break;
				if (!enactRegionPermission(r, e, e.getPlayer(), Flags.TELEPORT_IN, "teleport into")) break;
				PlayerProfile pp = PlayerProfile.from(e.getPlayer());

				if (e.getCause() != TeleportCause.NETHER_PORTAL && e.getCause() != TeleportCause.END_PORTAL) {
					e.getPlayer().setFallDistance(0F);
					pp.updateLastLocation(e.getFrom(), 0);
					pp.getStats().addToStat(StatType.GENERIC, "teleport", 1);

					// Temporary Invulnerability when warping
					pp.addCooldown("teleportInvulnerability", 7000, true);
				} else {
					pp.getStats().addToStat(StatType.GENERIC, "portalUse", 1);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemMend(PlayerItemMendEvent e) {
		BeanItem.addDurability(e.getItem(), e.getRepairAmount());
		PlayerProfile.from(e.getPlayer()).getStats().addToStat(StatType.GENERIC, "mending", e.getRepairAmount());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onItemDamage(PlayerItemDamageEvent e) {
		e.setCancelled(true);
		BeanItem.addDurability(e.getItem(), -e.getDamage());
		PlayerProfile.from(e.getPlayer()).getStats().addToStat(StatType.GENERIC, "durabilityUse", e.getDamage());
	}
	
	@EventHandler
	public void onGamemodeChange(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		GameMode fromGm = p.getGameMode();
		GameMode toGm = e.getNewGameMode();

		if (toGm == fromGm || !(hasGmPerm(p, toGm))) {
			e.setCancelled(true);
			return;
		}

		if (!p.hasPermission("bean.gm.spectator")) {
			if (toGm == GameMode.SPECTATOR)
				PlayerProfile.from(p).updateLastLocation(p.getLocation(), 1);
			else if (fromGm == GameMode.SPECTATOR)
				p.teleport(PlayerProfile.from(p).getLastLocation(1), TeleportCause.END_GATEWAY);
		}

		// Keep flight through changes
		if (PlayerProfile.from(p).isSettingEnabled(PlayerSetting.FLIGHT)) {
			boolean isFlying = p.isFlying();
			getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
					p.setAllowFlight(true);
					if (isFlying) p.setFlying(true);
			}, 1L);
		}

		p.sendMessage(Component.text("\u00a77You are now in \u00a7f").append(Component.translatable(toGm.translationKey())).append(Component.text("\u00a77.")));
	}
	
	private final Map<NamespacedKey, Integer> advancementCoins = new HashMap<>(2);
	
	@EventHandler
	public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
		Player p = e.getPlayer();
		PlayerProfile pp = PlayerProfile.from(p);
		pp.pokeAFK();
		
		if (advancementCoins.containsKey(e.getAdvancement().getKey()))
			pp.addToBalance(advancementCoins.get(e.getAdvancement().getKey()), "Advancement: " + e.getAdvancement().getKey().asString());

		if (e.getAdvancement().getDisplay() == null) return;

		if (e.message() != null) {
			Component msg = Component.text("\u00a7e\u2605 ")
					.append(pp.getComponentName()).append(Component.text(" has achieved ", NamedTextColor.GRAY))
					.append(e.getAdvancement().getDisplay().title().hoverEvent(HoverEvent.showText(e.getAdvancement().getDisplay().description())))
					.append(Component.text(".", NamedTextColor.GRAY));

			Bukkit.getOnlinePlayers().forEach(player -> {
				PlayerProfile tpp = PlayerProfile.from(player);
				if (tpp.isIgnoring(pp.getId())) return;
				if (!tpp.isSettingEnabled(PlayerSetting.SHOW_ACHIEVEMENTS)) return;
				player.sendMessage(msg);
			});
			e.message(null);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onConsume(PlayerItemConsumeEvent e) {
		if (PlayerProfile.from(e.getPlayer()).getHeirlooms().doPlayerItemConsumeEvent(e))
			getPlugin().getItemTrackingManager().incrementDemanifestationCount(e.getItem(), DemanifestationReason.EATEN, 1);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerFish(PlayerFishEvent e) {
		final Player p = e.getPlayer();

		switch(e.getState()) {
		case CAUGHT_ENTITY: // Protect animals and villagers from being reeled
			if (!(e.getCaught() instanceof Animals || e.getCaught() instanceof Villager)) return;

			if (!enactRegionPermission(getRegionAt(e.getCaught().getLocation()), e, p, Flags.PROTECT_ANIMALS, "fish animals"))
				e.getHook().remove();
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

		if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) { // Fishing
			Item caught = (Item) e.getCaught();
			final LootTable table = getPlugin().lootManager().getLootTable("fishing");
			ItemStack rod = p.getInventory().getItemInMainHand();
			if (rod.getType() != Material.FISHING_ROD)
				rod = p.getInventory().getItemInOffHand();

			if (table != null)
				caught.setItemStack(LootRetriever.from(table, RetrieveMethod.CUMULATIVE_CHANCE, p)
						.luck(PlayerProfile.from(p).getLuck() + rod.getItemMeta().getEnchantLevel(Enchantment.LUCK))
						.biome(e.getHook().getLocation().getBlock().getBiome())
						.burn(rod.getItemMeta().getEnchantLevel(BEnchantment.SCORCHING) > 0)
						.getLoot().get(0));
		}
		
		PlayerProfile.from(e.getPlayer()).getSkills().doSkillEvents(e, Skill.FISHING);
	}
	
	@EventHandler
	public void onNPCInteract(PlayerInteractNPCEvent e) {
		e.getPlayer().swingMainHand();
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		ItemRarity rarity = BeanItem.getItemRarity(e.getItemDrop().getItemStack());
		
		// Vanishes much faster
		if (rarity == ItemRarity.TRASH)
			e.getItemDrop().setTicksLived(20 * 240);
		
		// Remove immediately
		if (rarity == ItemRarity.UNTOUCHABLE) {
			e.getPlayer().getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, e.getItemDrop().getLocation(), 3, 0.3, 0.3, 0.3, 0.02);
			e.getItemDrop().remove();
			return;
		}

		if (!rarity.is(ItemRarity.UNCOMMON)) {
			// Common items vanish a bit faster
			e.getItemDrop().setTicksLived(20 * 120);
			return;
		}

		// Make all uncommon+ items invulnerable
		e.getItemDrop().setInvulnerable(true);
		e.getItemDrop().setGlowing(true);
		e.getItemDrop().setMetadata("rarity", new FixedMetadataValue(getPlugin(), rarity.name()));

		/*Scoreboard board = e.getPlayer().getScoreboard();
		Team team = board.getTeam("rarity_" + rarityName);
		if (team == null) 
			team = board.registerNewTeam("rarity_" + rarityName);
		team.color(NamedTextColor.nearestTo(rarity.getColour()));
		team.addEntity(e.getItemDrop());
		
		PacketContainer packet = getPlugin().getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, e.getItemDrop().getEntityId()); //Set packet's entity id
        
        WrappedDataWatcher metadata = WrappedDataWatcher.getEntityWatcher(e.getItemDrop()); // Get the Entity's Watchable Collection
        metadata.setEntity(e.getItemDrop()); // Assure the Entity
        
        WrappedDataWatcher.WrappedDataWatcherObject byteValue = new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class));
        byte ogValue = (byte) metadata.getByte(0); // Get the original value?
        metadata.setObject(byteValue, ogValue |= 0b01000000); // Set the 7th bit which is GLOWING EFFECT.
        packet.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects()); // Update the Packet's Watchable Collection
        
       try {
        	getPlugin().getProtocolManager().sendServerPacket(e.getPlayer(), packet);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        
        metadata.setObject(byteValue, ogValue ^= 0b01000000); // Flip it back
		*/
	}

	/**
	 * This is a precautionary event handler to prevent any and all players from opening these GUIs which no longer are valid on the server.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onOpenInventory(InventoryOpenEvent e) {
		if (e.getInventory() instanceof EnchantingInventory)
			e.setCancelled(true);
		else if (e.getInventory() instanceof AnvilInventory)
			e.setCancelled(true);
	}

	private boolean hasGmPerm(Player p, GameMode gm) {
		boolean ye = gm == GameMode.SURVIVAL || p.hasPermission("bean.gm." + gm.name().toLowerCase());
		if (!ye && gm == GameMode.SPECTATOR && p.hasPermission("bean.gm.moderator"))
			ye = true;
		
		return ye;
	}

	private void swingHand(Player p, @Nullable EquipmentSlot hand) {
		if (hand == null) return;
		switch(hand) {
			case HAND -> p.swingMainHand();
			case OFF_HAND -> p.swingOffHand();
		}
	}

}
