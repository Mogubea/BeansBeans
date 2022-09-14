package me.playground.items;

import me.playground.gui.BeanGuiConfirm;
import me.playground.items.lore.Lore;
import me.playground.regions.Region;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class BItemRegionCapsule extends BeanBlock {

	private final int claimSize;

	public BItemRegionCapsule(int numeric, String identifier, String name, ItemRarity rarity, int claimSize) {
		super(numeric, identifier, name, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=", rarity);
		this.claimSize = claimSize;
		setDefaultLore(Lore.getBuilder("Right click in an unclaimed area to automatically claim a &b" + claimSize + " Cubic Block&r sized &9region &rfor yourself.").build().getLore());
	}

	@Override
	protected void onBlockPlace(BlockPlaceEvent e) {
		e.setCancelled(true);

		final Player p = e.getPlayer();
		final PlayerProfile pp = PlayerProfile.from(e.getPlayer());
		final Location l = e.getBlock().getLocation();
		final RegionManager rm = Main.getRegionManager();
		final ItemStack itemStack = e.getItemInHand();

		if (pp.getRegions().size() >= pp.getRegionLimit()) {
			p.sendActionBar(Component.text("\u00a7cYou cannot create anymore regions."));
			return;
		}

		if (!rm.getRegions(l).isEmpty()) {
			p.sendActionBar(Component.text("\u00a7cThis area is already claimed."));
			return;
		}

		List<Region> nearbyRegions = rm.getRegions(l, 150 + claimSize);
		if (!nearbyRegions.isEmpty()) {
			p.sendActionBar(Component.text("\u00a7cThis area is too close to " + (nearbyRegions.size() > 1 ? nearbyRegions.size() + " other claimed areas" : "another claimed area.")));
			return;
		}

		itemStack.subtract(1);

		new BeanGuiConfirm(p, Lore.getBuilder("Are you sure you wish to settle your region here?\n\nA &b/rwarp&r will be created for you upon creation so you can always return here.").dontFormatColours().build().getLoree()) {
			@Override
			public void onAccept() {
				int eachDirection = (claimSize - 1) / 2;

				Region region = rm.createPlayerRegion(pp, p.getWorld(),
						new BlockVector(l.getBlockX() - eachDirection, l.getBlockY() - eachDirection, l.getBlockZ() - eachDirection),
						new BlockVector(l.getBlockX() + eachDirection, l.getBlockY() + eachDirection, l.getBlockZ() + eachDirection),
						new BlockVector(l.getBlockX(), l.getBlockY(), l.getBlockZ()));

				p.sendMessage(Component.text("You have successfully created a Region in this area.", NamedTextColor.GREEN));
			}

			@Override
			public void onDecline() {
				ItemStack giveBack = itemStack.clone();
				giveBack.setAmount(1);
				pp.giveItem(giveBack);
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
