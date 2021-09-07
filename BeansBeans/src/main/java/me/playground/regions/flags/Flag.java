package me.playground.regions.flags;

import javax.annotation.Nullable;

public abstract class Flag<T> {
	
	private final String name;
	private final boolean inheritsFromWorld;
	
	protected Flag(String name, boolean inheritFromWorld) {
		this.name = name;
		this.inheritsFromWorld = inheritFromWorld;
	}
	
	public String getName() {
		return name;
	}
	
	@Nullable
	public T getDefault() {
		return null;
	}
	
	public boolean inheritsFromWorld() {
		return inheritsFromWorld;
	}
	
	public abstract T parseInput(String input);
	
	public abstract T unmarshal(String o);
	
	public abstract String marshal(T o);
	
}
