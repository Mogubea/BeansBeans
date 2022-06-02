package me.playground.civilizations.jobs;

import me.playground.skills.Skill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JobMiner extends Job implements IMiningJob {
	
	protected JobMiner() {
		super("Miner", 
				0x78ffa8, 
				Component.text("A simple specialist job that pays for mining\n" +
							"ores and minerals based on their rarity.", NamedTextColor.GRAY));
		addSkillRequirement(Skill.MINING, 50);
		addStructureRequirements(1, "quarry", "mineshaft");
	}
	
}
