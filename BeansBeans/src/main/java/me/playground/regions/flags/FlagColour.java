package me.playground.regions.flags;

public class FlagColour extends FlagInt {

	public FlagColour(String name, String displayName, int def) {
		super(name, displayName, def);
	}

	public FlagColour(String name, String displayName, int def, int worldDef, boolean inheritFromWorld) {
		super(name, displayName, def, worldDef, inheritFromWorld);
	}
	
}
