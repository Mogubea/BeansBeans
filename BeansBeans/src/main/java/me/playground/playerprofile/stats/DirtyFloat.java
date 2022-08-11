package me.playground.playerprofile.stats;

public class DirtyFloat extends DirtyVal<Float> {

	public DirtyFloat(float value) {
		super(value);
	}
	
	public DirtyFloat addToValue(float add) {
		value += add;
		return setDirty(true);
	}
	
	public DirtyFloat setValue(float newValue, boolean dirty) {
		value = newValue;
		if (dirty) setDirty(true);
		return this;
	}
	
	@Override
	public DirtyFloat setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
