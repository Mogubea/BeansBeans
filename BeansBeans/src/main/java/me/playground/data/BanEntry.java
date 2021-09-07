package me.playground.data;

public class BanEntry {
	
	private long banStart, banEnd;
	private String banReason;
	
	public BanEntry(long start, long end, String reason) {
		this.banStart = start;
		this.banEnd = end;
		this.banReason = reason;
	}
	
	public long getBanStart() {
		return banStart;
	}
	
	public long getBanEnd() {
		return banEnd;
	}
	
	public String getTimeRemaining() {
		long rem = getBanEnd() - (System.currentTimeMillis()/1000);
		
		if (rem <= 60)
			return rem + " second" + (rem>1?"s":"");
		
		if (rem <= 60*60)
			return rem/60 + " minute" + ((rem/60)>1?"s":"");
		
		return (rem/60/60) + " hour" + ((rem/60/60/24)>1?"s":"");
	}
	
	public String getBanReason() {
		return banReason;
	}
	
}
