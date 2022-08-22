package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagInt extends Flag<Integer> {

	private int min, max;

	public FlagInt(String name, String displayName, int def) {
		super(name, displayName, def, def, true);
	}

	public FlagInt(String name, String displayName, int def, int worldDef, boolean inherit) {
		super(name, displayName, def, worldDef, inherit);
	}

	@Override
	public Integer parseInput(String input) throws CommandException {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException e) {
			throw new CommandException(null, "'"+input+"' is not a valid number value.");
		}
	}

	public int getMinimum() {
		return this.min;
	}

	public int getMaximum() {
		return this.max;
	}

	public FlagInt setMinimumValue(int min) {
		this.min = min;
		return this;
	}

	public FlagInt setMaximumValue(int max) {
		this.max = max;
		return this;
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
