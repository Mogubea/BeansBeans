package me.playground.regions.flags;

public class FlagMemberBoolean extends FlagBoolean {

	public FlagMemberBoolean(String name, String displayName, boolean def) {
		super(name, displayName, def);
	}

	public FlagMemberBoolean(String name, String displayName, boolean def, boolean worldDef, boolean inherit) {
		super(name, displayName, def, worldDef, inherit);
	}
}
