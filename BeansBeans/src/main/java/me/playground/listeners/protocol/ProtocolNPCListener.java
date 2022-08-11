package me.playground.listeners.protocol;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;

import me.playground.gui.BeanGuiNPCEdit;
import me.playground.listeners.events.PlayerInteractNPCEvent;
import me.playground.main.Main;
import me.playground.npc.NPC;
import me.playground.npc.NPCHuman;
import me.playground.playerprofile.PlayerProfile;

public class ProtocolNPCListener {

	final Main plugin;

	public ProtocolNPCListener(Main plugin) {
		this.plugin = plugin;

		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent e) {
               if (e.getPacketType() != PacketType.Play.Client.USE_ENTITY) return;
            	final Player p = e.getPlayer();
                final PacketContainer packet = e.getPacket();
                final int id = packet.getIntegers().getValues().get(0);
                final WrappedEnumEntityUseAction action = packet.getEnumEntityUseActions().getValues().get(0);

                // These checks are stupid
                if (action.getAction() == EntityUseAction.INTERACT_AT && action.getHand() == Hand.MAIN_HAND) {
                	 final NPC<?> npc = getMainPlugin().npcManager().getEntityNPC(id);
                     if (npc == null) return;

                     // Call Sync from Async
                     if (PlayerProfile.from(p).onCdElseAdd("npcInteract", 500, true)) return;
                     Bukkit.getServer().getScheduler().runTask(getPlugin(), () -> {
                    	 if (npc instanceof NPCHuman && p.isSneaking() && p.hasPermission("bean.npc.edit")) {
                    		 new BeanGuiNPCEdit(p, (NPCHuman)npc).openInventory();
							 e.setCancelled(true); // Cancel to prevent item interactions
                    		 return;
                    	 }

                    	 if (new PlayerInteractNPCEvent(p, npc).callEvent()) {
							 npc.onInteract(p);
							 e.setCancelled(true); // Cancel to prevent item interactions
						 }
                     });
                }
            }
		});
	}

	public Main getMainPlugin() {
		return plugin;
	}

}
