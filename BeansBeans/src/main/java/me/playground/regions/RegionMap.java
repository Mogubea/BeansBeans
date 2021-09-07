package me.playground.regions;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.World;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class RegionMap<T extends Region> {
	private final Multimap<Integer, T> map = LinkedListMultimap.create();
	private final World world;
	
	public RegionMap(World world) {
		this.world = world;
	}
	
	public void add(T entry) {
		int minX = toBitLimit(entry.getMinimumPoint().getBlockX());
		int maxX = toBitLimit(entry.getMaximumPoint().getBlockX());
		int minZ = toBitLimit(entry.getMinimumPoint().getBlockZ());
		int maxZ = toBitLimit(entry.getMaximumPoint().getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				int key = createRegionKey(x, z);
				map.put(key, entry);
				entry.addRegionKey(key);
			}
		}
	}
	
	public void remove(T entry) {
		for (int key : entry.getRegionKeys())
			map.get(key).remove(entry);
		entry.getRegionKeys().clear();
	}
	
	public void update(T entry) {
		remove(entry);
		add(entry);
	}
	
	public boolean contains(T entry) {
		return map.values().contains(entry);
	}
	
	public Collection<T> values() {
		return map.values();
	}
	
	public Collection<T> getRegions(int x, int y, int z) {
		Collection<T> ack = new ArrayList<T>();
		for (T r : map.get(createRegionKey(toBitLimit(x), toBitLimit(z))))
			if (r.isInsideRegion(x, y, z))
				ack.add(r);
		return ack;
	}
	
	public boolean containsAnyRegionsAt(int x, int y, int z) {
		return !getRegions(x, y, z).isEmpty();
	}
	
	private int toBitLimit(int val) {
	    return (val >> 4);
	}

	private int createRegionKey(int x, int z) {
	    return ((int) x << 16) | ((int) z - Short.MIN_VALUE);
	}
	
	public void clear() {
		map.clear();
	}
	
	public World getWorld() {
		return world;
	}
	
}
