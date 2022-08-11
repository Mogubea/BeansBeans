package me.playground.playerprofile.stats;

public class DirtyDouble extends DirtyVal<Double> {

	public DirtyDouble(double value) {
		super(value);
	}
	
	public DirtyDouble addToValue(int add) {
		value += add;
		return setDirty(true);
	}

	public DirtyDouble setValue(double newValue, boolean dirty) {
		value = newValue;
		if (dirty) setDirty(true);
		return this;
	}
	
	@Override
	public DirtyDouble setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
