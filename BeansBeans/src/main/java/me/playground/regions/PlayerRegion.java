package me.playground.regions;

import me.playground.playerprofile.ProfileStore;
import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class PlayerRegion extends Region {

	private RegionTier regionTier = RegionTier.BASIC;
	private BlockVector originPoint;
	private String displayOverride;

	public PlayerRegion(RegionManager rm, int ownerId, int id, String name, World world, BlockVector min, BlockVector max, @NotNull BlockVector originPoint) {
		super(rm, id, ownerId, 0, 0, name, world, min, max);
		this.originPoint = originPoint;
		checkName();
	}

	private void checkName() {
		if (getName().startsWith("%player")) {
			String[] split = getName().split("\\.");
			try {
				int parse = Integer.parseInt(split[1]);
				displayOverride = ProfileStore.from(parse).getDisplayName() + "'s Region";
				componentName = Component.text(displayOverride, getColour());
			} catch (Exception ignored) {
			}
		} else {
			displayOverride = null;
		}
	}

	@Override
	public void setName(@Nonnull Player p, @Nonnull String name) {
		super.setName(p, name);
		checkName();
	}

	@Override
	public String getDisplayName() {
		return displayOverride == null ? getName() : displayOverride;
	}

	public Location getOriginLocation() {
		return new Location(world, originPoint.getX(), originPoint.getY(), originPoint.getZ());
	}

	@NotNull
	public BlockVector getOriginPoint() {
		return originPoint;
	}

	@Override
	@NotNull
	protected RegionType getType() {
		return RegionType.PLAYER;
	}

	public RegionTier getTier() {
		return regionTier;
	}

}
