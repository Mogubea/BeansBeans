package me.playground.items;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemShopStand extends BeanItem {
	
	public BItemShopStand(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=", rarity);
		setDefaultLore(
				Component.text("Place this down to create your", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("very own ", NamedTextColor.GRAY).append(Component.text("Shop", NamedTextColor.YELLOW).append(Component.text(" and start selling", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false),
				Component.text("via an interactive shopping interface!", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
				Component.text("(Maximum Storage: 1728 Items)", NamedTextColor.DARK_GRAY));
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			e.setCancelled(true);
			final Location l = e.getClickedBlock().getLocation().add(e.getBlockFace().getDirection().toLocation(e.getClickedBlock().getWorld()));
			final boolean upwards = e.getBlockFace() == BlockFace.UP;
			final boolean carpet = l.subtract(0,upwards ? 1 : 0,0).getBlock().getType().toString().endsWith("CARPET");
			
			if (upwards && !carpet)
				l.add(0,1,0);
			
			final Block b = l.getBlock();
			final Block below = l.subtract(0,1,0).getBlock();
			
			if ((!carpet && b.getType() != Material.AIR) || !below.isSolid() || below.isPassable() || below.getType() == Material.HOPPER) {
				e.getPlayer().sendActionBar(Component.text("\u00a7cThat is an invalid shop location!"));
			} else if (b.getLocation().add(0.5, 0.5, 0.5).getNearbyEntitiesByType(ArmorStand.class, 2.49).size() > 0) {
				e.getPlayer().sendActionBar(Component.text("\u00a7cThere are too many stands here!"));
			} else {
				try {
					final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
					Main.getShopManager().createNewShop(pp.getId(), l);
					if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
						e.getItem().setAmount(e.getItem().getAmount() - 1);
				} catch (Throwable ee) {
					e.getPlayer().sendActionBar(Component.text("\u00a7cReport this, there was a problem creating this shop!"));
					ee.printStackTrace();
				}
			}
		}
	}
}
