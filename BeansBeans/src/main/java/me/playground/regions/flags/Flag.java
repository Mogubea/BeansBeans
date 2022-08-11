package me.playground.regions.flags;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public abstract class Flag<T> {
	
	private final String name;
	private final String displayName;
	private final boolean inheritsFromWorld;
	private final List<TextComponent> description = new ArrayList<>();
	private boolean needsPermission = false;
	private FlagCategory flagCategory = FlagCategory.MISCELLANEOUS; // Category for GUI
	
	protected final T def;
	protected final T worldDef;
	
	protected Flag(@NotNull String name, @NotNull String displayName, @NotNull T defaultValue, @NotNull T defaultWorldValue, boolean inheritFromWorld) {
		this.name = name;
		this.displayName = displayName;
		this.inheritsFromWorld = inheritFromWorld;
		this.def = defaultValue;
		this.worldDef = defaultWorldValue;
	}
	
	/**
	 * @return The flag's true name. This should never be changed once used on the live server.
	 */
	@NotNull
	public String getName() {
		return name;
	}
	
	@NotNull
	public String getDisplayName() {
		return displayName;
	}
	
	@NotNull
	public T getDefault() {
		return def;
	}
	
	@NotNull
	public T getWorldDefault() {
		return worldDef;
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
	@NotNull
	@SuppressWarnings("unchecked")
	public <F extends Flag<?>> F setDescription(TextComponent...desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+name+"\".");
		final int size = desc.length;
		for (int x = -1; ++x < size;)
			this.description.add(desc[x]);
		return (F) this;
	}

	/**
	 * Sets the description for this flag, is only used in the {@link Flags} class when declaring a flag.
	 * <p>Descriptions should be divided into multiple lines of {@link Component}s to best fit item lore as
	 * this is where most players will be seeing information about a region flag.
	 * @param desc - The desired description of this flag.
	 * @throws UnsupportedOperationException if an attempt to change the description is made.
	 * @return The flag.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public <F extends Flag<?>> F setDescription(List<TextComponent> desc) {
		if (!this.description.isEmpty()) throw new UnsupportedOperationException("A description was already given to Flag \""+name+"\".");
		final int size = desc.size();
		for (int x = -1; ++x < size;)
			this.description.addAll(desc);
		return (F) this;
	}
	
	/**
	 * @return The description given to the flag on server boot.
	 */
	@NotNull
	public List<TextComponent> getDescription() {
		return description;
	}
	
	/**
	 * Mark the flag as needing an explicit permission string to be changed by players.<br><br>
	 * This permission string is formatted as such: <b>bean.region.flag.{@link #getName()}</b>.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public <F extends Flag<T>> F setNeedsPermission() {
		this.needsPermission = true;
		return (F) this;
	}
	
	/**
	 * @return "bean.region.flag." + {@link #getName()}.
	 */
	@NotNull
	public String getPermission() {
		return "bean.region.flag." + getName();
	}
	
	public boolean needsPermission() {
		return this.needsPermission;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	protected <F extends Flag<T>> F setFlagCategory(FlagCategory category) {
		this.flagCategory = category;
		return (F) this;
	}

	@NotNull
	public FlagCategory getCategory() {
		return this.flagCategory;
	}

	/**
	 * Validates the value given. For example; if there's an invalid entry, change it.
	 */
	public abstract T validateValue(T o);
	
	public abstract T parseInput(String input);
	
	public abstract T unmarshal(String o);
	
	public abstract String marshal(T o);

	public enum FlagCategory {
		BLOCKS("Block Flags"),
		TELEPORTING("Teleportation Flags"),
		ENTITIES("Entity Flags"),
		NOTIFICATION("Notification Flags"),
		MISCELLANEOUS("Miscellaneous Flags");

		private final String title;
		FlagCategory(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}

}
