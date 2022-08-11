package me.playground.regions;

import me.playground.playerprofile.ProfileStore;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

public class PlayerRegion extends Region {

	private RegionTier regionTier = RegionTier.BASIC;
	private BlockVector originPoint;

	public PlayerRegion(RegionManager rm, int ownerId, int id, String name, World world, BlockVector min, BlockVector max, @NotNull BlockVector originPoint) {
		super(rm, id, ownerId, 0, 0, name, world, min, max);
		this.originPoint = originPoint;

		if (getName().startsWith("%player")) {
			String[] split = name.split("\\.");
			try {
				int parse = Integer.parseInt(split[1]);
				componentName = Component.text(ProfileStore.from(parse).getDisplayName() + "'s Region", getColour());
			} catch (Exception ignored) {
			}
		}
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
	protected RegionType getRegionType() {
		return RegionType.PLAYER;
	}

}
