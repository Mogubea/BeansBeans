package me.playground.enchants;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import me.playground.items.BeanItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import me.playground.main.Main;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

public class EnchantmentManager {
	
	private final Main plugin;
    private final Map<Enchantment, Enchantment> vanillaReplacements = new HashMap<>();
	
	public EnchantmentManager(Main plugin) {
		this.plugin = plugin;
		registerEnchantments();

        replaceEnchantment(Enchantment.DURABILITY, BEnchantment.UNBREAKING);
	}

    /**
     * Replace all future instances of the specified Enchantment.
     */
    public void replaceEnchantment(Enchantment enchantment, Enchantment newEnchantment) {
        vanillaReplacements.put(enchantment, newEnchantment);
    }

    public boolean isReplaced(Enchantment enchantment) {
        return vanillaReplacements.containsKey(enchantment);
    }

    @NotNull
    @Deprecated(forRemoval = true)
    public Enchantment getReplacementEnchantment(Enchantment enchantment) {
        return vanillaReplacements.getOrDefault(enchantment, enchantment);
    }
	
	private void registerEnchantments() {
		try {
            Field acceptingNew = Enchantment.class.getDeclaredField("acceptingNew");
            acceptingNew.setAccessible(true);
            acceptingNew.set(null, true);
            for (BEnchantment ench : BEnchantment.getCustomEnchants())
            	Enchantment.registerEnchantment(ench);
            plugin.getSLF4JLogger().info("Registered custom enchantments.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}

    /**
     * Replace all forbidden enchantments with their replacement.
     * @deprecated Forced removal of all forbidden enchantments will occur in future with use of LootTables etc. and there will be no reason to keep this method.
     */
    @Deprecated(forRemoval = true)
    public void replaceEnchantments(ItemStack item, boolean format) {
        vanillaReplacements.forEach((replacedEnchant, replacementEnchant) -> {
            if (item.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                if (meta.hasStoredEnchant(replacedEnchant)) {
                    int level = meta.getStoredEnchantLevel(replacedEnchant);
                    meta.removeStoredEnchant(replacedEnchant);
                    meta.addStoredEnchant(replacementEnchant, level, false);
                    item.setItemMeta(meta);
                }
            } else {
                if (!item.containsEnchantment(replacedEnchant)) return;
                int level = item.getEnchantmentLevel(replacedEnchant);
                item.removeEnchantment(replacedEnchant);
                item.addUnsafeEnchantment(replacementEnchant, level);
            }
        });

        if (format) BeanItem.formatItem(item);
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
           for (BEnchantment ench : BEnchantment.getCustomEnchants()) {
        	   byKey.remove(ench.getKey());
        	   byName.remove(ench.getName());
           }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
}
