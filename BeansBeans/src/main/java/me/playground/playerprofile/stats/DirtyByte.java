package me.playground.playerprofile.stats;

public class DirtyByte extends DirtyVal<Byte> {

	public DirtyByte(byte value) {
		super(value);
	}

	public DirtyByte setValue(byte newValue, boolean dirty) {
		value = newValue;
		if (dirty) setDirty(true);
		return this;
	}
	
	@Override
	public DirtyByte setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
