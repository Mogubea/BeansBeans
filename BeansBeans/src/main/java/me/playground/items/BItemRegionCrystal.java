package me.playground.items;

import me.playground.entity.CustomEntityType;
import me.playground.items.lore.Lore;
import me.playground.main.Main;
import me.playground.regions.Region;
import me.playground.regions.RegionManager;
import me.playground.regions.flags.MemberLevel;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BItemRegionCrystal extends BeanBlock {

	public BItemRegionCrystal(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTUwNjQ5NjI2YzQxMDEzNTJjNTk5NWM1M2I0OGJmZjYwYTkzODIxMmI3Y2U5MDI0MTVmZWI3NmVhMjczYjM1ZiJ9fX0=", rarity);
		setDefaultLore(Lore.getBuilder("A floating crystal that acts as a sparkly interaction point for the &"+ BeanColor.REGION.asHexString()+"Region Menu&r.").build().getLore());
	}

	@Override
	protected void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(true);

		final Player p = e.getPlayer();
		final Location l = e.getBlock().getLocation();
		final RegionManager rm = Main.getRegionManager();
		final ItemStack itemStack = e.getItemInHand();
		final Region region = rm.getRegion(l);

		boolean override = region.getMember(p).equals(MemberLevel.MASTER);
		int crystals = region.getCrystalCount();

		// World Region Check
		if (!override && region.isWorldRegion()) {
			p.sendActionBar(Component.text("You can't place this here.", NamedTextColor.RED));
		}
		// Maximum Crystal Check
		else if (!override && crystals >= region.getMaxCrystals()) {
			p.sendActionBar(region.getColouredName().append(Component.text(" already has " + (crystals == 1 ? " a crystal." : crystals + " crystals."), NamedTextColor.RED)));
		}
		// Place the Crystal and warn about crystal count.
		else {
			itemStack.subtract(1);
			CustomEntityType.REGION_CRYSTAL.spawn(l.add(0.5, 0.2, 0.5)).setRegion(region);
			p.sendMessage(Component.text("Placed a ", NamedTextColor.GRAY).append(getDisplayName().colorIfAbsent(NamedTextColor.WHITE).hoverEvent(getItemStack().asHoverEvent())).append(Component.text(" in ").append(region.toComponent())
					.append(Component.text(". (").append(Component.text((crystals + 1), BeanColor.REGION_COUNT)).append(Component.text("/").append(Component.text(region.getMaxCrystals(), BeanColor.REGION_MAX))).append(Component.text(")")))).colorIfAbsent(NamedTextColor.GRAY));
		}
	}

	protected String getRarityString() {
		return "Crystal";
	}
}
