package me.playground.listeners;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import me.playground.gui.BeanGuiShopCustomer;
import me.playground.gui.BeanGuiShopOwner;
import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.regions.Region;
import me.playground.regions.flags.Flags;
import me.playground.shop.Shop;
import net.kyori.adventure.text.Component;

public class ShopListener extends EventListener {
	
	public ShopListener(Main plugin) {
		super(plugin);
	}
	
	@EventHandler
	public void onShopInteract(PlayerInteractAtEntityEvent e) {
		if (!(e.getRightClicked() instanceof ArmorStand))
			return;
		
		final ArmorStand stand = (ArmorStand) e.getRightClicked();
		final int shopId = stand.getPersistentDataContainer().getOrDefault(key("shopId"), PersistentDataType.INTEGER, -1);
		
		if (shopId == -1)
			return;
		
		final Player p = e.getPlayer();
		
		if (getPlugin().shopManager().isEnabled()) {
			if (p.getGameMode() == GameMode.SPECTATOR)
				return;
			
			final Shop s = getPlugin().shopManager().getShop(shopId);
			if (s != null) {
				if (s.getOwnerId() == PlayerProfile.from(p).getId()) {
					new BeanGuiShopOwner(p, s).openInventory();
				} else {
					final Region r = getRegionAt(e.getRightClicked().getLocation());
					if (!r.getEffectiveFlag(Flags.SHOP_ACCESS)) {
						p.sendActionBar(Component.text("\u00a7cYou don't have permission to access the shops here."));
						return;
					}
					new BeanGuiShopCustomer(p, s).openInventory();
				}
			} else {
				p.sendMessage("\u00a7cAccording to the server, this shop display shouldn't exist, if you believe this is a bug please report it to the Administrators immediately (Shop ID: "+shopId+").");
				if (stand.isInsideVehicle())
					stand.getVehicle().remove();
				for (Entity en : stand.getPassengers())
					en.remove();
				stand.remove();
			}
		} else {
			p.sendActionBar(Component.text("\u00a7cShops have been temporarily disabled."));
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDeathEvent e) {
		if (e.getEntity() instanceof ArmorStand) {
			final int shopId = e.getEntity().getPersistentDataContainer().getOrDefault(key("shopId"), PersistentDataType.INTEGER, -1);
			if (shopId != -1)
				e.setCancelled(true);
		}
	}
	
	private static NamespacedKey key(String key) {
        return new NamespacedKey(Main.getInstance(), key);
    }
	
}
