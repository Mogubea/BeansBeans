package me.playground.civilizations.jobs;

import me.playground.playerprofile.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JobLumberjack extends Job implements IMiningJob {
	
	protected JobLumberjack() {
		super("Lumberjack", 
				0xc5a38a, 
				Component.text("A simple specialist job that pays for the\n" +
							"chopping of trees and selling logs", NamedTextColor.GRAY));
		addSkillRequirement(SkillType.LOGCUTTING, 50);
		addStructureRequirements(4);
	}
	
}
