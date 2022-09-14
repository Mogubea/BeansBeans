package me.playground.npc.interactions;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.playground.npc.NPC;
import org.jetbrains.annotations.NotNull;

public abstract class NPCInteraction {
	
	private final static HashMap<String, NPCInteraction> interactions = new HashMap<>();

	static {
		new NPCInteractBase();
		new NPCInteractShop();
	}

	@NotNull
	public static NPCInteraction getByName(String name) {
		if (name == null) return interactions.get("base");
		return interactions.getOrDefault(name.toLowerCase(), interactions.get("base"));
	}
	
	private final String name;
	
	protected NPCInteraction(String name) {
		this.name = name;
		interactions.put(name.toLowerCase(), this);
	}
	
	public String getName() {
		return name;
	}
	
	public abstract void onInteract(NPC<?> npc, Player p);
	
	public abstract void onInit(NPC<?> npc);
	
	public abstract boolean hasLockedTitle();
	
}
