package me.playground.data;

import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import me.playground.main.Main;

/**
 * 
 * A sub-class of {@link PrivateDatasource} specific to objects that like to show up on the dynmap. Sub-classes of this abstract
 * will have easier access to the appropriate dynmap methods.
 * 
 * @author Mogubean
 * @param <T> - The object type
 */
public abstract class DynmapDatasource<T> extends PrivateDatasource {
	
	protected final MarkerSet markerSet;
	
	protected DynmapDatasource(Main plugin, String markerSetName) {
		super(plugin);
		
		if (isDynmapEnabled()) {
			MarkerSet set = getMarkerAPI().createMarkerSet("me.playground.markers."+markerSetName.toLowerCase(), markerSetName, getMarkerAPI().getMarkerIcons(), false);
			if (set == null) { // If it already exists, it will return null. This is a /reload precaution.
				getMarkerAPI().getMarkerSet("me.playground.markers."+markerSetName.toLowerCase()).deleteMarkerSet();
				set = getMarkerAPI().createMarkerSet("me.playground.markers."+markerSetName.toLowerCase(), markerSetName, getMarkerAPI().getMarkerIcons(), false);
			}
			markerSet = set;
		} else {
			markerSet = null;
		}
	}
	
	protected boolean isDynmapEnabled() {
		return dc.isDynmapEnabled();
	}
	
	protected MarkerAPI getMarkerAPI() {
		return dc.getDynmapAPI().getMarkerAPI();
	}
	
	protected MarkerSet getMarkerSet() {
		return markerSet;
	}
	
	/**
	 * Update the dynmap marker for the specified object.
	 */
	public abstract void updateMarker(T object);
	
	/**
	 * Remove the dynmap marker for the specified object.
	 */
	public abstract void removeMarker(T object);
	
}
