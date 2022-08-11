package me.playground.regions.flags;

import me.playground.command.CommandException;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class FlagString extends Flag<String> {

	public FlagString(String name, String displayName, String def) {
		super(name, displayName, def, def, true);
	}

	@Override
	public String parseInput(String input) throws CommandException {
		return input;
	}
	
	@Override
    public String unmarshal(String o) {
        return o;
    }

    @Override
    public String marshal(String o) {
        return o;
    }

	@Override
	public String validateValue(String o) {
		return o;
	}

	@NotNull
	public Component getComponentValue(String o) {
		return (o == null || o.isEmpty() ? Component.empty() : Component.text(o));
	}
	
}
