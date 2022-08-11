package me.playground.playerprofile.stats;

public abstract class DirtyVal<T> {
	
	boolean dirty;
	T value;
	
	public DirtyVal(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}

	public DirtyVal<T> setValue(T newValue) {
		boolean dirty = !value.equals(newValue);
		value = newValue;
		return setDirty(isDirty() || dirty);
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public DirtyVal<T> setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}
	
}
