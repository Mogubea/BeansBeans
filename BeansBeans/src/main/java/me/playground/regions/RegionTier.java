package me.playground.regions;

public enum RegionTier {

	BASIC(25, 1, 1),
	DELUXE(40, 2, 2),
	SUPREME(70, 3, 5);

	private final int maxDistanceFromOrigin;
	private final int maxRegionCrystals;
	private final int maxSubRegions;

	RegionTier(int maxDist, int maxCrystal, int maxSubRegions) {
		this.maxDistanceFromOrigin = maxDist;
		this.maxRegionCrystals = maxCrystal;
		this.maxSubRegions = maxSubRegions;
	}

	public int getMaxDistanceFromOrigin() {
		return maxDistanceFromOrigin;
	}

	public int getMaxRegionCrystals() {
		return maxRegionCrystals;
	}

	public int getMaxSubRegions() {
		return maxSubRegions;
	}

}
