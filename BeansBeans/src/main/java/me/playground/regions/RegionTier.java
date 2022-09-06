package me.playground.regions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public enum RegionTier {

	BASIC(1, 25, 1, 0, 0x67ffbb, "Basic"),
	DELUXE(1, 40, 2, 1, 0xff67cf, "Deluxe"),
	PREMIUM(1, 70, 5, 3, 0xff6677, "Premium"),
	MARVELLOUS(2, 125, 10, 5, 0xaaffcf, "Marvellous"),
	EXQUISITE(3, 200, 25, 10, 0xff8867, "Exquisite"),
	LUXURIOUS(4, 250, 50, 20, 0x4499cc, "Luxurious"),
	EXTRAVAGANT(5, 300, 100, 30, 0xffcc66, "Extravagant");

	private final int maxDistanceFromOrigin;
	private final int maxRegionCrystals;
	private final int maxSubRegions;
	private final int countsAsRegions;

	private final String name;
	private final Component displayName;
	private final TextColor colour;

	RegionTier(int countsAs, int maxDist, int maxCrystal, int maxSubRegions, int colour, String displayName) {
		this.countsAsRegions = countsAs;
		this.maxDistanceFromOrigin = maxDist;
		this.maxRegionCrystals = maxCrystal;
		this.maxSubRegions = maxSubRegions;

		this.name = displayName;
		this.colour = TextColor.color(colour);
		this.displayName = Component.text(displayName, this.colour);
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
