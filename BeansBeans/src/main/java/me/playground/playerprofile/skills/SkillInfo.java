package me.playground.playerprofile.skills;

public class SkillInfo {
	
	private long totalXp;
	private int level;
	
	private long curLvlXp = 0;
	
	public SkillInfo() {
	}
	
	public SkillInfo(long totalXp) {
		this.totalXp = totalXp;
		calculateLevel();
	}
	
	public SkillInfo(int level, long totalXp) {
		this.level = level;
		this.totalXp = totalXp;
	}
	
	public long getXp() {
		return this.totalXp;
	}
	
	public SkillInfo addXp(long amt) {
		totalXp+=amt;
		curLvlXp+=amt;
		if (curLvlXp>=xpRequiredToLevelUp(level))
			calculateLevel();
		return this;
	}
	
	public SkillInfo setLevel(int level) {
		this.level = level;
		this.totalXp = xpRequiredToLevelUp(level-1);
		return this;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	private void calculateLevel() {
		long xp = totalXp;
		int lvl = 0;
		long xpReq = xpRequiredToLevelUp(lvl);
		
		while(xp>=(xpReq)) {
			lvl++;
			xp-=xpReq;
			xpReq = xpRequiredToLevelUp(lvl);
		}
		level = lvl;
		curLvlXp = xp;
	}
	
	public long xpRequiredToLevelUp(int level) {
		if (level < 1) 
			return 0;
		double calc = 200 + (level * 11) * (1+(double)level/333);
		return (long)calc;
	}
	
	public long getNLXPReq() {
		return xpRequiredToLevelUp(level);
	}
	
	public double getPercentageDone() {
		if (curLvlXp < 0)
			calculateLevel();
		return (curLvlXp+0.0) / (xpRequiredToLevelUp(level)+0.0);
	}
	
	public long getCurrentLevelXp() {
		return curLvlXp;
	}
	
}
