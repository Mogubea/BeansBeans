package me.playground.skills;

public class SkillInfo {
	
	private static final String[] levelTitles = {"F-", "F", "F+", "E-", "E", "E+", "D-", "D", "D+", "C-", "C", "C+", "B-", "B", "B+", "A-", "A", "A+", "S-", "S", "S+"};
	
	private byte level = 0;
	private long totalXp;
	private int curLvlXp;
	
	private short skillPoints;
	private byte[] perkLevels = new byte[14];
	
	public SkillInfo() {
	}
	
	public SkillInfo(long totalXp) {
		this.totalXp = totalXp;
		calculateLevel();
	}
	
	public SkillInfo(int level, long totalXp) {
		this.level = (byte) level;
		this.totalXp = totalXp;
	}
	
	public int getSkillPoints() {
		return skillPoints;
	}
	
	protected void addXP(long amt) {
		totalXp+=amt;
		curLvlXp+=amt;
		if (curLvlXp>=getXPRequirement(level))
			calculateLevel();
	}
	
	public long getTotalXP() {
		return this.totalXp;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public String getNextGrade() {
		if (level < 0) return levelTitles[0];
		if (level+1 >= levelTitles.length) return levelTitles[levelTitles.length-1];
		return levelTitles[level + 1];
	}
	
	public String getGrade() {
		if (level < 0) return levelTitles[0];
		if (level >= levelTitles.length) return levelTitles[levelTitles.length-1];
		return levelTitles[level];
	}
	
	private void calculateLevel() {
		long xp = totalXp;
		byte lvl = 0;
		long xpReq = getXPRequirement(lvl);
		
		while(xp>=(xpReq)) {
			lvl++;
			xp-=xpReq;
			xpReq = getXPRequirement(lvl);
		}
		level = lvl;
		curLvlXp = (int) xp;
	}
	
	private int getXPRequirement(int level) {
		if (level < 0) level = 0;
		return (level * (level * 3000) - ((level * 10000))) + 30000; // 1 = 23000 total, 10 = 905000 total, 20 = 7110000 total
	}
	
	public long getXPRequirement() {
		return getXPRequirement(level);
	}
	
	public double getLevelProgress() {
		if (curLvlXp < 0) calculateLevel();
		return (curLvlXp+0.0) / (getXPRequirement(level)+0.0);
	}
	
	public int getLevelXP() {
		return curLvlXp;
	}
	
}
