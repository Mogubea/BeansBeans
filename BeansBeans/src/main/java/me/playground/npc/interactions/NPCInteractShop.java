package me.playground.npc.interactions;

import org.bukkit.entity.Player;

import me.playground.gui.BeanGuiBasicMenuShop;
import me.playground.npc.NPC;

public class NPCInteractShop extends NPCInteraction {
	
	protected NPCInteractShop() {
		super("shop");
	}
	
	@Override
	public void onInteract(final NPC<?> npc, final Player p) {
		if (npc.getMenuShop() == null) return;
		
		new BeanGuiBasicMenuShop(p, npc.getMenuShop()).openInventory();
	}

	@Override
	public void onInit(NPC<?> npc) {
	}

	@Override
	public boolean hasLockedTitle() {
		return false;
	}

}
