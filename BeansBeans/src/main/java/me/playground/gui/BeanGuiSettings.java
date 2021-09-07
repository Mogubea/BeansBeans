package me.playground.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.playerprofile.settings.PlayerSetting;
import net.kyori.adventure.text.Component;

public class BeanGuiSettings extends BeanGui {
	
	protected static final ItemStack blank = newItem(new ItemStack(Material.RED_STAINED_GLASS_PANE, 1), "");
	
	public BeanGuiSettings(Player p) {
		super(p);
		
		this.name = "Settings";
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				null,null,null,null,null,null,null,null,null,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		e.setCancelled(true);
		
		if (slot < 0 || slot >= e.getInventory().getSize() || e.getInventory().getItem(slot) == null)
			return;
		
		if (slot - 10 > PlayerSetting.values().length)
			return;
		
		pp.flipSetting(PlayerSetting.values()[slot - 10]);
		p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.2F, 0.8F);
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		PlayerSetting[] settings = PlayerSetting.values();
		
		for (int x = 0; x < settings.length; x++) {
			PlayerSetting setting = settings[x];
			boolean enabled = pp.isSettingEnabled(setting);
			ItemStack settingItem = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
			ItemMeta meta = settingItem.getItemMeta();
			ArrayList<Component> lore = new ArrayList<Component>();
			meta.displayName(Component.text((enabled ? "\u00a7a" : "\u00a7c") + setting.getDisplayName()));
			lore.add(Component.text("\u00a77Enabled: \u00a7f" + (enabled ? "Yes" : "No")));
			lore.add(Component.empty());
			lore.addAll(setting.getDescription());
			meta.lore(lore);
			settingItem.setItemMeta(meta);
			
			contents[10 + (x % 7) + ((x / 7) * 9)] = settingItem;
		}
		
		i.setContents(contents);
	}
	
}
