package me.playground.regions;

import java.util.*;

import me.playground.playerprofile.PlayerProfile;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import me.playground.main.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionManager {
	private final RegionDatasource datasource;
	
	private final Map<UUID, Region> worldRegions = new HashMap<>();
	private final Map<UUID, RegionMap<Region>> worldMaps = new HashMap<>();
	private final Map<String, Region> allRegionsByName = new HashMap<>();
	private final Map<Integer, Region> allRegionsById = new HashMap<>();
	private final Map<Integer, Set<Region>> regionsByCreatorId = new HashMap<>();

	private final float BLOCK_COST = 8; // Base block cost when expanding region
	
	public RegionManager(Main plugin) {
		datasource = new RegionDatasource(plugin, this);
		datasource.loadAll();

		// Region Visualiser
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> plugin.getServer().getOnlinePlayers().forEach(player -> {
			PlayerProfile pp = PlayerProfile.from(player);
			pp.getVisualisedRegions().forEach((region, visualiser) -> visualiser.tick());

		}), RegionVisualiser.INTERVAL, RegionVisualiser.INTERVAL);
	}
	
	public WorldRegion initWorldRegion(World world) {
		worldMaps.put(world.getUID(), new RegionMap<>(world));
		WorldRegion nwr = new WorldRegion(this, -datasource.getWorldManager().getWorldId(world), world);
		worldRegions.put(world.getUID(), nwr);
		registerRegion(nwr);
		return nwr;
	}

	@NotNull
	protected RegionMap<Region> getRegionMap(World world) {
		return worldMaps.get(world.getUID());
	}

	@Nullable
	public Region getRegion(String name) {
		return allRegionsByName.get(name);
	}

	@Nullable
	public Region getRegion(int id) {
		return allRegionsById.get(id);
	}
	
	public void reload() {
		datasource.saveAll();
		worldRegions.clear();
		worldMaps.clear();
		allRegionsByName.clear();
		allRegionsById.clear();
		regionsByCreatorId.clear();
		datasource.loadAll();
	}

	/**
	 * Attempts to create a new {@link Region}.
	 * @return The newly created region or null.
	 */
	@Nullable
	public Region createRegion(int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		return datasource.createNewRegion(creator, priority, parent, name, world, min, max);
	}

	/**
	 * Attempts to create a new {@link PlayerRegion}.
	 * @return The newly created region or null.
	 */
	@Nullable
	public PlayerRegion createPlayerRegion(PlayerProfile owner, World world, BlockVector min, BlockVector max, BlockVector origin) {
		return datasource.createPlayerRegion(owner, world, min, max, origin);
	}

	protected void registerRegion(Region region) {
		if (!region.isWorldRegion() && region.getWorld() != null)
			getRegionMap(region.getWorld()).add(region);
		
		this.allRegionsByName.put(region.getName().toLowerCase(), region);
		this.allRegionsById.put(region.getRegionId(), region);
		addUnderRegionCreator(region);
	}
	
	public void refreshRegion(Region region) {
		getRegionMap(region.getWorld()).update(region);
	}
	
	public void renameRegion(String name, String newName) {
		Region r = allRegionsByName.get(name.toLowerCase());
		allRegionsByName.remove(name.toLowerCase());
		allRegionsByName.put(newName.toLowerCase(), r);
	}
	
	protected void removeRegion(Region region) {
		getRegionMap(region.getWorld()).remove(region);
		allRegionsByName.remove(region.getName().toLowerCase());
		allRegionsById.remove(region.getRegionId());
		if (regionsByCreatorId.containsKey(region.getCreatorId()))
			regionsByCreatorId.get(region.getCreatorId()).remove(region);
		datasource.deleteRegion(region);
	}

	private void addUnderRegionCreator(Region region) {
		Set<Region> regions = regionsByCreatorId.get(region.getCreatorId());
		if (regions == null) {
			regionsByCreatorId.put(region.getCreatorId(), new HashSet<>());
			regions = regionsByCreatorId.get(region.getCreatorId());
		}
		regions.add(region);
	}

	@NotNull
	public Set<Region> getRegionsMadeBy(int creatorId) {
		if (!regionsByCreatorId.containsKey(creatorId))
			regionsByCreatorId.put(creatorId, new HashSet<>());
		return regionsByCreatorId.getOrDefault(creatorId, new HashSet<>());
	}

	/**
	 * Gets the {@link World}'s region.
	 * @return The world's region.
	 */
	@NotNull
	public Region getWorldRegion(World world) {
		return worldRegions.get(world.getUID());
	}

	/**
	 * Gets the highest priority {@link Region} at the provided location.
	 * If there is no regular region, the {@link World}'s region will be returned instead.
	 * @return A region at the given location.
	 */
	@NotNull
	public Region getRegion(Location location) {
		return getRegion(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * Gets the highest priority {@link Region} at the provided location.
	 * If there is no regular region, the {@link World}'s region will be returned instead.
	 * @return A region at the given location.
	 */
	@NotNull
	public Region getRegion(World world, int x, int y, int z) {
		Region dominantRegion = getWorldRegion(world);
		List<Region> regions = getRegions(world, x, y, z);
		if (!regions.isEmpty()) {
			Collections.sort(regions);
			dominantRegion = regions.get(0);
		}
		
		return dominantRegion;
	}

	@NotNull
	public List<Region> getRegions(Location location, int range) {
		List<Region> regions = new ArrayList<>();
		for (int x = location.getBlockX() - range; x < location.getBlockX() + range; x+=4) {
			for (int y = location.getBlockY() - range; y < location.getBlockY() + range; y+=4) {
				for (int z = location.getBlockZ() - range; z < location.getBlockZ() + range; z+=4) {
					getRegionMap(location.getWorld()).getRegions(x, y, z).forEach(region -> {
						if (!regions.contains(region))
							regions.add(region);
					});
				}
			}
		}
		return regions;
	}

	@NotNull
	public List<Region> getRegions(Location location) {
		return getRegions(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	@NotNull
	public List<Region> getRegions(World world, int x, int y, int z) {
		return getRegionMap(world).getRegions(x, y, z);
	}

	@NotNull
	public ArrayList<Region> getAllRegions() {
		return new ArrayList<>(allRegionsByName.values());
	}
	
	public int countRegions() {
		return allRegionsByName.size();
	}
	
	/**
	 * @return the Datasource responsible for managing Region data.
	 */
	protected RegionDatasource getDatasource() {
		return datasource;
	}
	
}
