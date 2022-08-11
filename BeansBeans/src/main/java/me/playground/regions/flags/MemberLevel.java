package me.playground.regions.flags;

public enum MemberLevel {
	
	NONE, // A player that is not in the region member list
	VISITOR, // A player below the regular rank
	MEMBER, // The average player in the average region
	TRUSTED, // Slightly higher rank than member
	OFFICER, // Able to add and kick members from the region
	OWNER, // Able to modify the region flags
	MASTER; // Bypass and access all, natural default and override for Administrators.
	
	final String niceName;
	MemberLevel() {
		niceName = this.name().charAt(0) + name().substring(1).toLowerCase();
	}
	
	public boolean higherThan(MemberLevel that) {
		return this.ordinal() > that.ordinal();
	}
	
	public boolean lowerOrEqTo(MemberLevel that) {
		return !higherThan(that);
	}
	
	public boolean lowerThan(MemberLevel that) {
		return this.ordinal() < that.ordinal();
	}
	
	@Override
	public String toString() {
		return niceName;
	}
	
}
