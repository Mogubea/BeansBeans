package me.playground.listeners.events;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import me.playground.playerprofile.PlayerProfile;
import me.playground.skills.Skill;
import me.playground.skills.SkillInfo;

public class SkillLevelUpEvent extends PlayerEvent {
	private static final HandlerList handlers = new HandlerList();
	private final PlayerProfile pp;
	private final Skill skill;
	
	public SkillLevelUpEvent(@Nonnull Player who, @Nonnull Skill skill) {
		super(who);
		this.pp = PlayerProfile.from(who);
		this.skill = skill;
	}
	
	@Nonnull
	public PlayerProfile getProfile() {
		return pp;
	}
	
	@Nonnull
	public Skill getSkill() {
		return skill;
	}
	
	@Nonnull
	public int getPreviousLevel() {
		return pp.getSkills().getLevel(skill) - 1;
	}
	
	@Nonnull
	public SkillInfo getSkillInfo() {
		return pp.getSkills().getSkillInfo(skill);
	}
	
	@Override
	public @Nonnull HandlerList getHandlers() {
		return handlers;
	}
	
	public static @Nonnull HandlerList getHandlerList() {
		return handlers;
	}
	
}
