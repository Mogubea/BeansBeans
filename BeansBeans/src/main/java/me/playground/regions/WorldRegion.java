package me.playground.regions;

import me.playground.utils.BeanColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Represents a world region
 */
public class WorldRegion extends Region {

	public WorldRegion(RegionManager rm, int id, World world) {
		super(rm, id, 0, 0, world.getName(), world, null, null);
	}

	@Nullable
	@Override
	public BoundingBox getBoundingBox() {
		return null;
	}

	@Override
	public Location getRegionCenter() {
		return getWorld().getSpawnLocation();
	}

	@Override
	public boolean isByRegionBoundary(int x, int y, int z, int distance) {
		return false;
	}

	@Override
	@NotNull
	protected Collection<Player> getPlayersInRegion() {
		return getWorld().getPlayers();
	}

	@Override
	@NotNull
	public Component toComponent() {
		if (component != null) return component;
		Component text = Component.text("\u00a7rWorld Region" + "\n\u00a7e(\u00a78World: " + getWorld().getName() + "\u00a7e)").colorIfAbsent(BeanColor.REGION_WORLD);
		Component done = getColouredName().hoverEvent(HoverEvent.showText(text));
		component = Component.empty().append(done.clickEvent(ClickEvent.suggestCommand("/world tpto " + getName())));
		return component;
	}

	@Override
	public boolean isWorldRegion() {
		return true;
	}

	/**
	 * You cannot delete a world region.
	 */
	@Override
	public void delete() {
		throw new UnsupportedOperationException("World Regions cannot be deleted.");
	}

}
