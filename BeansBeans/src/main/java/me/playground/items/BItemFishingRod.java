package me.playground.items;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerFishEvent;

public class BItemFishingRod extends BItemDurable {
	
	protected BItemFishingRod(int numeric, String identifier, String name, ItemRarity rarity, int modelDataInt, int durability) {
		super(numeric, identifier, name, Material.FISHING_ROD, rarity, modelDataInt, durability);
	}
	
	public void onFish(PlayerFishEvent e) {
		
	}
	
}
