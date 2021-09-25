package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagInt extends Flag<Integer> {
	
	private int def;
	
	public FlagInt(String name, String displayName, int def) {
		super(name, displayName, true);
		this.def = def;
	}
	
	@Override
	public Integer getDefault() {
		return def;
	}

	@Override
	public Integer parseInput(String input) throws CommandException {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new CommandException(null, "'"+input+"' is not a valid number value.");
		}
	}
	
	@Override
    public Integer unmarshal(String o) {
        return Integer.parseInt(o);
    }

    @Override
    public String marshal(Integer o) {
        return o.toString();
    }

	@Override
	public Integer validateValue(Integer o) {
		return o;
	}
	
}
