package me.playground.skills;

public class SkillData {

    private int grade;
    private double totalExperience, currentGradeExperience;
    private int spentEssence, skillEssence;

    protected SkillData() {
    }

    public SkillData(double totalExperience, int essence) {
        this.totalExperience = totalExperience;
        calculateGrade();
        this.skillEssence = essence;
    }

    protected void spendEssence(int amount) {
        if (amount < 0) amount = Math.abs(amount);
        this.spentEssence += amount;
        this.skillEssence -= amount;
    }

    protected void addEssence(int amount) {
        this.skillEssence += amount;
    }

    /**
     * @return The current unspent skill essence
     */
    public int getEssence() {
        return skillEssence;
    }

    /**
     * @return The total amount of spent skill essence
     */
    public int getSpentEssence() {
        return spentEssence;
    }

    protected void addExperience(double amt) {
        totalExperience += amt;
        currentGradeExperience += amt;
        if (currentGradeExperience >= getXPRequirement(grade) || currentGradeExperience < 0)
            calculateGrade();
    }

    protected void setLevel(int level) {
        if (level < 0) level = 0;
        if (level > 20) level = 20;
        long xpReq = 0;
        for (int lv = level; --lv > -1;)
            xpReq += getXPRequirement(lv);

        this.totalExperience = xpReq;
        this.currentGradeExperience = 0;
        this.grade = (byte) level;
    }

    public double getTotalExperience() {
        return this.totalExperience;
    }

    public int getLevel() {
        return this.grade;
    }

    public String getNextGrade() {
        return Grade.fromLevel(grade + 1).toString();
    }

    public String getGrade() {
        return Grade.fromLevel(grade).toString();
    }

    private void calculateGrade() {
        double xp = totalExperience;
        byte lvl = 0;
        double xpReq = getXPRequirement(lvl);

        while(xp>=(xpReq)) {
            lvl++;
            xp-=xpReq;
            xpReq = getXPRequirement(lvl);
        }
        grade = lvl;
        currentGradeExperience = xp;
    }

    private int getXPRequirement(int level) {
        if (level > 20) return 25000;
        if (level < 0) level = 0;
        return level * (level+3) * 4250 - (level * 10000) + 25000;
    }

    public long getXPRequirement() {
        return getXPRequirement(grade);
    }

    public double getLevelProgress() {
        if (currentGradeExperience < 0) calculateGrade();
        return currentGradeExperience / (getXPRequirement(grade)+0.0);
    }

    public double getLevelExperience() {
        return currentGradeExperience;
    }

}
