package me.playground.items;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.playerprofile.stats.StatType;
import me.playground.regions.RegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BItemRegionCapsuleStarter extends BeanItem {
	
	public BItemRegionCapsuleStarter(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=", rarity);
		setDefaultLore(
				Component.text("Right Click to claim a ", NamedTextColor.GRAY).append(Component.text("23 Cubic Block", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
				Component.text("sized ", NamedTextColor.GRAY).append(Component.text("Region", NamedTextColor.BLUE).append(Component.text(" for yourself.", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("This ", NamedTextColor.GRAY).append(Component.text("Region", NamedTextColor.BLUE).append(Component.text(" can be expanded and upgraded.", NamedTextColor.GRAY))).decoration(TextDecoration.ITALIC, false),
				Component.text("(Testing)", NamedTextColor.DARK_GRAY));
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if (e.getHand() == EquipmentSlot.HAND && e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			e.setCancelled(true);
			
			final Player p = e.getPlayer();
			final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
			
			if (pp.getStat(StatType.GENERIC, "starterCapsule") > 0) {
				p.sendActionBar(Component.text("\u00a7cYou can only use this once."));
				return;
			}
			
			final Location l = e.getPlayer().getLocation();
			final RegionManager rm = Main.getRegionManager();
			
			if (!rm.getRegions(l).isEmpty()) {
				p.sendActionBar(Component.text("\u00a7cYou cannot use this here."));
				return;
			}
		}
	}
}
