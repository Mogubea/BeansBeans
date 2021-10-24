package me.playground.regions;

import org.bukkit.World;
import org.bukkit.util.BlockVector;

public class CivilizationRegion extends Region {
	
//	private Civilization owner;
	
	public CivilizationRegion(RegionManager rm, int id, int creator, int priority, int parent, String name, World world, BlockVector min, BlockVector max) {
		super(rm, id, creator, priority, parent, name, world, min, max);
	}
	
	
	
}
