package me.playground.playerprofile;

import org.bukkit.inventory.ItemStack;

import me.playground.utils.Utils;
import net.kyori.adventure.text.format.TextColor;

public enum DeliveryType {
	
	PACKAGE("package", 0xffeeee, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWYyNTg0OWU4Y2Q1ZTUzMmMzMGNiYThkZThiZWQwNjQwY2ZiMmVlYzUwOTI5OTk4YjEwNzgzOWJmYjBmMjRkNyJ9fX0="),
	TREASURE("loot chest", 0xffcc44, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RiY2E0YjY5ZWFmOGRjYjdhYzM3MjgyMjhkZThhNjQ0NDA3ODcwMTMzNDJkZGFhYmMxYjAwZWViOGVlYzFlMiJ9fX0="),
	SUPPORT("package", 0xc63dc6, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGEyMjQyZWU1YzUwYmMwYjU4MmUzYzA4NWIwODc5MTI0NTNiMDE0NTlkMWUyYTI5YzcyOTkzNmI0YjM1MGIzZCJ9fX0="),
	GIFT("gift", 0x44ffdd, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0="),
	EASTER("easter egg", 0xaaffff, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWViMzM1MTgyZGI1ZjNiZTgwZmNjZjZlYWJlNTk5ZjQxMDdkNGZmMGU5ZjQ0ZjM0MTc0Y2VmYTZlMmI1NzY4In19fQ=="),
	HALLOWEEN("lantern", 0xffaa88, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzYxM2U2NWVkZWZlNTY2MzgwOTE0M2RmZThhMmRlNjNiNzUwYTQ1NDc5OTRiNzllN2I5MmJhOTdiZWFlYWU0YyJ9fX0=");
	
	private final String text;
	private final ItemStack displayStack;
	private final TextColor titleColor;
	
	DeliveryType(String text, int titleCol, String base64Skull) {
		this.text = text;
		this.titleColor = TextColor.color(titleCol);
		this.displayStack = Utils.getSkullWithCustomSkin(base64Skull);
	}
	
	public TextColor getTitleColour() {
		return titleColor;
	}
	
	public ItemStack getDisplayStack() {
		return displayStack;
	}
	
	public String getName() {
		return text;
	}
	
}
