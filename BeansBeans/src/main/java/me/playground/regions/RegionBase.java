package me.playground.regions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.World;

import me.playground.data.Datasource;
import me.playground.regions.flags.Flag;

public abstract class RegionBase {
	
	protected final int regionId;
	protected final World world;
	protected int priority;
	
	protected ConcurrentMap<Flag<?>, Object> flags = new ConcurrentHashMap<>();
	
	public RegionBase(int id, World world) {
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
		Object obj = flags.get(flag);
		V val = (V) obj;
		if (val == null && !isWorldRegion() && flag.inheritsFromWorld())
			val = RegionManager.getWorldRegionAt(getWorld()).getFlag(flag);
		if (val == null)
			val = flag.getDefault();
		
		return val;
	}
	
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val) {
		return setFlag(flag, val, true);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Flag<V>, V> V setFlag(T flag, @Nullable Object val, boolean save) {
		if (val == null) {
			if (save)
				Datasource.removeRegionFlag(regionId, flag.getName());
			flags.remove(flag);
		} else {
			if (save)
				Datasource.setRegionFlag(regionId, flag.getName(), flag.marshal((V)val));
			flags.put(flag, val);
		}
		return (V) val;
	}
	
	public void setFlags(Map<Flag<?>, Object> flags) {
        this.flags = new ConcurrentHashMap<>(flags);
    }
	
	public int getPriority() {
		return priority;
	}
	
	public int setPriority(int val) {
		return priority = val;
	}
	
	public abstract String getName();
	
	public abstract boolean isWorldRegion();
	
}
