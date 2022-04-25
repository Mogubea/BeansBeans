package me.playground.gui.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.playground.playerprofile.settings.PlayerSetting;
import net.kyori.adventure.text.Component;

public class BeanGuiPlayerSettings extends BeanGuiPlayer {
	
	private Map<Integer, PlayerSetting> mapping = new HashMap<Integer, PlayerSetting>();
	
	public BeanGuiPlayerSettings(Player p) {
		super(p);
		
		setName("Settings");
		this.presetSize = 54;
		this.presetInv = new ItemStack[] {
				blank,blank,bBlank,bBlank,null,bBlank,bBlank,blank,blank,
				blank,null,null,null,null,null,null,null,blank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				bBlank,null,null,null,null,null,null,null,bBlank,
				blank,blank,blank,blank,goBack,blank,blank,blank,blank
		};
	}

	@Override
	public void onInventoryClosed(InventoryCloseEvent e) {
		
	}

	@Override
	public void onInventoryClicked(InventoryClickEvent e) {
		final int slot = e.getRawSlot();
		final PlayerSetting setting = mapping.get(slot);
		
		if (setting == null) return;
		
		tpp.flipSetting(setting);
		p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.2F, 0.8F);
		onInventoryOpened();
	}

	@Override
	public void onInventoryOpened() {
		ItemStack[] contents = i.getContents();
		contents[4] = newItem(tpp.getSkull(), tpp.getColouredName());
		
		PlayerSetting[] settings = PlayerSetting.values();
		
		for (int x = 0; x < settings.length; x++) {
			PlayerSetting setting = settings[x];
//			if (!pp.hasPermission(setting.getPermissionString())) continue; Does work, just need to change this gui
			
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
			
			int slot = 10 + (x % 7) + ((x / 7) * 9);
			contents[slot] = settingItem;
			mapping.put(slot, setting);
		}
		
		i.setContents(contents);
	}
	
}
