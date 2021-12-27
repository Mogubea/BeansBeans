package me.playground.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.World;

import me.playground.regions.flags.Flag;

public abstract class RegionBase {
	
	final protected RegionManager rm;
	protected final int regionId;
	protected final World world;
	protected int priority;
	
	protected ConcurrentMap<Flag<?>, Object> flags = new ConcurrentHashMap<>();
	protected List<Flag<?>> dirtyFlags = new ArrayList<Flag<?>>();
	
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
	
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V getFlag(T flag) {
		Object obj = flags.get(flag);
		return (obj != null ? (V)obj : null);
	}
	
	@SuppressWarnings("unchecked") @Nonnull
	public <T extends Flag<V>, V> V getEffectiveFlag(T flag) {
		V val = (V) flags.get(flag);
		if (val == null && !isWorldRegion() && flag.inheritsFromWorld())
			val = rm.getWorldRegion(getWorld()).getFlag(flag);
		if (val == null)
			val = flag.getDefault();
		
		return val;
	}
	
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val) {
		return setFlag(flag, val, true);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val, boolean markDirty) {
		if (val == null)
			flags.remove(flag);
		else
			flags.put(flag, flag.validateValue((V)val));
		
		if (markDirty && !dirtyFlags.contains(flag))
			dirtyFlags.add(flag);
		return (V)val;
	}
	
	public void setFlags(Map<Flag<?>, Object> flags) {
        this.flags = new ConcurrentHashMap<>(flags);
    }
	
	public int getPriority() {
		return priority;
	}
	
	public List<Flag<?>> getDirtyFlags() {
		return dirtyFlags;
	}
	
	public abstract String getName();
	
	public abstract boolean isWorldRegion();
	
}
