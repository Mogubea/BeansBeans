package me.playground.playerprofile.stats;

import java.util.Arrays;

public enum StatType {
	GENERIC(0, "generic"),
	BLOCK_BREAK(1, "blockbreak", "blockbreaks", "breaks", "broken", "block_break", "brokenblocks"),
	BLOCK_PLACE(2, "blockplace", "blockplaces", "blockplacements", "placed", "blocksplaced", "block_place"),
	LOOT_EARNED(3, "loot", "lootearned", "loot_earned", "reward", "rewards"),
	DAMAGE(4, "damage", "damagedone"),
	KILLS(5, "kill", "kills"),
	DEATHS(6, "death", "deaths", "deathby"),
	CRATES_OPENED(7, "crate", "crates", "cratesopened"),
	OCCUPATION(8, "job", "jobs"),
	VOTING(9, "vote", "votes"),
	CURRENCY(10, "currency", "currencies"),
	ATTRIBUTE_SOURCES(11, "attributesource", "attributes", "attributesources"),
	TRADING(12, "trading", "barters"),
	NPC_SHOPS(13, "npc_shops", "npcshop", "npcshops", "npc_shop"),
	ITEM_SELL(14, "item_sell", "itemsell"),
	ITEM_PURCHASE(15, "item_purchase", "itembuy", "itempurchase"),
	
	
	;
	
	private byte type;
	private String[] strings;
	StatType(int type, String... strings) {
		this.type = (byte) type;
		this.strings = strings;
	}
	
	public byte getId() {
		return type;
	}
	
	public static StatType fromId(int id) {
		StatType[] vals = StatType.values();
		int size = vals.length;
		for (int x = -1; ++x < size;) {
			StatType type = vals[x];
			if (type.getId() == id) return type;
		}
		return null;
	}
	
	public static StatType fromString(String s) {
		for (StatType type : StatType.values()) {
			if (s.equals(""+type.type) || Arrays.stream(type.strings).anyMatch(s::equalsIgnoreCase)) {
				return type;
			}
		}
		return null;
	}
	
}
