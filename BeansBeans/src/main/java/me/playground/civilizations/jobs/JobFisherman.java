package me.playground.civilizations.jobs;

import me.playground.playerprofile.skills.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JobFisherman extends Job implements IFishingJob {
	
	protected JobFisherman() {
		super("Fisherman", 
				0x488fdd, 
				Component.text("A simple specialist job that pays nicely\n" +
							"for reeling in fish and selling them.", NamedTextColor.GRAY));
		addSkillRequirement(SkillType.FISHING, 40);
		addStructureRequirements(1, "lake", "docks");
	}
	
}
