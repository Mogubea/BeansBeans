package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagBoolean extends Flag<Boolean> {
	
	public FlagBoolean(String name, String displayName, boolean def) {
		this(name, displayName, def, def, true);
	}
	
	public FlagBoolean(String name, String displayName, boolean def, boolean worldDef, boolean inherit) {
		super(name, displayName, def, worldDef, inherit);
	}

	@Override
	public Boolean parseInput(String input) throws CommandException {
		String s = input.toLowerCase();
		if (s.equals("deny") || s.equals("false"))
			return false;
		if (s.equals("allow") || s.equals("true"))
			return true;
		if (s.equals("none") || s.equals("null"))
			return null;
		
		throw new CommandException(null, "'"+input+"' is not a valid boolean value.");
	}
	
	@Override
    public Boolean unmarshal(String o) {
		if (o == null) return null;
		return o.equalsIgnoreCase("true");
	}

    @Override
    public String marshal(Boolean o) {
		if (o == null) return null;
		return o ? "true" : "false";
	}

	@Override
	public Boolean validateValue(Boolean o) {
		return o;
	}
	
}
