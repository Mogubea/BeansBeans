package me.playground.skills;

import net.kyori.adventure.text.Component;

public enum SkillPerk {
	
	MINING_1(Component.text("Test 1")),
	MINING_2(Component.text("Test 2"));
	
	private final Component name;
	
	private SkillPerk(Component name) {
		this.name = name;
	}
	
}
