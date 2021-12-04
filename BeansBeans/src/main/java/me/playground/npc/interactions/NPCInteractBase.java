package me.playground.npc.interactions;

import org.bukkit.entity.Player;

import me.playground.npc.NPC;

public class NPCInteractBase extends NPCInteraction {
	
	protected NPCInteractBase() {
		super("base");
	}
	
	@Override
	public void onInteract(final NPC<?> npc, final Player p) {
	}

	@Override
	public void onInit(NPC<?> npc) {
	}

	@Override
	public boolean hasLockedTitle() {
		return false;
	}

}
