package me.playground.skills;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum SkillPerk {
	
	MINING_PROFICIENCY("The Basics", 0, 10, 1, "Grants \u00a7a{0} \u00a7e\u2601 Mining Proficiency\u00a7r, reducing the extra Hunger and Durability penalty by \u00a7a{1}%\u00a7r when mining.", 1, 10),
	MINING_FORTUNE_ROCKS("Petrology Fortune", 1),
	MINING_DAMAGE_BELOW_0("Damage below Y 0", 2),
	MINING_TEST1("tEST1", 3),
	MINING_TEST2("tEST1", 4),
	MINING_FORTUNE_GEMOLOGY("Gemology Fortune", 5),
	MINING_TEST3("tEST1", 6),
	MINING_TEST4("tEST1", 7),
	MINING_FORTUNE_MINERALOGY("Mineralogy Fortune", 8),
	MINING_TEST5("tEST1", 9),
	MINING_TEST6("tEST1", 10),
	MINING_TEST7("tEST1", 11),
	MINING_TEST8("tEST1", 12),
	MINING_TEST9("tEST1", 13),
	MINING_TEST10("tEST1", 14),
	MINING_TEST11("tEST1", 15),
	MINING_TEST12("tEST1", 16),
	MINING_TEST13("tEST1", 17),
	;
	
	private final Component name;
	private final byte idx;
	private Skill skill;
	private final byte maxUpgrades;
	private final byte cost;
	private final String nonComponentinfo;
	private final float[] replacementValues; // Replaces {X} in the description string.
	
	SkillPerk(String name, int skillIdx, int maxUpgrades, int cost, String info, float...replacementValues) {
		this.name = Component.text(name);
		this.idx = (byte) skillIdx;
		this.maxUpgrades = (byte) maxUpgrades;
		this.cost = (byte) cost;
		this.nonComponentinfo = info;
		this.replacementValues = replacementValues;
	}
	
	SkillPerk(String name, int skillIdx) {
		this(name, skillIdx, 20, 1, "Coming soon.");
	}
	
	protected Skill getSkill() {
		return skill;
	}
	
	// esgjmfdsg
	protected void setSkill(Skill skill) {
		this.skill = skill;
	}
	
	public Component getName() {
		return name;
	}
	
	/**
	 * TODO: Update this to support custom colours in the traditional component system, also optimise it more.. Whatever.
	 */
	public List<Component> getInformation(int level) {
		if (level > maxUpgrades) level = maxUpgrades;
		if (level < 1) level = 1;
		
		String info = nonComponentinfo;
		int rSize = replacementValues.length;
		for (int x = -1; ++x < rSize;)
			info = info.replace("{"+x+"}", replacementValues[x] * level+"");
		
		List<Component> comps = new ArrayList<>();
		String cur = "";
		for (String s : info.split(" ")) {
			if (cur.length() >= 36 || cur.length()+s.length() >= 40) {
				comps.add(Component.text(cur).colorIfAbsent(TextColor.color(0xafafaf)).decoration(TextDecoration.ITALIC, false));
				cur = "";
			}
			cur += s+" ";
		}
		
		if (!cur.isEmpty())
			comps.add(Component.text(cur).colorIfAbsent(TextColor.color(0xafafaf)).decoration(TextDecoration.ITALIC, false));
		
		return comps;
	}
	
	protected int getSkillIdx() {
		return idx;
	}
	
	public int getMaxLevel() {
		return maxUpgrades;
	}
	
	public int getCost() {
		return cost;
	}
	
}
