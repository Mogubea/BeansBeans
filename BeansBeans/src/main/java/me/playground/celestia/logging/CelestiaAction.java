package me.playground.celestia.logging;

public enum CelestiaAction {

	/**
	 * Default enum, unknown event.
	 */
	UNKNOWN("UNKNOWN"),
	/**
	 * When a player talks in chat.
	 */
	CHAT("PLAYER_CHAT"),
	/**
	 * When a player joins the server.
	 */
	JOIN("PLAYER_JOIN"),
	/**
	 * When a player joins the server.
	 */
	FAIL_JOIN("PLAYER_FAIL_JOIN"),
	/**
	 * When a player leaves the server.
	 */
	QUIT("PLAYER_QUIT"),
	/**
	 * When a player fires a command.
	 */
	COMMAND("PLAYER_COMMAND"),
	/**
	 * When a player breaks a block.
	 */
	BLOCK_BREAK("PLAYER_BLOCK_BREAK"),
	/**
	 * When a player places a block.
	 */
	BLOCK_PLACE("PLAYER_BLOCK_PLACE"),
	/**
	 * When a player drops an item.
	 */
	ITEM_DROP("PLAYER_DROP_ITEM"),
	/**
	 * When a player picks up an item off of the ground or through Telekinesis.
	 */
	ITEM_PICKUP("PLAYER_PICKUP_ITEM"),
	/**
	 * When a player crafts or obtains an item of importance (e.g. an item with a trackable uuid).
	 */
	ITEM_CRAFT("PLAYER_GENERATE_ITEM"),
	/**
	 * When a player takes or places items into an inventory.
	 */
	INV_TRANSACTION("PLAYER_INVENTORY_TRANSACTION"),
	LINK_DISCORD("PLAYER_PROFILE_DISCORD_LINK"),
	REGION_CHANGE("REGION_UPDATE"),
	VOTE("PLAYER_VOTE"),
	MODIFY("PLAYER_PROFILE_MODIFY"),
	;

	/**
	 * Never change this. This exists to allow the changing of Enum Names if needed.
	 */
	final String mysqlIdentifier;

	CelestiaAction(String id) {
		this.mysqlIdentifier = id;
	}

	public String getIdentifier() {
		return mysqlIdentifier;
	}
	
}
