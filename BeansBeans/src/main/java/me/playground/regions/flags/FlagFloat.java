package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagFloat extends Flag<Float> {
	
	private final float def;
	
	public FlagFloat(String name, float def) {
		super(name, true);
		this.def = def;
	}
	
	@Override
	public Float getDefault() {
		return def;
	}

	@Override
	public Float parseInput(String input) throws CommandException {
		try {
			if (input.equalsIgnoreCase("NULL"))
				return null;
			return Float.parseFloat(input);
		} catch (NumberFormatException e) {
			throw new CommandException(null, "'"+input+"' is not a valid float value.");
		}
	}
	
	@Override
    public Float unmarshal(String o) {
        return Float.parseFloat(o);
    }

    @Override
    public String marshal(Float o) {
        return o.toString();
    }
	
}
