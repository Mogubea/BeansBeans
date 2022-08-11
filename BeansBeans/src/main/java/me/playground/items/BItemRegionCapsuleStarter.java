package me.playground.items;

import me.playground.gui.BeanGuiConfirm;
import me.playground.items.lore.Lore;
import me.playground.regions.PlayerRegion;
import me.playground.regions.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import me.playground.main.Main;
import me.playground.playerprofile.PlayerProfile;
import me.playground.regions.RegionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

import java.util.List;

public class BItemRegionCapsuleStarter extends BeanBlock {
	
	public BItemRegionCapsuleStarter(int numeric, String identifier, String name, ItemRarity rarity) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=", rarity);
		setDefaultLore(Lore.getBuilder("Right click in an unclaimed area to automatically claim a &b21 Cubic Block&r sized &9region &rfor yourself.").build().getLore());
	}

	@Override
	protected void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(true);

		final Player p = e.getPlayer();
		final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		final Location l = e.getBlock().getLocation();
		final RegionManager rm = Main.getRegionManager();
		final ItemStack itemStack = e.getItemInHand();

		if (!rm.getRegions(l).isEmpty()) {
			p.sendActionBar(Component.text("\u00a7cThis area is already claimed."));
			return;
		}

		List<Region> nearbyRegions = rm.getRegions(l, 45);
		if (!nearbyRegions.isEmpty()) {
			p.sendActionBar(Component.text("\u00a7cThis area is too close to " + (nearbyRegions.size() > 1 ? nearbyRegions.size() + " other claimed areas" : "another claimed area.")));
			return;
		}

		itemStack.subtract(1);

		new BeanGuiConfirm(p, Lore.getBuilder("Are you sure you wish to settle your region here?").dontFormatColours().build().getLoree()) {
			@Override
			public void onAccept() {
				rm.createPlayerRegion(pp, p.getWorld(),
						new BlockVector(l.getBlockX() - 10, l.getBlockY() - 10, l.getBlockZ() - 10),
						new BlockVector(l.getBlockX() + 10, l.getBlockY() + 10, l.getBlockZ() + 10),
						new BlockVector(l.getBlockX(), l.getBlockY(), l.getBlockZ()));

				p.sendMessage(Component.text("._."));
			}

			@Override
			public void onDecline() {
				ItemStack giveBack = itemStack.clone();
				giveBack.setAmount(1);
				pp.giveItem();
			}
		}.openInventory();

		/*p.sendBlockChange(l, Material.STRUCTURE_BLOCK.createBlockData());
		PacketContainer packet = Main.getInstance().getProtocolManager().createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);
		try {
			packet.getBlockPositionModifier().write(0, new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ()));

		}
		*/
	}
}
