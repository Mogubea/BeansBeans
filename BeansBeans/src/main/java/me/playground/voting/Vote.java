package me.playground.voting;

import me.playground.playerprofile.PlayerProfile;

public class Vote {
	
	private final VoteService service;
	private final String username;
	private final String address;
	private final String timeStamp;
	
	private final PlayerProfile profile;
	
	public Vote(VoteService service, String username, String address, String timestamp) {
		this.service = service;
		this.username = username;
		this.address = address;
		this.timeStamp = timestamp;
		
		this.profile = PlayerProfile.fromIfExists(username);
	}
	
	/**
	 * Gets the service.
	 * @return The service
	 */
	public VoteService getService() {
		return service;
	}

	/**
	 * Gets the username.
	 * @return The username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the address.
	 * @return The address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Gets the time stamp.
	 * @return The time stamp
	 */
	public String getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Gets the {@link PlayerProfile} for the player that voted, if it's a valid player.
	 * @return
	 */
	public PlayerProfile getPlayerProfile() {
		return profile;
	}
	
	public boolean isValid() {
		return profile != null;
	}
	
}
