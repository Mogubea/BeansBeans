package me.playground.regions.flags;

import me.playground.command.CommandException;

public class FlagFloat extends Flag<Float> {
	
	private final float def;
	private final float min, max;
	
	public FlagFloat(String name, String displayName, float def, float min, float max) {
		super(name, displayName, true);
		this.def = def;
		this.min = min;
		this.max = max;
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
    
    public float getMinimum() {
    	return this.min;
    }
    
    public float getMaximum() {
    	return this.max;
    }

	@Override
	public Float validateValue(Float o) {
		if (o < min) o = min;
		else if (o > max) o = max;
		
		return o;
	}
	
}
