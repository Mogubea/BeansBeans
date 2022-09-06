package me.playground.items;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.playground.listeners.BlockDestructionSequence;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemTreecapitator extends BItemDurable {
	
	private final DecimalFormat df = new DecimalFormat("#,###");
	private final DecimalFormat dec = new DecimalFormat("#.#");
	
	private final NamespacedKey KEY_ABILITY_USES = key("ABILITY_USES"); // int
	private final NamespacedKey KEY_LOGS_DESTROYED = key("LOGS_DESTROYED"); // long
	private final NamespacedKey KEY_LEVEL = key("AXE_LEVEL"); // byte
	private final byte baseDestroy = 3;
	
	private final int[] levelReqs = {100, 250, 375, 500, 750, 1000, 1750, 2500, 3750, 5000, 7500, 10000, 25000, 50000, 100000, 250000, 500000, 1000000, 2500000, 50000000, 10000000};
	private final byte maxLevel = (byte)levelReqs.length;
	
	protected BItemTreecapitator() {
		super(1000, "HUNGRY_TREECAPITATOR", "Hungry Treecapitator", Material.IRON_AXE, ItemRarity.EPIC, 3, 1500);
		addAttribute(Attribute.GENERIC_ATTACK_SPEED, -3, EquipmentSlot.HAND);
		addRepairMaterial(BeanItem.LIVING_METAL_INGOT, 3f);
		setRunicCapacity(10);
	}
	
	@Override
	public List<TextComponent> getCustomLore(ItemStack item) {
		final PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
		final byte axeLevel = pdc.getOrDefault(KEY_LEVEL, PersistentDataType.BYTE, (byte)0);
		final long logsDestroyed = pdc.getOrDefault(KEY_LOGS_DESTROYED, PersistentDataType.LONG, 0L);
		
		final List<TextComponent> lore = new ArrayList<>();
		lore.addAll(Arrays.asList(
				Component.text("When chopping a log, every ", NamedTextColor.GRAY).append(Component.text("5", NamedTextColor.GREEN).append(Component.text(" seconds,", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false),
				Component.text("this axe will attempt to harvest up to ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text(baseDestroy + axeLevel, NamedTextColor.GOLD).append(Component.text(" nearby logs. This number increases", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false),
				Component.text("as more logs are harvested.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
		
		lore.add(Component.empty());
		lore.add(Component.text("Logs Harvested: ", NamedTextColor.GRAY).append(Component.text(df.format(logsDestroyed), NamedTextColor.YELLOW))
				.append(Component.text("/", NamedTextColor.GRAY).append(Component.text(df.format(levelReqs[axeLevel]), TextColor.color(0xffcd55))))
				.decoration(TextDecoration.ITALIC, false));
		lore.add(Utils.getProgressBar('-', 16, logsDestroyed, levelReqs[axeLevel], 0x343422, 0xffcd55).append(Component.text(" " + dec.format(((double)logsDestroyed/(double)levelReqs[axeLevel]) * 100) + "%", NamedTextColor.GOLD))
				.decoration(TextDecoration.ITALIC, false));
		return lore;
	}
	
	@Override
	public void onBlockMined(BlockBreakEvent e) {
		if (!isValidLog(e.getBlock())) return;
		final ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		final int axeLevel =  item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_LEVEL, PersistentDataType.BYTE, (byte)0);
		final int maxLogs = baseDestroy + axeLevel;
		
		if (!pp.onCdElseAdd("treecapitator", 5000, true)) {
			List<Block> toIterate = new ArrayList<>();
			toIterate.add(e.getBlock());
			
			// Loop log checking on a synchronous bukkit runnable.
			new BukkitRunnable() {
				int destroyed = 0;
				int idx = -1;
				
				@Override
				public void run() {
					if (++idx >= toIterate.size()) { cancel(); return; }
					
					Block b = toIterate.get(idx);
					
					for (int x = -2; ++x < 2;)
						for (int y = -2; ++y < 2;)
							for (int z = -2; ++z < 2;) {
								Block bb = b.getRelative(x, y, z);
								if (!isValidLog(bb)) continue;
								
								boolean iterate;
								
								if (!(iterate = idx == 0) && (iterate = new BlockDestructionSequence(e.getPlayer(), bb, null, false, false).setPlayEffect(false).fireSequence())) {
									Location bLocCenter = bb.getLocation().add(0.5, 0.5, 0.5);
									
									// Play Effects
									bb.getWorld().playSound(bLocCenter, Sound.BLOCK_WOOD_BREAK, 0.75F, 0.9F + (getRandom().nextFloat()/5));
									bb.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, bLocCenter, 3, 0.25, 0.25, 0.25, 0.015);
									
									if (++destroyed >= maxLogs) { cancel(); return; }
								}
								
								if (iterate)
									toIterate.add(bb);
							}
				}
				
				@Override
				public void cancel() {
					item.editMeta(meta -> {
						PersistentDataContainer container = meta.getPersistentDataContainer();
						container.set(KEY_ABILITY_USES, PersistentDataType.INTEGER, container.getOrDefault(KEY_ABILITY_USES, PersistentDataType.INTEGER, 0) + 1);
						container.set(KEY_LOGS_DESTROYED, PersistentDataType.LONG, container.getOrDefault(KEY_LOGS_DESTROYED, PersistentDataType.LONG, 0L) + destroyed);
					});
					super.cancel();
				}
			}.runTaskTimer(Main.getInstance(), 1L, 1L);
		}
		
		// Add one log since it's the original log.
		item.editMeta(meta -> {
			PersistentDataContainer container = meta.getPersistentDataContainer();
			long totalLogs = container.getOrDefault(KEY_LOGS_DESTROYED, PersistentDataType.LONG, 0L) + 1;
			container.set(KEY_LOGS_DESTROYED, PersistentDataType.LONG, totalLogs);
			
			// Level Up
			if (axeLevel < maxLevel && totalLogs >= levelReqs[axeLevel]) {
				container.set(KEY_LEVEL, PersistentDataType.BYTE, (byte)(axeLevel + 1));
				e.getPlayer().sendMessage(Component.text("\u00a7b\u00a7l\u2191\u00a7a Your ").append(meta.displayName().hoverEvent(item.asHoverEvent())).append(Component.text("\u00a7a's hunger grows..")));
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.2F);
			}
		});
		
		BeanItem.formatItem(item);
	}
	
	private boolean isValidLog(Block b) {
		String n = b.getType().name();
		return !b.hasMetadata("protected") && ((n.endsWith("_WOOD") || n.endsWith("_LOG")) && !n.startsWith("STRIPPED_"));
	}
	
	
	
}
