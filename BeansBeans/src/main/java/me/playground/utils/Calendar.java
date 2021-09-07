package me.playground.utils;

import org.bukkit.World;

public class Calendar {
	
	public static int getTime(World w) {
		final int curTicks = (int) ((w.getTime()+1) / 250) * 250; // per 15 mins ingame
		return curTicks;
	}
	
	public static int getHour(int tick) {
		return (tick+6000) / 1000;
	}
	
	public static int getQuarterHour(int tick) {
		return (((tick+6000) % 1000) / 250) * 15;
	}
	
	public static String getTimeString(int tick, boolean ampm) {
		int hour = getHour(tick);
		final int minute = getQuarterHour(tick);
		final boolean afternoon = hour > 11 && hour < 24;
		
		if (ampm && hour > 24)
			hour -= 24;
		else if (ampm && hour > 12)
			hour -= 12;
		
		return (hour<10 ? "0" : "") + hour + ":" + (minute<15 ? "0" : "") + minute + (ampm ? (afternoon ? " pm" : " am") : "");
	}
	
	public static int getDay(long longtick) {
		return (int) (((longtick+6000) / 24000) + 1);
	}
	
}
