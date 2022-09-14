package me.playground.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.bukkit.World;

import me.playground.regions.flags.Flag;
import org.jetbrains.annotations.NotNull;

public abstract class RegionBase {
	
	final protected RegionManager rm;
	protected final int regionId;
	protected final World world;
	protected int priority;
	
	protected ConcurrentMap<Flag<?>, Object> flags = new ConcurrentHashMap<>();
	protected List<Flag<?>> dirtyFlags = new ArrayList<>();
	
	public RegionBase(RegionManager rm, int id, World world) {
		this.rm = rm;
		this.regionId = id;
		this.world = world;
	}
	
	public World getWorld() {
		return world;
	}
	
	public int getRegionId() {
		return regionId;
	}
	
	public Map<Flag<?>, Object> getFlags() {
		return this.flags;
	}

	/**
	 * Gets this region's flag value. Ignores inheritance. But considers defaults.
	 */
	@NotNull
	public <T extends Flag<V>, V> V getFlag(T flag) {
		return getFlag(flag, false);
	}

	/**
	 * Gets this region's flag value. Ignores inheritance.
	 * @param ignoreDefaults Whether to ignore the flag's default values or not.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V getFlag(T flag, boolean ignoreDefaults) {
		Object obj = flags.get(flag);
		if (ignoreDefaults) return (obj != null ? (V)obj : null);
		return obj != null ? (V)obj : getDefaultValue(flag);
	}

	/**
	 * Gets this region's flag value. Considers inheritance and defaults.
	 */
	@NotNull
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V getEffectiveFlag(T flag) {
		V val = (V) flags.get(flag);
		boolean world = isWorldRegion();
		if (val == null && !world && flag.inheritsFromWorld() && (getDefaultValue(flag) == flag.getWorldDefault()))
			val = rm.getWorldRegion(getWorld()).getFlag(flag);
		if (val == null)
			val = getDefaultValue(flag);
		
		return val;
	}
	
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val) {
		return setFlag(flag, val, true);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val, boolean markDirty) {
		if (val == null || val.equals(getDefaultValue(flag)))
			flags.remove(flag);
		else
			flags.put(flag, flag.validateValue((V)val));
		
		if (markDirty && !dirtyFlags.contains(flag))
			dirtyFlags.add(flag);

		if (this instanceof Region region)
			flag.onUpdate(region);

		return (V)val;
	}
	
	public void setFlags(Map<Flag<?>, Object> flags) {
        this.flags = new ConcurrentHashMap<>(flags);
    }

	/**
	 * Get the {@link Flag}'s default value based upon the type of {@link Region} this is.
	 * @return the {@link Flag}'s default value.
	 */
	@NotNull
	private <T extends Flag<V>, V> V getDefaultValue(T flag) {
		if (this instanceof WorldRegion)
			return flag.getWorldDefault();
		if (this instanceof PlayerRegion)
			return flag.getPlayerDefault();
		return flag.getDefault();
	}

	public int getPriority() {
		return priority;
	}
	
	public List<Flag<?>> getDirtyFlags() {
		return dirtyFlags;
	}
	
	public abstract String getName();

	public boolean isWorldRegion() {
		return false;
	}

	@NotNull
	protected abstract RegionType getType();
	
}
