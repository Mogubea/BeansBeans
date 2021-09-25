package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;

public abstract class Flag<T> {
	
	private final String name;
	private final String displayName;
	private final boolean inheritsFromWorld;
	private final List<Component> description = new ArrayList<Component>();
	private boolean needsPermission = false;
	
	protected Flag(@Nonnull String name, @Nonnull String displayName, boolean inheritFromWorld) {
		this.name = name;
		this.displayName = displayName;
		this.inheritsFromWorld = inheritFromWorld;
	}
	
	/**
	 * @return The flag's true name. This should never be changed once used on the live server.
	 */
	@Nonnull
	public String getName() {
		return name;
	}
	
	@Nonnull
	public String getDisplayName() {
		return displayName;
	}
	
	@Nullable
	public T getDefault() {
		return null;
	}
	
	public boolean inheritsFromWorld() {
		return inheritsFromWorld;
	}
	
	/**
	 * Sets the description for this flag, is only used in the {@link Flags} class when declaring a flag.
	 * <p>Descriptions should be divided into multiple lines of {@link Component}s to best fit item lore as
	 * this is where most players will be seeing information about a region flag.
	 * @param desc - The desired description of this flag.
	 * @throws UnsupportedOperationException if an attempt to change the description is made.
	 * @return The flag.
	 */
	@SuppressWarnings("unchecked")
	public <F extends Flag<?>> F setDescription(Component...desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+name+"\".");
		final int size = desc.length;
		for (int x = -1; ++x < size;)
			this.description.add(desc[x]);
		return (F) this;
	}
	
	/**
	 * @return The description given to the flag on server boot.
	 */
	@Nonnull
	public List<Component> getDescription() {
		return description;
	}
	
	/**
	 * Mark the flag as needing an explicit permission string to be changed by players.
	 */
	@SuppressWarnings("unchecked")
	public <F extends Flag<T>> F setNeedsPermission() {
		this.needsPermission = true;
		return (F) this;
	}
	
	public String getPermission() {
		return getName();
	}
	
	public boolean needsPermission() {
		return this.needsPermission;
	}
	
	/**
	 * Validates the value given. For example; if there's an invalid entry, change it.
	 */
	public abstract T validateValue(T o);
	
	public abstract T parseInput(String input);
	
	public abstract T unmarshal(String o);
	
	public abstract String marshal(T o);
	
}
