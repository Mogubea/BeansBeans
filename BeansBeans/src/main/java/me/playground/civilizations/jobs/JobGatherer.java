package me.playground.civilizations.jobs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class JobGatherer extends Job implements IFarmingJob, IMiningJob, IHuntingJob, IFishingJob {
	
	protected JobGatherer() {
		super("Gatherer", 
				0x688368, 
				Component.text("An unspecialised and low paying Job unlocked by default for\n" +
						"inexperienced workers that either have not yet earned enough\n" +
						"experience to take on a specialised Job or the Civilization\n" +
						"has not unlocked any other Job options.", NamedTextColor.GRAY));
	}
	
}
