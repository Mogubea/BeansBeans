package me.playground.items.heirlooms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import me.playground.items.BeanItemHeirloom;
import me.playground.items.ItemRarity;
import me.playground.main.Main;
import me.playground.playerprofile.HeirloomInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class BItemHeirloomMochi extends BeanItemHeirloom {

	private Random rand = new Random();
	
	public BItemHeirloomMochi(String identifier, String name, ItemStack item, ItemRarity rarity) {
		super(identifier, name, item, rarity);
	}
	
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributes() {
		final Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("8888888d-06a3-402d-a4a0-88888888fbfa"), "generic.movement_speed", 0.001, Operation.ADD_NUMBER, EquipmentSlot.FEET));
		return attributes;
	}
	
	@Override
	public ArrayList<Component> getCustomLore(ItemStack item) {
		final ArrayList<Component> lore = new ArrayList<Component>();
		lore.addAll(Arrays.asList(
				Component.text("\u00a77\"\u3082\u3050\u3082\u3050\u3082\u3050\u3082\u3050\""),
				Component.text("\u00a77Mogu Counter: \u00a7f" + item.getItemMeta().getPersistentDataContainer().getOrDefault(KEY_COUNTER, PersistentDataType.INTEGER, 0))));
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
