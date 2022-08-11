package me.playground.playerprofile.stats;

public class DirtyInteger extends DirtyVal<Integer> {
	
	public DirtyInteger(int value) {
		super(value);
	}
	
	public DirtyInteger addToValue(int add) {
		value += add;
		return setDirty(true);
	}

	public DirtyInteger setValue(int newValue, boolean dirty) {
		value = newValue;
		if (dirty) setDirty(true);
		return this;
	}
	
	@Override
	public DirtyInteger setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
