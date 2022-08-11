package me.playground.enchants;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import me.playground.main.Main;

public class EnchantManager {
	
	private final Main plugin;
	
	public EnchantManager(Main plugin) {
		this.plugin = plugin;
		registerEnchantments();
	}
	
	private void registerEnchantments() {
		try {
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
            for (BeanEnchantment ench : BeanEnchantment.getCustomEnchants())
            	Enchantment.registerEnchantment(ench);
            plugin.getSLF4JLogger().info("Registered custom enchantments.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	@SuppressWarnings("unchecked")
	public void unregisterEnchantments() {
		try {
           Field keyField = Enchantment.class.getDeclaredField("byKey");
           Field nameField = Enchantment.class.getDeclaredField("byName");
           keyField.setAccessible(true);
           nameField.setAccessible(true);
           HashMap<NamespacedKey, Enchantment> byKey = (HashMap<NamespacedKey, Enchantment>) keyField.get(null);
           HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) nameField.get(null);
           for (BeanEnchantment ench : BeanEnchantment.getCustomEnchants()) {
        	   byKey.remove(ench.getKey());
        	   byName.remove(ench.getName());
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
}
