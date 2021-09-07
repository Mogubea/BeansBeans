package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagBoolean extends Flag<Boolean> {
	
	private final boolean def;
	
	public FlagBoolean(String name, boolean def) {
		this(name, def, true);
	}
	
	public FlagBoolean(String name, boolean def, boolean inherit) {
		super(name, inherit);
		this.def = def;
	}
	
	@Override
	public Boolean getDefault() {
		return def;
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
        if (o.equalsIgnoreCase("true")) {
            return true;
        } else if (o.equalsIgnoreCase("false")) {
            return false;
        } else {
            return null;
        }
    }

    @Override
    public String marshal(Boolean o) {
        if (o == true) {
            return "true";
        } else if (o == false) {
            return "false";
        } else {
            return null;
        }
    }
	
}
