package me.playground.discord.voting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.playground.playerprofile.PlayerProfile;

public class VoteEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();

	private Vote vote;
	
	public VoteEvent(final Vote vote) {
		this.vote = vote;
	}
	
	public Vote getVote() {
		return vote;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public PlayerProfile getVoterProfile() {
		return vote.getPlayerProfile();
	}
}
