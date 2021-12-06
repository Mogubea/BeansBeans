package me.playground.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import me.playground.main.Main;
import me.playground.playerprofile.HeirloomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemHeirloomMochi extends BeanItemHeirloom {

	private Random rand = new Random();
	
	public BItemHeirloomMochi(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzNTUzYmU2ODMwMjJmODU5NTljNmMyMTdiNGE1NmJmMzNkZWZiZjUyYTZhODRjNjlkMmNiZGI1MTY0M2IifX19", rarity);
		addAttribute(Attribute.GENERIC_MOVEMENT_SPEED, 0.001, EquipmentSlot.FEET);
	}
	
	@Override
	public ArrayList<Component> getCustomLore(ItemStack item) {
		final ArrayList<Component> lore = new ArrayList<Component>();
		lore.addAll(Arrays.asList(
				Component.text("\"\u3082\u3050\u3082\u3050\u3082\u3050\u3082\u3050\"", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("Mogu Counter: ", NamedTextColor.GRAY).append(Component.text(item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_COUNTER, PersistentDataType.INTEGER, 0), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false)));
		return lore;
	}
	
	@Override
	public void onConsumeItem(PlayerItemConsumeEvent e, HeirloomInventory inv) { 
		if (!e.getItem().getType().isEdible()) return;
		
		inv.setCounterFor(this, inv.getCounterFor(this) + 1);
		
		final ArmorStand di = (ArmorStand) e.getPlayer().getWorld().spawnEntity(new Location(e.getPlayer().getWorld(), 0, -10, 0), EntityType.ARMOR_STAND);
		di.setInvisible(true);
		di.setMarker(true);
		di.customName(Component.text("\u3082\u3050~").color(TextColor.color(0xffef78)));
		di.setCustomNameVisible(true);
		di.teleport(e.getPlayer().getLocation());
				
		new BukkitRunnable() {
			int loopsRem = 8;
			
			@Override
			public synchronized void cancel() throws IllegalStateException {
				di.remove();
		        Bukkit.getScheduler().cancelTask(getTaskId());
		    }
			
		    public void run() {
		    	if (--loopsRem < 1)
		    		cancel();
		    	else
		    		di.teleport(e.getPlayer().getLocation().add(-0.5 + rand.nextDouble(), 1.2 + rand.nextDouble(), -0.5 + rand.nextDouble()));
		    }
		}.runTaskTimer(Main.getInstance(), 1L, 5L);
	}
	
}
