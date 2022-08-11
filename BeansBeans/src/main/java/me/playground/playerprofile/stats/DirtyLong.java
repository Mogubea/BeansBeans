package me.playground.playerprofile.stats;

public class DirtyLong extends DirtyVal<Long> {

	public DirtyLong(long value) {
		super(value);
	}
	
	public DirtyLong addToValue(long add) {
		value += add;
		return setDirty(true);
	}

	public DirtyLong setValue(long newValue, boolean dirty) {
		value = newValue;
		if (dirty) setDirty(true);
		return this;
	}

	@Override
	public DirtyLong setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
