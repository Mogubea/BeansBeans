package me.playground.civilizations.structures;

import org.bukkit.Location;

import me.playground.civilizations.Civilization;

public class Structure {
	
	final private Civilization civilization;
	final private Structures type;
	private int cost;
	private int requesterId;
	private int reviewerId;
	private Status status = Status.PENDING;
	private Location location;
	
	public Structure(Civilization civilization, Structures type, Location location, int requesterId, int cost) {
		this.civilization = civilization;
		this.type = type;
		this.location = location;
		this.requesterId = requesterId;
		this.cost = cost;
	}
	
	public Structure(Civilization civilization, Structures type, Location location, int requesterId, int reviewerId, int cost, Status status) {
		this(civilization, type, location, requesterId, cost);
		this.reviewerId = reviewerId;
		this.status = status;
	}
	
	public Civilization getCivilization() {
		return civilization;
	}
	
	public Structures getType() {
		return type;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public boolean isUnlocked() {
		return status == Status.APPROVED;
	}
	
	public void approve(int reviewerId) {
		this.status = Status.APPROVED;
	}
	
	public void deny(int reviewerId) {
		this.status = Status.DENIED;
	}
	
	public int getRequesterId() {
		return requesterId;
	}
	
	public int getReviewerId() {
		return reviewerId;
	}
	
	public int getCost() {
		return cost;
	}
	
	public Location getLocation() {
		return location;
	}
	
	/**
	 * We do a little trolling
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Structures)
			return ((Structures)obj) == type && isUnlocked();
		return super.equals(obj);
	}
	
	public enum Status {
		PENDING,
		DENIED,
		APPROVED;
	}
	
}
