package me.playground.warps;

import me.playground.utils.BeanColor;

public enum WarpType {
	
	PLAYER("\u00a7dPlayer Warp\u00a7r", BeanColor.WARP),
	SERVER("\u00a7bServer Warp\u00a7r", BeanColor.WARP_SERVER),
	SHOP("\u00a7eShop Warp\u00a7r", BeanColor.WARP_SHOP);
	
	private final String name;
	private final BeanColor col;
	
	WarpType(String n, BeanColor col) {
		this.name = n;
		this.col = col;
	}
	
	public String getName() {
		return name;
	}
	
	public BeanColor getColor() {
		return col;
	}
	
}
