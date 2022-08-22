package me.playground.playerprofile.stats;

public class DirtyFloat extends DirtyVal<Float> {

	public DirtyFloat(float value) {
		super(value);
	}
	
	public DirtyFloat addToValue(float add) {
		value += add;
		return setDirty(true);
	}
	
	@Override
	public DirtyFloat setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
