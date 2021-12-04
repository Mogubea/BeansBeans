package me.playground.npc.interactions;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import me.playground.npc.NPC;

public abstract class NPCInteraction {
	
	private final static HashMap<String, NPCInteraction> interactions = new HashMap<String, NPCInteraction>();
	static {
		new NPCInteractBase();
		new NPCInteractEmployer();
	}
	
	@Nullable
	public static NPCInteraction getByName(String name) {
		if (name == null) return null;
		return interactions.get(name.toLowerCase());
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
