package me.playground.playerprofile.stats;

public class DirtyDouble extends DirtyVal<Double> {

	public DirtyDouble(double value) {
		super(value);
	}
	
	public DirtyDouble addToValue(double add) {
		value += add;
		return setDirty(true);
	}

	@Override
	public DirtyDouble setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
